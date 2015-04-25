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

package com.umeng.comm.ui.activities;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;

import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.db.AbsDBHelper;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.fragments.AllFeedsFragment;

/**
 * 社区主界面, 页面包含消息流主页面、话题选择页面、消息发布页面, 默认为消息流主界面.
 * 注意：此Activity的名字不能修改，数据层需要回调此Activity
 * 
 * @author mrsimple
 */
public class FeedsActivity extends BaseFragmentActivity implements OnClickListener {
    /**
     * 主Feed显示的fragment页面。
     */
//    BaseFeedsFragment mFeedsFragment = new MainFeedsFragment();
    /**
     * 话题页面的Fragment
     */
    Fragment mTopicFragment = null;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(ResFinder.getLayout("umeng_comm_feeds_activity"));

        // 设置fragment的container id
        int container = ResFinder.getId("umeng_comm_main_container");
        initFragment(container);
        addLoginPlatforms();
    }
    
    /**
     * 
     * 该代码仅仅在“一建生成apk”情况下被调用</br>
     */
    private void addLoginPlatforms(){
        boolean isFromGenerateApk = getApplication().getClass().getSuperclass().equals(Application.class);
        if ( isFromGenerateApk ) {
            try {
                Method method = getApplication().getClass().getMethod("addLoginPlatforms", Activity.class);
                method.invoke(null, this);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == ResFinder.getId("umeng_comm_back_btn")) {// 点击返回按钮的情况
            this.finish();
        } 
//        else if (v.getId() == ResFinder.getId("umeng_comm_topic_select_btn")) {// 点击话题按钮的情况
//            showTopicFragment();
//        } else if (v.getId() == ResFinder.getId("umeng_comm_new_post_btn")) {// 点击写新鲜事按钮
//            showPostFeedPage();
//        } else if (v.getId() == ResFinder.getId("umeng_comm_post_back_btn")
//                || v.getId() == ResFinder.getId("umeng_comm_post_ok_btn")) {// 在写新鲜事页面点击返回或者发送后，回到主feed页面
//            showFragment(mFeedsFragment);
//        }
    }

//    /**
//     * 显示话题页面的Fragment。如果未登录，则需要先登录再进入话题页面。</br>
//     */
//    private void showTopicFragment() {
//        if (mTopicFragment == null) {
//            mTopicFragment = new TopicFragment();
//        }
//
//        CommonUtils.checkLoginAndFireCallback(FeedsActivity.this,
//                new SimpleFetchListener<LoginResponse>() {
//
//                    @Override
//                    public void onComplete(LoginResponse response) {
//                        if (response.errCode == Constants.NO_ERROR) {
//                            showFragment(mTopicFragment);
//                        } else {
//                            ToastMsg.showShortMsgByResName(FeedsActivity.this,
//                                    "umeng_comm_login_failed");
//                            return;
//                        }
//                    }
//                });
//    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
////        menu.add(Menu.NONE, Menu.FIRST + 1, 1, ResFinder.getString("umeng_comm_setting"));
////        super.onCreateOptionsMenu(menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == Menu.FIRST + 1) {
//            gotoSettingActivity();
//        }
//        return super.onOptionsItemSelected(item);
//    }

//    /**
//     * 根据登录状态做相应的操作。已登录则直接执行，否则登录成功后再执行回调</br>
//     */
//    private void showPostFeedPage() {
//        CommonUtils.checkLoginAndFireCallback(FeedsActivity.this, new SimpleFetchListener<LoginResponse>() {
//
//            @Override
//            public void onComplete(LoginResponse response) {
//                if (response.errCode == Constants.NO_ERROR) {
//                    gotoPostFeedActivity();
//                } else {
//                    ToastMsg.showShortMsgByResName(FeedsActivity.this,
//                            "umeng_comm_login_failed");
//                }
//            }
//        });
//    }

    /**
     * 跳转到设置页面</br>
     */
//    private void gotoSettingActivity() {
//        CommonUtils.checkLoginAndFireCallback(FeedsActivity.this, new SimpleFetchListener<LoginResponse>() {
//
//            @Override
//            public void onComplete(LoginResponse response) {
//                if (response.errCode == Constants.NO_ERROR) {
//                    Intent settingIntent = new Intent(FeedsActivity.this, SettingActivity.class);
//                    startActivity(settingIntent);
//                } else {
//                    ToastMsg.showShortMsgByResName(FeedsActivity.this,
//                            "umeng_comm_login_failed");
//                }
//            }
//        });
//
//    }
//
//    /**
//     * 跳转至发送新鲜事页面</br>
//     */
//    private void gotoPostFeedActivity() {
//        Intent postIntent = new Intent(FeedsActivity.this, PostFeedActivity.class);
//        startActivity(postIntent);
//    }
//
//    /**
//     * 显示主Feed页面的Fragment
//     */
//    public void showMainFeedFragment() {
//        showFragment(mFeedsFragment);
//    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 不是在feed页面按返回键则返回到feed页面
            if (!(mCurrentFragment instanceof AllFeedsFragment)) {
                showFragment(mFeedsFragment);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 绑定push service</br>
     */
    private void bindPushService() {
//        Pushable push = PushSDKManager.getInstance().getCurrentSDK();
//
//        if (mSdkImpl.getConfig().isPushEnable(this)) {
//            // 启动推送服务器
//            push.enable(this);
//            // 如果已登录,直接绑定推送
//            if (CommonUtils.isLogin(this)) {
//                push.setUserAlias(CommonUtils.getLoginUser(this));
//            }
//        } else {
//            push.disable();
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        dealLogoutLoginc();
        bindPushService();
    }
    
    /**
     * 
     * 处理退出登录后，回到FeedsActivity时的逻辑</br>
     */
    private void dealLogoutLoginc(){
        Bundle bundle = getIntent().getExtras();
        if ( bundle != null ) {
            boolean fromLogout = bundle.getBoolean(Constants.FROM_COMMUNITY_LOGOUT);
            if ( fromLogout ) {
                mFeedsFragment.cleanAdapterData();
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        // 关闭数据库连接
        AbsDBHelper.closeDBCollection();
        if ( mFeedsFragment != null ) {
            mFeedsFragment.hideCommentLayoutAndInputMethod();
        }
        super.onDestroy();
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

}
