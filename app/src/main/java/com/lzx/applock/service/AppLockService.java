package com.lzx.applock.service;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.lzx.applock.bean.LockAppInfo;
import com.lzx.applock.constants.Constants;
import com.lzx.applock.db.DbManager;
import com.lzx.applock.module.lock.UnlockView;
import com.lzx.applock.utils.LockUtil;
import com.lzx.applock.utils.LogUtil;
import com.lzx.applock.utils.SpUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Remote Service
 *
 * @author lzx
 * @date 2018/2/28
 */

public class AppLockService extends Service {

    private AtomicBoolean mIsServiceDestoryed = new AtomicBoolean(false);
    private ActivityManager activityManager;
    private PackageManager mPackageManager;
    public static String currOpenPackageName = "";
    public static boolean isChangeLockMode = false;
    private UnlockView mUnlockView;
    private AppLockReceiver mReceiver;
    private int lockMode;
    private Handler mHandler = new Handler();
    private List<String> homePackageNames;

    @Override
    public void onCreate() {
        super.onCreate();
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        mPackageManager = getPackageManager();
        mUnlockView = new UnlockView(this);
        mReceiver = new AppLockReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS); //Home键
        filter.addAction(Intent.ACTION_SCREEN_OFF); //锁屏
        filter.addAction(Constants.ACTION_UPDATE_LOCK_MODE);
        registerReceiver(mReceiver, filter);
        lockMode = SpUtil.getInstance().getInt(Constants.LOCK_MODEL);

        AsyncTask.SERIAL_EXECUTOR.execute(new ServiceWorker());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        homePackageNames = LockUtil.getHomePackageNames(this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        mIsServiceDestoryed.set(true);
        unregisterReceiver(mReceiver);
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private class ServiceWorker implements Runnable {

        @Override
        public void run() {
            while (!mIsServiceDestoryed.get()) {
                String packageName = LockUtil.getLauncherTopApp(AppLockService.this, activityManager);

                //关闭app后马上加锁
                if (lockMode == 2) {
                    //如果当前包名在桌面包名中，则代码app已经关闭
                    if (homePackageNames.contains(packageName) && !TextUtils.isEmpty(currOpenPackageName)) {
                        resumeLocking();
                    }
                }

                if (!LockUtil.inWhiteList(packageName) && !TextUtils.isEmpty(packageName) &&
                        !packageName.equals(currOpenPackageName)) {

                    //如果是加锁应用
                    if (DbManager.get().isLockedPackageName(packageName)) {
                        //先解锁，避免重复打开解锁View
                        DbManager.get().updateIsSetUnlockStatus(packageName, true);
                        openUnLockView(packageName);
                        continue;
                    }
                }
            }
        }
    }

    /**
     * 弹出解锁界面
     */
    private void openUnLockView(String packageName) {
        try {
            currOpenPackageName = packageName;
            LockAppInfo info = DbManager.get().queryLockAppInfoByPackageName(packageName);
            ApplicationInfo appInfo = mPackageManager.getApplicationInfo(info.getPackageName(), PackageManager.GET_UNINSTALLED_PACKAGES);
            info.setAppInfo(appInfo);

            mUnlockView.setLockAppInfo(info);
            mUnlockView.showUnLockView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class AppLockReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            switch (action) {
                case Intent.ACTION_CLOSE_SYSTEM_DIALOGS:
                    //Home键关闭解锁界面
                    mUnlockView.closeUnLockViewFormHomeAction();
                    break;
                case Intent.ACTION_SCREEN_OFF:
                    //currOpenPackageName不为空则证明锁屏界面有打开过
                    if (!TextUtils.isEmpty(currOpenPackageName)) {
                        if (lockMode == 0) {
                            //锁屏后立即加锁
                            resumeLocking();
                        } else if (lockMode == 1) {
                            //锁屏3分钟后加锁，记录当前时间并开始计时
                            SpUtil.getInstance().putLong(Constants.LOCK_CURR_MILLISENCONS, System.currentTimeMillis());
                            mHandler.post(mTimerRunnable);
                        }
                    }
                    break;
                case Constants.ACTION_UPDATE_LOCK_MODE:
                    lockMode = SpUtil.getInstance().getInt(Constants.LOCK_MODEL);
                    if (!TextUtils.isEmpty(currOpenPackageName)) {

                        isChangeLockMode = true;
                    }
                    break;
            }
        }
    }

    private Runnable mTimerRunnable = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(mTimerRunnable, 1000L);
            long screenOffTime = SpUtil.getInstance().getLong(Constants.LOCK_CURR_MILLISENCONS);
            //如果锁屏时间大于3分钟,加锁
            if (System.currentTimeMillis() - screenOffTime > 3 * 60 * 1000L) {
                if (!TextUtils.isEmpty(currOpenPackageName)) {
                    mHandler.removeCallbacks(mTimerRunnable);
                    resumeLocking();
                }
            }
        }
    };

    /**
     * 恢复加锁状态
     */
    private void resumeLocking() {
        DbManager.get().updateIsSetUnlockStatus(currOpenPackageName, false);
        currOpenPackageName = "";
    }

}
