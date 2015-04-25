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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.umeng.comm.core.beans.BaseBean;
import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.CommUser.Gender;
import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.beans.UserDetail;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.db.AbsDBHelper;
import com.umeng.comm.core.db.DbHelper;
import com.umeng.comm.core.db.DbHelperFactory;
import com.umeng.comm.core.db.RelationDBHelper.RelativeKeyPair;
import com.umeng.comm.core.db.cmd.DeleteCommand;
import com.umeng.comm.core.db.cmd.InsertCommand;
import com.umeng.comm.core.db.cmd.QueryCommand;
import com.umeng.comm.core.db.cmd.RelativeCommand;
import com.umeng.comm.core.db.cmd.concrete.DbCommandFactory;
import com.umeng.comm.core.imageloader.ImgDisplayOption;
import com.umeng.comm.core.listeners.Listeners.CommListener;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.listeners.Listeners.OnResultListener;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.Response;
import com.umeng.comm.core.nets.responses.LoginResponse;
import com.umeng.comm.core.nets.responses.ProfileResponse;
import com.umeng.comm.core.nets.responses.TopicResponse;
import com.umeng.comm.core.sdkmanager.ImageLoaderManager;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.DeviceUtils;
import com.umeng.comm.core.utils.Log;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.broadcastreceiver.NotifyBroadcastReceiver;
import com.umeng.comm.ui.broadcastreceiver.NotifyBroadcastReceiver.NotifyListener;
import com.umeng.comm.ui.broadcastreceiver.UserInfoNotifyListener;
import com.umeng.comm.ui.fragments.FansFragment;
import com.umeng.comm.ui.fragments.FollowedUserFragment;
import com.umeng.comm.ui.fragments.PostedFeedsFragment;
import com.umeng.comm.ui.fragments.PostedFeedsFragment.OnDeleteListener;
import com.umeng.comm.ui.utils.ViewFinder;
import com.umeng.comm.ui.widgets.CommentEditText;
import com.umeng.comm.ui.widgets.CommentEditText.EditTextBackEventListener;
import com.umeng.comm.ui.widgets.FlowLayout;
import com.umeng.comm.ui.widgets.RoundImageView;

/**
 * 用户个人信息页面, 包含已发布的消息、已关注的话题、已关注的人三个fragment, 以及用户的头像、个人基本信息等.
 * 
 * @author mrsimple
 */
public final class UserInfoActivity extends BaseFragmentActivity implements OnClickListener {

    /**
     * 已发送Feed的Fragment
     */
    private PostedFeedsFragment mPostedFragment = new PostedFeedsFragment();

    /**
     * 关注的好友Fragment
     */
    private FollowedUserFragment mFolloweredUserFragment;

    /**
     * 粉丝Fragment
     */
    private FansFragment mFansFragment;

    private TextView mUserNameTv;
    private RoundImageView mHeaderImageView;
    private ImageView mGenderImageView;
    private ToggleButton mFollowToggleButton;
    /** 该用户为传递进来的user，可能是好友、陌生人等身份 */
    private CommUser mUser;

    /**
     * 已经发布的消息标签, 用于切换Fragment
     */
    private TextView mPostedTv;
    /**
     * 已经发布的消息数量标签
     */
    private TextView mPostedCountTv;
    /**
     * 已经关注的用户标签, 用于切换Fragment
     */
    private TextView mFollowedUserTv;

    /**
     * 已经关注的用户数量标签
     */
    private TextView mFollowedUserCountTv;
    /**
     * 我的粉丝标签, 用于切换Fragment
     */
    private TextView mFansTextView;
    /**
     * 我的fans用户数量标签
     */
    private TextView mFansCountTextView;

    private int mFeedsCount;
    private int mFollowUserCount;
    private int mFansCount;

    private FlowLayout mTopicContainer;

    private CommentEditText mCommentEditText;

    private View mCommentLayout;

    /**
     * 
     */
    private int mSelectedColor = Color.BLUE;
    /**
     * 视图查找器,避免每次findViewById进行强转
     */
    ViewFinder mViewFinder;

