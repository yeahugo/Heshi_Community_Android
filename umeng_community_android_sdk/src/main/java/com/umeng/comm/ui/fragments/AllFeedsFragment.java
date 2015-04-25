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

import android.R.bool;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;

import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.db.DbHelper;
import com.umeng.comm.core.db.DbHelperFactory;
import com.umeng.comm.core.db.cmd.DeleteAllCommand;
import com.umeng.comm.core.db.cmd.QueryCommand;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.FeedsResponse;
import com.umeng.comm.core.nets.responses.LoginResponse;
import com.umeng.comm.core.nets.responses.TopicResponse;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.activities.PostFeedActivity;

import java.util.List;

/**
 * 消息流主页
 * 
 * @author mrsimple
 */
public class AllFeedsFragment extends BaseFeedsFragment implements OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        registerBroadcast();
        CommonUtils.saveComponentImpl(getActivity());// 注意此处必须保存登录组件的信息
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void initViews() {
        super.initViews();
               
        mPostBtn.setOnClickListener(this);
        mLocationBtn.setOnClickListener(this);
    }

    @Override
    protected void fetchFeeds() {
        mSdkImpl.fetchLastestFeeds(new SimpleFetchListener<FeedsResponse>() {

            @Override
            public void onComplete(FeedsResponse response) {

                mRefreshLayout.setRefreshing(false);
                final Activity ownerAct = getActivity();
                final List<FeedItem> newFeedItems = response.result;

                // 根据response进行Toast
                if (handlerResponse(response)) {
                    return;
                }

                if (CommonUtils.isActivityAlive(ownerAct)
                        && response.errCode == 0) {
                    parseNextpageUrl(newFeedItems, true);
                    // 第一次从网络上下拉到数据,那么则清除从数据库中加载进来的数据,避免下一页地址出问题.
                    clearDbCache(DbHelperFactory.getFeedDbHelper(ownerAct));
                    // 更新数据
                    addFeedItemsToHeader(newFeedItems);
                    // 保存加载的数据。如果该数据存在于DB中，则替换成最新的，否则Insert一条新纪录
                    saveFeedsToDB(response.result);
                }
            }
        });
    }

    /**
     * 第一次从网上下拉到数据时清空数据库中的缓存,然后会调用{@see saveFeedsToDB}将新的数据存储到缓存中
     * 
     * @param helper
     */
    private void clearDbCache(DbHelper<FeedItem> helper) {
        if (isNeedRemoveOldFeeds.get()) {
            // 清空原来的缓存数据
            mFeedLvAdapter.getDataSource().clear();
            // 清空数据库中的缓存数据
            DeleteAllCommand<FeedItem> deleteAllCommand = new DeleteAllCommand<FeedItem>(helper);
            deleteAllCommand.executeSync();
            isNeedRemoveOldFeeds.set(false);
        }
    }

    @Override
    protected void showPostButtonWithAnim() {
        AlphaAnimation showAnim = new AlphaAnimation(0.5f, 1.0f);
        showAnim.setDuration(500);

        if (mPostBtn != null) {
            mPostBtn.setVisibility(View.VISIBLE);
            mPostBtn.startAnimation(showAnim);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == ResFinder.getId("umeng_comm_new_post_btn")) {// 点击写新鲜事按钮
            showPostFeedActivity();
        }
        
        if(v.getId() == ResFinder.getId("umeng_comm_new_location_btn")){
        	showPostLocationActivity();
        }
    }

    /**
     * 根据登录状态做相应的操作。已登录则直接执行，否则登录成功后再执行回调</br>
     */
    private void showPostFeedActivity() {
        CommonUtils.checkLoginAndFireCallback(getActivity(),
                new SimpleFetchListener<LoginResponse>() {

                    @Override
                    public void onComplete(LoginResponse response) {
                        if (response.errCode == Constants.NO_ERROR) {
                            gotoPostFeedActivity();
                        } else {
                            ToastMsg.showShortMsgByResName(getActivity(),
                                    "umeng_comm_login_failed");
                        }
                    }
                });
    }

    /**
     * 跳转至编辑签到页面</br>
     */
    private void showPostLocationActivity() {
//    	gotoPostLocationActivity();
    	
    	CommonUtils.checkLoginAndFireCallback(getActivity(),
                new SimpleFetchListener<LoginResponse>() {

                    @Override
                    public void onComplete(LoginResponse response) {
                        if (response.errCode == Constants.NO_ERROR) {
                        	gotoPostLocationActivity();
                        } else {
                            ToastMsg.showShortMsgByResName(getActivity(),
                                    "umeng_comm_login_failed");
                        }
                    }
                });
    	
    }
    
    
    private void gotoPostLocationActivity(){
    	    	
    	QueryCommand<Topic> queryCommand = new QueryCommand<Topic>(
                DbHelperFactory.getTopicDbHelper(getActivity()), null, null);
        queryCommand.setFetchListener(new SimpleFetchListener<List<Topic>>() {

            @Override
            public void onComplete(List<Topic> result) {
            	
            	Intent postFeedIntent = new Intent(getActivity(),
		                PostFeedActivity.class);
            	
                if(!this.handleTopics(result)){
                    mSdkImpl.fetchTopics(new FetchListener<TopicResponse>() {
                        @Override
                        public void onComplete(TopicResponse response) {
                        	handleTopics(response.result);
                        }

    					@Override
    					public void onStart() {
    					}
                    });	
                }                
            }

			private boolean handleTopics(List<Topic> result) {
		    	Intent postFeedIntent = new Intent(getActivity(),
		                PostFeedActivity.class);
		    	for(int i = 0;i<result.size();i++){
		        	Topic topic = result.get(i);
		        	
		        	//找到签到话题id
		        	if(topic.id.toString().equals("553666917019c956f60b9f59")){
		        		postFeedIntent.putExtra("location", topic);
		        		getActivity().startActivity(postFeedIntent);
		        		return true;
		        	}
		        }
		    	return false;
			}
        });
        queryCommand.execute();    	
    }
    
    /**
     * 跳转至发送新鲜事页面</br>
     */
    private void gotoPostFeedActivity() {
        Intent postIntent = new Intent(getActivity(), PostFeedActivity.class);
        startActivity(postIntent);  	
    }
}
