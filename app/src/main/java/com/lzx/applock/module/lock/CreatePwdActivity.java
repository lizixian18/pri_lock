package com.lzx.applock.module.lock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.lzx.applock.R;
import com.lzx.applock.base.BaseActivity;
import com.lzx.applock.bean.LockStage;
import com.lzx.applock.constants.Constants;
import com.lzx.applock.module.lock.presenter.CreateContract;
import com.lzx.applock.module.lock.presenter.PwdPresenter;
import com.lzx.applock.utils.LockPatternUtils;
import com.lzx.applock.utils.LockPatternViewPattern;
import com.lzx.applock.utils.SpUtil;
import com.lzx.applock.widget.LockPatternView;

import java.util.List;

/**
 * 创建和修改密码
 *
 * @author lzx
 * @date 2018/2/28
 */

public class CreatePwdActivity extends BaseActivity implements CreateContract.View {

    public static void launch(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, CreatePwdActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    private TextView mLockTip;
    private LockPatternView mPatternView;

    private LockStage mUiStage = LockStage.Introduction;
    protected List<LockPatternView.Cell> mChosenPattern = null; //密码
    private static final String KEY_PATTERN_CHOICE = "chosenPattern";
    private static final String KEY_UI_STAGE = "uiStage";
    private LockPatternUtils mLockPatternUtils;
    private LockPatternViewPattern mPatternViewPattern;
    private PwdPresenter mPresenter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_create_pwd;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mLockTip = findViewById(R.id.lock_tip);
        mPatternView = findViewById(R.id.lock_pattern_view);
        mPresenter = new PwdPresenter(this, this);
        initLockPatternView();
        if (savedInstanceState == null) {
            mPresenter.updateStage(LockStage.Introduction);
        } else {
            final String patternString = savedInstanceState.getString(KEY_PATTERN_CHOICE);
            if (patternString != null) {
                mChosenPattern = LockPatternUtils.stringToPattern(patternString);
            }
            mPresenter.updateStage(LockStage.values()[savedInstanceState.getInt(KEY_UI_STAGE)]);
        }
    }

    /**
     * 初始化锁屏控件
     */
    private void initLockPatternView() {
        mLockPatternUtils = new LockPatternUtils(this);
        mPatternViewPattern = new LockPatternViewPattern(mPatternView);
        mPatternViewPattern.setPatternListener(new LockPatternViewPattern.onPatternListener() {
            @Override
            public void onPatternDetected(List<LockPatternView.Cell> pattern) {
                mPresenter.onPatternDetected(pattern, mChosenPattern, mUiStage);
            }
        });
        mPatternView.setOnPatternListener(mPatternViewPattern);
        mPatternView.setTactileFeedbackEnabled(true);
    }

    @Override
    public void updateUiStage(LockStage stage) {
        mUiStage = stage;
    }

    @Override
    public void updateChosenPattern(List<LockPatternView.Cell> mChosenPattern) {
        this.mChosenPattern = mChosenPattern;
    }

    @Override
    public void updateLockTip(String text, boolean isToast) {
        if (isToast) {
            Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
        } else {
            mLockTip.setText(text);
        }
    }

    @Override
    public void setHeaderMessage(int headerMessage) {
        mLockTip.setText(headerMessage);
    }

    @Override
    public void lockPatternViewConfiguration(boolean patternEnabled, LockPatternView.DisplayMode displayMode) {
        if (patternEnabled) {
            mPatternView.enableInput();
        } else {
            mPatternView.disableInput();
        }
        mPatternView.setDisplayMode(displayMode);
    }

    @Override
    public void Introduction() {
        clearPattern();
    }

    @Override
    public void HelpScreen() {

    }

    @Override
    public void ChoiceTooShort() {
        mPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);  //路径太短
        mPatternView.removeCallbacks(mClearPatternRunnable);
        mPatternView.postDelayed(mClearPatternRunnable, 1000);
    }

    private Runnable mClearPatternRunnable = new Runnable() {
        public void run() {
            mPatternView.clearPattern();
        }
    };

    @Override
    public void moveToStatusTwo() {

    }

    @Override
    public void clearPattern() {
        mPatternView.clearPattern();
    }

    @Override
    public void ConfirmWrong() {
        mChosenPattern.clear();
        clearPattern();
    }

    @Override
    public void ChoiceConfirmed() {
        mLockPatternUtils.saveLockPattern(mChosenPattern); //保存密码
        SpUtil.getInstance().putBoolean(Constants.IS_DRAW_PWD, true);
        clearPattern();
        setResult(RESULT_OK);
        finish();
    }
}
