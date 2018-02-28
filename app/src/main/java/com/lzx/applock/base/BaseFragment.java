package com.lzx.applock.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.trello.rxlifecycle2.components.support.RxFragment;

/**
 * @author lzx
 * @date 2018/2/27
 */

public abstract class BaseFragment extends RxFragment {
    protected View mRootView;
    protected LayoutInflater inflater;
    // 标志位 标志已经初始化完成。
    protected boolean isPrepared;
    //标志位 fragment是否可见
    protected boolean isVisible;

    protected Context mContext;
    protected Activity mActivity;


    @Override
    public void onAttach(Context context) {
        mActivity = (Activity) context;
        mContext = context;
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRootView != null) {
            ViewGroup parent = (ViewGroup) mRootView.getParent();
            if (parent != null)
                parent.removeView(mRootView);
        } else {
            mRootView = inflater.inflate(getLayoutId(), container, false);
            mActivity = getActivity();
            mContext = mActivity;
            this.inflater = inflater;
        }
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isPrepared = true;
        init();
        lazyLoad();
    }

    public abstract  @LayoutRes
    int getLayoutId();

    protected abstract void init();

    /**
     * 懒加载
     */
    private void lazyLoad() {
        if (!isPrepared || !isVisible) {
            return;
        }
        lazyLoadData();
        isPrepared = false;
    }

    /**
     * 懒加载
     */
    protected void lazyLoadData() {

    }

    protected void onVisible() {
        lazyLoad();
    }

    protected void onInvisible() {

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            isVisible = true;
            onVisible();
        } else {
            isVisible = false;
            onInvisible();
        }
    }

    /**
     * 获取ApplicationContext 信息
     */
    public Context getApplicationContext() {
        return this.mContext == null ? (getActivity() == null ? null : getActivity()
                .getApplicationContext()) : this.mContext.getApplicationContext();
    }

    public View findViewById(@IdRes int id) {
        View view;
        if (mRootView != null) {
            view = mRootView.findViewById(id);
            return view;
        }
        return null;
    }
}
