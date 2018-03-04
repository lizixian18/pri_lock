package com.lzx.applock.module.main;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.lzx.applock.R;
import com.lzx.applock.base.BaseActivity;
import com.lzx.applock.bean.LockAppInfo;
import com.lzx.applock.constants.Constants;
import com.lzx.applock.db.DbManager;
import com.lzx.applock.helper.LoadAppHelper;
import com.lzx.applock.module.setting.SettingFragment;
import com.lzx.applock.service.AppLockService;
import com.lzx.applock.utils.SpUtil;

import java.util.List;

import io.reactivex.functions.Consumer;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private TextView mBtnSetting, mBtnHome;
    private DrawerLayout mDrawerLayout;
    private Fragment[] fragments;
    private int currentTabIndex;
    private int index;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mBtnSetting = findViewById(R.id.btn_setting);
        mBtnHome = findViewById(R.id.btn_home);

        SpUtil.getInstance().putBoolean(Constants.IS_FIRST_TIME, false);

        initFragments();

        mBtnHome.setOnClickListener(this);
        mBtnSetting.setOnClickListener(this);
        mBtnHome.setBackgroundColor(Color.parseColor("#cccccc"));

        Intent intent = new Intent(this, AppLockService.class);
        startService(intent);
    }

    private void changeFragment(int position) {
        toggleDrawer();
        changeFragmentIndex(position);
    }

    private void initFragments() {
        fragments = new Fragment[2];
        fragments[0] = MainFragment.newInstance();
        fragments[1] = SettingFragment.newInstance();

        // 添加显示第一个fragment
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, fragments[0])
                .show(fragments[0]).commit();
    }

    /**
     * Fragment切换
     */
    private void switchFragment() {
        if (currentTabIndex != index) {
            FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
            trx.replace(R.id.container, fragments[index]).addToBackStack(null).commit(); //这里使用替换，不保留状态
        }
        currentTabIndex = index;
    }

    /**
     * 切换Fragment的下标
     */
    private void changeFragmentIndex(int currentIndex) {
        index = currentIndex;
        switchFragment();
    }

    /**
     * DrawerLayout侧滑菜单开关
     */
    public void toggleDrawer() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    /**
     * 监听back键处理DrawerLayout和SearchView
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mDrawerLayout.isDrawerOpen(mDrawerLayout.getChildAt(1))) {
                mDrawerLayout.closeDrawers();
            } else {
                finish();
            }
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_home:
                if (index != 0) {
                    changeFragmentIndex(0);
                    mBtnHome.setBackgroundColor(ContextCompat.getColor(this,R.color.statue_bar_color));
                    mBtnSetting.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                }
                break;
            case R.id.btn_setting:
                if (index != 1) {
                    changeFragmentIndex(1);
                    mBtnHome.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                    mBtnSetting.setBackgroundColor(ContextCompat.getColor(this,R.color.statue_bar_color));
                }
                break;
        }
        toggleDrawer();
    }
}