    /**
     * 用户信息更新的广播接收器
     */
    NotifyBroadcastReceiver mUserBroadcastReceiver;

    /**
     * feed条数更新的广播接收器
     */
    NotifyBroadcastReceiver mPostBroadcastReceiver;
    NotifyBroadcastReceiver mBroadcastReceiver;
    NotifyBroadcastReceiver mTopicBroadcastReceiver;
    
    private List<Topic> mFollowTopics = new ArrayList<Topic>();
    
    // 由于开发者可能直接使用Fragment，在退出登录的时候，我们需要回到该Activity
//    private String mContainerClass = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ResFinder.getLayout("umeng_comm_user_info_layout"));
        mUser = getIntent().getExtras().getParcelable(Constants.TAG_USER);
//        mContainerClass = getIntent().getExtras().getString(Constants.TYPE_CLASS);
        if (mUser == null) {
            return;
        }

        // 视图查找器
        mViewFinder = new ViewFinder(getWindow().getDecorView());

        mPostedFragment.setCurrentUser(mUser);
        mPostedFragment.setOnDeleteListener(new OnDeleteListener() {

            @Override
            public void onDelete(BaseBean item) {
                updateFeedCount(--mFeedsCount);
            }
        });
        // 初始化UI
        initUIComponents();
        // // 从数据库中加载话题
        loadTopicFromDB();
        // 从Server抓取话题
        loadTopicsFromServer();
        // 获取用户的profile信息
        initUserInfoFromSharePref();
        // 从server抓取用户信息
        fetchUserProfile();
        // 从数据库加载已经关注的用户
        loadFollowedUserFromDatabase();
        // 设置用户信息View的显示内容
        setupUserInfo(mUser);

