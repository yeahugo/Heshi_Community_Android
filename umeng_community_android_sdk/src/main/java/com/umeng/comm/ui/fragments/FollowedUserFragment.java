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

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.db.AbsDBHelper;
import com.umeng.comm.core.db.DbHelper;
import com.umeng.comm.core.db.DbHelperFactory;
import com.umeng.comm.core.db.cmd.DeleteAllCommand;
import com.umeng.comm.core.db.cmd.DeleteCommand;
import com.umeng.comm.core.db.cmd.InsertCommand;
import com.umeng.comm.core.db.cmd.QueryCommand;
import com.umeng.comm.core.db.cmd.concrete.DbCommandFactory;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.listeners.Listeners.OnResultListener;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.FansResponse;
import com.umeng.comm.core.nets.responses.LoginResponse;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.Log;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.activities.UserInfoActivity;
import com.umeng.comm.ui.adapters.UserAdapter;
import com.umeng.comm.ui.broadcastreceiver.NotifyBroadcastReceiver;
import com.umeng.comm.ui.broadcastreceiver.NotifyBroadcastReceiver.NotifyListener;
import com.umeng.comm.ui.utils.ViewFinder;
import com.umeng.comm.ui.widgets.RefreshGvLayout;
import com.umeng.comm.ui.widgets.RefreshLayout.OnLoadListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 已关注的用户的Fragment
 * 
 * @author mrsimple
 */
public class FollowedUserFragment extends FontFragment {

    /**
     * 已关注好友的适配器
     */
    protected UserAdapter mAdapter;

    /**
     * 下拉刷新的View
     */
    protected RefreshGvLayout mRefreshGvLayout;
    /**
     * 显示已关注好友的GridView
     */
    private GridView mGridView;
    /**
     * 用户id。根据该uid获取该用户关注的好友信息
     */
    protected String mUserId;
    /**
     * 用于更新follow、fans条数的更新
     */
    protected OnResultListener mListener;
    /**
     * 
     */
    NotifyBroadcastReceiver mBroadcastReceiver;

    /**
     * 是否是第一次更新数据
     */
    AtomicBoolean isFirstRefresh = new AtomicBoolean(false);

    protected String mNextPageUrl;

    private boolean hasRefresh = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        int layoutResId = ResFinder.getLayout("umeng_comm_followed_user_layout");
        int refershResId = ResFinder.getId("umeng_comm_user_swipe_layout");
        int gvResId = ResFinder.getId("umeng_comm_user_gridview");

        View rootView = inflater
                .inflate(layoutResId, container, false);

        mViewFinder = new ViewFinder(rootView);

