package com.lzx.applock.service;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.lzx.applock.bean.LockAppInfo;
import com.lzx.applock.constants.Constants;
import com.lzx.applock.db.DbManager;
import com.lzx.applock.module.lock.UnlockView;

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
    private static String currOpenPackageName = "";
    private UnlockView mUnlockView;

    @Override
    public void onCreate() {
        super.onCreate();
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        mPackageManager = getPackageManager();
        mUnlockView = new UnlockView(this);

        new Thread(new ServiceWorker()).start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        mIsServiceDestoryed.set(true);
        super.onDestroy();
    }

    private class ServiceWorker implements Runnable {

        @Override
        public void run() {
            while (!mIsServiceDestoryed.get()) {
                String packageName = getLauncherTopApp(AppLockService.this, activityManager);
                if (!inWhiteList(packageName) && !TextUtils.isEmpty(packageName) && !packageName.equals(currOpenPackageName)) {

                    //如果是加锁应用
                    if (DbManager.get().isLockedPackageName(packageName)) {
                        //先解锁，避免重复打开解锁View
                        DbManager.get().updateLockedStatus(packageName, false);
                        openUnLockView(packageName);
                        continue;
                    }
                }
            }
        }
    }

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


    /**
     * 白名单
     */
    private boolean inWhiteList(String packageName) {
        return packageName.equals(Constants.APP_PACKAGE_NAME) || packageName.equals("com.android.settings");
    }

    /**
     * 获取栈顶应用包名
     */
    public String getLauncherTopApp(Context context, ActivityManager activityManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            List<ActivityManager.RunningTaskInfo> appTasks = activityManager.getRunningTasks(1);
            if (null != appTasks && !appTasks.isEmpty()) {
                return appTasks.get(0).topActivity.getPackageName();
            }
        } else {
            //5.0以后需要用这方法
            UsageStatsManager sUsageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            long endTime = System.currentTimeMillis();
            long beginTime = endTime - 10000;
            String result = "";
            UsageEvents.Event event = new UsageEvents.Event();
            UsageEvents usageEvents = sUsageStatsManager.queryEvents(beginTime, endTime);
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event);
                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    result = event.getPackageName();
                }
            }
            if (!android.text.TextUtils.isEmpty(result)) {
                return result;
            }
        }
        return "";
    }

    /**
     * 获得属于桌面的应用的应用包名称
     */
    private List<String> getHomes() {
        List<String> names = new ArrayList<>();
        PackageManager packageManager = this.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolveInfo) {
            names.add(ri.activityInfo.packageName);
        }
        return names;
    }


}
