/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Umeng, Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.umeng.comm.ui.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.login.LoginListener;
import com.umeng.comm.core.push.NullPushImpl;
import com.umeng.comm.core.push.Pushable;
import com.umeng.comm.core.sdkmanager.LoginSDKManager;
import com.umeng.comm.core.sdkmanager.PushSDKManager;
import com.umeng.comm.core.strategy.logout.InnerLogoutStrategy;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.DeviceUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.dialogs.ConfirmDialog;

/**
 * 
 */
public class SettingFragment extends FontFragment implements OnClickListener {

    // 由于开发者可能直接使用Fragment，在退出登录的时候，我们需要回到该Activity
    private String mContainerClass = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int layout = ResFinder.getLayout("umeng_comm_setting");
        int userSettingResId = ResFinder.getId("umeng_comm_user_setting");
        int settingResId = ResFinder.getId("umeng_comm_msg_setting");
        int logoutResId = ResFinder.getId("umeng_comm_logout");
        View rootView = inflater.inflate(layout, null);
        //
        OnClickListener clickListener = (OnClickListener) getActivity();
        rootView.findViewById(userSettingResId).setOnClickListener(clickListener);
        rootView.findViewById(settingResId).setOnClickListener(clickListener);
        // 登出
        rootView.findViewById(logoutResId).setOnClickListener(this);
        checkConfigPush(rootView);
        return rootView;
    }

    public void setContainerClass(String clz) {
        mContainerClass = clz;
    }

    /**
     * 检查是否配置push。如果未配置则不显示push开关</br>
     */
    private void checkConfigPush(View rootView) {
        Pushable pushImpl = PushSDKManager.getInstance().getCurrentSDK();
        // 判断Push是否配置,或者为NullPushImpl实现
        if (pushImpl == null || pushImpl instanceof NullPushImpl) {
            // 没有配置推送，推送设置按钮不可见
            rootView.findViewById(ResFinder.getId("umeng_comm_msg_setting")).setVisibility(
                    View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int logoutResId = ResFinder.getId("umeng_comm_logout");
        if (v.getId() == logoutResId) {
            String title = ResFinder.getString("umeng_comm_setting_logout");
            ConfirmDialog.showDialog(getActivity(),
                    title + "?",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            logout();
                        }
                    });

        }
    }

    /**
     * 注销登录。清除保存的用户信息，并调用开发者的注销逻辑</br>
     */
    private void logout() {
        if (CommonUtils.isActivityAlive(getActivity())
                && !DeviceUtils.isNetworkAvailable(getActivity())) {
            ToastMsg.showShortMsg(getActivity(), ResFinder.getString("umeng_comm_not_network"));
            return;
        }
        // 退出登录的情况
        LoginSDKManager.getInstance().getCurrentSDK()
                .logout(getActivity(), new LoginListener() {

                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onComplete(int stCode, CommUser userInfo) {

                        Log.d(getTag(), "### 社区登出 , stCode = " + stCode);
                        if (mContainerClass == null) {
                            Log.e(getTag(), " container class is null...");
                            return;
                        }
                        if (stCode != 200) {
                            ToastMsg.showShortMsgByResName(getActivity(),
                                    "umeng_comm_logout_failed");
                            return;
                        }
                        // 清空SDK内部保存的用户信息
                        CommonUtils.logout(getActivity());
                        // 置空用户信息
                        CommConfig.getConfig().loginedUser = new CommUser();
                        // 关闭推送
                        Pushable pushable = PushSDKManager.getInstance().getCurrentSDK();
                        pushable.disable();
                        Class<?> clz;
                        try {
                            clz = Class.forName(mContainerClass);
                            // Intent intent = new Intent(getActivity(), clz);
                            // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            // // 启动该Activity来自社区的退出
                            // intent.putExtra(Constants.FROM_COMMUNITY_LOGOUT,
                            // true);
                            // getActivity().startActivity(intent);
                            // getActivity().finish();
                            InnerLogoutStrategy strategy = CommConfig.getConfig()
                                    .getInnerLogoutStrategy();
                            if (strategy != null) {
                                strategy.afterLogout(getActivity(), clz);
                            }
                            // finish activity 也作为策略的一部分
//                            getActivity().finish();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}
