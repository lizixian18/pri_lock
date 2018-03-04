package com.lzx.applock.module.setting;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lzx.applock.R;
import com.lzx.applock.base.BaseFragment;
import com.lzx.applock.constants.Constants;
import com.lzx.applock.module.lock.CreatePwdActivity;
import com.lzx.applock.module.main.MainActivity;
import com.lzx.applock.utils.SpUtil;

/**
 * 设置
 * Created by xian on 2018/3/4.
 */

public class SettingFragment extends BaseFragment implements View.OnClickListener {

    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_setting;
    }

    private static final int REQUEST_CREATEPWD = 1000;
    private Toolbar mToolbar;
    private LinearLayout mBtnLockSetting, mBtnShowPath;
    private TextView mLockMode, mBtnChangePwd, mShowPathStatus;
    private boolean isHidPathLine;

    @Override
    protected void init() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mBtnLockSetting = (LinearLayout) findViewById(R.id.btn_lock_setting);
        mLockMode = (TextView) findViewById(R.id.lock_mode);
        mBtnChangePwd = (TextView) findViewById(R.id.btn_change_pwd);
        mBtnShowPath = (LinearLayout) findViewById(R.id.btn_show_path);
        mShowPathStatus = (TextView) findViewById(R.id.show_path_status);

        mBtnLockSetting.setOnClickListener(this);
        mBtnChangePwd.setOnClickListener(this);
        mBtnShowPath.setOnClickListener(this);

        initToolBar();

        isHidPathLine = SpUtil.getInstance().getBoolean(Constants.LOCK_IS_HIDE_LINE, false);
        mShowPathStatus.setText(isHidPathLine ? "Hide" : "Show");
    }

    private void initToolBar() {
        mToolbar.setTitleTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        mToolbar.setTitle("Setting");
        ((MainActivity) getActivity()).setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.bar_menu);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).toggleDrawer();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        int lockMode = SpUtil.getInstance().getInt(Constants.LOCK_MODEL);
        if (lockMode == 0) {
            mLockMode.setText(getString(R.string.model_screen_off));
        } else if (lockMode == 1) {
            mLockMode.setText(getString(R.string.model_time));
        } else if (lockMode == 2) {
            mLockMode.setText(getString(R.string.model_every_time));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_lock_setting:
                LockTheTimeActivity.launch(getActivity());
                break;
            case R.id.btn_change_pwd:
                CreatePwdActivity.launch(getActivity(), REQUEST_CREATEPWD);
                break;
            case R.id.btn_show_path:
                String currStatus = mShowPathStatus.getText().toString();
                if (currStatus.equals("Hide")) {
                    SpUtil.getInstance().putBoolean(Constants.LOCK_IS_HIDE_LINE, false);
                    mShowPathStatus.setText("Show");
                }else {
                    SpUtil.getInstance().putBoolean(Constants.LOCK_IS_HIDE_LINE, true);
                    mShowPathStatus.setText("Hide");
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CREATEPWD) {
            Toast.makeText(mContext, "密码已修改成功", Toast.LENGTH_SHORT).show();
        }
    }
}

