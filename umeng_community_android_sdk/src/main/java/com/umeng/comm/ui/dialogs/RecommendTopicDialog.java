/**
 * 
 */

package com.umeng.comm.ui.dialogs;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.umeng.comm.core.CommunitySDK;
import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.db.DbHelper;
import com.umeng.comm.core.db.DbHelperFactory;
import com.umeng.comm.core.db.RelationDBHelper.RelativeKeyPair;
import com.umeng.comm.core.db.cmd.DeleteCommand;
import com.umeng.comm.core.db.cmd.InsertCommand;
import com.umeng.comm.core.db.cmd.concrete.DbCommandFactory;
import com.umeng.comm.core.impl.CommunityFactory;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.Response;
import com.umeng.comm.core.nets.responses.AbsResponse;
import com.umeng.comm.core.nets.responses.TopicResponse;
import com.umeng.comm.core.nets.uitls.NetworkUtils;
import com.umeng.comm.core.utils.Log;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.adapters.BackupAdapter;
import com.umeng.comm.ui.adapters.RecommendTopicAdapter;
import com.umeng.comm.ui.adapters.TopicAdapter.FollowListener;
import com.umeng.comm.ui.broadcastreceiver.NotifyBroadcastReceiver;
import com.umeng.comm.ui.broadcastreceiver.NotifyBroadcastReceiver.NotifyListener;
import com.umeng.comm.ui.utils.FontUtils;
import com.umeng.comm.ui.widgets.RefreshLayout.OnLoadListener;
import com.umeng.comm.ui.widgets.RefreshLvLayout;

/**
 * 
 */
public class RecommendTopicDialog extends Dialog implements android.view.View.OnClickListener {

    // private EditText mSearchEdit;
    protected BackupAdapter<Topic, ?> mAdapter;
    protected ListView mTopicListView;
    // private SearchTask mSearchTask = new SearchTask();
    protected List<Topic> mTopics = new ArrayList<Topic>();
    protected RefreshLvLayout mRefreshLvLayout;

    // private static final int CHECK_RESULT = 0x01;
    // private static final int DELAY = 100;
    //
    // private boolean mIsBackup = false;
    // private boolean hasRefersh = false;

    protected Activity mActivity;
    protected CommunitySDK mSdkImpl;
    protected boolean fromRecommedTopic = true;
    private boolean mSaveButtonVisiable = true;
    private ViewStub mStub;
    private TextView mEmptyView;

