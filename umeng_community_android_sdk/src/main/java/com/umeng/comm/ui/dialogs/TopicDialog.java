/**
 * 
 */

package com.umeng.comm.ui.dialogs;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.db.DbHelperFactory;
import com.umeng.comm.core.db.cmd.QueryCommand;
import com.umeng.comm.core.db.cmd.RelativeCommand;
import com.umeng.comm.core.db.cmd.concrete.DbCommandFactory;
import com.umeng.comm.core.impl.CommunityFactory;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.TopicResponse;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.Log;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.adapters.TopicAdapter;
import com.umeng.comm.ui.adapters.TopicAdapter.FollowListener;
import com.umeng.comm.ui.utils.FontUtils;

/**
 * 
 */
public class TopicDialog extends RecommendTopicDialog implements android.view.View.OnClickListener {

    private EditText mSearchEdit;
    private SearchTask mSearchTask = new SearchTask();

    private static final int CHECK_RESULT = 0x01;
    private static final int DELAY = 100;

    private boolean mIsBackup = false;
    private boolean hasRefersh = false;

    private String mNextPageUrl = "";
    private InputMethodManager mInputMan;

    // 检测消息是否加载完成，防止block
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what != CHECK_RESULT) {
                return;
            }
            boolean done = mSearchTask.isDone();
            if (!done) {
                mHandler.sendEmptyMessageDelayed(CHECK_RESULT, DELAY);
            } else {
                mSearchTask.updateResult();
            }
        }
    };
    
    // umeng_comm_dialog_fullscreen
    public TopicDialog(Activity activity, int theme) {
        super(activity, theme);
        mInputMan = (InputMethodManager) mActivity.getSystemService(
                Context.INPUT_METHOD_SERVICE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initDatas();
    }

    @Override
    protected void initView() {
        int layoutResId = ResFinder.getLayout("umeng_comm_topic_search");
        View rootView = LayoutInflater.from(mActivity.getApplicationContext()).inflate(layoutResId,
                null);
        setContentView(rootView);
        FontUtils.changeTypeface(rootView);
        initRefreshView();
        initSearchView();
        initTitleView();
    }

    @Override
    protected void initDatas() {
        loadTopicsFromDB();
//        super.initDatas();
        loadTopics();
    }

    @Override
    protected void initTitleView() {
        int searchButtonResId = ResFinder.getId("umeng_comm_topic_search");
        int backButtonResId = ResFinder.getId("umeng_comm_back");

        findViewById(backButtonResId).setOnClickListener(this);
        findViewById(searchButtonResId).setOnClickListener(this);
    }
    
    @Override
    protected void loadTopics() {
        mSdkImpl.fetchTopics(new FetchListener<TopicResponse>() {

            @Override
            public void onStart() {
                mRefreshLvLayout.setRefreshing(true);
            }

            @Override
            public void onComplete(final TopicResponse response) {
//                mRefreshLvLayout.setLoading(false);
                mRefreshLvLayout.setRefreshing(false);

                // 根据response进行Toast
                if (handlerResponse(response)) {
                    return;
                }

                final List<Topic> results = response.result;
                updateNextPageUrl(results.get(0).nextPage);
                Log.d("TopicDialog",
                        "### 话题数量 : " + response.result.size() + ", 下一页 : "
                                + results.get(0).nextPage);
                fetchTopicComplete(results, true);
            }
        });
    }

    @Override
    protected void initAdapter() {
        mAdapter = new TopicAdapter(mActivity, new ArrayList<Topic>());
        ((TopicAdapter) mAdapter).setFollowListener(new FollowListener<Topic>() {

            @Override
            public void onFollowOrUnFollow(Topic topic, ToggleButton toggleButton,
                    boolean isFollow) {
                if (isFollow) {
                    followTopic(topic, toggleButton);
                } else {
                    cancelFollowTopic(topic, toggleButton);
                }
            }
        });
        mTopicListView.setAdapter(mAdapter);
    }

    /**
     * 初始化搜索话题View跟事件处理</br>
     * 
     * @param rootView
     */
    private void initSearchView() {
        int searchEditResId = ResFinder.getId("umeng_comm_topic_edittext");
        mSearchEdit = (EditText) findViewById(searchEditResId);
        mSearchEdit.setOnKeyListener(new android.view.View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    mSearchTask.execute(mSearchEdit.getText().toString().trim());
                }
                return false;
            }
        });

        // 话题本地搜索
        mSearchEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (!TextUtils.isEmpty(s) && mTopics.size() > 0) {
                    // 如果keyword不为空，做本地搜索
                    List<Topic> result = localSearchTopic(s.toString());
                    mAdapter.updateListViewData(result);
                    if (!mIsBackup) {
                        mIsBackup = true;
                        mAdapter.backupData();
                    }
                } else {
                    // 显示本地所有的话题
                    mAdapter.restoreData();
                    mIsBackup = false;
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * 根据关键字从本地搜索话题</br>
     * 
     * @param keyword 话题关键字
     * @return 能够匹配keyword的话题列表
     */
    private List<Topic> localSearchTopic(String keyword) {
        List<Topic> resultList = new ArrayList<Topic>();
        final List<Topic> topics = mTopics;
        String name = null;
        for (Topic topic : topics) {
            name = topic.name;
            if (!TextUtils.isEmpty(name) && name.contains(keyword)) {
                resultList.add(topic);
            }
        }
        return resultList;
    }

    /**
     * 从数据库中获取所有话题,再从话题-用户关系表中获取用户已经关注的话题,并且将已经关注的话题放在最前面</br>
     */
    private void loadTopicsFromDB() {

        final List<Topic> cacheTopics = new LinkedList<Topic>();
        // 查询所有话题
        QueryCommand<Topic> queryCommand = new QueryCommand<Topic>(
                DbHelperFactory.getTopicDbHelper(mActivity), null, null);
        queryCommand.setFetchListener(new SimpleFetchListener<List<Topic>>() {

            @Override
            public void onComplete(List<Topic> result) {
                // 将数据添加到列表中
                cacheTopics.addAll(result);

                // 获取用户已经关注的话题
                RelativeCommand<Topic> followedCommand = DbCommandFactory.createFollowedTopicCmd(
                        mActivity, CommConfig.getConfig().loginedUser.id);
                followedCommand.setFetchListener(new SimpleFetchListener<List<Topic>>() {

                    @Override
                    public void onComplete(List<Topic> followedTopics) {
                        if (CommonUtils.isActivityAlive(mActivity)) {

                            List<Topic> newTopics = setFollowedTag(followedTopics);
                            // 将已经关注的话题插入到最前面
                            cacheTopics.removeAll(newTopics);
                            cacheTopics.addAll(0, newTopics);
                            // 更新listview数据
                            mAdapter.addData(cacheTopics);
                            mTopics.addAll(cacheTopics);
                        }
                    }
                });
                followedCommand.execute();
            }
        });
        queryCommand.execute();

    }

    /**
     * 将已经关注的Topic设置标识,数据库中没有保存该字段
     * 
     * @param results
     * @return
     */
    private List<Topic> setFollowedTag(List<Topic> results) {
        // 将过滤后的数据添加到listview中,这些为用户已经关注的话题列表
        List<Topic> newTopics = filterTopics(results);
        for (Topic topic : newTopics) {
            topic.isFocused = true;
        }

        return newTopics;
    }

    /**
     * 移除重复的话题</br>
     * 
     * @param dest 目标话题列表。
     * @return
     */
    private List<Topic> filterTopics(List<Topic> dest) {
        List<Topic> src = mAdapter.getDataSource();
        src.removeAll(dest);
        return dest;
    }

    /**
     * 根据url加载更多的话题</br>
     */
    protected void loadMoreTopic() {
        final List<Topic> datas = mAdapter.getDataSource();
        if (datas == null || datas.size() <= 0) {
            mRefreshLvLayout.setLoading(false);
            return;
        }
        String url = datas.get(datas.size() - 1).nextPage;
        if (TextUtils.isEmpty(url)) {
            mRefreshLvLayout.setLoading(false);
            ToastMsg.showShortMsgByResName(mActivity, "umeng_comm_load_complete");
            return;
        }
        Log.d("TopicDialog", "加载更多下一页 : " + url);
        //
        mSdkImpl.fetchNextPageData(url, TopicResponse.class,
                new SimpleFetchListener<TopicResponse>() {

                    @Override
                    public void onComplete(TopicResponse response) {
                        mRefreshLvLayout.setLoading(false);

                        // 根据response进行Toast
                        if (handlerResponse(response)) {
                            return;
                        }

                        fetchTopicComplete(response.result, false);
                    }
                });
    }

    protected void parseNextpageUrl(List<Topic> topics, boolean fromRefersh) {
        if (topics == null || topics.size() == 0) {
            return;
        }
        if (fromRefersh && TextUtils.isEmpty(mNextPageUrl) && !hasRefersh) {
            hasRefersh = true;
            mNextPageUrl = topics.get(0).nextPage;
        } else if (!fromRefersh) {
            mNextPageUrl = topics.get(topics.size() - 1).nextPage;
        }
    }

    /**
     * 话题搜索处理类
     */
    private class SearchTask {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future<List<Topic>> future = null;

        /**
         * 请求根据关键字搜索话题。对于对个请求，总是以最新的请求为准</br>
         * 
         * @param keyword 话题的关键字
         */
        public void execute(final String keyword) {
            if (TextUtils.isEmpty(keyword)) {
                ToastMsg.showShortMsgByResName(mActivity, "umeng_comm_search_keyword_input");
                return;
            }
            // 如果本次搜索未完成，直接取消，搜索新的话题
            cancelTask();
            Callable<List<Topic>> callable = new Callable<List<Topic>>() {

                @Override
                public List<Topic> call() throws Exception {
                    return searchTopic(keyword);
                }
            };
            future = executorService.submit(callable);
            mHandler.sendEmptyMessageDelayed(CHECK_RESULT, DELAY);
        }

        // 取消未完成的搜索任务
        void cancelTask() {
            if (future != null && !future.isDone()) {
                future.cancel(true);
                mHandler.removeMessages(CHECK_RESULT);
            }
        }

        // 检查该搜索任务是否完成
        boolean isDone() {
            if (future != null) {
                return future.isDone();
            }
            return true;
        }

        // 获取搜索结果并更新listView
        void updateResult() {
            try {
                List<Topic> topics = future.get();
                if (topics != null && topics.size() > 0) {
                    updateTopicFocusable(topics);
                    mAdapter.updateListViewData(topics);
                    // mAdapter.addData(topics);
                } else if (topics.size() == 0) {
                    ToastMsg.showShortMsgByResName(mActivity, "umeng_comm_search_topic_failed");
                } else {
                    ToastMsg.showShortMsgByResName(mActivity, "umeng_comm_search_topic_failed");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 更新搜索到的话题的isFocus字段。由于Server比较难处理是否关注（数据量大），
     * 获取话题时不返回是否关注字段。目前暂时跟本地话题对比的方式</br>
     * 
     * @param newTopics
     */
    private void updateTopicFocusable(List<Topic> newTopics) {
        if (mTopics.size() == 0) {
            return;
        }

        int len = mTopics.size();
        Topic topic = null;
        for (int i = 0; i < len; i++) {
            topic = mTopics.get(i);
            if (newTopics.contains(topic)) {
                int index = newTopics.indexOf(topic);
                newTopics.get(index).isFocused = topic.isFocused;
            }
        }
    }

    /**
     * 搜索话题的请求</br>
     * 
     * @param keyword 搜索话题的关键字
     * @return
     */
    private List<Topic> searchTopic(String keyword) {
        return CommunityFactory.getCommSDK(mActivity).searchTopicSync(keyword);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        int backButtonResId = ResFinder.getId("umeng_comm_back");
        int searchButtonResId = ResFinder.getId("umeng_comm_topic_search");
        if (id == backButtonResId) {
            mSearchTask.cancelTask();
            // BaseFragmentActivity activity = (BaseFragmentActivity) mActivity;
            // activity.hideInputMethod(mSearchEdit);
            // activity.showMainFeedFragment();
            mInputMan.hideSoftInputFromWindow(mSearchEdit.getWindowToken(), 0);
            dismiss();
        } else if (id == searchButtonResId) {
            mSearchTask.execute(mSearchEdit.getText().toString().trim());
        }
    }

    // @Override
    // public void onDestroy() {
    // mHandler.removeCallbacks(null);
    // if (mBroadcastReceiver != null) {
    // mActivity.unregisterReceiver(mBroadcastReceiver);
    // }
    // super.onDestroy();
    // }

    @Override
    public void dismiss() {
        mHandler.removeCallbacks(null);
        super.dismiss();
    }
    //
    // protected boolean handlerResponse(AbsResponse<?> response) {
    // return NetworkUtils.handlerResponse(mActivity, response);
    // }

}
