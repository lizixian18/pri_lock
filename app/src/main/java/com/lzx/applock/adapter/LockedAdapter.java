package com.lzx.applock.adapter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lzx.applock.R;
import com.lzx.applock.bean.LockAppInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lzx
 * @date 2018/2/27
 */

public class LockedAdapter extends RecyclerView.Adapter<LockedAdapter.LockedHolder> {

    private Context mContext;
    private PackageManager packageManager;
    private List<LockAppInfo> mLockAppInfos = new ArrayList<>();

    public LockedAdapter(Context context) {
        mContext = context;
        packageManager = mContext.getPackageManager();
    }

    public void setLockAppInfos(List<LockAppInfo> lockAppInfos) {
        mLockAppInfos.clear();
        mLockAppInfos.addAll(lockAppInfos);
        notifyDataSetChanged();
    }

    @Override
    public LockedHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_app_card, parent, false);
        return new LockedHolder(view);
    }

    @Override
    public void onBindViewHolder(LockedHolder holder, int position) {
        LockAppInfo info = mLockAppInfos.get(position);
        holder.mAppName.setText(info.getAppName());
        holder.mTextLockStatue.setText(info.isLocked() ? "Locked" : "unLocked");
        if (info.isSysApp()) {
            holder.mImageLockStatue.setImageResource(info.isLocked() ?
                    R.drawable.ic_sys_lock : R.drawable.ic_unlock);
        } else {
            holder.mImageLockStatue.setImageResource(info.isLocked() ?
                    R.drawable.ic_user_lock : R.drawable.ic_unlock);
        }
        holder.mAppIcon.setImageDrawable(packageManager.getApplicationIcon(info.getAppInfo()));

    }

    @Override
    public int getItemCount() {
        return mLockAppInfos.size();
    }

    class LockedHolder extends RecyclerView.ViewHolder {
        ImageView mImageLockStatue, mAppIcon;
        TextView mTextLockStatue, mAppName;

        LockedHolder(View itemView) {
            super(itemView);
            mImageLockStatue = itemView.findViewById(R.id.image_lock_statue);
            mAppIcon = itemView.findViewById(R.id.image_app_icon);
            mAppName = itemView.findViewById(R.id.text_app_name);
            mTextLockStatue = itemView.findViewById(R.id.text_lock_statue);
        }
    }
}
