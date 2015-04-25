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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.listeners.Listeners.CommListener;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.Response;
import com.umeng.comm.core.nets.responses.FeedItemResponse;
import com.umeng.comm.core.nets.responses.LoginResponse;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.fragments.FeedDetailFragment;
import com.umeng.comm.ui.widgets.CommentEditText;
import com.umeng.comm.ui.widgets.CommentEditText.EditTextBackEventListener;

/**
 * 某条Feed的详情页面,会根据feed id每次都会从服务器获取最新数据,暂时没有使用数据库缓存.
 * 
 * @author mrsimple
 */
public class FeedDetailActivity extends BaseFragmentActivity implements OnClickListener {

    /**
     * 评论布局
     */
    private View mCommentLayout;
    /**
     * 评论ditText
     */
    private CommentEditText mCommentEditText;
    /**
     * 进度条
     */
    ProgressBar mProgressBar;

    /**
     * 目标feed的id
     */
    private String mFeedId = "54acd4a10bbbaf1293c6000e";
    /**
     * Feed详情Fragment
     */
    FeedDetailFragment mFeedFrgm;
    /**
     * 刷新按钮
     */
    private ImageButton mRefreshButton;
    /**
     * 布局监听器,监听布局高度，用以计算评论时布局应该滚动的高度
     */
    private OnGlobalLayoutListener mGlobalLayoutListener;

    /**
     * 软键盘弹出，需要在OnGlobalLayoutListener中监听键盘弹出，避免键盘弹出后不断的移动消息流listview
     */
    private boolean isShowKeyboard = false;
    /**
     * ScrollView
     */
    private ScrollView mScrollView;
    /**
     * 
     */
    private View mRootView;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(ResFinder.getLayout("umeng_comm_feed_detail"));
        
