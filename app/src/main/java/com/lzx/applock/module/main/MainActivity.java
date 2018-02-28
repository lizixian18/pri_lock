package com.lzx.applock.module.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.widget.Toast;

import com.lzx.applock.R;
import com.lzx.applock.base.BaseActivity;
import com.lzx.applock.bean.LockAppInfo;
import com.lzx.applock.constants.Constants;
import com.lzx.applock.db.DbManager;
import com.lzx.applock.helper.LoadAppHelper;
import com.lzx.applock.utils.SpUtil;

import java.util.List;

import io.reactivex.functions.Consumer;

public class MainActivity extends BaseActivity {

    private DrawerLayout mDrawerLayout;
    private Fragment[] fragments;
    private long exitTime;
    private int currentTabIndex;
    private int index;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        SpUtil.getInstance().putBoolean(Constants.IS_FIRST_TIME, false);
        initFragments();

        LoadAppHelper.loadAllLockAppInfoAsync(this)
                .subscribe(new Consumer<List<LockAppInfo>>() {
                    @Override
                    public void accept(List<LockAppInfo> lockAppInfos) throws Exception {
                        DbManager.get().saveLockAppInfoListAsync(lockAppInfos);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(mContext, "数据处理错误", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void changeFragment(int position) {
        toggleDrawer();
        changeFragmentIndex(position);
    }

    private void initFragments() {
        fragments = new Fragment[1];
        for (int i = 0; i < fragments.length; i++) {
            fragments[i] = MainFragment.newInstance();
        }
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
                exitApp();
            }
        }
        return true;
    }

    /**
     * 双击退出App
     */
    private void exitApp() {
        if (System.currentTimeMillis() - exitTime > 2000) {
            Toast.makeText(mContext, "再按一次退出", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }
}