        mRefreshGvLayout = mViewFinder
                .findViewById(refershResId);
        // 设置颜色
        mRefreshGvLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                fetchUsers();
            }
        });
        mRefreshGvLayout.setOnLoadListener(new OnLoadListener() {

            @Override
            public void onLoad() {
                fetchNextPage();
            }

        });
        mGridView = mViewFinder.findViewById(gvResId);
        // 跳转到用户信息页面
        mGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                CommonUtils.checkLoginAndFireCallback(getActivity(),
                        new SimpleFetchListener<LoginResponse>() {

                            @Override
                            public void onComplete(LoginResponse response) {
                                if (response.errCode == Constants.NO_ERROR) {
                                    Intent userIntent = new Intent(getActivity(),
                                            UserInfoActivity.class);
                                    userIntent.putExtra(Constants.TAG_USER,
                                            mAdapter.getItem(position));
                                    startActivity(userIntent);
                                } else {
                                    ToastMsg.showShortMsgByResName(getActivity(),
                                            "umeng_comm_login_failed");
                                }
                            }
                        });
            }
        });

        Log.d(getTag(), "### 屏幕宽度 : " + Constants.SCREEN_WIDTH);
        if (Constants.SCREEN_WIDTH > 800) {
            mGridView.setNumColumns(Constants.SCREEN_WIDTH / 200);
        }

        mGridView.setHorizontalSpacing(20);
        mGridView.setVerticalSpacing(10);

        mAdapter = new UserAdapter(getActivity(), new ArrayList<CommUser>(0));
        mGridView.setAdapter(mAdapter);

        loadFollowFromDB();
        fetchUsers();

        registerBroadcast();
        return rootView;
    }

    protected void removeFromDB(CommUser user) {
        Map<String, String> wheres = new HashMap<String, String>();
        wheres.put(AbsDBHelper.FOLLOWED_USER_ID, user.id);
        DeleteCommand<CommUser> deleteCommand = new DeleteCommand<CommUser>(
                DbHelperFactory.getFollowedUserDbHelper(getActivity()), wheres);
        deleteCommand.execute();
    }

    private void registerBroadcast() {
        mBroadcastReceiver = new NotifyBroadcastReceiver(new NotifyListener() {

            @Override
            public void onNotify(Intent intent) {
                CommUser user = intent.getExtras().getParcelable(Constants.USER);
                mAdapter.getDataSource().remove(user);
                mAdapter.notifyDataSetChanged();
                // 从DB中移除
                removeFromDB(user);
            }
        });

        IntentFilter intentFilter = new IntentFilter(NotifyBroadcastReceiver.CANCEL_FOLLOWED);
        getActivity().registerReceiver(mBroadcastReceiver, intentFilter);
    }

    /*
     * 从缓存中加载已经关注的好友
     */
    protected void loadFollowFromDB() {
        QueryCommand<CommUser> queryCommand = DbCommandFactory.createFollowedUserCmd(getActivity(),
                mUserId);
        queryCommand.setFetchListener(new SimpleFetchListener<List<CommUser>>() {
            @Override
            public void onComplete(List<CommUser> results) {
                Log.d(getTag(), "### 我的关注 : " + results.size());

                if (CommonUtils.isActivityAlive(getActivity()) && !CommonUtils.isListEmpty(results)) {
                    updateFollow(results);
                    if (mListener != null) {
                        mListener.onResult(results.size());
                    }
                }
            }
        });

        queryCommand.execute();
    }

    /**
     * 获取最新的数据, 在该fragment中为获取当前用户已关注的用户</br>
     */
    protected void fetchUsers() {
        mSdkImpl.fetchFollowedUser(mUserId, new FetchListener<FansResponse>() {

            @Override
            public void onStart() {
                mRefreshGvLayout.setRefreshing(true);
            }

            @Override
            public void onComplete(FansResponse response) {
                mRefreshGvLayout.setRefreshing(false);

                final List<CommUser> followedUsers = response.result;
                // 保存数据
                saveUserInDB(DbHelperFactory.getFollowedUserDbHelper(getActivity()), followedUsers);
                // 根据response进行Toast
                if (handlerResponse(response)) {
                    return;
                }

                mListener.onResult(followedUsers.size());

                // 更新ListView
                updateFollow(followedUsers);
                // 解析下一页地址
                parseNextpageUrl(response, true);
            }
        });
    }

    private void fetchNextPage() {
        if (TextUtils.isEmpty(mNextPageUrl)) {
            return;
        }

        Log.d(getTag(), "## follow user : " + mNextPageUrl);
        mSdkImpl.fetchNextPageData(mNextPageUrl, FansResponse.class,
                new FetchListener<FansResponse>() {

                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onComplete(FansResponse response) {
                        mRefreshGvLayout.setLoading(false);
                        // 保存到数据库
                        saveUserInDB(DbHelperFactory.getFollowedUserDbHelper(getActivity()),
                                response.result);

                        // 根据response进行Toast
                        if (handlerResponse(response)) {
                            return;
                        }

                        appendUsers(response.result);
                        parseNextpageUrl(response, false);
                    }
                });
    }

    protected void parseNextpageUrl(FansResponse response, boolean fromRefersh) {
        if (fromRefersh && TextUtils.isEmpty(mNextPageUrl) && !hasRefresh) {
            hasRefresh = true;
            mNextPageUrl = response.mNextPageUrl;
        } else if (!fromRefersh) {
            mNextPageUrl = response.mNextPageUrl;
        }
    }

    /**
     * 设置fans所属的用户, 即是谁的fans, 以用户id标识.
     */
    private void setFansOwnerId(List<CommUser> fans) {
        if (fans == null) {
            return;
        }
        for (CommUser fan : fans) {
            fan.extraData.putString(AbsDBHelper.USER_ID, mUserId);
        }
    }

    protected void saveUserInDB(DbHelper<CommUser> dbHelper, List<CommUser> users) {
        setFansOwnerId(users);
        syncNewUsersInDB(dbHelper, users);
    }

    /**
     * 保存用户的fans到数据库,第一次刷新数据会将旧的数据清除
     * 
     * @param fans
     */
    private void syncNewUsersInDB(DbHelper<CommUser> dbHelper, final List<CommUser> fans) {
        // 如果是第一次刷新数据则清空数据库中的内容
        clearDatasAtFirstRefresh(dbHelper, fans);
        // 将新数据存入数据库中
        InsertCommand<CommUser> insertCommand = new InsertCommand<CommUser>(dbHelper, fans);
        insertCommand.execute();
    }

    private void clearDatasAtFirstRefresh(DbHelper<CommUser> dbHelper, List<CommUser> fans) {
        if (!isFirstRefresh.get()) {
            DeleteAllCommand<CommUser> deleteAllCommand = new DeleteAllCommand<CommUser>(dbHelper);
            deleteAllCommand.execute();
            isFirstRefresh.set(true);
            if (mListener != null && fans.size() == 0) {
                mListener.onResult(fans.size());
            }
            mAdapter.getDataSource().clear();
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 更新ListView的数据，并且保存到数据库</br>
     * 
     * @param fans
     */
    private void updateFollow(final List<CommUser> fans) {
        fans.removeAll(mAdapter.getDataSource());
        if (fans.size() > 0 && CommonUtils.isActivityAlive(getActivity())) {
            mAdapter.addData(fans);
        }
    }

    /**
     * 追加已关注的用户，并刷新adapter</br>
     * 
     * @param uewUsers 新关注的好友
     */
    protected void appendUsers(List<CommUser> newUsers) {
        mAdapter.getDataSource().removeAll(newUsers);
        mAdapter.addData(newUsers);
    }

    /**
     * 设置用户id</br>
     * 
     * @param uid
     */
    public void setUerId(String uid) {
        this.mUserId = uid;
    }

    /**
     * 设置follow、fans回调</br>
     * 
     * @param listener
     */
    public void setOnResultListener(OnResultListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onDestroy() {
        if (mBroadcastReceiver != null) {
            getActivity().unregisterReceiver(mBroadcastReceiver);
        }
        super.onDestroy();
    }

}
