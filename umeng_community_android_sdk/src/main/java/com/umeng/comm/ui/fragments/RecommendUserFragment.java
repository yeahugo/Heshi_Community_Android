/**
 * 
 */

package com.umeng.comm.ui.fragments;

import java.util.List;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.TextView;

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.impl.CommunityFactory;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.listeners.Listeners.OnResultListener;
import com.umeng.comm.core.nets.responses.FansResponse;
import com.umeng.comm.core.nets.responses.UsersResponse;
import com.umeng.comm.core.utils.ResFinder;

/**
 * 用户推荐页面
 */
public class RecommendUserFragment extends ActiveUserFragment implements OnClickListener {

    private boolean mSaveButtonVisiable = true;
    private ViewStub mViewStub;
    private View mEmptyView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater
                .inflate(ResFinder.getLayout("umeng_comm_recommend_user_layout"), null);
        mSdkImpl = CommunityFactory.getCommSDK(getActivity());
        loadDataFromServer();
        initView(rootView);
        registerUserBroadcast();
        return rootView;
    }

    @Override
    protected void initView(View rootView) {
        super.initView(rootView);
        Button button = (Button) rootView.findViewById(ResFinder.getId("umeng_comm_save_bt"));
        button.setOnClickListener(this);
        button.setText(ResFinder.getString("umeng_comm_skip"));
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        button.setTextColor(ResFinder.getColor("umeng_comm_skip_text_color"));
        if (!mSaveButtonVisiable) {
            button.setVisibility(View.GONE);
            rootView.findViewById(ResFinder.getId("umeng_comm_setting_back")).setOnClickListener(
                    this);
            mAdapter.setFromFindPage(!mSaveButtonVisiable);
        } else {
            rootView.findViewById(ResFinder.getId("umeng_comm_setting_back")).setVisibility(
                    View.GONE);
        }
        TextView textView = (TextView) rootView.findViewById(ResFinder
                .getId("umeng_comm_setting_title"));
        textView.setText(ResFinder.getString("umeng_comm_recommend_user"));
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

        rootView.findViewById(ResFinder.getId("umeng_comm_title_bar_root"))
                .setBackgroundColor(Color.WHITE);

        mRefreshLvLayout.setEnabled(false);
        mViewStub = (ViewStub) rootView.findViewById(ResFinder.getId("umeng_comm_empty"));
    }

    @Override
    protected void loadDataFromServer() {
        mSdkImpl.fetchRecommendedUsers(new FetchListener<UsersResponse>() {

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
    
    @Override
    void dealResult(FansResponse response, boolean fromRefresh) {
        List<CommUser> users = response.result;
        if ( users == null || users.size() == 0 ) {
            mEmptyView = mViewStub.inflate();
            mEmptyView.setVisibility(View.VISIBLE);
            return ;
        } else if ( mEmptyView != null && mEmptyView.getVisibility() == View.VISIBLE) {
            mEmptyView.setVisibility(View.GONE);
        }
        super.dealResult(response, fromRefresh);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == ResFinder.getId("umeng_comm_save_bt")
                || id == ResFinder.getId("umeng_comm_setting_back")) { // 跳过事件
            mListener.onResult(0);
        }
    }

    /**
     * 设置跳过按钮不可见。在设置页面显示推荐用户的时候不需要显示。</br>
     */
    public void setSaveButtonInvisiable() {
        mSaveButtonVisiable = false;
    }

    /**
     * 设置点击跳过时得回调</br>
     * 
     * @param listener
     */
    public void setOnResultListener(OnResultListener listener) {
        mListener = listener;
    }

    /**
     * 默认逻辑。点击跳过时销毁该Activity
     */
    private OnResultListener mListener = new OnResultListener() {

        @Override
        public void onResult(int status) {
            getActivity().finish();
        }
    };
}
