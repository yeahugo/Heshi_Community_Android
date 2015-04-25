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

package com.umeng.comm.ui.dialogs;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.db.AbsDBHelper;
import com.umeng.comm.core.db.DbHelper;
import com.umeng.comm.core.db.DbHelperFactory;
import com.umeng.comm.core.db.cmd.QueryCommand;
import com.umeng.comm.core.imageloader.ImgDisplayOption;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.FansResponse;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.adapters.PickerAdapter;
import com.umeng.comm.ui.adapters.viewparser.FriendItemViewParser.ImgTextViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AtFriendDialog extends PickerDialog<CommUser> {

    // protected PickerAdapter<CommUser> mAdapter = null;
    /**
     * 
     */
    private CommUser mUser = CommConfig.getConfig().loginedUser;
    /**
     * @好友的下一页url地址。每次从server获取好友列表时，都能够拿到该url，因此不cache到DB
     */
    private String mNextPageUrl;

    public AtFriendDialog(Context context) {
        this(context, 0);
    }

    public AtFriendDialog(Context context, int theme) {
        super(context, theme);
        setContentView(this.createContentView());
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        loadFriendsFromDB(mUser.id);
        loadDataFromServer();
    }

    @Override
    protected void setupAdater() {
        mAdapter = new PickerAdapter<CommUser>(getContext(), new ArrayList<CommUser>()) {

            @Override
            public void fillData(ImgTextViewHolder viewHolder, CommUser item, int position) {
                viewHolder.mTextView.setText(item.name);
                viewHolder.mDetailTextView.setVisibility(View.GONE);
                ImgDisplayOption option = ImgDisplayOption.getOptionByGender(item.gender);
                viewHolder.mImageView.setImageUrl(item.iconUrl, option);
            }
        };

        // mListView.setAdapter(mAdapter);
        mRefreshLvLayout.setAdapter(mAdapter);
        String title = ResFinder.getString("umeng_comm_my_friends");
        mTitleTextView.setText(title);
    }

    @Override
    protected void setupLvOnItemClickListener() {
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pickItemAtPosition(position);
            }

        });
    }

    /**
     * 从数据库中加载最近的联系人。</br>
     * 
     * @param uid
     */
    private void loadFriendsFromDB(final String uid) {

        final Map<String, String> wheres = new HashMap<String, String>(1);
        wheres.put(AbsDBHelper.FOLLOWED_USER_ID, uid);
        //
        final DbHelper<CommUser> dbHelper = DbHelperFactory
                .getFollowedUserDbHelper(getContext());
        //
        QueryCommand<CommUser> queryCommand = new QueryCommand<CommUser>(dbHelper, wheres, null);
        queryCommand.setFetchListener(new SimpleFetchListener<List<CommUser>>() {

            @Override
            public void onComplete(List<CommUser> result) {
                mAdapter.addData(result);
            }
        });
        queryCommand.execute();
    }

    @Override
    public void loadDataFromServer() {
        mSdkImpl.fetchFollowedUser(mUser.id, new FetchListener<FansResponse>() {

            @Override
            public void onStart() {
                mRefreshLvLayout.setRefreshing(true);
            }

            @Override
            public void onComplete(FansResponse resp) {
                mRefreshLvLayout.setRefreshing(false);
                handleResultData(resp);
            }
        });
    }

    @Override
    public void loadMore() {
        if (TextUtils.isEmpty(mNextPageUrl)) {
            mRefreshLvLayout.setLoading(false);
            return;
        }
        mSdkImpl.fetchNextPageData(mNextPageUrl, FansResponse.class,
                new FetchListener<FansResponse>() {

                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onComplete(FansResponse data) {
                        mRefreshLvLayout.setLoading(false);
                        handleResultData(data);
                    }
                });
    }

    
    @Override
    protected void pickItemAtPosition(int position) {
        super.pickItemAtPosition(position);
        mSelectedItem = null;
    }
    
    /**
     * 处理从server加载后返回的数据</br>
     * 
     * @param resp
     */
    private void handleResultData(FansResponse response) {
        List<CommUser> follows = response.result;
        if (follows.size() == 0) {
            // ToastMsg.showShortMsgByResName(getContext(),
            // "umeng_comm_followed_no_user");
            return;
        }
        List<CommUser> sourceList = mAdapter.getDataSource();
        follows.removeAll(sourceList);
        mAdapter.addData(follows);
        mNextPageUrl = response.mNextPageUrl;

        // TODO : 待review
        // 将我关注的好友的owner id 设置为当前用户的id.
        for (CommUser commUser : follows) {
            commUser.extraData.putString(AbsDBHelper.FOLLOWED_USER_ID, mUser.id);
        }
        // 保存关注的用户到DB
        DbHelperFactory.getFollowedUserDbHelper(getContext()).insert(follows);
    }
    

}
