package com.lzx.applock.bean;

import android.content.pm.ApplicationInfo;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 应用信息
 *
 * @author lzx
 * @date 2018/2/27
 */

public class LockAppInfo implements Parcelable {
    private long id;
    private String packageName;
    private String appName;
    private String appType;
    private ApplicationInfo appInfo;
    private boolean isLocked;  //是否已加锁
    private boolean isRecommend;  //是否是推荐加锁的app
    private boolean isSysApp; //是否是系统应用
    private boolean isSetUnLock; //是否设置了不锁

    public LockAppInfo(String packageName, boolean isLocked, boolean isRecommend) {
        this.packageName = packageName;
        this.isLocked = isLocked;
        this.isRecommend = isRecommend;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public ApplicationInfo getAppInfo() {
        return appInfo;
    }

    public void setAppInfo(ApplicationInfo appInfo) {
        this.appInfo = appInfo;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public boolean isRecommend() {
        return isRecommend;
    }

    public void setRecommend(boolean recommend) {
        isRecommend = recommend;
    }

    public boolean isSysApp() {
        return isSysApp;
    }

    public void setSysApp(boolean sysApp) {
        isSysApp = sysApp;
    }

    public boolean isSetUnLock() {
        return isSetUnLock;
    }

    public void setSetUnLock(boolean setUnLock) {
        isSetUnLock = setUnLock;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.packageName);
        dest.writeString(this.appName);
        dest.writeString(this.appType);
        dest.writeParcelable(this.appInfo, flags);
        dest.writeByte(this.isLocked ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isRecommend ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isSysApp ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isSetUnLock ? (byte) 1 : (byte) 0);
    }

    protected LockAppInfo(Parcel in) {
        this.id = in.readLong();
        this.packageName = in.readString();
        this.appName = in.readString();
        this.appType = in.readString();
        this.appInfo = in.readParcelable(ApplicationInfo.class.getClassLoader());
        this.isLocked = in.readByte() != 0;
        this.isRecommend = in.readByte() != 0;
        this.isSysApp = in.readByte() != 0;
        this.isSetUnLock = in.readByte() != 0;
    }

    public static final Parcelable.Creator<LockAppInfo> CREATOR = new Parcelable.Creator<LockAppInfo>() {
        @Override
        public LockAppInfo createFromParcel(Parcel source) {
            return new LockAppInfo(source);
        }

        @Override
        public LockAppInfo[] newArray(int size) {
            return new LockAppInfo[size];
        }
    };
}
