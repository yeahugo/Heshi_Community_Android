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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.db.DbHelperFactory;
import com.umeng.comm.core.db.cmd.QueryCommand;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.TopicResponse;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.adapters.TopicPickerAdater;
import com.umeng.comm.ui.adapters.viewparser.FriendItemViewParser.ImgTextViewHolder;
import com.umeng.comm.ui.widgets.RefreshLayout.OnLoadListener;
import com.umeng.comm.ui.widgets.RefreshLvLayout;

/**
 * 用户发布feed时的话题选择fragment. TODO 此页面是有需要使用缓存数据
 * 
 * @author mrsimple
 */
public class TopicPickerFragment extends FontFragment {

    /**
     * 下来刷新布局
     */
    RefreshLvLayout mRefreshLvLayout;
    /**
     * 显示选择话题的ListView
     */
    private ListView mTopicListView;
    /**
     * 选择话题的适配器
     */
    private TopicPickerAdater mAdapter;
    /**
     * 已经选择的话题
     */
    private List<Topic> mSelectedTopics = new ArrayList<Topic>();
    /**
     * 话题被选中后，点击确认的回调
     */
    private ResultListener<Topic> mTopicListener;

    private String mNextPageUrl = "";
    
    private boolean hasRefersh = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        int layout = ResFinder.getLayout("umeng_comm_topic_select");
        int refreshResId = ResFinder.getId("umeng_comm_topic_lv_layout");
        int topicListViewResId = ResFinder.getId("umeng_comm_topic_listview");
        View rootView = inflater.inflate(layout, container, false);

        mRefreshLvLayout = (RefreshLvLayout) rootView.findViewById(refreshResId);
        mRefreshLvLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                loadTopicFromServer();
            }
        });

        mRefreshLvLayout.setOnLoadListener(new OnLoadListener() {
            @Override
            public void onLoad() {
                loadMore();
            }
        });

        //
        mTopicListView = (ListView) rootView.findViewById(topicListViewResId);
        mTopicListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 对于原来的值取反
                setItemSelected(view, position);
            }

        });
        loadTopicFromDB();
        loadTopicFromServer();
        mSelectedTopics.clear();
        mAdapter = new TopicPickerAdater(getActivity(), new LinkedList<Topic>());

        // mTopicListView.setAdapter(mAdapter);
        mRefreshLvLayout.setAdapter(mAdapter);
        return rootView;
    }

    /**
     * 从数据库中获取话题</br>
     */
    private void loadTopicFromDB() {

        QueryCommand<Topic> queryCommand = new QueryCommand<Topic>(
                DbHelperFactory.getTopicDbHelper(getActivity()), null, null);
        queryCommand.setFetchListener(new SimpleFetchListener<List<Topic>>() {

            @Override
            public void onComplete(List<Topic> result) {
                mAdapter.addData(result);
            }
        });
        queryCommand.execute();
    }

    /**
     * 目前策略是每次都从server端加载数据</br>
     * 
     * @param start
     */
    private void loadTopicFromServer() {

        mSdkImpl.fetchTopics(new FetchListener<TopicResponse>() {

            @Override
            public void onStart() {
                mRefreshLvLayout.setRefreshing(true);
            }

            @Override
            public void onComplete(TopicResponse response) {
                mRefreshLvLayout.setRefreshing(false);
                mRefreshLvLayout.setLoading(false);

                // 根据response进行Toast
                if (handlerResponse(response)) {
                    return;
                }

                handleTopicResultSet(response.result, true);
            }
        });
    }

    /**
     * 根据url加载更多话题</br>
     */
    private void loadMore() {
        List<Topic> topics = mAdapter.getDataSource();
        if (topics.size() <= 0) {
            mRefreshLvLayout.setLoading(false);
            return;
        }
        if (TextUtils.isEmpty(mNextPageUrl)) {
            mRefreshLvLayout.setLoading(false);
            return;
        }
        mSdkImpl.fetchNextPageData(mNextPageUrl, TopicResponse.class,
                new FetchListener<TopicResponse>() {

                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onComplete(TopicResponse response) {
                        mRefreshLvLayout.setLoading(false);
                        // 根据response进行Toast
                        if (handlerResponse(response)) {
                            return;
                        }

                        List<Topic> topics = response.result;
                        handleTopicResultSet(topics, false);
                    }
                });
    }

    /**
     * 根据加载的话题做不同的处理跟toast显示</br>
     * 
     * @param topics
     */
    private void handleTopicResultSet(List<Topic> topics, boolean fromRefersh) {
        if (topics == null) {
            ToastMsg.showShortMsgByResName(getActivity(), "umeng_comm_load_topic_failed");
        } else if (topics.size() > 0) {
            // 移除重复的Topic
            mAdapter.getDataSource().removeAll(topics);
            // topics.removeAll(mAdapter.getDataSource());
            // 更新成功,刷新话题列表
            if (fromRefersh) {
                mAdapter.addToFirst(topics);
            } else {
                mAdapter.addData(topics);
            }
            // 避免上拉几次后再刷新，此时下一地址变化
            parseNextpageUrl(topics, fromRefersh);
        } else {
            ToastMsg.showShortMsgByResName(getActivity(), "umeng_comm_not_have_more");
        }
    }
    
    private void parseNextpageUrl(List<Topic> topics,boolean fromRefersh){
        if ( topics == null || topics.size() == 0 ) {
            return ;
        }
        if ( fromRefersh && TextUtils.isEmpty(mNextPageUrl) && !hasRefersh) {
            hasRefersh = true;
            mNextPageUrl = topics.get(0).nextPage;
        } else if ( !fromRefersh ) {
            mNextPageUrl = topics.get(topics.size()-1).nextPage;
        }
    }

    /**
     * 某个话题被选中，执行回调，更新TextView的显示
     * 
     * @param isSelected
     */
    private void setItemSelected(View itemView, int position) {
        ImgTextViewHolder viewHolder = (ImgTextViewHolder) itemView.getTag();
        if (viewHolder == null) {
            return;
        }

        Topic topicItem = mAdapter.getItem(position);
        // 对上一次是否含有该项进行取反,即原来没有选中的,那么点击该项以后就变为选选中状态了.
        boolean isChecked = !mSelectedTopics.contains(topicItem);
        // viewHolder.mCheckBox.setChecked(isChecked);
        if (isChecked) {
            mSelectedTopics.add(topicItem);
            mTopicListener.onAdd(topicItem);
        } else {
            mSelectedTopics.remove(topicItem);
            mTopicListener.onRemove(topicItem);
        }

    }

    /**
     * 取消选中的话题
     * 
     * @param topic
     */
    public void uncheckTopic(Topic topic) {
        Iterator<Topic> iterator = mSelectedTopics.iterator();
        while (iterator.hasNext()) {
            Topic item = iterator.next();
            if (item.equals(topic)) {
                item.isFocused = false;
                iterator.remove();
                mAdapter.notifyDataSetChanged();
                break;
            }

        }
    }

    /**
     * 设置话题被选择后的回调（点击确认按钮执行该回调）
     * 
     * @param listener
     */
    public void addTopicListener(ResultListener<Topic> listener) {
        mTopicListener = listener;
    }

    /**
     * @author mrsimple
     */
    public static interface ResultListener<T> {
        /**
         * @param topic
         */
        public void onAdd(T t);

        /**
         * @param topic
         */
        public void onRemove(T t);
    }

}
