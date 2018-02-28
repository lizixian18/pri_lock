package com.lzx.applock.module.lock;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.lzx.applock.R;
import com.lzx.applock.base.BaseActivity;
import com.lzx.applock.module.main.MainActivity;
import com.lzx.applock.utils.LockPatternUtils;
import com.lzx.applock.utils.LockPatternViewPattern;
import com.lzx.applock.widget.LockPatternView;

import java.util.List;

/**
 * 应用内解锁
 * @author lzx
 * @date 2018/2/28
 */

public class UnlockActivity extends BaseActivity {
    @Override
    protected int getLayoutId() {
        return R.layout.activity_create_pwd;
    }

    private TextView mLockTip;
    private LockPatternView mPatternView;

    private LockPatternUtils mLockPatternUtils;
    private LockPatternViewPattern mPatternViewPattern;
    private int mFailedPatternAttemptsSinceLastTimeout = 0;

    @Override
    protected void init(Bundle savedInstanceState) {
        mLockTip = findViewById(R.id.lock_tip);
        mPatternView = findViewById(R.id.lock_pattern_view);
        mLockTip.setText("Confirm identity");
        initLockPatternView();
    }

    /**
     * 初始化解锁控件
     */
    private void initLockPatternView() {
        mLockPatternUtils = new LockPatternUtils(this);
        mPatternViewPattern = new LockPatternViewPattern(mPatternView);
        mPatternViewPattern.setPatternListener(new LockPatternViewPattern.onPatternListener() {
            @Override
            public void onPatternDetected(List<LockPatternView.Cell> pattern) {
                if (mLockPatternUtils.checkPattern(pattern)) { //解锁成功,更改数据库状态
                    mPatternView.setDisplayMode(LockPatternView.DisplayMode.Correct);

                    Intent intent = new Intent(mContext, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    mPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                    if (pattern.size() >= LockPatternUtils.MIN_PATTERN_REGISTER_FAIL) {
                        mFailedPatternAttemptsSinceLastTimeout++;
                        int retry = LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT - mFailedPatternAttemptsSinceLastTimeout;
                        if (retry >= 0) {

                        }
                    } else {

                    }
                    if (mFailedPatternAttemptsSinceLastTimeout >= 3) { //失败次数大于3次

                    }
                    if (mFailedPatternAttemptsSinceLastTimeout >= LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT) { //失败次数大于阻止用户前的最大错误尝试次数

                    } else {
                        mPatternView.postDelayed(mClearPatternRunnable, 500);
                    }
                }
            }
        });
        mPatternView.setOnPatternListener(mPatternViewPattern);
        mPatternView.setTactileFeedbackEnabled(true);
    }

    private Runnable mClearPatternRunnable = new Runnable() {
        public void run() {
            mPatternView.clearPattern();
        }
    };
}
