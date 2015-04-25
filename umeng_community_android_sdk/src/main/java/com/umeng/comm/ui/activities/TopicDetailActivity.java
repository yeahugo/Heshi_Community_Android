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

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.db.DbHelper;
import com.umeng.comm.core.db.DbHelperFactory;
import com.umeng.comm.core.db.RelationDBHelper.RelativeKeyPair;
import com.umeng.comm.core.db.cmd.DeleteCommand;
import com.umeng.comm.core.db.cmd.InsertCommand;
import com.umeng.comm.core.db.cmd.RelativeCommand;
import com.umeng.comm.core.db.cmd.concrete.DbCommandFactory;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.Response;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ResFinder.ResType;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.broadcastreceiver.NotifyBroadcastReceiver;
import com.umeng.comm.ui.fragments.ActiveUserFragment;
import com.umeng.comm.ui.fragments.BaseFeedsFragment;
import com.umeng.comm.ui.fragments.TopicFeedFragment;
import com.umeng.comm.ui.widgets.ViewPagerIndicator;

/**
 * @author mrsimple
 */
public class TopicDetailActivity extends BaseFragmentActivity implements OnClickListener {

    /**
     * 话题详情的Fragment
     */
    private BaseFeedsFragment mDetailFragment;
    private ActiveUserFragment mActiveUserFragment;
    private Topic mTopic;
    private ViewPagerIndicator mIndicator;
    private ViewPager mViewPager;
    private String[] mTitles = null;
    private FragmentPagerAdapter mAdapter;
    private ToggleButton mFollowToggleBtn;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(ResFinder.getLayout("umeng_comm_topic_detail_layout"));
        mTopic = getIntent().getExtras().getParcelable(Constants.TAG_TOPIC);
        if (mTopic == null) {
            finish();
            return;
        }
        mTitles = getResources().getStringArray(
                ResFinder.getResourceId(ResType.ARRAY, "umeng_comm_topic_detail_tabs"));
        // 根据话题的id信息初始化fragment
        // initFragment();
        initView();
    }

    private void initView() {
        mIndicator = (ViewPagerIndicator) findViewById(ResFinder.getId("indicator"));
        mViewPager = (ViewPager) findViewById(ResFinder.getId("viewPager"));
        mIndicator.setTabItemTitles(mTitles);
        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {

            @Override
            public int getCount() {
                return mTitles.length;
            }

            @Override
            public Fragment getItem(int pos) {
                return getFragment(pos);
            }
        };
        mViewPager.setAdapter(mAdapter);
        // 设置关联的ViewPager
        mIndicator.setViewPager(mViewPager, 0);
        // 初始化Header的控件跟数据
        initHeader();
        initTitle();
    }

    /**
     * 初始化标题栏相关控件跟设置数据</br>
     */
    private void initTitle() {
        findViewById(ResFinder.getId("umeng_comm_title_back_btn")).setOnClickListener(this);
        TextView titleTextView = (TextView) findViewById(ResFinder.getId("umeng_comm_title_tv"));
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        titleTextView.setText(mTopic.name);
        findViewById(ResFinder.getId("umeng_comm_title_setting_btn")).setVisibility(View.GONE);
    }

    /**
     * 获取对应的Fragment。0：话题聚合 1：活跃用户</br>
     * 
     * @param pos
     * @return
     */
    private Fragment getFragment(int pos) {
        if (pos == 0) {
            if (mDetailFragment == null) {
                mDetailFragment = TopicFeedFragment.newTopicFeedFrmg(mTopic);
            }
            return mDetailFragment;
        } else if (pos == 1) {
            if (mActiveUserFragment == null) {
                mActiveUserFragment = ActiveUserFragment.newActiveUserFragment(mTopic);
            }
            return mActiveUserFragment;
        }
        Log.d("", "#### invalid fragment position...");
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 为了保证ListView被初始化,将initViews放在onResume中执行
        // initViews();
    }

    private void initHeader() {
        // 话题描述
        TextView topicDescTv = (TextView) findViewById(ResFinder.getId(
                "umeng_comm_topic_desc_tv"));
        String desc = mTopic.desc;
        String noDescStr = ResFinder.getString("umeng_comm_topic_no_desc");
        boolean hasText = TextUtils.isEmpty(desc) || "null".equals(desc);
        String showText = hasText ? noDescStr : desc;
        topicDescTv.setText(showText);

        mFollowToggleBtn = (ToggleButton)
                findViewById(ResFinder.getId("umeng_comm_topic_toggle_btn"));
        // mFollowToggleBtn.setChecked(mTopic.isFocused);
        setTopicStatus();
        mFollowToggleBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 在未关注的状态下点击,那么会checked会先变成true, 所以此时应该对isChecked取反
                if (!mFollowToggleBtn.isChecked()) {
                    cancelFollowTopic(mTopic, mFollowToggleBtn);
                } else {
                    followTopic(mTopic, mFollowToggleBtn);
                }
            }
        });

    }

    // /**
    // * 初始化视图, 将话题信息的布局添加到下拉刷新列表的header中.
    // * 下拉刷新ListView在FeedsFragment中的onCreateView中被初始化,
    // * 因此为了保证调用该方法时ListView已经被初始化,该方法将在onResume中被调用.
    // */
    // private void initViews() {
    // if (mListViewHeaderView == null) {
    // mListViewHeaderView = View.inflate(this,
    // ResFinder.getLayout("umeng_comm_topic_info"), null);
    // mListViewHeaderView.setBackgroundColor(Color.rgb(0xf5, 0xf5, 0xf5));
    // // 先置空Adapter, 避免添加header时抛出异常,
    // //
    // " Cannot add header view to list -- setAdapter has already been called."
    // mDetailFragment.getListView().setAdapter(null);
    // mDetailFragment.getListView().addHeaderView(mListViewHeaderView);
    // // 重新设置adater
    // mDetailFragment.initAdapter();
    // }
    //
    // // 话题描述
    // mTopicDescTv = (TextView)
    // mListViewHeaderView.findViewById(ResFinder.getId(
    // "umeng_comm_topic_desc_tv"));
    // String desc = mTopic.desc;
    // String noDescStr = ResFinder.getString("umeng_comm_topic_no_desc");
    // boolean hasText = TextUtils.isEmpty(desc) || "null".equals(desc);
    // String showText = hasText ? noDescStr : desc;
    // mTopicDescTv.setText(showText);
    //
    // mFollowToggleBtn = (ToggleButton) mListViewHeaderView
    // .findViewById(ResFinder.getId("umeng_comm_topic_toggle_btn"));
    // // mFollowToggleBtn.setChecked(mTopic.isFocused);
    // setTopicStatus();
    // mFollowToggleBtn.setOnClickListener(new OnClickListener() {
    //
    // @Override
    // public void onClick(View v) {
    // // 在未关注的状态下点击,那么会checked会先变成true, 所以此时应该对isChecked取反
    // if (!mFollowToggleBtn.isChecked()) {
    // cancelFollowTopic(mTopic, mFollowToggleBtn);
    // } else {
    // followTopic(mTopic, mFollowToggleBtn);
    // }
    // }
    // });
    //
    // }

    /**
     * 检查当前登录用户是否已关注该话题，并设置ToggleButton的状态</br>
     */
    private void setTopicStatus() {
        String loginUserId = CommConfig.getConfig().loginedUser.id;
        if (TextUtils.isEmpty(loginUserId)) {
            Log.d("###", "### user dont login...");
            return;
        }
        RelativeCommand<Topic> relativeCommand = DbCommandFactory.createFollowedTopicCmd(
                this, loginUserId);
        relativeCommand.setFetchListener(new SimpleFetchListener<List<Topic>>() {

            @Override
            public void onComplete(List<Topic> topics) {
                if (topics != null && topics.size() > 0) {
                    for (Topic topic : topics) {
                        if (mTopic.id.equals(topic.id)) {
                            mFollowToggleBtn.setChecked(true);
                            break;
                        }
                    }
                }
            }
        });

        relativeCommand.execute();
    }

    /**
     * 关注某个话题</br>
     * 
     * @param id 话题的id
     */
    private void followTopic(final Topic topic, final ToggleButton toggleButton) {
        // 关注话题
        mSdkImpl.followTopic(topic,
                new SimpleFetchListener<Response>() {

                    @Override
                    public void onComplete(Response response) {
                        String toastMsg = "";
                        if (response.errCode == Constants.NO_ERROR) {
                            toastMsg = ResFinder.getString(
                                    "umeng_comm_topic_follow_success");
                            toggleButton.setChecked(true);
                            topic.isFocused = true;

                            // updateTopicDB(TopicDetalActivity.this, topic,
                            // true);
                            saveFollowTopicInDB(TopicDetailActivity.this, topic);
                        } else if (response.errCode == Constants.ORIGIN_TOPIC_DELETE_ERR_CODE) {
                            // 在数据库中删除该话题并Toast
                            // removeFollowedTopicOnDatabase(getActivity(),
                            // topic);
                            ToastMsg.showShortMsgByResName(TopicDetailActivity.this,
                                    "umeng_comm__topic_has_deleted");
                        } else {
                            toastMsg = ResFinder.getString(
                                    "umeng_comm_topic_follow_failed");
                            toggleButton.setChecked(false);
                        }

                        sendTopicBroadcast(topic);
                        ToastMsg.showShortMsg(TopicDetailActivity.this, toastMsg);
                    }
                });
    }

    /**
     * 取消关注某个话题</br>
     * 
     * @param id
     */
    private void cancelFollowTopic(final Topic topic, final ToggleButton toggleButton) {
        // 取消关注话题
        mSdkImpl.cancelFollowTopic(topic,
                new SimpleFetchListener<Response>() {

                    @Override
                    public void onComplete(Response response) {
                        String toastMsg = "";
                        if (response.errCode == Constants.NO_ERROR) {
                            toastMsg = ResFinder.getString(
                                    "umeng_comm_topic_cancel_success");
                            topic.isFocused = false;
                            toggleButton.setChecked(false);
                            // updateTopicDB(TopicDetalActivity.this, topic,
                            // false);
                            saveCancelFollowTopicInDB(TopicDetailActivity.this, topic);
                        } else if (response.errCode == Constants.ORIGIN_TOPIC_DELETE_ERR_CODE) {
                            // 在数据库中删除该话题并Toast
                            // removeFollowedTopicOnDatabase(getActivity(),
                            // topic);
                            ToastMsg.showShortMsgByResName(TopicDetailActivity.this,
                                    "umeng_comm__topic_has_deleted");
                        } else {
                            toastMsg = ResFinder.getString(
                                    "umeng_comm_topic_cancel_failed");
                            toggleButton.setChecked(true);
                        }

                        sendTopicBroadcast(topic);
                        ToastMsg.showShortMsg(TopicDetailActivity.this, toastMsg);
                    }
                });
    }

    private void sendTopicBroadcast(Topic topic) {
        String action = topic.isFocused ? NotifyBroadcastReceiver.TOPIC_FOLLOWED
                : NotifyBroadcastReceiver.CANCEL_TOPIC_FOLLOWED;
        Intent intent = new Intent(action);
        intent.putExtra(Constants.TAG_TOPIC, topic);
        sendBroadcast(intent);
    }

    private void saveFollowTopicInDB(Context context, Topic topic) {
        DbHelper<RelativeKeyPair> relationDBHelper = DbHelperFactory
                .getTopicUserDbHelper(context);
        String loginUserId = CommConfig.getConfig().loginedUser.id;
        // relationDBHelper.insert(new RelativeKeyPair(topic.mId, loginUserId));

        InsertCommand<RelativeKeyPair> insertCommand = new InsertCommand<RelativeKeyPair>(
                relationDBHelper, new RelativeKeyPair(topic.id, loginUserId));
        insertCommand.execute();
    }

    private void saveCancelFollowTopicInDB(Context context, Topic topic) {
        DeleteCommand<RelativeKeyPair> deleteCommand = DbCommandFactory.createCancelFollowTopicCmd(
                this, topic.id);
        deleteCommand.execute();

    }

    /**
     * 初始化Fragment
     */
    // private void initFragment() {
    // mDetailFragment = TopicFeedFragment.newTopicFeedFrmg(mTopic);
    // addFragment(ResFinder.getId("umeng_comm_topic_detail_container"),
    // mDetailFragment);
    // // 设置Feed的过滤器
    // mDetailFragment.setFeedFilter(new Filter<FeedItem>() {
    //
    // @Override
    // public List<FeedItem> doFilte(List<FeedItem> originItems) {
    //
    // Iterator<FeedItem> feedIterator = originItems.iterator();
    // while (feedIterator.hasNext()) {
    // FeedItem feedItem = feedIterator.next();
    // boolean exist = feedItem.topics.contains(mTopic);
    // if (!exist) { // 如果没有找到,那么删除该项
    // feedIterator.remove();
    // }
    // }
    // return originItems;
    // }
    // });
    // }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == ResFinder.getId("umeng_comm_title_back_btn")) {
            finish();
        }
        // finish();
    }

}