        CommonUtils.injectComponentImpl(getApplicationContext());// 重新注入登录组件的实现，避免的推送启动时无自定义的登录组件实现
        initFeedId(getIntent());
        initViews();
    }

    /**
     * 
     */
    protected final void onNewIntent(Intent paramIntent) {
        super.onNewIntent(paramIntent);
        initFeedId(paramIntent);
        fetchFeedInfo();
    }

    private void initFeedId(Intent intent) {
        Bundle extraBundle = intent.getExtras();
        if (extraBundle != null && extraBundle.containsKey(Constants.FEED_ID)) {
            mFeedId = extraBundle.getString(Constants.FEED_ID);
        }
    }

    /**
     * 初始化view</br>
     */
    private void initViews() {
        initTitleLayout();
        initCommentLayout();

        mProgressBar = (ProgressBar) findViewById(ResFinder.getId("umeng_comm_feed_loading"));
        mScrollView = (ScrollView) findViewById(ResFinder.getId("umeng_comm_scroll_view"));
        mRootView = findViewById(ResFinder.getId("umeng_comm_feed_detail_root"));

        findViewById(ResFinder.getId("umeng_comm_feed_container")).setOnTouchListener(
                new OnTouchListener() {

                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (mCommentLayout != null) {
                            mCommentLayout.setVisibility(View.GONE);
                            hideInputMethod(mCommentEditText);
                            return true;
                        }
                        return false;
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchFeedInfo();
    }

    /**
     * 根据feedid从server加载该条feed的信息</br>
     */
    private void fetchFeedInfo() {

        // 检查是否登录
        CommonUtils.checkLoginAndFireCallback(this, new SimpleFetchListener<LoginResponse>() {

            @Override
            public void onComplete(LoginResponse response) {
                if (response.errCode != Constants.NO_ERROR) {
                    ToastMsg.showShortMsgByResName(FeedDetailActivity.this,
                            "umeng_comm_login_failed");
                    return;
                }
                // 获取feed的信息
                mSdkImpl.fetchFeedWithId(mFeedId,
                        new FetchListener<FeedItemResponse>() {

                            @Override
                            public void onStart() {
                                mProgressBar.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onComplete(FeedItemResponse response) {

                                mProgressBar.setVisibility(View.GONE);
                                // 根据response进行Toast
                                if (handlerResponse(response)) {
                                    return;
                                }

                                if (isValidFeedItem(response.result)) {

                                    if (mFeedFrgm == null) {
                                        // 初始化fragment
                                        initFragment(response.result);
                                        mRefreshButton.setVisibility(View.GONE);
                                    } else {
                                        mFeedFrgm.updateFeed(response.result);
                                    }

                                }
                            }
                        });
            }
        });
    }

    /**
     * 检测该条feed是否有效。由于在Response中构造了一个默认的Feed，此时需要验证其有效性。</br>
     * 
     * @param feedItem
     * @return
     */
    private boolean isValidFeedItem(FeedItem feedItem) {
        return feedItem != null
                && !TextUtils.isEmpty(feedItem.text);
    }

    /**
     * 初始化feed detail fragment</br>
     * 
     * @param feedItem
     */
    private void initFragment(FeedItem feedItem) {
        mFeedFrgm = FeedDetailFragment.newFeedDetailFragment(feedItem);
        addFragment(ResFinder.getId("umeng_comm_feed_container"), mFeedFrgm);
        mFeedFrgm.setCommentBtnClickListener(new OnItemCommentClickListener() {

            @Override
            public void onItemClick(String text) {
                showCommentLayout();
                mCommentEditText.setHint(text);
                mCommentEditText.setHintTextColor(ResFinder.getColor(
                        "umeng_comm_linked_text"));
            }
        });
    }

    /**
     * 初始化评论view跟点击事假</br>
     */
    private void initCommentLayout() {

        // 评论视图
        mCommentLayout = findViewById(ResFinder
                .getId("umeng_comm_detail_commnet_edit_layout"));

        mCommentEditText = (CommentEditText)
                findViewById(ResFinder.getId("umeng_comm_comment_edittext"));
        mCommentEditText.setEditTextBackListener(new EditTextBackEventListener() {

            @Override
            public void onClickBack() {
                mCommentLayout.setVisibility(View.INVISIBLE);
            }
        });

        // 发布评论
        findViewById(ResFinder.getId("umeng_comm_comment_send_button")).setOnClickListener(this);
    }

    private void postComment() {
        if (isInvalidComment()) {
            String text = mCommentEditText.getText().toString().trim();
            mFeedFrgm.postComment(text, mPostCommentListener);
        } else {
            ToastMsg.showShortMsgByResName(FeedDetailActivity.this,
                    "umeng_comm_content_invalid");
        }
    }

    /**
     * 隐藏输入法软键盘</br>
     */
    private void hideInPutMethod() {
        super.hideInputMethod(mCommentEditText);
        // 移除OnGlobalLayoutListener回调
        mRootView.getViewTreeObserver().removeGlobalOnLayoutListener(
                mGlobalLayoutListener);
    }

    /**
     * 发送评论的后的回调函数。如果发送成功将清空编辑的内容、隐藏该输入框的view。</br>
     */
    CommListener mPostCommentListener = new CommListener() {

        @Override
        public void onStart() {
            hideInPutMethod();
            mCommentLayout.setVisibility(View.GONE);
        }

        @Override
        public void onComplete(Response resp) {
            if (resp.errCode == Constants.NO_ERROR) {
                mCommentLayout.setVisibility(View.GONE);
                mCommentEditText.setText("");
            }
        }
    };

    /*
     * 显示输入评论内容的布局
     */
    private void showCommentLayout() {

        mCommentLayout.post(new Runnable() {

            @Override
            public void run() {
                mCommentLayout.setVisibility(View.VISIBLE);
                if (mCommentEditText.requestFocus()) {
                    isShowKeyboard = true;
                    listenKeyboard();
                    showInputMethod(mCommentEditText);
                }
            }
        });

    }

    /**
     * 检测是否已经显示了键盘，如果已经显示则计算滚动的距离
     */
    private void listenKeyboard() {

        if (mGlobalLayoutListener == null) {
            mGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {

                @Override
                public void onGlobalLayout() {

                    // final int screenHeight =
                    // mRootView.getRootView().getHeight();
                    // int keyboardHeight = screenHeight -
                    // mRootView.getHeight();
                    // if (keyboardHeight > screenHeight / 3 && isShowKeyboard)
                    // {
                    // isShowKeyboard = false;
                    // final int[] layoutSize = new int[2];
                    // View view = mCommentLayout;
                    // view.getLocationOnScreen(layoutSize);
                    // // 滚动距离：点击评论某项的view的顶部坐标+ view height - editText的顶部坐标
                    // int editTextPosY = layoutSize[1];
                    // view = mFeedFrgm.getClickView();
                    // int commentItemPosY = 0;
                    // if (view != null) {
                    // view.getLocationOnScreen(layoutSize);
                    // commentItemPosY = layoutSize[1] + view.getHeight();
                    // }
                    // int scrollY = commentItemPosY - editTextPosY;
                    // if (scrollY > 0) {
                    // mScrollView.scrollBy(0, scrollY);
                    // }
                    // }

                    // 滚动到评论项的位置
                    scrollToCommentItem();
                    // 没次监听完,删除监听器,避免内存泄露
                    mRootView.getViewTreeObserver().removeGlobalOnLayoutListener(
                            mGlobalLayoutListener);
                }
            };
        }
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);
    }

    private void scrollToCommentItem() {
        int scrollY = calculateScrollY();
        if (scrollY > 0) {
            mScrollView.scrollBy(0, scrollY);
        }
    }

    /**
     * 用户评论时,需要将内容视图向上滚动,这里就是计算滚动的高度.
     * 
     * @return
     */
    private int calculateScrollY() {
        final int screenHeight = mRootView.getRootView().getHeight();
        // 计算得到键盘高度
        int keyboardHeight = screenHeight - mRootView.getHeight();
        // 键盘高度大于屏幕高度的1/3则滚动内容视图
        if (keyboardHeight > screenHeight / 3 && isShowKeyboard) {
            isShowKeyboard = false;
            final int[] layoutSize = new int[2];
            View view = mCommentLayout;
            view.getLocationOnScreen(layoutSize);
            // 滚动距离：点击评论某项的view的顶部坐标+ view height - editText的顶部坐标
            int editTextPosY = layoutSize[1];
            view = mFeedFrgm.getClickView();
            int commentItemPosY = 0;
            if (view != null) {
                view.getLocationOnScreen(layoutSize);
                commentItemPosY = layoutSize[1] + view.getHeight();
            }
            return commentItemPosY - editTextPosY;

        }

        return -1;
    }

    /**
     * 检查评论是否有效。目前仅仅判空</br>
     * 
     * @return
     */
    private boolean isInvalidComment() {
        // 检查评论的内容是否合法
        String mCommentText = mCommentEditText.getText().toString();
        if (TextUtils.isEmpty(mCommentText)) {

            ToastMsg.showShortMsg(this, ResFinder.getString("umeng_comm_content_invalid"));
            return false;
        }
        return true;
    }

    /**
     * 
     */
    private void initTitleLayout() {

        TextView titleTextView = (TextView) findViewById(ResFinder.getId(
                "umeng_comm_title_tv"));
        titleTextView.setText(ResFinder.getString("umeng_comm_feed_detail"));

        // back btn
        findViewById(ResFinder.getId("umeng_comm_title_back_btn")).setOnClickListener(this);
        // 刷新按钮
        mRefreshButton = (ImageButton) findViewById(ResFinder.getId(
                "umeng_comm_title_setting_btn"));
        mRefreshButton.setOnClickListener(this);
        setRefreshButtonDrawable();
    }

    private void setRefreshButtonDrawable() {
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[] {
                android.R.attr.state_enabled
        }, ResFinder.getDrawable("umeng_comm_refresh"));
        stateListDrawable.addState(new int[] {
                android.R.attr.state_pressed
        }, ResFinder.getDrawable("umeng_comm_refresh_pressed"));
        mRefreshButton.getLayoutParams().width = 40;// 重新设置刷新按钮的宽度
        mRefreshButton.setBackgroundDrawable(stateListDrawable);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == ResFinder.getId("umeng_comm_title_back_btn")) {
            this.finish();
        } else if (v == mRefreshButton) {
            fetchFeedInfo();
        } else if (v.getId() == ResFinder.getId("umeng_comm_comment_send_button")) { // 发表评论
            postComment();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // 删除监听器,避免内存泄露
        mRootView.getViewTreeObserver().removeGlobalOnLayoutListener(
                mGlobalLayoutListener);
    }

    public interface OnItemCommentClickListener {
        public void onItemClick(String text);
    }
}
