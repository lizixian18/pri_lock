package com.lzx.applock.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.lzx.applock.R;

import java.util.List;

/**
 * 辅助功能Service
 * @author lzx
 * @date 2018/2/27
 */

public class LockAccessibilityService extends AccessibilityService {

    public static int INVOKE_TYPE = 0;
    public static final int TYPE_KILL_APP = 1;
    public static final int TYPE_INSTALL_APP = 2;
    public static final int TYPE_UNINSTALL_APP = 3;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        this.processAccessibilityEnvent(event);
    }

    public static void reset(){
        INVOKE_TYPE = 0;
    }

    private void processAccessibilityEnvent(AccessibilityEvent event) {
        if (event.getSource() == null) {

        } else {
            switch (INVOKE_TYPE) {
                case TYPE_KILL_APP:
                    processKillApplication(event);
                    break;
                case TYPE_INSTALL_APP:
                    processinstallApplication(event);
                    break;
                case TYPE_UNINSTALL_APP:
                    processUninstallApplication(event);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 自动安装
     * @param event
     */
    private void processinstallApplication(AccessibilityEvent event) {
        if (event.getSource() != null) {
            if (event.getPackageName().equals("com.android.packageinstaller")) {
                List<AccessibilityNodeInfo> unintall_nodes = event.getSource().findAccessibilityNodeInfosByText("安装");
                if (unintall_nodes!=null && !unintall_nodes.isEmpty()) {
                    AccessibilityNodeInfo node;
                    for(int i=0; i<unintall_nodes.size(); i++){
                        node = unintall_nodes.get(i);
                        if (node.getClassName().equals("android.widget.Button") && node.isEnabled()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            }
                        }
                    }
                }

                List<AccessibilityNodeInfo> next_nodes = event.getSource().findAccessibilityNodeInfosByText("下一步");
                if (next_nodes!=null && !next_nodes.isEmpty()) {
                    AccessibilityNodeInfo node;
                    for(int i=0; i<next_nodes.size(); i++){
                        node = next_nodes.get(i);
                        if (node.getClassName().equals("android.widget.Button") && node.isEnabled()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            }
                        }
                    }
                }

                List<AccessibilityNodeInfo> ok_nodes = event.getSource().findAccessibilityNodeInfosByText("打开");
                if (ok_nodes!=null && !ok_nodes.isEmpty()) {
                    AccessibilityNodeInfo node;
                    for(int i=0; i<ok_nodes.size(); i++){
                        node = ok_nodes.get(i);
                        if (node.getClassName().equals("android.widget.Button") && node.isEnabled()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            }
                        }
                    }
                }
            }
        }

    }


    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        sendBroadcast(new Intent("_accessibility_service_connected"));
    }

    @Override
    public void onInterrupt() {
    }

    /**
     * 强行停止
     * @param event
     */
    private void processKillApplication(AccessibilityEvent event) {

        if (event.getSource() != null) {
            if (event.getPackageName().equals("com.android.settings")) {
                List<AccessibilityNodeInfo> stop_nodes = event.getSource().findAccessibilityNodeInfosByText(getString(R.string.force_stop));
                if (stop_nodes!=null && !stop_nodes.isEmpty()) {
                    AccessibilityNodeInfo node;
                    for(int i=0; i<stop_nodes.size(); i++){
                        node = stop_nodes.get(i);
                        if (node.getClassName().equals("android.widget.Button")) {
                            if(node.isEnabled()){
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                }
                            }
                        }
                        node.recycle();
                    }
                }

                List<AccessibilityNodeInfo> ok_nodes = event.getSource().findAccessibilityNodeInfosByText(getString(R.string.force_stop_OK));
                if (ok_nodes!=null && !ok_nodes.isEmpty()) {
                    AccessibilityNodeInfo node;
                    for(int i=0; i<ok_nodes.size(); i++){
                        node = ok_nodes.get(i);
                        if (node.getClassName().equals("android.widget.Button")) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            }
                        }
                        node.recycle();
                    }
                }
//                AccessibilityNodeInfo.(GLOBAL_ACTION_BACK);
            }
        }
    }

    /**
     * 卸载
     * @param event
     */
    private void processUninstallApplication(AccessibilityEvent event) {

        if (event.getSource() != null) {
            if (event.getPackageName().equals("com.android.packageinstaller")) {
                List<AccessibilityNodeInfo> ok_nodes = event.getSource().findAccessibilityNodeInfosByText("确定");
                if (ok_nodes!=null && !ok_nodes.isEmpty()) {
                    AccessibilityNodeInfo node;
                    for(int i=0; i<ok_nodes.size(); i++){
                        node = ok_nodes.get(i);
                        if (node.getClassName().equals("android.widget.Button") && node.isEnabled()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            }
                        }
                    }

                }
            }
        }
    }

    private void simulationClick(AccessibilityEvent event, String text){
        List<AccessibilityNodeInfo> nodeInfoList = event.getSource().findAccessibilityNodeInfosByText(text);
        for (AccessibilityNodeInfo node : nodeInfoList) {
            if (node.isClickable() && node.isEnabled()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }
    }

}
