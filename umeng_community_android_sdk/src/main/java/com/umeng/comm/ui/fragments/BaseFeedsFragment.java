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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.Comment;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.beans.ForwardFeedItem;
import com.umeng.comm.core.beans.Like;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.db.AbsDBHelper;
import com.umeng.comm.core.db.DbHelper;
import com.umeng.comm.core.db.DbHelperFactory;
import com.umeng.comm.core.db.cmd.DeleteCommand;
import com.umeng.comm.core.db.cmd.InsertCommand;
import com.umeng.comm.core.db.cmd.QueryCommand;
import com.umeng.comm.core.db.cmd.concrete.DbCommandFactory;
import com.umeng.comm.core.listeners.Listeners.CommListener;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.Response;
import com.umeng.comm.core.nets.responses.FeedsResponse;
import com.umeng.comm.core.nets.responses.LoginResponse;
import com.umeng.comm.core.nets.responses.SimpleResponse;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.DeviceUtils;
import com.umeng.comm.core.utils.Log;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.SharePrefUtils;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.activities.FindActivity;
import com.umeng.comm.ui.adapters.FeedAdapter;
import com.umeng.comm.ui.adapters.FeedAdapter.InitListener;
import com.umeng.comm.ui.adapters.viewparser.FeedItemViewParser.FeedItemViewHolder;
import com.umeng.comm.ui.broadcastreceiver.NotifyBroadcastReceiver;
import com.umeng.comm.ui.broadcastreceiver.NotifyBroadcastReceiver.NotifyListener;
import com.umeng.comm.ui.broadcastreceiver.UserInfoNotifyListener;
import com.umeng.comm.ui.dialogs.ConfirmDialog;
import com.umeng.comm.ui.utils.Filter;
import com.umeng.comm.ui.utils.ViewFinder;
import com.umeng.comm.ui.widgets.CommentEditText;
import com.umeng.comm.ui.widgets.CommentEditText.EditTextBackEventListener;
import com.umeng.comm.ui.widgets.RefreshLayout.OnLoadListener;
import com.umeng.comm.ui.widgets.RefreshLvLayout;
import com.umeng.comm.ui.widgets.WrapperListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 这是消息流主页面,继承自FeedDetailFragment(
 * 某条feed的详情页)，包含当前最新的消息列表.从该页面可以跳转到话题搜索页面、消息发布页面，可以浏览消息流中的图片、评论某项消息、进入某个好友的主页等.
 * 该页面是整个社区的主页.
 * 
 * @author mrsimple
 */
public abstract class BaseFeedsFragment extends FeedDetailFragment {

    /**
     * 下拉刷新, 上拉加载的布局, 包裹了Feeds ListView
     */
    protected RefreshLvLayout mRefreshLayout;
    /**
     * feeds ListView
     */
    protected ListView mFeedsListView;

    /**
     * 消息流适配器
     */
    protected FeedAdapter mFeedLvAdapter;

    /**
     * 评论编辑框
     */
    public CommentEditText mCommentEditText;
    /**
     * 评论编辑布局
     */
    protected View mCommentLayout;

    /**
     * title的文本TextView
     */
    protected TextView mTitleTextView;

    /**
     * ListView的footers
     */
    protected List<View> mFooterViews = new ArrayList<View>();

    /**
     * 软键盘弹出，需要在OnGlobalLayoutListener中监听键盘弹出，避免键盘弹出后不断的移动消息流listview
     */
    private boolean isShowKeyboard = false;

    /**
     * 过滤掉某些关键字的filter
     */
    protected Filter<FeedItem> mFeedFilter;

    /**
     * 布局改变时的回调。主要用于监测输入法是否已经打开，并做相关的逻辑处理（评论中某项的具体滚动距离）
     */
    private OnGlobalLayoutListener mOnGlobalLayoutListener;
    /**
     * feed 中like距离top and bottom 的padding
     */
    private int mLikeItemPadding = 0;

    /**
     * 
     */
    protected CommUser mUser = CommConfig.getConfig().loginedUser;

    /**
     * 用户信息修改的广播接收器
     */
    NotifyBroadcastReceiver mUserBroadcastReceiver;
    /**
     * feed发布成功的广播接收器
     */
    NotifyBroadcastReceiver mPostBroadcastReceiver;

    /**
     * 取消关注的广播接收器
     */
    protected NotifyBroadcastReceiver mBroadcastReceiver;
    /**
     * 是否需要清空数据库缓存的标志位
     */
    protected AtomicBoolean isNeedRemoveOldFeeds = new AtomicBoolean(true);

    protected String mNextPageUrl = "";

    private boolean hasRefresh = false;

    private int totalTime = 0;
    private boolean isFinish = false;
    private InputMethodManager mInputMan;
    private int mSlop;

    List<String> mTabTitls = new ArrayList<String>();
    /**
     * 发表feed的button
     */
    protected ImageView mPostBtn;
    
    /**
     * 签到的button
     */
    protected ImageView mLocationBtn;

