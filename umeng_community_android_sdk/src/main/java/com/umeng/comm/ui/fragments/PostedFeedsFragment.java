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

import android.view.View;

import com.umeng.comm.core.beans.BaseBean;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.FeedsResponse;
import com.umeng.comm.ui.utils.Filter;

import java.util.Iterator;
import java.util.List;

/**
 * 用户已经已经发布的feed页面
 * 
 * @author mrsimple
 */
public class PostedFeedsFragment extends BaseFeedsFragment {

    // private OnClickListener mClickListener;
    /**
     * Feed 删除监听器,删除页面时回调给个人信息页面使得feed数量减1 [ TODO : 考虑是否和转发效果一样,使用广播 ]
     */
    private OnDeleteListener mDeleteListener;

    public PostedFeedsFragment() {
        setRetainInstance(true);
    }

    @Override
    protected void initViews() {
        super.initViews();
        mPostBtn.setVisibility(View.GONE);
    }

    @Override
    protected void showPostButtonWithAnim() {

    }

    @Override
    public void initAdapter() {
        // 设置只显示当前用户创建的feeds,过滤掉其他用户的feed
        setFeedFilter(new Filter<FeedItem>() {

            @Override
            public List<FeedItem> doFilte(List<FeedItem> originItems) {
                Iterator<FeedItem> myIterator = originItems.iterator();
                while (myIterator.hasNext()) {
                    final FeedItem feedItem = myIterator.next();
                    // id等于当前用户的id或者列表中已经包含该feed,那么移除该feed
                    if (!isMyFeed(feedItem)
                            || mFeedLvAdapter.getDataSource().contains(feedItem)) {
                        myIterator.remove();
                    }
                }
                return originItems;
            }
        });

        super.initAdapter();
    }

    @Override
    protected boolean isMyPage(FeedItem feedItem) {
        if (feedItem == null
                || !feedItem.creator.id.equals(mUser.id)) {
            return false;
        }

        return true;
    }

    // @Override
    // protected boolean isMyFeed(FeedItem feedItem) {
    // if (mUser == null || TextUtils.isEmpty(mUser.id)) {
    // return false;
    // }
    // return feedItem.creator.id.equals(mUser.id);
    // }

    @Override
    protected void addNewFeed(FeedItem feedItem) {
        if (isMyFeed(feedItem)) {
            super.addNewFeed(feedItem);
        }
    }

    /*
     * 获取我已经发表的feeds列表
     * @see com.umeng.community.views.fragments.MsgFlowFragment#fetchFeeds()
     */
    @Override
    protected void fetchFeeds() {
        mSdkImpl.fetchUserTimeLine(mUser.id,
                new SimpleFetchListener<FeedsResponse>() {

                    @Override
                    public void onComplete(FeedsResponse response) {
                        mRefreshLayout.setRefreshing(false);

                        // 根据response进行Toast
                        if (handlerResponse(response)) {
                            return;
                        }

                        // 解析下一页地址
                        parseNextpageUrl(response.result, true);
                        // 更新数据
                        addFeedItemsToHeader(response.result);
                        // 保存加载的数据,如果该数据存在于DB中，则替换成最新的，否则Insert一条新纪录
                        saveFeedsToDB(response.result);
                    }
                });
    }

    /**
     * 设置评论按钮的点击事件</br>
     * 
     * @param listener
     */
    // public void setOnClickListener(OnClickListener listener) {
    // this.mClickListener = listener;
    // }

    // @Override
    // protected void clickCommentListItem(FeedItemViewHolder viewHolder, int
    // position) {
    // mClickListener.onClick(null);
    // }

    /**
     * @param user
     */
    public void setCurrentUser(CommUser user) {
        mUser = user;
    }

    protected void updateAfterDelete(FeedItem feedItem) {
        super.updateAfterDelete(feedItem);
        if (mDeleteListener != null) {
            mDeleteListener.onDelete(feedItem);
        }
    };

    public void setOnDeleteListener(OnDeleteListener listener) {
        mDeleteListener = listener;
    }

    /**
     * 删除监听器
     * 
     * @author mrsimple
     */
    public static interface OnDeleteListener {
        public void onDelete(BaseBean item);
    }
}
