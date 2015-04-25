/**
 * 
 */

package com.umeng.comm.ui.fragments;

import java.util.List;

import android.text.TextUtils;
import android.view.View;

import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.db.DbHelper;
import com.umeng.comm.core.db.DbHelperFactory;
import com.umeng.comm.core.db.cmd.InsertCommand;
import com.umeng.comm.core.db.cmd.QueryCommand;
import com.umeng.comm.core.db.cmd.concrete.DbCommandFactory;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.FeedsResponse;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.DeviceUtils;
import com.umeng.comm.core.utils.Log;
import com.umeng.comm.core.utils.ToastMsg;

/**
 * feed推荐页面
 */
public class RecommendFeedFragment extends BaseFeedsFragment {

    @Override
    protected void initViews() {
        super.initViews();
        mPostBtn.setVisibility(View.GONE);
    }

    @Override
    protected void fetchFeeds() {
        mRefreshLayout.setRefreshing(true);
        mSdkImpl.fetchRecommendedFeeds(new SimpleFetchListener<FeedsResponse>() {

            @Override
            public void onComplete(FeedsResponse response) {
                mRefreshLayout.setRefreshing(false);
                // 根据response进行Toast
                if (handlerResponse(response)) {
                    return;
                }
                mFeedLvAdapter.getDataSource().clear();
                mFeedLvAdapter.notifyDataSetChanged();
                addFeedItemsToHeader(response.result);
                saveFeedsToDB(response.result);
            }
        });
    }

    @Override
    protected void executeLoadFeedsCommand() {
        // 构建命令,查询缓存在数据库中的feed
        QueryCommand<FeedItem> feedQueryCommand = DbCommandFactory
                .createQueryRecommendFeedCmd(getActivity(), mUser.id,
                        mFeedLvAdapter.getCount());
        // 没查询到一个数据则追加到ListView中,提升响应速度
        feedQueryCommand.setOnItemFetchedListener(new
                SimpleFetchListener<FeedItem>() {
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
        feedQueryCommand.setFetchListener(new
                SimpleFetchListener<List<FeedItem>>() {

                    @Override
                    public void onComplete(List<FeedItem> response) {
                        mRefreshLayout.setLoading(false);
                    }
                });
        feedQueryCommand.execute();
    }

    @Override
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

    @Override
    protected void saveFeedsToDB(List<FeedItem> newFeedItems) {
        DbHelper<FeedItem> helper = DbHelperFactory.getRecommendFeedDbHelper(getActivity());
        InsertCommand<FeedItem> insertCommand = new InsertCommand<FeedItem>(helper, newFeedItems);
        insertCommand.execute();
    }

    public void cleanAdapterData() {
        mFeedLvAdapter.getDataSource().clear();
        mFeedLvAdapter.notifyDataSetChanged();
    }
    
    /**
     * 推荐页面不接受新发布的feed。【不可删除】
     */
    @Override
    protected void registerPostNotifyReceiver() {
    }
}
