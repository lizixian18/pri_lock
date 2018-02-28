package com.lzx.applock.module.splash;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.lzx.applock.R;
import com.lzx.applock.base.BaseActivity;
import com.lzx.applock.constants.Constants;
import com.lzx.applock.module.lock.UnlockActivity;
import com.lzx.applock.utils.SpUtil;
import com.lzx.applock.utils.SystemBarHelper;

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
        animator = ObjectAnimator.ofFloat(mImageSplash, "alpha", 0.5f, 1);
        animator.setDuration(1500);
        animator.start();
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
}
