package com.lzx.applock.module.main;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.lzx.applock.R;
import com.lzx.applock.adapter.LockedAdapter;
import com.lzx.applock.base.BaseFragment;
import com.lzx.applock.bean.LockAppInfo;
import com.lzx.applock.db.DbManager;
import com.lzx.applock.helper.LoadAppHelper;
import com.lzx.applock.module.detail.CardDetailActivity;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;

/**
 * @author lzx
 * @date 2018/2/27
 */

public class MainFragment extends BaseFragment {

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    private Toolbar mToolbar;
    private RecyclerView mLockedRecycleview, mUnLockedRecycleview;
    private CardView mSysCardView, mUserCardView;
    private LockedAdapter mLockedAdapter;
    private LockedAdapter mUnLockedAdapter;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_main;
    }

    @Override
    protected void init() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mLockedRecycleview = (RecyclerView) findViewById(R.id.locked_recycleview);
        mUnLockedRecycleview = (RecyclerView) findViewById(R.id.unlocked_recycleview);
        mSysCardView = (CardView) findViewById(R.id.sys_cardview);
        mUserCardView = (CardView) findViewById(R.id.user_cardview);
        initToolBar();

        mLockedAdapter = new LockedAdapter(getActivity());
        mUnLockedAdapter = new LockedAdapter(getActivity());

        initRecyclerView(mLockedRecycleview, mLockedAdapter);
        initRecyclerView(mUnLockedRecycleview, mUnLockedAdapter);
        updateAdapterData();

        mSysCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CardDetailActivity.launch(getActivity(), "sys");
            }
        });
        mUserCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CardDetailActivity.launch(getActivity(), "user" +
                        "");
            }
        });
    }

    private void initToolBar() {
        mToolbar.setTitleTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        mToolbar.setTitle("AppLock");
        ((MainActivity) getActivity()).setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.bar_menu);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).toggleDrawer();
            }
        });
    }

    private void initRecyclerView(RecyclerView recyclerView, LockedAdapter adapter) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void updateAdapterData() {
        DbManager.get().queryLockAppInfoListAsync()
                .subscribe(new Consumer<List<LockAppInfo>>() {
                    @Override
                    public void accept(List<LockAppInfo> lockAppInfos) throws Exception {
                        List<LockAppInfo> lockedList = new ArrayList<>();
                        List<LockAppInfo> unlockedList = new ArrayList<>();
                        for (LockAppInfo info : lockAppInfos) {
                            if (info.isLocked()) {
                                lockedList.add(info);
                            } else {
                                unlockedList.add(info);
                            }
                        }
                        mLockedAdapter.setLockAppInfos(lockedList);
                        mUnLockedAdapter.setLockAppInfos(unlockedList);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(mContext, "加载数据失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