    /**
     * 该Handler主要处理软键盘的弹出跟隐藏
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            View view = (View) msg.obj;
            // 显示软键盘
            if (msg.what == Constants.INPUT_METHOD_SHOW) {
                boolean result = mInputMan.showSoftInput(view, 0);
                if (!result && totalTime < Constants.LIMIT_TIME) {
                    totalTime += Constants.IDLE;
                    Message message = Message.obtain(msg);
                    mHandler.sendMessageDelayed(message, Constants.IDLE);
                } else if (!isFinish) {
                    totalTime = 0;
                    result = view.requestFocus();
                    isFinish = true;
                }
            } else if (msg.what == Constants.INPUT_METHOD_DISAPPEAR) {
                // 隐藏软键盘
                mInputMan.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mRootView = inflater.inflate(ResFinder.getLayout("umeng_comm_feeds_frgm_layout"),
                container, false);
        mInputMan = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);

        // 初始化视图
        initViews();
        // 初始化点击ActionButton（like、转发、评论）、图片浏览Dialog View
        initWidgets();
        // 初始化Feed Adapter
        initAdapter();
        // 注册广播接收器
        registerBroadcast();
        // 从cache中获取数据
        loadFeedsFromDB();
        // 请求中的状态
        mRefreshLayout.setRefreshing(true);
        // 从server端获取数据
        fetchFeeds();
        mSlop = ViewConfiguration.get(container.getContext()).getScaledTouchSlop();
        return mRootView;
    }

    /**
     * 初始化feed流 页面显示相关View
     */
    protected void initViews() {
        mViewFinder = new ViewFinder(mRootView);

        // 初始化刷新相关View跟事件
        initRefreshView();
        // 初始化Title 布局View
        // initTitleView();
        // 初始化评论编辑框跟事件
        initCommentView();
        mPostBtn = mViewFinder.findViewById(ResFinder.getId("umeng_comm_new_post_btn"));
        mLocationBtn = mViewFinder.findViewById(ResFinder.getId("umeng_comm_new_location_btn"));
    }

