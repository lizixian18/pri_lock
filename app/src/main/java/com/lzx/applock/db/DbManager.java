package com.lzx.applock.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;

import com.lzx.applock.LockApplication;
import com.lzx.applock.bean.LockAppInfo;
import com.lzx.applock.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * @author lzx
 * @date 2018/2/6
 */

public class DbManager {

    private Context mContext;
    private ContentResolver mResolver;
    private PackageManager mPackageManager;

    private static final byte[] sLock = new byte[0];

    private static volatile DbManager sInstance;

    public static DbManager get() {
        if (sInstance == null) {
            synchronized (sLock) {
                if (sInstance == null) {
                    sInstance = new DbManager();
                }
            }
        }
        return sInstance;
    }

    private DbManager() {
        mContext = LockApplication.getContext();
        mResolver = mContext.getContentResolver();
        mPackageManager = mContext.getPackageManager();
    }

    /**
     * 获取播放列表
     *
     * @return
     */
    public List<LockAppInfo> queryInfoList() {
        Uri uri = LockContentProvider.LOCK_URI;
        Cursor cursor = mResolver.query(uri, null, null, null, null);
        if (cursor == null) {
            return new ArrayList<>();
        }
        LogUtil.i("getCount = " + cursor.getCount());
        List<LockAppInfo> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndex(DbConstants.ID));
            String packageName = cursor.getString(cursor.getColumnIndex(DbConstants.PACKAGENAME));
            String appName = cursor.getString(cursor.getColumnIndex(DbConstants.APPNAME));
            String appType = cursor.getString(cursor.getColumnIndex(DbConstants.APPTYPE));
            boolean isLocked = cursor.getInt(cursor.getColumnIndex(DbConstants.ISLOCK)) == 0;
            boolean isRecommend = cursor.getInt(cursor.getColumnIndex(DbConstants.ISRECOMMEND)) == 0;
            boolean isSysApp = cursor.getInt(cursor.getColumnIndex(DbConstants.ISYSAPP)) == 0;
            boolean isSetUnLock = cursor.getInt(cursor.getColumnIndex(DbConstants.ISETUNLOCK)) == 0;
            LockAppInfo info = new LockAppInfo();
            info.setId(id);
            info.setPackageName(packageName);
            info.setAppName(appName);
            info.setAppType(appType);
            info.setLocked(isLocked);
            info.setRecommend(isRecommend);
            info.setSysApp(isSysApp);
            info.setSetUnLock(isSetUnLock);
            list.add(info);
        }
        cursor.close();
        return list;
    }


    public void saveInfoList(List<LockAppInfo> list) {
        clearInfoList();
        Uri uri = LockContentProvider.LOCK_URI;
        for (LockAppInfo info : list) {
            ContentValues values = new ContentValues();
            values.put(DbConstants.ID, info.getId());
            values.put(DbConstants.PACKAGENAME, info.getPackageName());
            values.put(DbConstants.APPNAME, info.getAppName());
            values.put(DbConstants.APPTYPE, info.getAppType());
            values.put(DbConstants.ISLOCK, info.isLocked() ? 0 : 1);
            values.put(DbConstants.ISRECOMMEND, info.isRecommend() ? 0 : 1);
            values.put(DbConstants.ISYSAPP, info.isSysApp() ? 0 : 1);
            values.put(DbConstants.ISETUNLOCK, info.isSetUnLock() ? 0 : 1);
            mResolver.insert(uri, values);
        }
    }


    public int deleteInfoByPackageName(String packageName) {
        Uri uri = LockContentProvider.LOCK_URI;
        return mResolver.delete(uri, DbConstants.PACKAGENAME + " = ?", new String[]{packageName});
    }

    public void deleteInfoByList(List<LockAppInfo> list) {
        for (LockAppInfo info : list) {
            deleteInfoByPackageName(info.getPackageName());
        }
    }

    private int clearInfoList() {
        Uri uri = LockContentProvider.LOCK_URI;
        return mResolver.delete(uri, null, null);
    }


    public LockAppInfo queryLockAppInfoByPackageName(String packageName) {
        Uri uri = LockContentProvider.LOCK_URI;
        Cursor cursor = mResolver.query(uri, null, DbConstants.PACKAGENAME + " = ?", new String[]{packageName}, null);
        if (cursor == null) {
            return null;
        }
        LockAppInfo info = new LockAppInfo();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndex(DbConstants.ID));
            String pkg = cursor.getString(cursor.getColumnIndex(DbConstants.PACKAGENAME));
            String appName = cursor.getString(cursor.getColumnIndex(DbConstants.APPNAME));
            String appType = cursor.getString(cursor.getColumnIndex(DbConstants.APPTYPE));
            boolean isLocked = cursor.getInt(cursor.getColumnIndex(DbConstants.ISLOCK)) == 0;
            boolean isRecommend = cursor.getInt(cursor.getColumnIndex(DbConstants.ISRECOMMEND)) == 0;
            boolean isSysApp = cursor.getInt(cursor.getColumnIndex(DbConstants.ISYSAPP)) == 0;
            boolean isSetUnLock = cursor.getInt(cursor.getColumnIndex(DbConstants.ISETUNLOCK)) == 0;
            info.setId(id);
            info.setPackageName(pkg);
            info.setAppName(appName);
            info.setAppType(appType);
            info.setLocked(isLocked);
            info.setRecommend(isRecommend);
            info.setSysApp(isSysApp);
            info.setSetUnLock(isSetUnLock);
        }
        cursor.close();
        return info;
    }

    /**
     * 判断是否为加锁应用
     * 只有 isLock = true 和 isSetUnLock = false 才是加锁应用
     * 解锁时，只改变 isSetUnLock
     * 在 app 内设置应用是否加锁的时候同时改变 isLock 和 isSetUnLock
     */
    public boolean isLockedPackageName(String packageName) {
        Uri uri = LockContentProvider.LOCK_URI;
        Cursor cursor = mResolver.query(uri, null, DbConstants.PACKAGENAME + " = ?", new String[]{packageName}, null);
        if (cursor == null) {
            return false;
        }
        boolean result = false;
        while (cursor.moveToNext()) {
            boolean isLock = cursor.getInt(cursor.getColumnIndex(DbConstants.ISLOCK)) == 0;
            boolean isSetUnLock = cursor.getInt(cursor.getColumnIndex(DbConstants.ISETUNLOCK)) == 0;
            result = isLock && !isSetUnLock;
        }
        cursor.close();
        return result;
    }

    /**
     * 应用内设置加不加锁时调用
     * 同时改变 isLock 和 isSetUnLock
     */
    public void updateLockedStatus(String packageName, boolean isLocked) {
        Uri uri = LockContentProvider.LOCK_URI;
        ContentValues values = new ContentValues();
        values.put(DbConstants.ISLOCK, isLocked ? 0 : 1);
        values.put(DbConstants.ISETUNLOCK, isLocked ? 0 : 1);
        mResolver.update(uri, values, DbConstants.PACKAGENAME + " = ?", new String[]{packageName});
    }

    /**
     * 解锁时调用
     * 只改变 isSetUnLock
     */
    public void updateIsSetUnlockStatus(String packageName, boolean isLocked) {
        Uri uri = LockContentProvider.LOCK_URI;
        ContentValues values = new ContentValues();
        values.put(DbConstants.ISETUNLOCK, isLocked ? 0 : 1);
        mResolver.update(uri, values, DbConstants.PACKAGENAME + " = ?", new String[]{packageName});
    }

    public Observable<Boolean> saveLockAppInfoListAsync(final List<LockAppInfo> list) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                saveInfoList(list);
                emitter.onNext(true);
            }
        });
    }

    public Observable<List<LockAppInfo>> queryLockAppInfoListAsync() {
        return Observable.create(new ObservableOnSubscribe<List<LockAppInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<LockAppInfo>> emitter) throws Exception {
                List<LockAppInfo> list = queryInfoList();
                for (LockAppInfo info : list) {
                    ApplicationInfo appInfo = mPackageManager.getApplicationInfo(info.getPackageName(), PackageManager.GET_UNINSTALLED_PACKAGES);
                    info.setAppInfo(appInfo);
                }
                emitter.onNext(list);
            }
        });
    }


}
