package com.lzx.applock.module.splash;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.lzx.applock.R;
import com.lzx.applock.base.BaseActivity;
import com.lzx.applock.bean.LockAppInfo;
import com.lzx.applock.constants.Constants;
import com.lzx.applock.db.DbManager;
import com.lzx.applock.helper.LoadAppHelper;
import com.lzx.applock.module.lock.UnlockActivity;
import com.lzx.applock.utils.LogUtil;
import com.lzx.applock.utils.SpUtil;
import com.lzx.applock.utils.SystemBarHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

/**
 * @author lzx
 * @date 2018/2/28
 */

public class SplashActivity extends BaseActivity {

    private ImageView mImageSplash;
    private ObjectAnimator animator;
    private boolean isFirstTime;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_splash;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        SystemBarHelper.hideStatusBar(getWindow(), true);
        mImageSplash = findViewById(R.id.image_splash);
        isFirstTime = SpUtil.getInstance().getBoolean(Constants.IS_FIRST_TIME, true);
        initSplashAnim();
        //初始化数据
        LoadAppHelper.loadAllLockAppInfoAsync(this)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(new Predicate<List<LockAppInfo>>() {
                    @Override
                    public boolean test(List<LockAppInfo> lockAppInfos) throws Exception {
                        if (isFirstTime) {
                            DbManager.get()
                                    .saveLockAppInfoListAsync(lockAppInfos)
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Consumer<Boolean>() {
                                        @Override
                                        public void accept(Boolean aBoolean) throws Exception {
                                            animator.start();
                                        }
                                    });
                            return false;
                        } else {
                            return true;
                        }
                    }
                })
                .observeOn(Schedulers.newThread())
                .map(new Function<List<LockAppInfo>, List<LockAppInfo>>() {
                    @Override
                    public List<LockAppInfo> apply(List<LockAppInfo> appList) throws Exception {
                        //数据库数据与最新获取的app数据作对比
                        List<LockAppInfo> dbList = DbManager.get().queryInfoList();
                        if (appList.size() > dbList.size()) { //如果有安装新应用
                            List<LockAppInfo> resultList = new ArrayList<>();
                            HashMap<String, LockAppInfo> hashMap = new HashMap<>();
                            for (LockAppInfo info : dbList) {
                                hashMap.put(info.getPackageName(), info);
                            }
                            for (LockAppInfo info : appList) {
                                if (!hashMap.containsKey(info.getPackageName())) {
                                    resultList.add(info);
                                }
                            }
                            //将新应用数据插入数据库
                            if (resultList.size() != 0) {
                                DbManager.get().saveInfoList(resultList);
                            }
                        } else if (appList.size() < dbList.size()) { //如果有卸载应用
                            List<LockAppInfo> resultList = new ArrayList<>();
                            HashMap<String, LockAppInfo> hashMap = new HashMap<>();
                            for (LockAppInfo info : appList) {
                                hashMap.put(info.getPackageName(), info);
                            }
                            for (LockAppInfo info : dbList) {
                                if (!hashMap.containsKey(info.getPackageName())) {
                                    resultList.add(info);
                                }
                            }
                            //将卸载的应用从数据库删除
                            if (resultList.size() != 0) {
                                DbManager.get().deleteInfoByList(resultList);
                            }
                        }
                        return DbManager.get().queryInfoList();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<LockAppInfo>>() {
                    @Override
                    public void accept(List<LockAppInfo> lockAppInfos) throws Exception {
                        if (lockAppInfos.size() != 0) {
                            animator.start();
                        } else {
                            Toast.makeText(mContext, "数据处理错误", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(mContext, "数据处理错误", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initSplashAnim() {
        PropertyValuesHolder pvh1 = PropertyValuesHolder.ofFloat("scaleX", 1f, 1.5f);
        PropertyValuesHolder pvh2 = PropertyValuesHolder.ofFloat("scaleY", 1f, 1.5f);
        animator = ObjectAnimator.ofPropertyValuesHolder(mImageSplash, pvh1, pvh2);
        animator.setDuration(2000);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (isFirstTime) {
                    PermissionSettingActivity.launch(SplashActivity.this);
                } else {
                    Intent intent = new Intent(SplashActivity.this, UnlockActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        animator = null;
    }
}
