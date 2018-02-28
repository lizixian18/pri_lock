package com.lzx.applock.module.lock;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.lzx.applock.R;
import com.lzx.applock.bean.LockAppInfo;
import com.lzx.applock.utils.BlurUtil;
import com.lzx.applock.utils.LockPatternUtils;
import com.lzx.applock.utils.LockPatternViewPattern;
import com.lzx.applock.widget.LockPatternView;

import java.util.List;

/**
 * 解锁界面
 *
 * @author lzx
 * @date 2018/2/28
 */

public class UnlockView {

    private int mFailedPatternAttemptsSinceLastTimeout = 0;

    private WindowManager.LayoutParams mLayoutParams;
    private WindowManager mWindowManager;
    private Context mContext;
    private View mUnLockView;
    private Drawable iconDrawable;
    private String appLabel;

    private ImageView mBgView, mUnLockIcon, mBtnMore;
    private TextView mUnLockAppName, mUnlockFailTip;
    private LockPatternView mPatternView;

    private LockPatternUtils mPatternUtils;
    private LockPatternViewPattern mPatternViewPattern;
    private LockAppInfo mLockAppInfo;
    private ApplicationInfo mApplicationInfo;
    private PackageManager mPackageManager;

    public UnlockView(Context context) {
        mContext = context;
        mPackageManager = mContext.getPackageManager();
        //创建悬浮窗
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT
        );
        mLayoutParams.gravity = Gravity.CENTER;
    }

    public void setLockAppInfo(LockAppInfo lockAppInfo) {
        mLockAppInfo = lockAppInfo;
    }

    public void showUnLockView() {
        if (mLockAppInfo == null) {
            return;
        }
        mUnLockView = LayoutInflater.from(mContext).inflate(R.layout.layout_unlock_view, null, false);
        mBgView = mUnLockView.findViewById(R.id.bg_view);
        mUnLockIcon = mUnLockView.findViewById(R.id.unlock_icon);
        mBtnMore = mUnLockView.findViewById(R.id.btn_more);
        mUnLockAppName = mUnLockView.findViewById(R.id.unlock_app_name);
        mPatternView = mUnLockView.findViewById(R.id.unlock_lock_view);
        mUnlockFailTip = mUnLockView.findViewById(R.id.unlock_fail_tip);
        initBgView();
        initLockPatternView();
        mWindowManager.addView(mUnLockView, mLayoutParams);
    }

    private void closeUnLockView() {
        if (mUnLockView != null) {
            mWindowManager.removeView(mUnLockView);
            mUnLockView = null;
        }
    }

    /**
     * 背景图片
     */
    private void initBgView() {
        mApplicationInfo = mLockAppInfo.getAppInfo();
        if (mApplicationInfo != null) {
            try {
                iconDrawable = mPackageManager.getApplicationIcon(mApplicationInfo);
                appLabel = mLockAppInfo.getAppName();
                mUnLockIcon.setImageDrawable(iconDrawable);
                mUnLockAppName.setText(appLabel);
                mUnlockFailTip.setText(mContext.getString(R.string.password_gestrue_tips));
                mBgView.setBackground(iconDrawable);
                mBgView.getViewTreeObserver().addOnPreDrawListener(
                        new ViewTreeObserver.OnPreDrawListener() {
                            @Override
                            public boolean onPreDraw() {
                                mBgView.getViewTreeObserver().removeOnPreDrawListener(this);
                                mBgView.buildDrawingCache();
                                Bitmap bmp = BlurUtil.drawableToBitmap(iconDrawable, mBgView);
                                BlurUtil.blur(mContext, BlurUtil.big(bmp), mBgView);  //高斯模糊
                                return true;
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 初始化解锁控件
     */
    private void initLockPatternView() {
        mPatternView.setLineColorRight(0x80ffffff);
        mPatternUtils = new LockPatternUtils(mContext);
        mPatternViewPattern = new LockPatternViewPattern(mPatternView);
        mPatternViewPattern.setPatternListener(new LockPatternViewPattern.onPatternListener() {
            @Override
            public void onPatternDetected(List<LockPatternView.Cell> pattern) {
                if (mPatternUtils.checkPattern(pattern)) { //解锁成功,更改数据库状态
                    mPatternView.setDisplayMode(LockPatternView.DisplayMode.Correct);
                    //TODO
                    closeUnLockView();
                } else {
                    mPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                    if (pattern.size() >= LockPatternUtils.MIN_PATTERN_REGISTER_FAIL) {
                        mFailedPatternAttemptsSinceLastTimeout++;
                        int retry = LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT - mFailedPatternAttemptsSinceLastTimeout;
                        if (retry >= 0) {
                            String format = mContext.getResources().getString(R.string.password_error_count);
                            mUnlockFailTip.setText(format);
                        }
                    } else {
                        mUnlockFailTip.setText(mContext.getResources().getString(R.string.password_short));
                    }
                    if (mFailedPatternAttemptsSinceLastTimeout >= 3) { //失败次数大于3次
                        mPatternView.postDelayed(mClearPatternRunnable, 500);
                    }
                    if (mFailedPatternAttemptsSinceLastTimeout >= LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT) { //失败次数大于阻止用户前的最大错误尝试次数
                        mPatternView.postDelayed(mClearPatternRunnable, 500);
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