        // 注册广播接收器
        registerUserInfoReceiver();
        registerFeedPostedReceiver();
        registerUpdateCountBroadcast();
        registerTopicBroadcast();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从数据库中加载话题
//        loadTopicFromDB();
    }

    NotifyListener mUserInfoListener = new UserInfoNotifyListener() {

        @Override
        public void onNotify(Intent intent) {
            CommUser user = newUser(intent);
            if (user != null) {
                mUser = user;
                Log.d("", "### 新的用户信息 : " + mUser.toString());
                Log.d("", "### icon : " + mUser.iconUrl);
                // updateUserInfo();
                setupUserInfo(user);
            }
        }
    };

    /**
     * 
     */
    private void registerUserInfoReceiver() {
        if (mUserBroadcastReceiver == null) {
            mUserBroadcastReceiver = new NotifyBroadcastReceiver(mUserInfoListener);
            this.registerReceiver(mUserBroadcastReceiver,
                    new IntentFilter(NotifyBroadcastReceiver.USER_INFO_UPDATED));
        }
    }

    private void updateFeedCount(int count) {
        mPostedCountTv.setText(String.valueOf(count));
    }

    /**
     * 
     */
    private void registerFeedPostedReceiver() {
        if (mPostBroadcastReceiver == null) {
            mPostBroadcastReceiver = new NotifyBroadcastReceiver(new NotifyListener() {

                @Override
                public void onNotify(Intent intent) {
                    // 确保仅仅更新自己的用户中心的相关数据
                    if (mUser.equals(CommConfig.getConfig().loginedUser)) {
                        updateFeedCount(++mFeedsCount);
                    }
                }
            });
            this.registerReceiver(mPostBroadcastReceiver,
                    new IntentFilter(NotifyBroadcastReceiver.FEED_POSTED));
        }
    }

    @Override
    protected void onDestroy() {
        if (mUserBroadcastReceiver != null) {
            unregisterReceiver(mUserBroadcastReceiver);
            mUserBroadcastReceiver = null;
        }

        if (mPostBroadcastReceiver != null) {
            unregisterReceiver(mPostBroadcastReceiver);
            mPostBroadcastReceiver = null;
        }

        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
        
        if (mTopicBroadcastReceiver != null) {
            unregisterReceiver(mTopicBroadcastReceiver);
            mTopicBroadcastReceiver = null;
        }

        super.onDestroy();
    }

    private void initCommentView() {
        mCommentEditText = mViewFinder.findViewById(ResFinder
                .getId("umeng_comm_comment_edittext"));
        mCommentLayout = findViewById(ResFinder.getId("umeng_comm_commnet_edit_layout"));

        findViewById(ResFinder.getId("umeng_comm_comment_send_button")).setOnClickListener(this);
        mCommentEditText.setEditTextBackListener(new EditTextBackEventListener() {

            @Override
            public void onClickBack() {
                hideCommentLayout();
            }
        });
    }

    private void hideCommentLayout() {
        //
        mCommentLayout.setVisibility(View.GONE);
        hideInputMethod(mCommentEditText);
    }

    /**
     * 从DB重加载已经关注的用户，判断当前用户是否是其好友</br>
     */
    private void loadFollowedUserFromDatabase() {
        // 构建查询命令
        QueryCommand<CommUser> queryCommand = DbCommandFactory.createFollowedUserCmd(this,
                mUser.id);
        // 设置查询回调
        queryCommand.setFetchListener(new SimpleFetchListener<List<CommUser>>() {

            @Override
            public void onComplete(List<CommUser> users) {
                if (CommonUtils.isActivityAlive(UserInfoActivity.this)) {
                    final String uid = mUser.id;
                    if (!CommonUtils.isListEmpty(users)) {
                        for (CommUser user : users) {
                            if (uid.equals(user.id)) {
                                mFollowToggleButton.setChecked(true);
                                break;
                            }
                        }

                        mFollowUserCount = users.size();
                        mFollowedUserCountTv.setText(String.valueOf(mFollowUserCount));
                    }
                }
            }
        });
        queryCommand.execute();

    }

    private void loadTopicFromDB() {
        RelativeCommand<Topic> relativeCommand = DbCommandFactory.createFollowedTopicCmd(this,
                mUser.id);
        relativeCommand.setFetchListener(new SimpleFetchListener<List<Topic>>() {
            @Override
            public void onComplete(List<Topic> result) {
                if (CommonUtils.isActivityAlive(UserInfoActivity.this)) {
                    // 清空所有view
                    mTopicContainer.removeAllViews();
                    updateTopicView(result);
                    mFollowTopics.clear();
                    mFollowTopics.addAll(result);
                }
            }
        });
        relativeCommand.execute();

    }

    /**
     * 加载用户已经关注的话题</br>
     * 
     * @param start 起始页
     */
    private void loadTopicsFromServer() {

        mSdkImpl.fetchFollowedTopics(mUser.id,
                new SimpleFetchListener<TopicResponse>() {

                    @Override
                    public void onComplete(final TopicResponse response) {
                        if (handlerResponse(response)) {
                            return;
                        }
                        List<Topic> results = response.result;
                        mFollowTopics.clear();
                        mFollowTopics.addAll(results);
                        userTopicPolicy(results);
                        updateTopicView(results);
                        // 保存话题本身
                        saveTopicsInDatabase(results);
                        // 保存话题本身的数据后再保存用户关注的话题到数据库
                        saveFollowedTopicListToDB(results);
                    }

                });
    }

    /**
     * 
     */
    private void initUserInfoFromSharePref() {
        QueryCommand<UserDetail> queryCommand = new QueryCommand<UserDetail>(DbHelperFactory
                .getUserDetailDbHelper(getApplicationContext()));
        queryCommand.addParam(AbsDBHelper.USER_ID, mUser.id);
        queryCommand.setFetchListener(new SimpleFetchListener<List<UserDetail>>() {

            @Override
            public void onComplete(List<UserDetail> response) {
                if (response != null && response.size() > 0) {
                    mFeedsCount = response.get(0).postedFeedCount;
                    mFollowUserCount = response.get(0).followedUserCount;
                    mFansCount = response.get(0).fansCount;
                }

                updateCountTextView();
            }
        });
        queryCommand.execute();

    }

    /**
     * 对用户关注的话题设置已关注flag。在获取用户关注的话题时，server不返回is_focused。
     * 这里仅仅只针用户为当前登录用户时，才改变该值</br>
     * 
     * @param topics 从server端获取到的最新feed
     */
    private void userTopicPolicy(List<Topic> topics) {
        CommUser loginUser = CommConfig.getConfig().loginedUser;
        if (loginUser.id.equals(mUser.id)) {
            for (Topic topic : topics) {
                topic.isFocused = true;
            }
        }
    }

    /**
     * 更新feed，好友、粉丝的数目</br>
     */
    private void updateCountTextView() {
        mPostedCountTv.setText(String.valueOf(mFeedsCount));
        mFansCountTextView.setText(String.valueOf(mFansCount));
        mFollowedUserCountTv.setText(String.valueOf(mFollowUserCount));
    }

    /**
     * 更新显示话题的viewflow
     */
    private void updateTopicView(List<Topic> results) {
        mTopicContainer.removeAllViews();
        mTopicContainer.requestLayout();
        for (Topic topic : results) {
            addTopicView(topic);
        }
    }

    /**
     * 保存话题到数据库</br>
     * 
     * @param results
     */
    private void saveTopicsInDatabase(final List<Topic> results) {
        // 保存话题本身
        DbHelper<Topic> topicDBHelper = DbHelperFactory.getTopicDbHelper(this);
        // 先保存话题本身
        InsertCommand<Topic> insertCommand = new InsertCommand<Topic>(topicDBHelper, results);
        insertCommand.execute();
    }

    private void saveFollowedTopicListToDB(List<Topic> topics) {
        // 再保存关注关系的数据
        DbHelper<RelativeKeyPair> relationDBHelper = DbHelperFactory.getTopicUserDbHelper(this);
        List<RelativeKeyPair> relativeKeyPairs = new LinkedList<RelativeKeyPair>();
        for (Topic topic : topics) {
            // 构建关系对象列表
            relativeKeyPairs.add(new RelativeKeyPair(topic.id,
                    mUser.id));
        }
        // 插入关系对象
        InsertCommand<RelativeKeyPair> relativeCommand = new InsertCommand<RelativeKeyPair>(
                relationDBHelper, relativeKeyPairs);
        relativeCommand.execute();
    }

    /**
     * 初始化用户关注的话题View</br>
     * 
     * @param label 该话题名称
     */
    private void addTopicView(final Topic topic) {
        TextView textView = new TextView(this);
        FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        textView.setBackgroundColor(Color.LTGRAY);
        textView.setLayoutParams(params);
        textView.setBackgroundDrawable(ResFinder.getDrawable("umeng_community_topic_bg"));
        textView.setText(topic.name);
        textView.setPadding(16, 4, 3, 2);
        textView.setTextColor(ResFinder.getColor("umeng_comm_text_topic_light_color"));
        // 超出将显示省略号
        textView.setSingleLine();
        textView.setEllipsize(TruncateAt.END);
        textView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserInfoActivity.this, TopicDetailActivity.class);
                // 用户中心过来的话题都是已经关注的话题
                // topic.isFocused = true;
                intent.putExtra(Constants.TAG_TOPIC, topic);
                UserInfoActivity.this.startActivity(intent);
            }
        });
        mTopicContainer.addView(textView);
    }

    /**
     * 
     */
    private void initUIComponents() {
        //
        addFragment(ResFinder.getId("umeng_comm_user_info_fragment_container"),
                mPostedFragment);
        // 初始化用户关注的话题viewgroup
        mTopicContainer = mViewFinder.findViewById(ResFinder.getId(
                "umeng_comm_user_follow_topics"));

        // 选中的某个tab时的文字颜色
        mSelectedColor = ResFinder.getColor("umeng_comm_text_topic_light_color");

        // 初始化feed、好友、粉丝、back、设置的listener
        findViewById(ResFinder.getId("umeng_comm_posted_layout")).setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_comm_follow_user_layout")).setOnClickListener(
                this);
        findViewById(ResFinder.getId("umeng_comm_my_fans_layout")).setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_comm_setting_back")).setOnClickListener(this);

        Button settingButton = (Button) findViewById(ResFinder.getId("umeng_comm_save_bt"));
        settingButton.setBackgroundDrawable(ResFinder.getDrawable("umeng_comm_setting_bt"));
        ViewGroup.LayoutParams params = settingButton.getLayoutParams();
        params.width = DeviceUtils.dp2px(this, 28);
        params.height = DeviceUtils.dp2px(this, 28);
        settingButton.setLayoutParams(params);
        settingButton.setOnClickListener(this);
        //目前将设置功能移到发现页面，此时暂时隐藏设置按钮
        settingButton.setVisibility(View.INVISIBLE);

        TextView titleTextView = mViewFinder.findViewById(ResFinder
                .getId("umeng_comm_setting_title"));
        titleTextView.setText(ResFinder.getString("umeng_comm_user_center"));
        //
        mPostedTv = mViewFinder.findViewById(ResFinder.getId("umeng_comm_posted_msg_tv"));
        mPostedTv.setTextColor(mSelectedColor);

        //
        mPostedCountTv = mViewFinder.findViewById(ResFinder
                .getId("umeng_comm_posted_count_tv"));
        mPostedCountTv.setTextColor(mSelectedColor);

        mFollowedUserTv = mViewFinder.findViewById(ResFinder.getId(
                "umeng_comm_followed_user_tv"));
        mFollowedUserCountTv = mViewFinder.findViewById(ResFinder.getId(
                "umeng_comm_follow_user_count_tv"));

        mFansTextView = mViewFinder.findViewById(ResFinder.getId("umeng_comm_my_fans_tv"));
        mFansCountTextView = mViewFinder.findViewById(ResFinder.getId(
                "umeng_comm_fans_count_tv"));
        // 昵称
        mUserNameTv = mViewFinder.findViewById(ResFinder.getId("umeng_comm_user_name_tv"));
        mUserNameTv.setText(mUser.name);

        mHeaderImageView = mViewFinder.findViewById(ResFinder.getId(
                "umeng_comm_user_header"));

        ImgDisplayOption option = ImgDisplayOption.getOptionByGender(mUser.gender);
        mHeaderImageView.setImageUrl(mUser.iconUrl, option);

        // 用户性别
        mGenderImageView = mViewFinder.findViewById(ResFinder.getId("umeng_comm_user_gender"));

        // 关注按钮
        mFollowToggleButton = mViewFinder.findViewById(ResFinder.getId(
                "umeng_comm_user_follow"));
        mFollowToggleButton.setOnClickListener(this);

        CommUser loginUser = CommConfig.getConfig().loginedUser;
        // 用户自己(在未登录的情况下，点击设置跳转到登录，此时传递进来的uid是空的情况)，隐藏关注按钮，显示设置按钮
        if (mUser.id.equals(loginUser.id) || TextUtils.isEmpty(mUser.id)) {
//            settingButton.setVisibility(View.VISIBLE);
            mFollowToggleButton.setVisibility(View.GONE);
        } else {
//            settingButton.setVisibility(View.GONE);
            // 检查该用户是否是当前登录用户的好友 [ 关注 ]
            findFollowedByMe();
        }
        
        initCommentView();
    }

    /**
     * 检查该用户是否是当前登录用户的好友 [ 关注 ]
     */
    private void findFollowedByMe() {
        QueryCommand<CommUser> queryCommand = DbCommandFactory.createIfIFollowedTheUserCmd(this,
                mUser.id);
        queryCommand.setFetchListener(new SimpleFetchListener<List<CommUser>>() {
            @Override
            public void onComplete(List<CommUser> results) {
                // 确保activity没有被销毁
                if (!CommonUtils.isActivityAlive(UserInfoActivity.this)) {
                    return;
                }

                if (!CommonUtils.isListEmpty(results)) {
                    mFollowToggleButton.setChecked(true);// 设置为已关注状态
                } else {
                    mFollowToggleButton.setChecked(false); // 未关注状态
                }
            }
        });
        queryCommand.execute();

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == ResFinder.getId("umeng_comm_posted_layout")
                && !(mCurrentFragment instanceof PostedFeedsFragment)) {// 已发消息
            showFragment(mPostedFragment);
        } else if (id == ResFinder.getId("umeng_comm_follow_user_layout")) {// 关注用户
            if (mFolloweredUserFragment == null) {
                mFolloweredUserFragment = new FollowedUserFragment();
            }
            mFolloweredUserFragment.setUerId(mUser.id);
            mFolloweredUserFragment.setOnResultListener(mFollowListener);
            showFragment(mFolloweredUserFragment);
        } else if (id == ResFinder.getId("umeng_comm_my_fans_layout")
                && !(mCurrentFragment instanceof FansFragment)) { // 我的粉丝
            if (mFansFragment == null) {
                mFansFragment = new FansFragment();
            }
            mFansFragment.setUerId(mUser.id);
            mFansFragment.setOnResultListener(mFansListener);
            showFragment(mFansFragment);
        } else if (id == ResFinder.getId("umeng_comm_setting_back")) { // 返回
            this.finish();
        } else if (id == ResFinder.getId("umeng_comm_save_bt")) { // 设置
            // goto setting。暂时不处理设置相关逻辑
//            Intent setting = new Intent(this, SettingActivity.class);
//            setting.putExtra(Constants.TYPE_CLASS, mContainerClass);
//            startActivity(setting);
        } else if (id == ResFinder.getId("umeng_comm_user_follow")) { // 关注or取消关注
            followOrUnFollow();
            return;
        } else if (id == ResFinder.getId("umeng_comm_comment_send_button")) {
            // 处理点击发送时得逻辑
            mPostedFragment.postComment(mCommentEditText.getText().toString(),
                    new CommListener() {

                        @Override
                        public void onStart() {
                            hideCommentLayout();
                        }

                        @Override
                        public void onComplete(Response response) {
                            if (response.errCode == Constants.NO_ERROR) {
                                mCommentEditText.setText("");
                            }
                        }
                    });
        }
        changeSelectedText();
    }

    /**
     * 关注或者取消关注某个用户</br>
     */
    private void followOrUnFollow() {
        CommonUtils.checkLoginAndFireCallback(this, new SimpleFetchListener<LoginResponse>() {

            @Override
            public void onComplete(LoginResponse response) {
                if (response.errCode != Constants.NO_ERROR) {
                    ToastMsg.showShortMsgByResName(UserInfoActivity.this,
                            "umeng_comm_login_failed");
                    return;
                }
                // true为选中状态为已关注，此时显示文本为“取消关注”；false代表未关注，此时显示文本为“关注”
                if (mFollowToggleButton.isChecked()) {
                    followUser();
                } else {
                    cancelFollowUser();
                }
            }
        });
    }

    /**
     * 关注某个用户</br>
     * 
     * @param uid 被关注用户的id
     */
    private void followUser() {
        mSdkImpl.followUser(mUser, new SimpleFetchListener<Response>() {

            @Override
            public void onComplete(Response response) {
                if (response.errCode == Constants.NO_ERROR) {
                    ToastMsg.showShortMsgByResName(getApplicationContext(),
                            "umeng_comm_follow_user_success");
                    mFollowToggleButton.setChecked(true);
                    // updateUserDB(mUser, true);
                    saveFollowUserToDB(mUser);
                    sendFollowedBroadcast(mUser);
                } else {
                    ToastMsg.showShortMsgByResName(getApplicationContext(),
                            "umeng_comm_follow_user_failed");
                    mFollowToggleButton.setChecked(false);
                }
            }
        });
    }

    /**
     * 取消关注某个用户</br>
     * 
     * @param uid 需要取消关注的用户的id
     */
    private void cancelFollowUser() {
        mSdkImpl.cancelFollowUser(mUser, new SimpleFetchListener<Response>() {

            @Override
            public void onComplete(Response response) {
                if (response.errCode == Constants.NO_ERROR) {
                    ToastMsg.showShortMsg(getApplicationContext(),
                            ResFinder.getString(
                                    "umeng_comm_follow_cancel_success"));
                    mFollowToggleButton.setChecked(false);
                    saveCancelFollowUserToDB(mUser);
                    // 发送取消关注的广播
                    sendCancelFollowedBroadcast(mUser);
                } else {
                    ToastMsg.showShortMsg(getApplicationContext(),
                            ResFinder.getString("umeng_comm_follow_user_failed"));
                    mFollowToggleButton.setChecked(true);
                }
            }
        });
    }

    private void sendCancelFollowedBroadcast(CommUser user) {
        Intent intent = new Intent(NotifyBroadcastReceiver.CANCEL_FOLLOWED);
        intent.putExtra(Constants.USER, user);
        sendBroadcast(intent);
    }
    
    private void sendFollowedBroadcast(CommUser user) {
        Intent intent = new Intent(NotifyBroadcastReceiver.FOLLOWED);
        intent.putExtra(Constants.USER, user);
        sendBroadcast(intent);
    }

    private void saveFollowUserToDB(CommUser user) {
        DbHelper<CommUser> dbHelper = DbHelperFactory.getFollowedUserDbHelper(this);
        InsertCommand<CommUser> insertCommand = new InsertCommand<CommUser>(dbHelper, user);
        insertCommand.execute();
    }

    void saveCancelFollowUserToDB(CommUser user) {
        DeleteCommand<CommUser> deleteCommand = DbCommandFactory.createCancelFollowUserCmd(this,
                user.id);
        deleteCommand.execute();
    }

    /**
     * 获取用户信息并设置</br>
     */
    private void fetchUserProfile() {
        mSdkImpl.fetchUserProfile(mUser.id, new FetchListener<ProfileResponse>() {

            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(ProfileResponse response) {
                if (handlerResponse(response)) {
                    return;
                }
                
                updateFollowedStatus(response.hasFollowed);
                CommUser user = response.result;
                Log.d("", "### 用户信息 : " + response.toString());
                if (!TextUtils.isEmpty(user.id)) {
                    // feeds, fans, follow user个数
                    mFeedsCount = response.mFeedsCount;
                    mFollowUserCount = response.mFollowedUserCount;
                    mFansCount = response.mFansCount;
                    // 保存用户的这些数量信息
                    saveCountToDB();
                    // 更新相关的现实VIew
                    setupUserInfo(user);
                }
            }
        });
    }
    
    /**
     * 
     * 更新是否关注的状态</br>
     * @param hasFollowed
     */
    private void updateFollowedStatus(boolean hasFollowed){
        mFollowToggleButton.setChecked(hasFollowed);
    }

    private void saveCountToDB() {
        UserDetail detail = new UserDetail();
        detail.id = mUser.id;
        detail.postedFeedCount = mFeedsCount;
        detail.followedUserCount = mFollowUserCount;
        detail.fansCount = mFansCount;
        // 插入数据
        InsertCommand<UserDetail> insertCommand = new InsertCommand<UserDetail>(
                DbHelperFactory.getUserDetailDbHelper(getApplicationContext()), detail);
        insertCommand.execute();
    }

    /**
     * 设置用户相关的信息 </br>
     * 
     * @param user
     */
    private void setupUserInfo(CommUser user) {

        mUserNameTv.setText(user.name);
        updateCountTextView();

        if (user.gender == Gender.MALE) {
            mGenderImageView.setImageDrawable(ResFinder.getDrawable("umeng_comm_gender_male"));
        } else if (user.gender == Gender.FEMALE) {
            mGenderImageView.setImageDrawable(ResFinder.getDrawable("umeng_comm_gender_female"));
        }
        ImgDisplayOption option = ImgDisplayOption.getOptionByGender(mUser.gender);
        // 设置用户头像
        mHeaderImageView.setImageUrl(user.iconUrl, option);
        ImageLoaderManager.getInstance().getCurrentSDK().resume();
    }

    /**
     * 修改文本颜色 </br>
     */
    private void changeSelectedText() {
        if ((mCurrentFragment instanceof PostedFeedsFragment)) {
            mFansCountTextView.setTextColor(Color.BLACK);
            changeTextColor(mSelectedColor, Color.BLACK, Color.BLACK);
        } else if ((mCurrentFragment instanceof FansFragment)) {
            changeTextColor(Color.BLACK, Color.BLACK, mSelectedColor);
        } else if ((mCurrentFragment instanceof FollowedUserFragment)) {
            changeTextColor(Color.BLACK, mSelectedColor, Color.BLACK);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mCommentLayout.isShown()) {
            mCommentLayout.setVisibility(View.VISIBLE);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 设置文本颜色</br>
     * 
     * @param postedColor 已发送feed文本颜色
     * @param followColor 关注文本颜色
     * @param fansColor 粉丝文本颜色
     */
    private void changeTextColor(int postedColor, int followColor, int fansColor) {
        mPostedTv.setTextColor(postedColor);
        mPostedCountTv.setTextColor(postedColor);
        mFollowedUserTv.setTextColor(followColor);
        mFollowedUserCountTv.setTextColor(followColor);
        mFansTextView.setTextColor(fansColor);
        mFansCountTextView.setTextColor(fansColor);
    }

    /**
     * 关注用户数的回调函数。在加载缓存或者下拉刷新时，可能需要更新显示的用户数字。
     */
    private OnResultListener mFollowListener = new OnResultListener() {

        @Override
        public void onResult(final int status) {
            if (mFollowUserCount == 0) {
                CommonUtils.runOnUIThread(UserInfoActivity.this, new Runnable() {

                    @Override
                    public void run() {
                        mFollowedUserCountTv.setText(String.valueOf(status));
                    }
                });
            }
        }
    };

    /**
     * 粉丝数的回调函数。在加载缓存或者下拉刷新时，可能需要更新显示的用户数字。
     */
    private OnResultListener mFansListener = new OnResultListener() {

        @Override
        public void onResult(final int status) {
            if (mFansCount == 0) {
                CommonUtils.runOnUIThread(UserInfoActivity.this, new Runnable() {

                    @Override
                    public void run() {
                        mFansCountTextView.setText(String.valueOf(status));
                    }
                });
            }
        }
    };

    /**
     * 在取消关注的时候，更新已关注用户的数目。</br>
     */
    private void registerUpdateCountBroadcast() {
        mBroadcastReceiver = new NotifyBroadcastReceiver(new NotifyListener() {

            @Override
            public void onNotify(Intent intent) {
                // 确保在自己额用户中心
                if (mUser.equals(CommConfig.getConfig().loginedUser)) {
                    --mFollowUserCount;
                    mFollowedUserCountTv.setText(String.valueOf(mFollowUserCount));
                }

            }
        });

        IntentFilter intentFilter = new IntentFilter(NotifyBroadcastReceiver.CANCEL_FOLLOWED);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    /**
     * 在取消关注的时候，更新已关注用户的数目。</br>
     */
    private void registerTopicBroadcast() {
        mTopicBroadcastReceiver = new NotifyBroadcastReceiver(new NotifyListener() {

            @Override
            public void onNotify(Intent intent) {
                //非当前用户不做任何处理
                if ( !mUser.id.equals(CommConfig.getConfig().loginedUser.id) ) {
                    return ;
                }
                String action = intent.getAction();
                Topic topic = intent.getExtras().getParcelable(Constants.TAG_TOPIC);
                if (NotifyBroadcastReceiver.TOPIC_FOLLOWED.equals(action) ){
                    mFollowTopics.add(topic);
                } else if (NotifyBroadcastReceiver.CANCEL_TOPIC_FOLLOWED.equals(action)) {
                    mFollowTopics.remove(topic);
                }
                updateTopicView(mFollowTopics);
            }
        });
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NotifyBroadcastReceiver.TOPIC_FOLLOWED);
        intentFilter.addAction(NotifyBroadcastReceiver.CANCEL_TOPIC_FOLLOWED);
        registerReceiver(mTopicBroadcastReceiver, intentFilter);
    }
    
}
