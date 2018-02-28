package com.lzx.applock.module.splash;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
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

import java.util.List;

import io.reactivex.functions.Consumer;

/**
 * @author lzx
 * @date 2018/2/28
 */

public class SplashActivity extends BaseActivity {

    private ImageView mImageSplash;
    private ObjectAnimator animator;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_splash;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        SystemBarHelper.hideStatusBar(getWindow(), true);
        mImageSplash = findViewById(R.id.image_splash);
        initSplashAnim();
        LoadAppHelper.loadAllLockAppInfoAsync(this)
                .subscribe(new Consumer<List<LockAppInfo>>() {
                    @Override
                    public void accept(List<LockAppInfo> lockAppInfos) throws Exception {
                        DbManager.get().saveLockAppInfoListAsync(lockAppInfos)
                                .subscribe(new Consumer<Boolean>() {
                                    @Override
                                    public void accept(Boolean aBoolean) throws Exception {
                                        animator.start();
                                    }
                                }, new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception {
                                        LogUtil.i("saveLockAppInfoListAsync = "+throwable.getMessage());
                                    }
                                });
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(mContext, "数据处理错误", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initSplashAnim() {
        animator = ObjectAnimator.ofFloat(mImageSplash, "alpha", 0.5f, 1);
        animator.setDuration(1500);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                boolean isFirstTime = SpUtil.getInstance().getBoolean(Constants.IS_FIRST_TIME, true);
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
