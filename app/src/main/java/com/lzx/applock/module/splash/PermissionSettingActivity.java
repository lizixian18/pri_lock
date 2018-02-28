package com.lzx.applock.module.splash;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.lzx.applock.R;
import com.lzx.applock.base.BaseActivity;
import com.lzx.applock.constants.Constants;
import com.lzx.applock.helper.PermissionHelper;
import com.lzx.applock.module.lock.CreatePwdActivity;
import com.lzx.applock.module.main.MainActivity;
import com.lzx.applock.utils.DisplayUtil;
import com.lzx.applock.utils.SpUtil;

/**
 * @author lzx
 * @date 2018/2/28
 */

public class PermissionSettingActivity extends BaseActivity {

    public static void launch(Context context) {
        Intent intent = new Intent(context, PermissionSettingActivity.class);
        context.startActivity(intent);
    }

    private TextView mTextHello, mTextComma, mTextWelcome, mTextDesc, mPermissionName, mGetBtn;

    private PropertyValuesHolder pvh1, pvh2;
    private ObjectAnimator mHelloAnim, mCommaAnim, mWelcomeAnim, mDescAnim;
    private float visualY = 0;
    private String currRequestPermission;
    private static final int REQUEST_FLOATWINDOW = 1000;
    private static final int REQUEST_USAGESTATS = 2000;
    private static final int REQUEST_CREATEPWD = 3000;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_permission_setting;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mTextHello = findViewById(R.id.text_hello);
        mTextComma = findViewById(R.id.text_comma);
        mTextWelcome = findViewById(R.id.text_welcome);
        mTextDesc = findViewById(R.id.text_desc);
        mPermissionName = findViewById(R.id.permission_name);
        mGetBtn = findViewById(R.id.get_btn);
        mTextHello.post(new Runnable() {
            @Override
            public void run() {
                visualY = mTextHello.getY() / 3;
                mTextComma.setY(visualY * 4);
                mTextWelcome.setY(visualY * 4);
                initHelloAnim();
            }
        });
        initGetBtn();
        mGetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();
            }
        });
    }

    private void initGetBtn() {
        boolean hasFlowWindowPermission = PermissionHelper.isHasManageOverlay(this);
        boolean hasUsageStatsPermission = PermissionHelper.isStatAccessPermissionSet(this);
        if (!hasFlowWindowPermission) {
            mPermissionName.setText("Floating Window Permission");
            currRequestPermission = "floatWindow";
        } else if (!hasUsageStatsPermission) {
            mPermissionName.setText("Usage Stats Permission");
            currRequestPermission = "UsageStats";
        } else {
            boolean isDrawPwd = SpUtil.getInstance().getBoolean(Constants.IS_DRAW_PWD, false);
            if (!isDrawPwd) {
                mPermissionName.setText("Congratulations !\nAll permission already obtain, Now please create your unlock password");
                currRequestPermission = "CreatePwd";
            }
        }
    }

    private void initHelloAnim() {
        pvh1 = PropertyValuesHolder.ofFloat("translationY", 0, visualY);
        pvh2 = PropertyValuesHolder.ofFloat("alpha", 0, 1);
        mHelloAnim = ObjectAnimator.ofPropertyValuesHolder(mTextHello, pvh1, pvh2).setDuration(2000);
        mHelloAnim.start();
        mHelloAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mTextComma.setVisibility(View.VISIBLE);
                initCommaAnim();
            }
        });
    }

    private void initCommaAnim() {
        mCommaAnim = ObjectAnimator.ofFloat(mTextComma, "alpha", 0, 1).setDuration(2000);
        mCommaAnim.setInterpolator(new LinearInterpolator());
        mCommaAnim.start();
        mCommaAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mTextWelcome.setVisibility(View.VISIBLE);
                initWelcomeAnim();
            }
        });
    }

    private void initWelcomeAnim() {
        mWelcomeAnim = ObjectAnimator.ofFloat(mTextWelcome, "alpha", 0, 1).setDuration(1500);
        mWelcomeAnim.setInterpolator(new LinearInterpolator());
        mWelcomeAnim.start();
        mWelcomeAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mTextDesc.setVisibility(View.VISIBLE);
                initDescAnim();
            }
        });
    }

    private void initDescAnim() {
        int phoneWidth = DisplayUtil.getPhoneWidth(this);
        pvh1 = PropertyValuesHolder.ofFloat("translationX", phoneWidth, 50);
        pvh2 = PropertyValuesHolder.ofFloat("alpha", 0, 1);
        mDescAnim = ObjectAnimator.ofPropertyValuesHolder(mTextDesc, pvh1, pvh2).setDuration(1000);
        mDescAnim.start();
        mDescAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mPermissionName.setVisibility(View.VISIBLE);
                mGetBtn.setVisibility(View.VISIBLE);
            }
        });
    }

    private void requestPermission() {
        if (TextUtils.isEmpty(currRequestPermission)) {
            return;
        }
        switch (currRequestPermission) {
            case "floatWindow":
                PermissionHelper.launchManageOverlaySettings(this, REQUEST_FLOATWINDOW);
                break;
            case "UsageStats":
                if (PermissionHelper.isStatAccessNoOption(this)) {
                    PermissionHelper.launchUsageAccessSettings(this, REQUEST_USAGESTATS);
                }
                break;
            case "CreatePwd":
                CreatePwdActivity.launch(this, REQUEST_CREATEPWD);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        boolean hasFlowWindowPermission = PermissionHelper.isHasManageOverlay(this);
        boolean hasUsageStatsPermission = PermissionHelper.isStatAccessPermissionSet(this);
        if (requestCode == REQUEST_FLOATWINDOW) {
            if (hasFlowWindowPermission) {
                mPermissionName.setText("Usage Stats Permission");
                currRequestPermission = "UsageStats";
            }
        } else if (requestCode == REQUEST_USAGESTATS) {
            if (hasUsageStatsPermission) {
                mPermissionName.setText("OK，Now please create your unlock password");
                currRequestPermission = "CreatePwd";
            }
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_CREATEPWD) {
            Intent intent = new Intent(mContext, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
