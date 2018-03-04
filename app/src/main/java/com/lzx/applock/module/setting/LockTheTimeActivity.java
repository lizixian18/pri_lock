package com.lzx.applock.module.setting;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import com.lzx.applock.R;
import com.lzx.applock.base.BaseActivity;
import com.lzx.applock.constants.Constants;
import com.lzx.applock.module.detail.CardDetailActivity;
import com.lzx.applock.module.main.MainActivity;
import com.lzx.applock.utils.SpUtil;
import com.lzx.applock.utils.SystemBarHelper;

/**
 * 设置锁屏时间
 * Created by xian on 2018/3/4.
 */

public class LockTheTimeActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {

    public static void launch(Context context) {
        Intent intent = new Intent(context, LockTheTimeActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_lock_the_time;
    }

    private RadioButton mCheckScreenOff, mCheckAfterTime, mCheckEveryTime;
    private RadioButton[] mArrayCheckBoxs = new RadioButton[3];

    @Override
    protected void init(Bundle savedInstanceState) {
        getSupportActionBar().setTitle("Setting");
        SystemBarHelper.tintStatusBar(getWindow(), ContextCompat.getColor(this, R.color.statue_bar_color));
        mCheckScreenOff = findViewById(R.id.check_screen_off);
        mCheckAfterTime = findViewById(R.id.check_time);
        mCheckEveryTime = findViewById(R.id.check_every_time);

        mArrayCheckBoxs[0] = mCheckScreenOff;
        mArrayCheckBoxs[1] = mCheckAfterTime;
        mArrayCheckBoxs[2] = mCheckEveryTime;

        int lockMode = SpUtil.getInstance().getInt(Constants.LOCK_MODEL);

        for (int i = 0; i < mArrayCheckBoxs.length; i++) {
            if (i == lockMode) {
                mArrayCheckBoxs[i].setChecked(true);
            }
            mArrayCheckBoxs[i].setOnCheckedChangeListener(this);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
            for (int i = 0; i < mArrayCheckBoxs.length; i++) {
                boolean isChecked = mArrayCheckBoxs[i].getTag().toString().equals(compoundButton.getTag().toString());
                mArrayCheckBoxs[i].setChecked(isChecked);
                if (isChecked) {
                    SpUtil.getInstance().putInt(Constants.LOCK_MODEL, i);
                    updateServiceLockMode();
                }
            }
        }
    }

    public void updateServiceLockMode() {
        sendBroadcast(new Intent(Constants.ACTION_UPDATE_LOCK_MODE));
    }
}
