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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;

import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.FeedsResponse;
import com.umeng.comm.core.nets.responses.LoginResponse;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.activities.PostFeedActivity;
import com.umeng.comm.ui.utils.Filter;

/**
 * 某个话题的所有feed页面,即话题的详情页面.
 * 
 * @author mrsimple
 */
public class TopicFeedFragment extends BaseFeedsFragment {

    private Topic mTopic;

    /**
     * 先创建一个TopicFeedFragment对象
     * 
     * @param topicId
     * @return
     */
    public static TopicFeedFragment newTopicFeedFrmg(final Topic topic) {
        TopicFeedFragment topicFeedFragment = new TopicFeedFragment();
        topicFeedFragment.mTopic = topic;
        topicFeedFragment.mFeedFilter = new Filter<FeedItem>() {

            @Override
            public List<FeedItem> doFilte(List<FeedItem> newItems) {
                if ( newItems == null || newItems.size() == 0 ) {
                    return newItems;
                }
                Iterator<FeedItem> iterator = newItems.iterator();
                while (iterator.hasNext()) {
                    List<Topic> topics = iterator.next().topics;
                    if ( !topics.contains(topic) ) {
                        iterator.remove();
                    }
                }
                return newItems;
            }
        };
        return topicFeedFragment;
    }

    @Override
    public void initAdapter() {
        // 添加header
        // initHeader();
        super.initAdapter();
    }

    @Override
    protected void showPostButtonWithAnim() {
        AlphaAnimation showAnim = new AlphaAnimation(0.5f, 1.0f);
        showAnim.setDuration(500);

        mPostBtn.setVisibility(View.VISIBLE);
        mPostBtn.startAnimation(showAnim);
    }

    @Override
    protected void initViews() {
        super.initViews();
        mPostBtn.setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        CommonUtils.checkLoginAndFireCallback(getActivity(),
                                new SimpleFetchListener<LoginResponse>() {

                                    @Override
                                    public void onComplete(LoginResponse response) {
                                        if (response.errCode == Constants.NO_ERROR) {
                                            Intent postFeedIntent = new Intent(getActivity(),
                                                    PostFeedActivity.class);
                                            postFeedIntent.putExtra(Constants.TAG_TOPIC, mTopic);
                                            getActivity().startActivity(postFeedIntent);
                                        } else {
                                            ToastMsg.showShortMsgByResName(getActivity(),
                                                    "umeng_comm_login_failed");
                                        }
                                    }
                                });
                    }
                });
        // 设置分割线不可见
        // mRootView.findViewById(ResFinder.getId("umeng_comm_feeds_frmg_divider")).setVisibility(View.GONE);
    }

    @Override
    protected void loadMoreFeed() {
        if (TextUtils.isEmpty(mNextPageUrl)) {
            mRefreshLayout.setLoading(false);
            return;
        }
        mSdkImpl.fetchNextPageData(mNextPageUrl,
                FeedsResponse.class, new FetchListener<FeedsResponse>() {

                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onComplete(FeedsResponse response) {
                        mRefreshLayout.setLoading(false);

                        // 根据response进行Toast
                        if (handlerResponse(response)) {
                            return;
                        }
                        parseNextpageUrl(response.result, false);
                        updateFeeds(response.result);
                        parseNextpageUrl(response.result, false);
                    }
                });
    }

    @Override
    protected void fetchFeeds() {
        mSdkImpl.fetchTopicFeed(mTopic.id,
                new FetchListener<FeedsResponse>() {

                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onComplete(FeedsResponse response) {
                        mRefreshLayout.setRefreshing(false);

                        // 根据response进行Toast
                        if (handlerResponse(response)) {
                            return;
                        }

//                        handlerResponse(response);
                        updateFeeds(response.result);
                        parseNextpageUrl(response.result, true);
                        saveFeedsToDB(response.result);
                    }
                });
    }

    
//    private  Filter<FeedItem> mTopicFeedFilter = new Filter<FeedItem>() {
//
//        @Override
//        public List<FeedItem> doFilte(List<FeedItem> newItems) {
//            if ( newItems == null || newItems.size() == 0 ) {
//                return newItems;
//            }
//            Iterator<FeedItem> iterator = newItems.iterator();
//            while (iterator.hasNext()) {
//                List<Topic> topics = iterator.next().topics;
//                if ( !topics.contains(mTopic) ) {
//                    iterator.remove();
//                }
//            }
//            return newItems;
//        }
//    }; 
    
    /**
     * 更新Feed。首先去重处理，然后在更新ListView</br>
     * 
     * @param feedItems
     */
    private void updateFeeds(List<FeedItem> feedItems) {

        feedItems = filteFeeds(feedItems);
        // TODO :

        List<FeedItem> items = mFeedLvAdapter.getDataSource();
        items.removeAll(feedItems);

        items.addAll(feedItems);
        Collections.sort(items, mComparator);
        mFeedLvAdapter.notifyDataSetChanged();
        mFeedsListView.invalidate();
    }
}