    public RecommendTopicDialog(Activity activity, int theme) {
        super(activity, theme);
        this.mActivity = activity;
        mSdkImpl = CommunityFactory.getCommSDK(activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initDatas();
    }

    protected void initView() {
        int layoutResId = ResFinder.getLayout("umeng_comm_topic_recommend");
        View rootView = LayoutInflater.from(mActivity.getApplicationContext()).inflate(layoutResId,
                null);
        setContentView(rootView);
        FontUtils.changeTypeface(rootView);
        initRefreshView();
        initTitleView();
        mStub = (ViewStub) rootView.findViewById(ResFinder.getId("umeng_comm_empty"));
    }

    protected void initTitleView() {
        Button button = (Button) findViewById(ResFinder.getId("umeng_comm_save_bt"));
        button.setOnClickListener(this);
        button.setText(ResFinder.getString("umeng_comm_skip"));
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        button.setTextColor(ResFinder.getColor("umeng_comm_skip_text_color"));
        if (!mSaveButtonVisiable) {
            button.setVisibility(View.GONE);
            findViewById(ResFinder.getId("umeng_comm_setting_back")).setOnClickListener(this);
        } else {
            findViewById(ResFinder.getId("umeng_comm_setting_back")).setVisibility(View.GONE);
        }
        TextView textView = (TextView) findViewById(ResFinder
                .getId("umeng_comm_setting_title"));
        textView.setText(ResFinder.getString("umeng_comm_recommend_topic"));
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        findViewById(ResFinder.getId("umeng_comm_title_bar_root"))
                .setBackgroundColor(Color.WHITE);
    }

    /**
     * 设置保存按钮魏不可见。在设置页面显示推荐话题时，不需要显示</br>
     */
    public void setSaveButtonInVisiable() {
        // findViewById(ResFinder.getId("umeng_comm_save_bt")).setVisibility(View.INVISIBLE);
        mSaveButtonVisiable = false;
    }

    protected void initDatas() {
        loadTopics();
    }

    /**
     * 确保每次显示时都注册广播，dismiss时取消注册
     */
    public void show() {
        registerTopicBroadcast();
        super.show();
    }
    
    /**
     * dismiss的时候注销广播
     */
    public void dismiss() {
        if ( mBroadcastReceiver != null ) {
            mActivity.unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
        super.dismiss();
    }

    NotifyBroadcastReceiver mBroadcastReceiver = null;
    IntentFilter mIntentFilter = null;

    private void registerTopicBroadcast() {

        if (mBroadcastReceiver == null) {
            mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(NotifyBroadcastReceiver.TOPIC_FOLLOWED);
            mIntentFilter.addAction(NotifyBroadcastReceiver.CANCEL_TOPIC_FOLLOWED);

            mBroadcastReceiver = new NotifyBroadcastReceiver(new NotifyListener() {

                @Override
                public void onNotify(Intent intent) {
                    Topic topic = intent.getExtras().getParcelable(Constants.TAG_TOPIC);
                    if (topic != null) {
                        Topic originTopic = findTopicById(topic.id);
                        originTopic.isFocused = topic.isFocused;
                        Log.d("TopicDialog", "### 是否关注话题 : " + originTopic.name + ", "
                                + originTopic.isFocused);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
        mActivity.registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    private Topic findTopicById(String id) {
        for (Topic topic : mAdapter.getDataSource()) {
            if (topic.id.equals(id)) {
                return topic;
            }
        }

        return new Topic();
    }

    /**
     * 初始化刷新相关的view跟事件</br>
     * 
     * @param rootView
     */
    protected void initRefreshView() {
        int refreshResId = ResFinder.getId("umeng_comm_topic_refersh");
        mRefreshLvLayout = (RefreshLvLayout) findViewById(refreshResId);
        mRefreshLvLayout.setLoading(false);
        // 推荐用户页面无加载更多跟下拉刷新
        if (fromRecommedTopic) {
            mRefreshLvLayout.setOnRefreshListener(new OnRefreshListener() {

                @Override
                public void onRefresh() {
                    // 下来刷新的情况
                    loadTopics();
                }
            });
            mRefreshLvLayout.setOnLoadListener(new OnLoadListener() {
                @Override
                public void onLoad() {
                    // 加载更多的情况
                    loadMoreTopic();
                }
            });
        }

        int listViewResId = ResFinder.getId("umeng_comm_topic_listview");
        mTopicListView = mRefreshLvLayout.findRefreshViewById(listViewResId);
        //
        initAdapter();
        if (!mSaveButtonVisiable) {
            // 目前推荐话题不需要刷新跟加载更多，因此暂时设置不可用
            mRefreshLvLayout.setEnabled(false);
        } else {
            mRefreshLvLayout.setDefaultFooterView();
        }
    }

    protected void initAdapter() {
        RecommendTopicAdapter adapter = new RecommendTopicAdapter(mActivity, new ArrayList<Topic>());
        adapter.setFromFindPage(!mSaveButtonVisiable);
        mAdapter = adapter;
        adapter.setFollowListener(new FollowListener<Topic>() {

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
     * 保存话题列表到数据库中,首先将话题本身的内容保存到topic表中,然后将话题与用户的关系( 关注与否 ) 插入到topic_user表中.
     * 
     * @param topicList 要保存的话题列表.
     */
    private void insertTopicsToDB(final List<Topic> topicList) {
        //
        saveNewTopicInDatabase(topicList);
        // 如果是已经关注那么将该条记录放到关系表中
        saveFollowedTopicInRelativeDB(topicList);
    }

    /**
     * 保存从服务器上更新下来的新话题
     * 
     * @param topicItem
     */
    private void saveNewTopicInDatabase(List<Topic> topicItems) {
        // //
        InsertCommand<Topic> insertCommand = new InsertCommand<Topic>(
                DbHelperFactory.getTopicDbHelper(mActivity), topicItems);
        insertCommand.execute();
    }

    /**
     * 保存已经关注的话题到关系表中
     */
    private void saveFollowedTopicInRelativeDB(List<Topic> topics) {

        List<RelativeKeyPair> followedPairs = new LinkedList<RelativeKeyPair>();
        String loginUid = CommConfig.getConfig().loginedUser.id;
        for (Topic topicItem : topics) {
            if (topicItem.isFocused) {
                // 保存话题和用户之间的关系
                followedPairs.add(new RelativeKeyPair(topicItem.id, loginUid));
            }
        }

        final DbHelper<RelativeKeyPair> topic_userDbHelper = DbHelperFactory
                .getTopicUserDbHelper(mActivity);
        // 插入命令,将所有已经关注的话题插入关系表中
        InsertCommand<RelativeKeyPair> insertCommand = new InsertCommand<RelativeKeyPair>(
                topic_userDbHelper, followedPairs);
        insertCommand.execute();

    }

    /**
     * 获取所有话题
     * 
     * @param start
     */
    protected void loadTopics() {
        mSdkImpl.fetchRecommendedTopics(new FetchListener<TopicResponse>() {

            @Override
            public void onStart() {
                mRefreshLvLayout.setRefreshing(true);
            }

            @Override
            public void onComplete(final TopicResponse response) {
                mRefreshLvLayout.setLoading(false);
                mRefreshLvLayout.setRefreshing(false);
                
                final List<Topic> results = response.result;
                
                dealEmptyData(results);//放在handlerResponse之前处理
                // 根据response进行Toast
                if (handlerResponse(response)) {
                    return;
                }

                updateNextPageUrl(results.get(0).nextPage);
                Log.d("TopicDialog",
                        "### 话题数量 : " + response.result.size() + ", 下一页 : "
                                + results.get(0).nextPage);
                fetchTopicComplete(results, true);
            }
        });
    }

    protected void updateNextPageUrl(String newUrl) {
        for (Topic topic : mTopics) {
            topic.nextPage = newUrl;
        }
    }
    
    /**
     * 
     * 处理返回的推荐话题为空的情况</br>
     * @param topics
     */
    private void dealEmptyData(List<Topic> topics){
        if ( (topics == null || topics.size() == 0) && mAdapter.getDataSource().size() == 0 ) {
            mEmptyView = (TextView) mStub.inflate();
            mEmptyView.setVisibility(View.VISIBLE);
            mEmptyView.setText(ResFinder.getString("umeng_comm_no_recommend_topic"));
            mTopicListView.setVisibility(View.GONE);
        } else {
            if (mEmptyView != null ){
                mEmptyView.setVisibility(View.GONE);
            }
            mTopicListView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 根据url加载更多的话题</br>
     */
    protected void loadMoreTopic() {

    }

    protected void fetchTopicComplete(List<Topic> topics, boolean fromRefersh) {
        parseNextpageUrl(topics, fromRefersh);
        // 过滤已经存在的数据
        final List<Topic> newTopics = filterTopics(topics);
        if (newTopics != null && newTopics.size() > 0) {
            Log.d("TopicDialog", "### 新的话题 : " + newTopics.size());
            // 添加新话题
            if (fromRefersh) {
                mAdapter.addToFirst(newTopics);// 下拉刷新的数据追加到头部
            } else {
                mAdapter.addData(newTopics); // 加载更多的数据追加到尾部
            }
            // 将新的话题数据插入到数据库中
            insertTopicsToDB(newTopics);
            // mTopics.addAll(newTopics);
        }
    }

    protected void parseNextpageUrl(List<Topic> topics, boolean fromRefersh) {

    }

    /**
     * @author mrsimple
     */

    /**
     * 关注某个话题</br>
     * 
     * @param id 话题的id
     */
    protected void followTopic(final Topic topic, final ToggleButton toggleButton) {
        mSdkImpl.followTopic(topic, new SimpleFetchListener<Response>() {

            @Override
            public void onComplete(Response response) {
                if (response.errCode == Constants.NO_ERROR) {
                    topic.isFocused = true;
                    // 存储到数据
                    List<Topic> topics = new ArrayList<Topic>();
                    topics.add(topic);
                    insertTopicsToDB(topics);
                    toggleButton.setChecked(true);
                    sendBroadcast(topic);
                    ToastMsg.showShortMsgByResName(mActivity, "umeng_comm_topic_follow_success");
                } else if (response.errCode == Constants.ORIGIN_TOPIC_DELETE_ERR_CODE) {
                    // 在数据库中删除该话题并Toast
                    deleteTopic(topic);
                    ToastMsg.showShortMsgByResName(mActivity, "umeng_comm__topic_has_deleted");
                } else {
                    toggleButton.setChecked(false);
                    ToastMsg.showShortMsgByResName(mActivity, "umeng_comm_topic_follow_failed");
                }
            }
        });
    }

    /**
     * 取消关注某个话题</br>
     * 
     * @param id
     */
    protected void cancelFollowTopic(final Topic topic, final ToggleButton toggleButton) {
        mSdkImpl.cancelFollowTopic(topic,
                new SimpleFetchListener<Response>() {

                    @Override
                    public void onComplete(Response response) {
                        if (response.errCode == Constants.NO_ERROR) {
                            topic.isFocused = false;
                            // 将该记录从数据库中移除
                            removeFollowedTopicOnDatabase(mActivity, topic);
                            toggleButton.setChecked(false);
                            sendBroadcast(topic);
                            List<Topic> topics = new ArrayList<Topic>();
                            topics.add(topic);
                            insertTopicsToDB(topics);
                            ToastMsg.showShortMsgByResName(mActivity,
                                    "umeng_comm_topic_cancel_success");
                        } else if (response.errCode == Constants.ORIGIN_TOPIC_DELETE_ERR_CODE) {
                            // 在数据库中删除该话题并Toast
                            deleteTopic(topic);
                            ToastMsg.showShortMsgByResName(mActivity,
                                    "umeng_comm__topic_has_deleted");
                        } else {
                            toggleButton.setChecked(true);
                            ToastMsg.showShortMsgByResName(mActivity,
                                    "umeng_comm_topic_cancel_failed");
                        }
                    }
                });
    }

    // /**
    // * 将关注话题的记录存到数据库中.
    // */
    // private void addFollowedTopicOnDatabase(Context context, Topic topic) {
    // List<Topic> topics = new ArrayList<Topic>();
    // topic.isFocused = true;
    // topics.add(topic);
    // insertTopicsToDB(topics);
    // }

    /**
     * 
     *发送广播，通知话题发生了改变。场景：比如用户在话题推荐页面关注了话题，此时需要通知话题列表页面</br>
     * @param topic
     * @param action
     */
    private void sendBroadcast(Topic topic) {
        String action = topic.isFocused ? NotifyBroadcastReceiver.TOPIC_FOLLOWED
                : NotifyBroadcastReceiver.CANCEL_TOPIC_FOLLOWED;
        Intent intent = new Intent(action);
        intent.putExtra(Constants.TAG_TOPIC, topic);
        mActivity.sendBroadcast(intent);
    }

    /**
     * 删除话题。包括删除关系表跟话题本身，以及从adapter中删除</br>
     * 
     * @param topic
     */
    private void deleteTopic(Topic topic) {
        deleteTopicOnDataBase(topic);
        // 从adapter删除该条topic
        mAdapter.getDataSource().remove(topic);
        mAdapter.notifyDataSetChanged();
    }

    private void deleteTopicOnDataBase(Topic topic) {
        // DeleteCommand<Topic> deleteCommand = DbCommandFactory
        // .createCancelFollowTopicCmd(mActivity, topic);
        DeleteCommand<RelativeKeyPair> deleteCommand = DbCommandFactory.createCancelFollowTopicCmd(
                mActivity,
                topic.id);
        deleteCommand.execute();
    }

    /**
     * 移除关注某话题的记录.
     * 
     * @param context
     * @param topic
     */
    private void removeFollowedTopicOnDatabase(Context context, Topic topic) {
        DeleteCommand<RelativeKeyPair> deleteCommand = DbCommandFactory.createCancelFollowTopicCmd(
                mActivity, topic.id);
        deleteCommand.execute();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == ResFinder.getId("umeng_comm_save_bt")
                || id == ResFinder.getId("umeng_comm_setting_back")) {
            dismiss();
        }
    }

    protected boolean handlerResponse(AbsResponse<?> response) {
        return NetworkUtils.handlerResponse(mActivity, response);
    }
    
}