    /**
     * 初始化下拉刷新试图, listview
     */
    private void initRefreshView() {
        // 下拉刷新, 上拉加载的布局
        mRefreshLayout = mViewFinder.findViewById(ResFinder.getId("umeng_comm_swipe_layout"));

        // 下拉刷新时执行的回调
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                // 加载最新的feed
                fetchFeeds();
            }
        });

        // 上拉加载更多
        mRefreshLayout.setOnLoadListener(new OnLoadListener() {

            @Override
            public void onLoad() {
                loadMoreFeed();
            }
        });

        // 滚动监听器, 滚动停止时才加载图片
        mRefreshLayout.addOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                    mImageLoader.resume();
                } else {
                    mImageLoader.pause();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                    int totalItemCount) {

            }
        });

        int feedListViewResId = ResFinder.getId("umeng_comm_feed_listview");
        // feed列表 listview
        mFeedsListView = mRefreshLayout.findRefreshViewById(feedListViewResId);
        // 添加footer
        mRefreshLayout.setDefaultFooterView();
        // 关闭动画缓存
        mFeedsListView.setAnimationCacheEnabled(false);
        // 开启smooth scrool bar
        mFeedsListView.setSmoothScrollbarEnabled(true);
        // 在评论EditText显示的时候，touch到Listview时得情况，将评论的Layout设置为不可见并隐藏输入法
        mFeedsListView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mCommentLayout.isShown()) {
                    hideCommentLayoutAndInputMethod();
                    return true;
                }
                return false;
            }
        });

        mFeedsListView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                checkWhetherExecuteAnimation(event);
                if (mCommentLayout.isShown()) {
                    hideCommentLayoutAndInputMethod();
                    return true;
                }
                return false;
            }
        });
    }

    private int mLastScrollY = 0;// 上次滑动时Y的起始坐标
    private static final int STATUS_NORMAL = 0x01;// 正常状态。无意义
    private static final int STATUS_SHOW = 0x02;// 显示状态
    private static final int STATUS_DISMISS = 0x03;// 隐藏状态
    private transient int currentStatus = STATUS_NORMAL; // 当前Float Button的状态
    private transient boolean isExecutingAnim = false; // 是否正在执行动画

    /**
     * 检查是否为Float button执行动画</br>
     * 
     * @param event
     */
    private void checkWhetherExecuteAnimation(MotionEvent event) {
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastScrollY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaY = mLastScrollY - y;
                mLastScrollY = y;
                if (Math.abs(deltaY) < mSlop) {
                    return;
                }
                if (deltaY > 0) {
                    executeAnimation(false);
                } else {
                    executeAnimation(true);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 为Float button执行动画</br>
     * 
     * @param show 显示 or 影藏
     */
    private void executeAnimation(final boolean show) {
        if (isExecutingAnim || (show && currentStatus == STATUS_SHOW)
                || (!show && currentStatus == STATUS_DISMISS)) {
            return;
        }
        isExecutingAnim = true;
        int moveDis = ((FrameLayout.LayoutParams) (mPostBtn.getLayoutParams())).bottomMargin
                + mPostBtn.getHeight();
        Animation animation = null;
        if (show) {
            animation = new TranslateAnimation(0, 0, moveDis, 0);
        } else {
            animation = new TranslateAnimation(0, 0, 0, moveDis);
        }
        animation.setDuration(300);
        animation.setFillAfter(true);
        animation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isExecutingAnim = false;
                if (show) {
                    currentStatus = STATUS_SHOW;
                } else {
                    currentStatus = STATUS_DISMISS;
                }
                // 对于3.0以下系统，原来的地方仍有点击事件。由于我们的需要是处理可见性，因此此处不在对Float
                // Button做layout处理。
                mPostBtn.setClickable(show);
            }
        });
        mPostBtn.startAnimation(animation);
    }

    /**
     * 隐藏评论的布局跟软键盘</br>
     */
    public void hideCommentLayoutAndInputMethod() {
        resetCommentLayout();
        hideInputMethod();
        showPostButtonWithAnim();
    }

    /**
     * 显示输入法</br>
     */
    private void showInputMethod() {
        sendInputMethodMessage(Constants.INPUT_METHOD_SHOW, mCommentEditText);
    }

    /**
     * 关闭输入法</br>
     */
    private void hideInputMethod() {
        if (CommonUtils.isActivityAlive(getActivity())) {
            sendInputMethodMessage(Constants.INPUT_METHOD_DISAPPEAR, mCommentEditText);
            mRootView.getViewTreeObserver().removeGlobalOnLayoutListener(
                    mOnGlobalLayoutListener);
        }
    }

    /**
     * 
     */
    protected void showPostButtonWithAnim() {
    }

    /**
     * 
     */
    private void hidePostButtonWithAnim() {
        AlphaAnimation animation = new AlphaAnimation(1.0f, 0f);
        animation.setDuration(500);

        mPostBtn.setVisibility(View.GONE);
        mPostBtn.startAnimation(animation);
    }

    @Override
    protected void initButtonsClickListener(FeedItemViewHolder viewHolder, final FeedItem feedItem,
            int position) {
        super.initButtonsClickListener(viewHolder, feedItem, position);

        viewHolder.mDialogButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mCurFeedItem = feedItem;
                showSelectDialog();
            }
        });
    }

    @Override
    protected FeedItem findFeedWithId(String feedId) {

        if (mCurFeedItem.id.equals(feedId)) {
            return mCurFeedItem;
        }

        List<FeedItem> allFeedItems = mFeedLvAdapter.getDataSource();
        for (FeedItem feedItem : allFeedItems) {
            if (feedId.equals(feedItem.id)) {
                return feedItem;
            }
        }

        return new FeedItem();
    }

    private String[] prepareMenus() {
        String[] menus = null;
        if (isMyFeed(mCurFeedItem)) {
            menus = new String[] {
                    ResFinder.getString("umeng_comm_delete_feed_tips")
            };
        } else {
            menus = new String[] {
                    ResFinder.getString("umeng_comm_report_feed_tips")
            };
        }

        return menus;
    }

    private void showSelectDialog() {
        AlertDialog.Builder dBuilder = new AlertDialog.Builder(getActivity());
        final String[] menus = prepareMenus();
        dBuilder.setItems(menus, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, final int which) {
                dialog.dismiss();

                final String msg = menus[which]
                        + ResFinder.getString("umeng_comm_this_feed");
                showOprationDialog(msg, which);

            }
        });
        dBuilder.create().show();
    }

    private void showOprationDialog(String msg, final int which) {
        ConfirmDialog.showDialog(getActivity(), msg,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int invalid) {
                        // 删除feed
                        if (which == 0 && isMyFeed(mCurFeedItem) && isDeletable(mCurFeedItem)) {
                            deleteCurrentFeed();
                        } else if (which != -1) {
                            reportCurrentFeed();
                        }
                    }
                });
    }

    /**
     * 判断该条feed是否可以被删除，如果不可删除则给出Toast提示。 对于正常feed（0）可以删除，取值参考{@link FeedItem}
     * satus字段说明</br>
     * 
     * @param item
     * @return
     */
    private boolean isDeletable(FeedItem item) {
        if (item.status <= FeedItem.STATUS_VOTED) {
            return true;
        }
        ToastMsg.showShortMsgByResName(getActivity(), "umeng_comm_user_name_feed_invalid");
        return false;
    }

    @Override
    protected void deleteInvalidateFeed(FeedItem feedItem) {
        super.deleteInvalidateFeed(feedItem);
        // 将无效的feed从listview中删除
        mFeedLvAdapter.getDataSource().remove(feedItem);
        mFeedLvAdapter.notifyDataSetChanged();
    }

    /**
     * 从server端删除该条feed</br>
     */
    private void deleteCurrentFeed() {
        mSdkImpl.deleteFeed(mCurFeedItem.id,
                new CommListener() {

                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onComplete(Response data) {
                        // 判断用户是否被禁言
                        if (data.errCode == Constants.USER_FORBIDDEN_ERR_CODE) {
                            ToastMsg.showShortMsg(getActivity(),
                                    ResFinder.getString("umeng_comm_user_unusable"));
                            return;
                        }

                        if (data.errCode == 0) {
                            updateAfterDelete(mCurFeedItem);
                            deleteFeedFromDB();
                        }

                        final String toast = data.errCode == 0 ? ResFinder
                                .getString("umeng_comm_delete_success") :
                                ResFinder.getString("umeng_comm_delete_failed");
                        ToastMsg.showShortMsg(getActivity(), toast);
                    }
                });
    }

    protected void updateAfterDelete(FeedItem feedItem) {
        mFeedLvAdapter.getDataSource().remove(feedItem);
        mFeedLvAdapter.notifyDataSetChanged();

        // 发送删除广播
        sendDeleteFeedBrocast(feedItem);
    }

    private void sendDeleteFeedBrocast(FeedItem feedItem) {
        Intent intent = new Intent(NotifyBroadcastReceiver.FEED_DELETED);
        intent.putExtra(Constants.FEED, feedItem);
        getActivity().sendBroadcast(intent);
    }

    /**
     * 从数据库中删除此feed</br>
     */
    private void deleteFeedFromDB() {
        Map<String, String> wheres = new HashMap<String, String>();
        wheres.put(AbsDBHelper.ID, mCurFeedItem.id);
        // 执行删除命令
        DbHelper<FeedItem> helper = DbHelperFactory.getFeedDbHelper(getActivity());
        DeleteCommand<FeedItem> deleteCommand = new DeleteCommand<FeedItem>(
                helper, wheres);
        helper.setOnItemFetchedListener(null);
        deleteCommand.execute();
    }

    private void reportCurrentFeed() {

        SimpleFetchListener<LoginResponse> loginListener = new SimpleFetchListener<LoginResponse>() {
            @Override
            public void onComplete(LoginResponse response) {
                if (response.errCode == Constants.NO_ERROR) {
                    // 举报feed
                    mSdkImpl.spammerFeed(mCurFeedItem.id,
                            new FetchListener<SimpleResponse>() {

                                @Override
                                public void onStart() {
                                }

                                @Override
                                public void onComplete(SimpleResponse response) {
                                    if (response.errCode == Constants.NO_ERROR) {
                                        ToastMsg.showShortMsgByResName(getActivity(),
                                                "umeng_comm_text_spammer_success");
                                    } else if (response.errCode == Constants.SPAMMERED_CODE) {
                                        ToastMsg.showShortMsgByResName(getActivity(),
                                                "umeng_comm_text_spammered");
                                    } else {
                                        ToastMsg.showShortMsgByResName(getActivity(),
                                                "umeng_comm_text_spammer_failed");
                                    }
                                }
                            });
                }
            }
        };
        CommonUtils.checkLoginAndFireCallback(getActivity(), loginListener);

    }

    /**
     * 评论视图
     */
    private void initCommentView() {
        int commentLayoutResId = ResFinder.getId("umeng_comm_commnet_edit_layout");
        // 评论布局
        mCommentLayout = mRootView.findViewById(commentLayoutResId);
        int commentEditTextResId = ResFinder.getId("umeng_comm_comment_edittext");
        mCommentEditText = mViewFinder.findViewById(commentEditTextResId);
        mCommentEditText.setEditTextBackListener(new EditTextBackEventListener() {

            @Override
            public void onClickBack() {
                // mCommentLayout.setVisibility(View.INVISIBLE);
                resetCommentLayout();
                showPostButtonWithAnim();
            }
        });

        int sendButtonId = ResFinder.getId("umeng_comm_comment_send_button");
        // 发布评论
        mRootView.findViewById(sendButtonId).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        final String commentText = mCommentEditText.getText().toString();
                        boolean result = checkCommentData(commentText);
                        if (result) {
                            postComment(commentText, mPostCommentListener);
                        } else {
                            ToastMsg.showShortMsgByResName(getActivity(),
                                    "umeng_comm_content_invalid");
                        }
                    }
                });

    }

    /**
     * 移除存在的feed，获取最新的feed</br>
     * 
     * @param feedItems 最新的feed（待被移除）
     * @return
     */
    private List<FeedItem> getNewFeedItems(List<FeedItem> feedItems) {
        List<FeedItem> notExsitFeeds = removeExsitItems(mFeedLvAdapter.getDataSource(), feedItems);
        // 过滤数据
        return filteFeeds(notExsitFeeds);
    }

    /**
     * 去重并更新adapter的数据,查到前边。</br>
     * 
     * @param feedItems
     */
    protected void addFeedItemsToHeader(List<FeedItem> feedItems) {
        feedItems = removeSpamFeed(feedItems);
        List<FeedItem> olds = mFeedLvAdapter.getDataSource();
        olds.removeAll(feedItems);
        olds.addAll(0, feedItems);
        // 所有feed都按照feed的时间降序排列。【该代码避免用户首次登录时，推荐的feed时间较新，但是管理员的帖子较旧的情况】
        Collections.sort(olds, mComparator);
        mFeedLvAdapter.notifyDataSetChanged();
    }

    /**
     * 去重并更新adapter的数据,追加到后面。</br>
     * 
     * @param feedItems
     */
    protected List<FeedItem> appendFeedItems(List<FeedItem> feedItems) {
        List<FeedItem> newFeeds = getNewFeedItems(feedItems);
        // 添加到listview中
        mFeedLvAdapter.addData(newFeeds);
        return newFeeds;
    }

    /**
     * 发送评论监听器
     */
    private CommListener mPostCommentListener = new CommListener() {

        @Override
        public void onStart() {
            hideCommentLayoutAndInputMethod();
        }

        @Override
        public void onComplete(Response resp) {
            if (resp.errCode == Constants.NO_ERROR) {
                resetCommentLayout();
                // // 刷新feed。此时刷新整个listview的原因是：在评论listview从viewstub inflate后，
                // // 其刷新将不会显示评论列表
                // mCommentListView.setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    protected void savePostedComment(Comment comment, String commId) {
        mCommentEditText.setText("");
        super.savePostedComment(comment, commId);
    }

    /**
     * 从新的删除旧的 [ 这里不要随意更改 ]
     * 
     * @param oldItems 这是原来listview中的数据
     * @param newItems 这是新的要添加的数据
     * @return
     */
    protected <T> List<T> removeExsitItems(List<T> oldItems, List<T> newItems) {
        // 去掉在本地已经存在的feeds
        oldItems.removeAll(newItems);
        return newItems;
    }

    protected void parseNextpageUrl(List<FeedItem> items, boolean fromRefersh) {
        if (items == null || items.size() == 0) {
            return;
        }
        if (fromRefersh && TextUtils.isEmpty(mNextPageUrl) && !hasRefresh) {
            hasRefresh = true;
            mNextPageUrl = items.get(0).nextPageUrl;
        } else if (!fromRefersh) {
            mNextPageUrl = items.get(items.size() - 1).nextPageUrl;
        }
    }

    /**
     * 加载更多数据</br>
     */
    protected void loadMoreFeed() {

        // 没有网络的情况下从数据库加载
        if (!DeviceUtils.isNetworkAvailable(getActivity())) {
            loadFeedsFromDB();
            return;
        }

        if (TextUtils.isEmpty(mNextPageUrl)) {
            ToastMsg.showShortMsgByResName(getActivity(), "umeng_comm_text_load_over");
            mRefreshLayout.setLoading(false);
            return;
        }
        mSdkImpl.fetchNextPageData(mNextPageUrl,
                FeedsResponse.class, new SimpleFetchListener<FeedsResponse>() {

                    @Override
                    public void onComplete(FeedsResponse response) {
                        mRefreshLayout.setLoading(false);
                        Log.d(getTag(), "### 下一页数据 : " + response.result.size());

                        // 根据response进行Toast
                        if (handlerResponse(response)) {
                            return;
                        }
                        parseNextpageUrl(response.result, false);
                        // 去掉重复的feed
                        final List<FeedItem> feedItems = filteFeeds(response.result);
                        if (feedItems != null && feedItems.size() > 0) {
                            removeSpamFeed(feedItems);
                            // 追加数据
                            appendFeedItems(feedItems);
                            saveFeedsToDB(feedItems);
                        }
                    }
                });
    }

    /**
     * 移除>=2的feed</br>
     * 
     * @param items
     * @return
     */
    protected List<FeedItem> removeSpamFeed(List<FeedItem> items) {
        Iterator<FeedItem> iterator = items.iterator();
        while (iterator.hasNext()) {
            FeedItem item = iterator.next();
            if (item.status >= 2) {
                iterator.remove();
            }
        }
        return items;
    }

    /**
     * 从cache中加载数据</br>
     */
    protected void loadFeedsFromDB() {
        // 判断是否还有更多
        if (CommonUtils.isActivityAlive(getActivity())
        /* && !TextUtils.isEmpty(mUser.id) */) {
            executeLoadFeedsCommand();
        } else {
            mRefreshLayout.setLoading(false);
        }
    }

    /**
     * 执行从数据库加载feed命令
     */
    protected void executeLoadFeedsCommand() {
        // 构建命令,查询缓存在数据库中的feed
        QueryCommand<FeedItem> feedQueryCommand = DbCommandFactory
                .createQueryFeedCmd(getActivity(), mUser.id, mFeedLvAdapter.getCount());
        // 没查询到一个数据则追加到ListView中,提升响应速度
        feedQueryCommand.setOnItemFetchedListener(new SimpleFetchListener<FeedItem>() {
            @Override
            public void onComplete(FeedItem response) {
                if (CommonUtils.isActivityAlive(getActivity())) {
                    dealFeedData(response);
                    if (mImageLoader != null) {
                        mImageLoader.resume();
                    }
                }
            }
        });
        // 整个查询完成的执行回调
        feedQueryCommand.setFetchListener(new SimpleFetchListener<List<FeedItem>>() {

            @Override
            public void onComplete(List<FeedItem> response) {
                mRefreshLayout.setLoading(false);
            }
        });
        feedQueryCommand.execute();
    }

    /**
     * 对feed数据进行过滤处理</br>
     * 
     * @param data
     */
    protected void dealFeedData(FeedItem data) {
        List<FeedItem> items = new ArrayList<FeedItem>();
        items.add(data);
        items = getNewFeedItems(items);
        if (items.size() > 0) {
            // 添加数据到Listview中
            addNewFeed(items.get(0));
        }
    }

    /**
     * 添加一个新的feed到listview中. [ 抽离成函数便于在子类中使用过滤器来过滤数据 ]
     * 
     * @param feedItem
     */
    protected void addNewFeed(FeedItem feedItem) {
        final List<FeedItem> lists = mFeedLvAdapter.getDataSource();
        lists.add(feedItem);
        Collections.sort(lists, mComparator);
        mFeedLvAdapter.updateListViewData(lists);
    }

    protected Comparator<FeedItem> mComparator = new Comparator<FeedItem>() {

        @Override
        public int compare(FeedItem lhs, FeedItem rhs) {
            return rhs.publishTime.compareTo(lhs.publishTime);
        }
    };

    /**
     * 初始化适配器
     */
    public void initAdapter() {
        if (mFeedLvAdapter == null) {
            mFeedLvAdapter = new FeedAdapter(getActivity(), new ArrayList<FeedItem>());
        }
        mFeedLvAdapter.setInitItemListener(new InitListener() {

            @Override
            public void initItem(View rootView, final FeedItemViewHolder holder, final int position) {
                // 计算like的padding，用于在评论item滚动时使用
                calculateLikePadding(holder.mLikeView);
                // get FeedItem at position
                final FeedItem feedItem = mFeedLvAdapter.getItem(position);
                // 填充数据
                setFeedItemData(rootView, feedItem, position);
                // 设置一些点击监听器
                initButtonsClickListener(holder, feedItem, position);
            }
        });
        mFeedsListView.setAdapter(mFeedLvAdapter);
    }

    private void calculateLikePadding(View likeView) {
        if (mLikeItemPadding == 0) {
            mLikeItemPadding = likeView.getPaddingBottom() + likeView.getPaddingTop();
        }
    }

    // /**
    // * 隐藏Title 布局
    // */
    // protected void initTitleView() {
    // int titleLayoutResId = ResFinder.getId("topic_action_bar");
    // mTitleLayout = mRootView.findViewById(titleLayoutResId);
    // mTitleLayout.setVisibility(View.GONE);
    //
    // //
    // mProfileBtn =
    // mViewFinder.findViewById(ResFinder.getId("umeng_comm_user_info_btn"));
    // mProfileBtn.setOnClickListener(new OnClickListener() {
    //
    // @Override
    // public void onClick(View v) {
    // // gotoUserInfoActivity(CommConfig.getConfig().loginedUser);
    // gotoFindActivity(CommConfig.getConfig().loginedUser);
    // }
    // });
    //
    // mPostBtn =
    // mViewFinder.findViewById(ResFinder.getId("umeng_comm_new_post_btn"));
    //
    // mTabTitls.add("全部");
    // // mTabTitls.add("朋友圈");
    // mTabTitls.add("推荐");
    //
    // SlidingTextView textView =
    // mViewFinder.findViewById(ResFinder.getId("umeng_comm_title_tv"));
    // // TextView textView =
    // mViewFinder.findViewById(ResFinder.getId("umeng_comm_title_tv"));
    // textView.setSlidingAdapter(new SlidingAdapter() {
    //
    // @Override
    // public String getTitle(int position) {
    // return mTabTitls.get(position);
    // }
    //
    // @Override
    // public int getCount() {
    // return mTabTitls.size();
    // }
    // });
    // textView.setOnIndexChangeListener(new OnIndexChangeListener() {
    //
    // @Override
    // public void onChange(int oldIndex, int index) {
    // Log.d(getTag(), "### old index = " + oldIndex + ", new index " + index);
    // backData(oldIndex);
    // fetchNewFeedsWithIndex(index);
    // }
    // });
    // }

    // /**
    // * @param index
    // */
    // private void backData(int oldIndex) {
    // if (oldIndex == 0) {
    // mMainFeedList = mFeedLvAdapter.getDataSource();
    // } else if (oldIndex == 1) {
    // mMomentsFeedList = mFeedLvAdapter.getDataSource();
    // } else {
    // mRecommendFeedList = mFeedLvAdapter.getDataSource();
    // }
    // }

    // private void fetchNewFeedsWithIndex(int newIndex) {
    // if (newIndex == 0) {
    // mFeedLvAdapter.addData(mMainFeedList);
    // fetchFeeds();
    // }
    // // else if (newIndex == 1) {
    // // mFeedLvAdapter.addData(mMomentsFeedList);
    // // fetchMomentsFeeds();
    // // }
    // else {
    // mFeedLvAdapter.addData(mRecommendFeedList);
    // fetchRecomendedFeeds();
    // }
    // }

    // /**
    // * 获取朋友圈feed数据
    // */
    // private void fetchMomentsFeeds() {
    //
    // }

    /**
     * 跳转到发现Activity</br>
     * 
     * @param user
     */
    public void gotoFindActivity(final CommUser user) {
        CommonUtils.checkLoginAndFireCallback(getActivity(),
                new SimpleFetchListener<LoginResponse>() {

                    @Override
                    public void onComplete(LoginResponse response) {
                        Intent intent = new Intent(getActivity(), FindActivity.class);
                        if (user == null) {// 来自开发者外部调用的情况
                            intent.putExtra(Constants.TAG_USER, CommConfig.getConfig().loginedUser);
                        } else {
                            intent.putExtra(Constants.TAG_USER, user);
                        }
                        intent.putExtra(Constants.TYPE_CLASS, mContainerClass);
                        getActivity().startActivity(intent);
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        mFeedsListView.postDelayed(new Runnable() {

            @Override
            public void run() {
                hideInputMethod();
                if (mImageLoader != null) {
                    // 启动加载数据
                    mImageLoader.resume();
                }
            }
        }, 300);

        //
        registerPostNotifyReceiver();
        //
        registerUserInfoReceiver();
        //
        removeDeletedFeeds();
    }

    public void onStop() {
        resetCommentLayout();
        super.onStop();
    }

    NotifyListener mPostedNotifyListener = new NotifyListener() {

        @Override
        public void onNotify(Intent intent) {
            mFeedsListView.scrollTo(0, 0);
            mFeedsListView.invalidate();
            fetchFeeds();
        }
    };

    protected boolean isMyPage(FeedItem feedItem) {
        return feedItem != null;
    }

    /**
     * 注册广播，当用户取消关注某个用户时，移除其feed</br>
     */
    protected void registerBroadcast() {
        mBroadcastReceiver = new NotifyBroadcastReceiver(new NotifyListener() {

            @Override
            public void onNotify(Intent intent) {
                CommUser user = intent.getExtras().getParcelable(Constants.USER);
                List<FeedItem> items = mFeedLvAdapter.getDataSource();
                List<FeedItem> scrapItems = new ArrayList<FeedItem>();
                for (FeedItem item : items) {
                    if (item.creator.equals(user)) {
                        scrapItems.add(item);
                    }
                }
                if (scrapItems.size() > 0) {
                    mFeedLvAdapter.getDataSource().removeAll(scrapItems);
                    mFeedLvAdapter.notifyDataSetChanged();
                }
            }
        });

        IntentFilter intentFilter = new IntentFilter(NotifyBroadcastReceiver.CANCEL_FOLLOWED);
        getActivity().registerReceiver(mBroadcastReceiver, intentFilter);
    }

    protected void registerPostNotifyReceiver() {
        if (mPostBroadcastReceiver == null) {
            mPostBroadcastReceiver = new NotifyBroadcastReceiver(new NotifyListener() {

                @Override
                public void onNotify(Intent intent) {
                    FeedItem feedItem = intent.getExtras().getParcelable(Constants.FEED);
                    if (!isMyPage(feedItem)) {
                        return;
                    }
                    final String action = intent.getAction();
                    if (action.equals(NotifyBroadcastReceiver.FEED_POSTED)) {
                        Log.d(getTag(), "### 新的feed : " + feedItem);
                        mFeedLvAdapter.addToFirst(feedItem);
                        mFeedsListView.setSelection(0);
                        // [目前策略：对于已经完成feed，不主动下来刷新最新得feed]
                        // fetchFeeds();
                    } else if (action.equals(NotifyBroadcastReceiver.FEED_DELETED)) {
                        if (feedItem != null) {
                            mFeedLvAdapter.getDataSource().remove(feedItem);
                            mFeedLvAdapter.notifyDataSetChanged();
                            Log.d(getTag(), "### 删除feed");
                        }
                    }

                    mFeedsListView.invalidate();
                }
            });

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(NotifyBroadcastReceiver.FEED_POSTED);
            intentFilter.addAction(NotifyBroadcastReceiver.FEED_DELETED);
            // 注册post相关的接收器
            getActivity().registerReceiver(mPostBroadcastReceiver, intentFilter);
        }
    }

    NotifyListener mUserInfoListener = new UserInfoNotifyListener() {

        @Override
        public void onNotify(Intent intent) {
            CommUser user = newUser(intent);
            if (user != null) {
                mUser = user;
                updatedUserInfo(mUser);
            }
        }
    };

    /**
     * 
     */
    private void registerUserInfoReceiver() {
        if (mUserBroadcastReceiver == null) {
            mUserBroadcastReceiver = new NotifyBroadcastReceiver(mUserInfoListener);
            getActivity().registerReceiver(mUserBroadcastReceiver,
                    new IntentFilter(NotifyBroadcastReceiver.USER_INFO_UPDATED));
        }
    }

    /**
     * 判断该Feed是否来源于特定用户</br>
     * 
     * @param feedItem
     * @return
     */
    protected boolean isMyFeed(FeedItem feedItem) {
        CommUser user = CommConfig.getConfig().loginedUser;
        if (user == null || TextUtils.isEmpty(user.id)) {
            return false;
        }
        return feedItem.creator.id.equals(user.id);
    }

    /**
     * 用户信息修改以后更新feed的用户信息
     * 
     * @param user
     */
    public void updatedUserInfo(CommUser user) {
        mUser = user;
        List<FeedItem> feedItems = mFeedLvAdapter.getDataSource();
        for (FeedItem feed : feedItems) {
            updateFeedDetail(feed, user);
        }

        mFeedLvAdapter.notifyDataSetChanged();
    }

    private void updateFeedDetail(FeedItem feed, CommUser user) {
        if (isMyFeed(feed)) {
            feed.creator = user;
            Log.d(getTag(), "昵称 : " + user.name);
        }

        // 更新like的创建者信息
        updateLikeCreator(feed.likes, user);
        // 更新评论信息
        updateCommentCreator(feed.comments, user);
        // 更新at好友的creator
        updateAtFriendCreator(feed.atFriends, user);
        // 转发类型的feed
        if (feed instanceof ForwardFeedItem) {
            updateFeedDetail(((ForwardFeedItem) feed).forwardItem, user);
        }
    }

    private void updateLikeCreator(List<Like> likes, CommUser user) {
        for (Like likeItem : likes) {
            if (likeItem.creator.id.equals(user.id)) {
                likeItem.creator = user;
            }
        }
    }

    private void updateCommentCreator(List<Comment> comments, CommUser user) {
        for (Comment commentItem : comments) {
            if (commentItem.creator.id.equals(user.id)) {
                commentItem.creator = user;
            }
        }
    }

    private void updateAtFriendCreator(List<CommUser> friends, CommUser user) {
        for (CommUser item : friends) {
            if (item.id.equals(user.id)) {
                item = user;
            }
        }
    }

    /**
     * 用户在个人中心删除feed后,在Feed流页面的ListView中要对应的删除.已删除的feed通过SharedPreferences来存储.
     */
    private void removeDeletedFeeds() {
        SharedPreferences deletedSharedPref = SharePrefUtils.getSharePrefEdit(getActivity(),
                Constants.DELETED_FEEDS_PREF);
        // all deleted feeds iterator.
        Iterator<String> deletedIterator = deletedSharedPref.getAll().keySet().iterator();
        // 遍历移除所有已经删除的feed
        while (deletedIterator.hasNext()) {
            String feedId = deletedIterator.next();
            //
            Iterator<FeedItem> feedIterator = mFeedLvAdapter.getDataSource().iterator();
            // find the target feed
            while (feedIterator.hasNext()) {
                FeedItem feedItem = feedIterator.next();
                if (feedItem.id.equals(feedId)) {
                    feedIterator.remove();
                    break;
                }
            } // end of second while
        } // first while

        mFeedLvAdapter.notifyDataSetChanged();
        deletedSharedPref.edit().clear();
    }

    /**
     * 设置feed的过滤器</br>
     * 
     * @param filter
     */
    public void setFeedFilter(Filter<FeedItem> filter) {
        mFeedFilter = filter;
    }

    /**
     * 获取Feed的ListView</br>
     * 
     * @return
     */
    public ListView getListView() {
        return mFeedsListView;
    }

    /**
     * 过滤数据</br>
     * 
     * @return
     */
    protected List<FeedItem> filteFeeds(List<FeedItem> list) {
        List<FeedItem> destList = mFeedFilter != null ? mFeedFilter.doFilte(list) : list;
        // 移除status>=2的feed，具体值得的含义参考文档说明
        Iterator<FeedItem> iterator = destList.iterator();
        while (iterator.hasNext()) {
            FeedItem item = iterator.next();
            if (item.status > 1) {
                iterator.remove();
            }
        }
        return destList;
    }

    /**
     * 从Server端抓取最新的feed
     */
    protected abstract void fetchFeeds();

    /**
     * 保存新加载的数据。如果该数据存在于DB中，则替换成最新的，否则Insert一条新纪录.
     * 
     * @param newFeedItems
     */
    protected void saveFeedsToDB(final List<FeedItem> newFeedItems) {
        DbHelper<FeedItem> helper = DbHelperFactory.getFeedDbHelper(getActivity());
        InsertCommand<FeedItem> insertCommand = new InsertCommand<FeedItem>(helper, newFeedItems);
        insertCommand.execute();
    }

    /**
     * 检查评论是否有效。目前仅仅判空</br>
     * 
     * @param content 评论的内容
     * @return
     */
    private boolean checkCommentData(String content) {
        // 检查评论的内容是否合法
        if (TextUtils.isEmpty(content)) {
            ToastMsg.showShortMsgByResName(getActivity(), "umeng_comm_content_invalid");
            return false;
        }
        return true;
    }

    /**
     * 检测是否已经显示了键盘，如果已经显示则计算滚动的距离
     */
    private void performListenKeyboard() {
        if (mOnGlobalLayoutListener == null) {
            mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {

                    final int screenHeight = mRootView.getRootView().getHeight();
                    int keyboardHeight = screenHeight - mRootView.getHeight();
                    // 如果软键盘的高度小于屏幕的1/3或者已经显示软键盘了，则直接返回
                    if (keyboardHeight < screenHeight / 3 || !isShowKeyboard) {
                        return;
                    }
                    // 显示软键盘，计算滚动距离
                    isShowKeyboard = false;
                    final int[] layoutSize = new int[2];
                    mCommentLayout.getLocationOnScreen(layoutSize);
                    mFeedsListView.post(new Runnable() {

                        @Override
                        public void run() {
                            int dis = 0;
                            dis = layoutSize[1] + mScrollDis;
                            if (mCommentListView != null) {
                                dis -= mCommentListView.getPaddingBottom()
                                        + mCommentListView.getPaddingTop();
                            }
                            if (mClickItemView != null) {
                                dis -= mClickItemView.getHeight();
                            }
                            mFeedsListView.setSelectionFromTop(
                                    mCurFeedItemIndex + mFeedsListView.getHeaderViewsCount(), dis);
                        }
                    });
                }
            };
        }
        // 注册一个GlobalLayoutListener回调
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
    }

    /*
     * 显示输入评论内容的布局
     */
    @Override
    protected void showCommentLayout(final int pos, boolean fromClickBtn) {

        mRootView.post(new Runnable() {

            @Override
            public void run() {
                mCommentLayout.setVisibility(View.VISIBLE);
                if (mCommentEditText.requestFocus()) {
                    // 显示输入法
                    showInputMethod();
                }
                isShowKeyboard = true;
                performListenKeyboard();
                hidePostButtonWithAnim();
                // getCommentPrefix(pos);
            }
        });
    }

    /**
     * 设置回复评论文本的内容</br>
     * 
     * @param pos
     */
    private void setCommentContent(int pos, boolean clickBtn) {
        // 如果来源于点击评论按钮，则不设置文本
        if (clickBtn) {
            mReplyUser = null;
            mCommentEditText.setHint("");
            return;
        }
        String replyText = getCommentHint(pos, clickBtn);
        mCommentEditText.setHint(replyText);
    }

    /**
     * 执行点击Comment item。目的是计算所点击的item的偏移量，在软键盘弹出后，设置Editview的滚动位置
     */

    @Override
    protected void performClickCommentItem(final WrapperListView commListView,
            final View itemView, final int position, boolean clickBtn) {
        mClickItemView = itemView;
        if (commListView != null) {
            // 计算滚动距离
            int lastVisible = commListView.getLastVisiblePosition();
            mScrollDis = 0;
            for (int i = position; i < lastVisible; i++) {
                mScrollDis += commListView.getChildAt(i).getHeight();
            }
        }

        // 由于此时来自于点击评论按钮，因此设置一个无效的点击位置
        setCommentContent(position, clickBtn);
        // 显示数据如跟评论的布局
        showInputMethod();
        showCommentLayout(position, clickBtn);
    }

    @Override
    protected void clickActionButton(FeedItemViewHolder viewHolder, int position) {
        mCurFeedItemIndex = position + 1;
        super.clickActionButton(viewHolder, position);
    }

    @Override
    protected void updateCommentListView(WrapperListView commentListView, FeedItem feedItem,
            Comment comment) {
        super.updateCommentListView(commentListView, feedItem, comment);
        mFeedLvAdapter.notifyDataSetChanged();
    }

    @Override
    protected void likeSuccess(String feedId, String likeId) {
        super.likeSuccess(feedId, likeId);
        mFeedsListView.requestLayout();
        // 更新数据
        mFeedLvAdapter.notifyDataSetChanged();
    }

    private void resetCommentLayout() {
        if (mCommentLayout != null) {
            mCommentLayout.setVisibility(View.INVISIBLE);
        }
        if (mCommentEditText != null) {
            mCommentEditText.setText("");
        }
    }

    private void unregisterReceiver() {

        if (mPostBroadcastReceiver != null) {
            getActivity().unregisterReceiver(mPostBroadcastReceiver);
        }

        if (mUserBroadcastReceiver != null) {
            getActivity().unregisterReceiver(mUserBroadcastReceiver);
        }

        if (mBroadcastReceiver != null) {
            getActivity().unregisterReceiver(mBroadcastReceiver);
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver();
        super.onDestroy();
    }

    /**
     * 发送show or hide输入法消息</br>
     * 
     * @param type
     * @param view
     */
    private void sendInputMethodMessage(int type, View view) {
        Message message = mHandler.obtainMessage(type);
        message.obj = view;
        mHandler.sendMessage(message);
    }

    void cleanAdapterData() {
        mFeedLvAdapter.getDataSource().clear();
        mFeedLvAdapter.notifyDataSetChanged();
    }

}
