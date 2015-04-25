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

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.db.AbsDBHelper;
import com.umeng.comm.core.db.DbHelperFactory;
import com.umeng.comm.core.db.cmd.DeleteCommand;
import com.umeng.comm.core.db.cmd.QueryCommand;
import com.umeng.comm.core.db.cmd.concrete.DbCommandFactory;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.FansResponse;
import com.umeng.comm.core.utils.CommonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 粉丝页面
 */
public class FansFragment extends FollowedUserFragment {

    @Override
    protected void loadFollowFromDB() {
        QueryCommand<CommUser> queryCommand = DbCommandFactory.createQueryFansCmd(getActivity(),
                mUserId);
        queryCommand.setFetchListener(new SimpleFetchListener<List<CommUser>>() {

            @Override
            public void onComplete(List<CommUser> results) {
                if (CommonUtils.isActivityAlive(getActivity())) {
                    updateFans(results);
                    if (results != null) {
                        mListener.onResult(results.size());
                    }
                }
            }
        });
        queryCommand.execute();
    }

    /**
     * 获取我的粉丝列表
     */
    @Override
    protected void fetchUsers() {
        mSdkImpl.fetchFans(mUserId, new FetchListener<FansResponse>() {

            @Override
            public void onStart() {
                mRefreshGvLayout.setRefreshing(true);
            }

            @Override
            public void onComplete(FansResponse response) {
                mRefreshGvLayout.setRefreshing(false);
                final List<CommUser> fans = response.result;
                // 保存到数据库
                saveUserInDB(DbHelperFactory.getFansDbHelper(getActivity()), fans);
                // 根据response进行Toast
                if (handlerResponse(response)) {
                    return;
                }

                // 加载完成后，首先更新粉丝的条数,因为可能在下拉刷新的时候有新的粉丝。
                mListener.onResult(fans.size());

                // 去重操作
                fans.removeAll(mAdapter.getDataSource());
                mAdapter.addData(fans);
                // 解析下一页地址
                parseNextpageUrl(response, true);
            }
        });
    }

    protected void removeFromDB(CommUser user) {
        Map<String, String> wheres = new HashMap<String, String>();
        wheres.put(AbsDBHelper.FANS_USER_ID, user.id);
        DeleteCommand<CommUser> deleteCommand = new DeleteCommand<CommUser>(
                DbHelperFactory.getFansDbHelper(getActivity()), wheres);
        deleteCommand.execute();
    }

    /**
     * 更新粉丝的ListView病保存数据到数据库</br>
     * 
     * @param users
     */
    private void updateFans(final List<CommUser> users) {

        if (users == null || users.size() <= 0) {
            return;
        }
        List<CommUser> dataSource = mAdapter.getDataSource();
        users.removeAll(dataSource);
        mAdapter.addData(users);
    }

}
