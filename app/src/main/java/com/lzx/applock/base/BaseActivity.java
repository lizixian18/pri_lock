package com.lzx.applock.base;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.lzx.applock.R;
import com.lzx.applock.utils.SystemBarHelper;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

/**
 * @author lzx
 * @date 2018/2/27
 */

public abstract class BaseActivity extends RxAppCompatActivity {
    protected Context mContext;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        mContext = this;
        initStatusBar();
        init(savedInstanceState);
    }

    protected abstract @LayoutRes  int getLayoutId();

    /**
     * 初始化StatusBar
     */
    protected void initStatusBar() {
        mToolbar = findViewById(R.id.toolbar);
        if (mToolbar != null) {
            SystemBarHelper.immersiveStatusBar(this, 0);
            SystemBarHelper.setHeightAndPadding(this, mToolbar);
            setSupportActionBar(mToolbar);
            getSupportActionBar().setTitle("");
            mToolbar.setTitleMarginStart(0);
            mToolbar.setContentInsetStartWithNavigation(0);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        finish();
                    } else {
                        supportFinishAfterTransition();
                    }
                }
            });
        }
    }

    protected abstract void init(Bundle savedInstanceState);
}
