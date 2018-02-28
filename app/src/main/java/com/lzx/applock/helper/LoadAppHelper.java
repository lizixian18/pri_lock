package com.lzx.applock.helper;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

import com.lzx.applock.bean.LockAppInfo;
import com.lzx.applock.constants.Constants;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * 加载app列表帮助类
 *
 * @author lzx
 * @date 2018/2/27
 */

public class LoadAppHelper {

    /**
     * 获取手机上的所有应用
     */
    private static List<ResolveInfo> loadPhoneAppList(PackageManager packageManager) {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        return packageManager.queryIntentActivities(intent, 0);
    }

    /**
     * 初始化推荐加锁的应用
     */
    private static List<String> loadRecommendApps() {
        List<String> packages = new ArrayList<>();
        packages.add("com.android.gallery3d");       //相册
        packages.add("com.android.mms");             //短信
        packages.add("com.tencent.mm");              //微信
        packages.add("com.android.contacts");        //联系人和电话
        packages.add("com.facebook.katana");         //facebook
        packages.add("com.facebook.orca");           //facebook Messenger
        packages.add("com.mediatek.filemanager");    //文件管理器
        packages.add("com.sec.android.gallery3d");   //也是个相册
        packages.add("com.android.email");           //邮箱
        packages.add("com.sec.android.app.myfiles"); //三星的文件
        packages.add("com.android.vending");         //应用商店
        packages.add("com.google.android.youtube");  //youtube
        packages.add("com.tencent.mobileqq");        //qq
        packages.add("com.tencent.qq");              //qq
        packages.add("com.android.dialer");          //拨号
        packages.add("com.twitter.android");         //twitter
        return packages;
    }

    /**
     * 将app信息封装成需要的数据
     */
    private static List<LockAppInfo> loadLockAppInfo(Activity activity) {
        List<LockAppInfo> list = new ArrayList<>();
        try {
            PackageManager mPackageManager = activity.getPackageManager();
            List<ResolveInfo> resolveInfos = loadPhoneAppList(mPackageManager);
            for (ResolveInfo resolveInfo : resolveInfos) {
                String packageName = resolveInfo.activityInfo.packageName;
                boolean isRecommend = isRecommendApp(packageName);
                LockAppInfo info = new LockAppInfo(packageName, false, isRecommend);
                ApplicationInfo appInfo = mPackageManager.getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
                String appName = mPackageManager.getApplicationLabel(appInfo).toString();
                if (isFilterOutApps(packageName)) {
                    boolean isSysApp = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                    info.setLocked(isRecommend);
                    info.setAppName(appName);
                    info.setSysApp(isSysApp);
                    info.setAppType(isSysApp ? "SystemApp" : "OtherApp");
                    info.setSetUnLock(false);
                    info.setAppInfo(appInfo);
                    list.add(info);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 异步获取
     */
    public static Observable<List<LockAppInfo>> loadAllLockAppInfoAsync(final Activity activity) {
        return Observable.create(new ObservableOnSubscribe<List<LockAppInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<LockAppInfo>> emitter) throws Exception {
                emitter.onNext(loadLockAppInfo(activity));
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<List<LockAppInfo>> loadLockedAppInfoAsync(final Activity activity) {
        return Observable.create(new ObservableOnSubscribe<List<LockAppInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<LockAppInfo>> emitter) throws Exception {
                List<LockAppInfo> list = loadLockAppInfo(activity);
                List<LockAppInfo> lockAppInfos = new ArrayList<>();
                for (LockAppInfo info : list) {
                    if (info.isLocked()) {
                        lockAppInfos.add(info);
                    }
                }
                emitter.onNext(lockAppInfos);
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<List<LockAppInfo>> loadUnLockAppInfoAsync(final Activity activity) {
        return Observable.create(new ObservableOnSubscribe<List<LockAppInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<LockAppInfo>> emitter) throws Exception {
                List<LockAppInfo> list = loadLockAppInfo(activity);
                List<LockAppInfo> lockAppInfos = new ArrayList<>();
                for (LockAppInfo info : list) {
                    if (!info.isLocked()) {
                        lockAppInfos.add(info);
                    }
                }
                emitter.onNext(lockAppInfos);
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<List<LockAppInfo>> loadSystemAppInfoAsync(final Activity activity) {
        return Observable.create(new ObservableOnSubscribe<List<LockAppInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<LockAppInfo>> emitter) throws Exception {
                List<LockAppInfo> list = loadLockAppInfo(activity);
                List<LockAppInfo> lockAppInfos = new ArrayList<>();
                for (LockAppInfo info : list) {
                    if (info.isSysApp()) {
                        lockAppInfos.add(info);
                    }
                }
                emitter.onNext(lockAppInfos);
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<List<LockAppInfo>> loadUserAppInfoAsync(final Activity activity) {
        return Observable.create(new ObservableOnSubscribe<List<LockAppInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<LockAppInfo>> emitter) throws Exception {
                List<LockAppInfo> list = loadLockAppInfo(activity);
                List<LockAppInfo> lockAppInfos = new ArrayList<>();
                for (LockAppInfo info : list) {
                    if (!info.isSysApp()) {
                        lockAppInfos.add(info);
                    }
                }
                emitter.onNext(lockAppInfos);
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 是否是推荐加锁app
     */
    private static boolean isRecommendApp(String packageName) {
        List<String> packages = loadRecommendApps();
        return !TextUtils.isEmpty(packageName) && packages.contains(packageName);
    }

    /**
     * 过滤的应用
     */
    private static boolean isFilterOutApps(String packageName) {
        return !packageName.equals(Constants.APP_PACKAGE_NAME) &&
                !packageName.equals("com.android.settings") &&
                !packageName.equals("com.google.android.googlequicksearchbox");
    }

}
