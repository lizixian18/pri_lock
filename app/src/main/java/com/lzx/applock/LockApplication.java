package com.lzx.applock;

import android.app.Application;
import android.content.Context;

import com.lzx.applock.utils.SpUtil;

/**
 * @author lzx
 * @date 2018/2/27
 */

public class LockApplication extends Application {

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
        SpUtil.getInstance().init(this);
    }

    public static Context getContext() {
        return sContext;
    }
}
