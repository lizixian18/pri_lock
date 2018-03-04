package com.lzx.applock.module.detail;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.lzx.applock.R;
import com.lzx.applock.adapter.LockedListAdapter;
import com.lzx.applock.base.BaseActivity;
import com.lzx.applock.bean.LockAppInfo;
import com.lzx.applock.db.DbManager;
import com.lzx.applock.helper.LoadAppHelper;
import com.lzx.applock.utils.AppBarStateChangeEvent;
import com.lzx.applock.utils.SystemBarHelper;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @author lzx
 * @date 2018/2/27
 */

public class CardDetailActivity extends BaseActivity {

    private View mBgView;
    private ImageView mIcon;
    private TextView mTitle, mToolTitle;
    private Switch mSwitch;
    private RecyclerView mRecyclerView;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private AppBarLayout mAppBarLayout;
    private LockedListAdapter mLockedListAdapter;
    private String type;

    public static void launch(Context context, String type) {
        Intent intent = new Intent(context, CardDetailActivity.class);
        intent.putExtra("type", type);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_card_detail;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mBgView = findViewById(R.id.bg_view);
        mIcon = findViewById(R.id.icon);
        mTitle = findViewById(R.id.title);
        mSwitch = findViewById(R.id.sys_switch);
        mToolTitle = findViewById(R.id.tool_title);
        mRecyclerView = findViewById(R.id.recycle_view);
        mAppBarLayout = findViewById(R.id.app_bar_layout);
        mCollapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        type = getIntent().getStringExtra("type");
        if (type.equals("sys")) {
            mBgView.setBackgroundColor(Color.parseColor("#47A7FF"));
            mIcon.setImageResource(R.drawable.ic_home_sys);
            mTitle.setText(getString(R.string.sys_app));
            mToolTitle.setText(getString(R.string.sys_app));
        } else {
            mBgView.setBackgroundColor(Color.parseColor("#F69147"));
            mIcon.setImageResource(R.drawable.ic_home_user);
            mTitle.setText(getString(R.string.user_app));
            mToolTitle.setText(getString(R.string.user_app));
        }
        updateStatusBarColor();
        //设置还没收缩时状态下字体颜色
        mCollapsingToolbarLayout.setExpandedTitleColor(Color.TRANSPARENT);
        //设置收缩后Toolbar上字体的颜色
        mCollapsingToolbarLayout.setCollapsedTitleTextColor(ContextCompat.getColor(this, R.color.colorAccent));

        mAppBarLayout.addOnOffsetChangedListener(new AppBarStateChangeEvent() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state, int verticalOffset) {
                if (state == State.EXPANDED) {
                    //展开状态
                    mToolTitle.setVisibility(View.GONE);
                    updateStatusBarColor();
                } else if (state == State.COLLAPSED) {
                    //折叠状态
                    mToolTitle.setVisibility(View.VISIBLE);
                    SystemBarHelper.tintStatusBar(CardDetailActivity.this, ContextCompat.getColor(CardDetailActivity.this, R.color.statue_bar_color));
                }
            }
        });

        mLockedListAdapter = new LockedListAdapter(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mLockedListAdapter);

        DbManager.get().queryLockAppInfoListAsync()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<LockAppInfo>>() {
                    @Override
                    public void accept(List<LockAppInfo> lockAppInfos) throws Exception {
                        List<LockAppInfo> sysList = new ArrayList<>();
                        List<LockAppInfo> userList = new ArrayList<>();
                        for (LockAppInfo info : lockAppInfos) {
                            if (info.isSysApp()) {
                                sysList.add(info);
                            } else {
                                userList.add(info);
                            }
                        }
                        if (type.equals("sys")) {
                            mLockedListAdapter.setLockAppInfos(sysList);
                        } else {
                            mLockedListAdapter.setLockAppInfos(userList);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(mContext, "获取数据失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateStatusBarColor() {
        if (type.equals("sys")) {
            SystemBarHelper.tintStatusBar(this, Color.parseColor("#47A7FF"));
        } else {
            SystemBarHelper.tintStatusBar(this, Color.parseColor("#F69147"));
        }
    }


}
