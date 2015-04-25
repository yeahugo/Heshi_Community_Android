/**
 * 
 */

package com.umeng.comm.ui.fragments;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.umeng.comm.core.CommunitySDK;
import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.db.DbHelper;
import com.umeng.comm.core.db.DbHelperFactory;
import com.umeng.comm.core.db.cmd.DeleteCommand;
import com.umeng.comm.core.db.cmd.InsertCommand;
import com.umeng.comm.core.db.cmd.concrete.DbCommandFactory;
import com.umeng.comm.core.impl.CommunityFactory;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.Response;
import com.umeng.comm.core.nets.responses.FansResponse;
import com.umeng.comm.core.nets.responses.UsersResponse;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.adapters.ActiveUserAdapter;
import com.umeng.comm.ui.adapters.TopicAdapter.FollowListener;
import com.umeng.comm.ui.broadcastreceiver.NotifyBroadcastReceiver;
import com.umeng.comm.ui.broadcastreceiver.UserInfoNotifyListener;
import com.umeng.comm.ui.widgets.RefreshLvLayout;

/**
 * 
 */
public class ActiveUserFragment extends FontFragment {

    protected RefreshLvLayout mRefreshLvLayout;
    protected CommunitySDK mSdkImpl = null;
    protected ActiveUserAdapter mAdapter;
    private String mNextPageUrl;
    private boolean hasRefresh;
    private Topic mTopic;
    private NotifyBroadcastReceiver mUserBroadcast;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater
                .inflate(ResFinder.getLayout("umeng_comm_active_user_layout"), null);
        mSdkImpl = CommunityFactory.getCommSDK(getActivity());
        loadDataFromServer();
        initView(rootView);
        registerUserBroadcast();
        return rootView;
    }

    /**
     * 初始化view并设置数据</br>
     * 
     * @param rootView
     */
    protected void initView(View rootView) {
        mRefreshLvLayout = (RefreshLvLayout) rootView.findViewById(ResFinder
                .getId("umeng_comm_swipe_layout"));
        mAdapter = new ActiveUserAdapter(getActivity(), new ArrayList<CommUser>());
        mRefreshLvLayout.setAdapter(mAdapter);
        mRefreshLvLayout.setEnabled(false);
        mAdapter.setFromFindPage(true);
        mAdapter.setFollowListener(mListener);
    }

    public static ActiveUserFragment newActiveUserFragment(Topic topic) {
        ActiveUserFragment activeUserFragment = new ActiveUserFragment();
        activeUserFragment.mTopic = topic;
        return activeUserFragment;
    }

    /**
     * 从Server加载好友</br>
     */
    protected void loadDataFromServer() {
        mSdkImpl.fetchActiveUsers(mTopic.id, new FetchListener<UsersResponse>() {

            @Override
            public void onStart() {
                mRefreshLvLayout.setRefreshing(true);
            }

            @Override
            public void onComplete(UsersResponse response) {
                mRefreshLvLayout.setRefreshing(false);
                dealResult(response, true);
            }
        });
    }

    /**
     * 处理结果集数据</br>
     * 
     * @param response
     * @param addFirst
     */
    void dealResult(FansResponse response, boolean fromRefresh) {

        if (response.errCode != Constants.NO_ERROR) {
            // 加载数据失败
            ToastMsg.showShortMsgByResName(getActivity(), "umeng_comm_load_failed");
            return;
        }

        List<CommUser> users = response.result;
        if (users == null || users.size() == 0) {
            // 加载用户为空
            ToastMsg.showShortMsgByResName(getActivity(), "umeng_comm_no_recommend_user");
            return;
        }
        
        dealNextpageUrl(response.mNextPageUrl, fromRefresh);

        users.removeAll(mAdapter.getDataSource());
        if (hasRefresh) {
            mAdapter.addToFirst(users);
        } else {
            mAdapter.getDataSource().addAll(users);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void dealNextpageUrl(String url, boolean fromRefersh) {
        if (fromRefersh && TextUtils.isEmpty(mNextPageUrl) && !hasRefresh) {
            hasRefresh = true;
            mNextPageUrl = url;
        } else if (!fromRefersh) {
            mNextPageUrl = url;
        }
    }

    /**
     * 目前仅仅返回前10个活跃用户，不存在下一页地址问题 加载更多的好友</br>
     */
    // private void loadMoreData() {
    // if ( TextUtils.isEmpty(mNextPageUrl) ) {
    // return ;
    // }
    //
    // mSdkImpl.fetchNextPageData("url", FansResponse.class, new
    // FetchListener<FansResponse>(){
    //
    // @Override
    // public void onStart() {
    //
    // }
    //
    // @Override
    // public void onComplete(FansResponse response) {
    // dealResult(response, false);
    // }});
    // }

    private FollowListener<CommUser> mListener = new FollowListener<CommUser>() {

        @Override
        public void onFollowOrUnFollow(CommUser user, ToggleButton toggleButton, boolean isFollow) {
            if (isFollow) {
                followUser(user, toggleButton);
            } else {
                cancelFollowUser(user, toggleButton);
            }
        }
    };

    /**
     * 关注某个好友</br>
     * 
     * @param user
     */
    private void followUser(final CommUser user, final ToggleButton toggleButton) {
        if ( isMySelf(user) ) {
            return ;
        }
        mSdkImpl.followUser(user, new SimpleFetchListener<Response>() {

            @Override
            public void onComplete(Response response) {
                if (response.errCode == Constants.NO_ERROR) {
                    ToastMsg.showShortMsgByResName(getActivity(),
                            "umeng_comm_follow_user_success");
                    toggleButton.setChecked(true);
                    // updateUserDB(mUser, true);
                    saveFollowUserToDB(user);
                    // 改变状态
                    int Index = mAdapter.getDataSource().indexOf(user);
                    mAdapter.getDataSource().get(Index).extraData.putBoolean(Constants.IS_FOCUSED, true);
                    mAdapter.notifyDataSetChanged();
                } else {
                    ToastMsg.showShortMsgByResName(getActivity(),
                            "umeng_comm_follow_user_failed");
                    toggleButton.setChecked(false);
                }
            }
        });
    }
    
    private  boolean isMySelf(CommUser user){
        if ( user.id.equals(CommConfig.getConfig().loginedUser.id) ) {
            ToastMsg.showShortMsgByResName(getActivity(), "umeng_comm_no_follow_unfollow_myself");
            return true;
        }
        return false;
    }

    /**
     * 取消关注某个好友</br>
     * 
     * @param user
     */
    private void cancelFollowUser(final CommUser user, final ToggleButton toggleButton) {
        if ( isMySelf(user) ) {
            return ;
        }
        mSdkImpl.cancelFollowUser(user, new SimpleFetchListener<Response>() {

            @Override
            public void onComplete(Response response) {
                if (response.errCode == Constants.NO_ERROR) {
                    ToastMsg.showShortMsg(getActivity(),
                            ResFinder.getString(
                                    "umeng_comm_follow_cancel_success"));
                    toggleButton.setChecked(false);
                    saveCancelFollowUserToDB(user);
                    // 改变状态
                    int Index = mAdapter.getDataSource().indexOf(user);
                    mAdapter.getDataSource().get(Index).extraData.putBoolean(Constants.IS_FOCUSED, false);
                    mAdapter.notifyDataSetChanged();
                    // 发送取消关注的广播
                    // sendCancelFollowedBroadcast(user);
                } else {
                    ToastMsg.showShortMsg(getActivity(),
                            ResFinder.getString("umeng_comm_follow_user_failed"));
                    toggleButton.setChecked(true);
                }
            }
        });
    }
    
    /**
     * 保存关注的用户</br>
     * 
     * @param user
     */
    private void saveFollowUserToDB(CommUser user) {
        DbHelper<CommUser> dbHelper = DbHelperFactory.getFollowedUserDbHelper(getActivity());
        InsertCommand<CommUser> insertCommand = new InsertCommand<CommUser>(dbHelper, user);
        insertCommand.execute();
    }

    /**
     * 取消关注的用户</br>
     * 
     * @param user
     */
    private void saveCancelFollowUserToDB(CommUser user) {
        DeleteCommand<CommUser> deleteCommand = DbCommandFactory.createCancelFollowUserCmd(
                getActivity(),
                user.id);
        deleteCommand.execute();
    }
    
    protected void registerUserBroadcast(){
        if (mUserBroadcast == null ) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(NotifyBroadcastReceiver.CANCEL_FOLLOWED);
            filter.addAction(NotifyBroadcastReceiver.FOLLOWED);
            getActivity().registerReceiver(new NotifyBroadcastReceiver(mUserListener), filter);
        }
    }
    
    private UserInfoNotifyListener mUserListener  = new UserInfoNotifyListener(){

        @Override
        public void onNotify(Intent intent) {
            String action = intent.getAction();
            CommUser user = intent.getExtras().getParcelable(Constants.USER);
            int index  = mAdapter.getDataSource().indexOf(user);
            if ( index < 0 ){
                return ;
            }
            if ( action.equals(NotifyBroadcastReceiver.FOLLOWED) ) {
                mAdapter.getDataSource().get(index).extraData.putBoolean(Constants.IS_FOCUSED, true);
                mAdapter.notifyDataSetChanged();
            } else if (action.equals(NotifyBroadcastReceiver.CANCEL_FOLLOWED) ){
                mAdapter.getDataSource().get(index).extraData.putBoolean(Constants.IS_FOCUSED, false);
                mAdapter.notifyDataSetChanged();
            }
        }};
        
        @Override
        public void onDestroy() {
            if (mUserBroadcast != null ) {
                getActivity().unregisterReceiver(mUserBroadcast);
                mUserBroadcast = null;
            }
            super.onDestroy();
        }

}
