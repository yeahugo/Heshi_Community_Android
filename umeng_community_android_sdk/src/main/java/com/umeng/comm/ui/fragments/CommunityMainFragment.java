/**
 * 
 */

package com.umeng.comm.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.LoginResponse;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ResFinder.ResType;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.activities.FindActivity;
import com.umeng.comm.ui.dialogs.TopicDialog;
import com.umeng.comm.ui.utils.FontUtils;
import com.umeng.comm.ui.widgets.SlidingTextView;
import com.umeng.comm.ui.widgets.SlidingTextView.OnIndexChangeListener;
import com.umeng.comm.ui.widgets.SlidingTextView.SlidingAdapter;

/**
 * 
 */
public class CommunityMainFragment extends Fragment implements OnClickListener {

    private ViewPager mViewPager;
    private String[] mTitles;
    private Fragment mCurrentFragment;
    private AllFeedsFragment mMainFeedFragment;
    private RecommendFeedFragment mRecommendFragment;

    /**
     * 回退按钮的可见性
     */
    private int mBackButtonVisible = View.VISIBLE;
    /**
     * 跳转到话题搜索按钮的可见性
     */
    private int mTitleVisible = View.VISIBLE;
    /**
     * 话题搜索跳转按钮
     */
    private View mTopicButton;
    /**
     * title的根布局
     */
    private View mTitleLayout;
    /**
     * 右上角的个人信息Button
     */
    private ImageView mProfileBtn;

    private String mContainerClass;
    SlidingTextView mTitleTextView;

    /**
     * 话题选择、搜索的Dialog
     */
    TopicDialog mTopicDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(ResFinder.getLayout("umeng_comm_community_frag_layout"),
                null);
        FontUtils.changeTypeface(rootView);
        mContainerClass = getActivity().getClass().getName();
        initTitle(container.getContext(), rootView);
        initFragment();
        initViewPager(rootView);
        return rootView;
    }

    /**
     * 初始化title</br>
     * 
     * @param context
     */
    private void initTitle(Context context, View rootView) {
        mTitles = context.getResources().getStringArray(
                ResFinder.getResourceId(ResType.ARRAY, "umeng_comm_feed_titles"));
        int titleLayoutResId = ResFinder.getId("topic_action_bar");
        mTitleLayout = rootView.findViewById(titleLayoutResId);
        mTitleLayout.setVisibility(View.GONE);

        int backButtonResId = ResFinder.getId("umeng_comm_back_btn");
        int selectButtonResId = ResFinder.getId("umeng_comm_topic_select_btn");
        rootView.findViewById(backButtonResId).setOnClickListener(this);
        mTopicButton = rootView.findViewById(selectButtonResId);
        mTopicButton.setOnClickListener(this);

        if (mBackButtonVisible != View.VISIBLE) {
            rootView.findViewById(backButtonResId).setVisibility(mBackButtonVisible);
        }

        mTitleLayout.setVisibility(mTitleVisible);

        //
        mProfileBtn = (ImageView) rootView
                .findViewById(ResFinder.getId("umeng_comm_user_info_btn"));
        mProfileBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                gotoFindActivity(CommConfig.getConfig().loginedUser);
            }
        });

        mTitleTextView = (SlidingTextView) rootView.findViewById(ResFinder
                .getId("umeng_comm_title_tv"));
        mTitleTextView.setSlidingAdapter(new SlidingAdapter() {

            @Override
            public String getTitle(int position) {
                return mTitles[position];
            }

            @Override
            public int getCount() {
                return mTitles.length;
            }
        });
        mTitleTextView.setOnIndexChangeListener(new OnIndexChangeListener() {

            @Override
            public void onChange(int oldIndex, int index) {
                mViewPager.setCurrentItem(index, true);
            }
        });
    }

    public ViewPager getViewPager() {
        return mViewPager;
    }

    /**
     * 跳转到发现Activity</br>
     * 
     * @param user
     */
    public void gotoFindActivity(final CommUser user) {
        CommonUtils.checkLoginAndFireCallback(getActivity(),
                new SimpleFetchListener<LoginResponse>() {

                    @Override
                    public void onComplete(LoginResponse response) {
                        if ( response.errCode != Constants.NO_ERROR ) {
                            return ;
                        }
                        Intent intent = new Intent(getActivity(), FindActivity.class);
                        if (user == null) {// 来自开发者外部调用的情况
                            intent.putExtra(Constants.TAG_USER, CommConfig.getConfig().loginedUser);
                        } else {
                            intent.putExtra(Constants.TAG_USER, user);
                        }
                        intent.putExtra(Constants.TYPE_CLASS, mContainerClass);
                        getActivity().startActivity(intent);
                    }
                });
    }

    /**
     * 设置回退按钮的可见性
     * 
     * @param visible
     */
    public void setBackButtonVisibility(int visible) {
        if (visible == View.VISIBLE || visible == View.INVISIBLE || visible == View.GONE) {
            this.mBackButtonVisible = visible;
        }
    }

    /**
     * 设置Title区域的可见性
     * 
     * @param visible {@see View#VISIBLE},{@see View#INVISIBLE},{@see View#GONE}
     */
    public void setNavTitleVisibility(int visible) {
        if (visible == View.VISIBLE || visible == View.INVISIBLE || visible == View.GONE) {
            mTitleVisible = visible;
        }
    }

    /**
     * 初始化ViewPager VIew</br>
     * 
     * @param rootView
     */
    private void initViewPager(View rootView) {
        mViewPager = (ViewPager) rootView.findViewById(ResFinder.getId("viewPager"));
        CommFragmentPageAdapter adapter = new CommFragmentPageAdapter(getChildFragmentManager());
        mViewPager.setAdapter(adapter);
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int page) {
                mCurrentFragment = getFragment(page);
                mTitleTextView.selectItemWithIndex(page);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
    }

    class CommFragmentPageAdapter extends FragmentPagerAdapter {

        public CommFragmentPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            return getFragment(pos);
        }

        @Override
        public int getCount() {
            return mTitles.length;
        }
    }

    /**
     * 初始化Fragment</br>
     */
    private void initFragment() {
        mMainFeedFragment = new AllFeedsFragment();
        mRecommendFragment = new RecommendFeedFragment();
        mCurrentFragment = mMainFeedFragment;// 默认是MainFeedFragment
    }

    /**
     * 获取当前页面被选中的Fragment</br>
     * 
     * @return
     */
    public Fragment getCurrentFragment() {
        return mCurrentFragment;
    }

    /**
     * </br>
     * 
     * @param pos
     * @return
     */
    private Fragment getFragment(int pos) {
        Fragment fragment = null;
        if (pos == 0) {
            fragment = mMainFeedFragment;
        } else if (pos == 1) {
            fragment = mRecommendFragment;
        }
        return fragment;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == ResFinder.getId("umeng_comm_back_btn")) {
            getActivity().finish();
        } else if (v.getId() == ResFinder.getId("umeng_comm_topic_select_btn")) {// 点击话题按钮的情况
            showTopicDialog();
//            Intent intent = new Intent(getActivity(), GuideActivity.class);
//            startActivity(intent);
        }
    }

    /**
     * 显示话题页面的Fragment。如果未登录，则需要先登录再进入话题页面。</br>
     */
    public void showTopicDialog() {
        if (mTopicDialog == null) {
            mTopicDialog = new TopicDialog(getActivity(),
                    ResFinder.getStyle("umeng_comm_dialog_fullscreen"));
        }
        // 检测是否登录
        CommonUtils.checkLoginAndFireCallback(getActivity(),
                new SimpleFetchListener<LoginResponse>() {

                    @Override
                    public void onComplete(LoginResponse response) {
                        if (response.errCode == Constants.NO_ERROR) {
                            mTopicDialog.show();
                        } else {
                            ToastMsg.showShortMsgByResName(getActivity(),
                                    "umeng_comm_login_failed");
                        }
                    }
                });
    }

    /**
     * 隐藏MianFeedFragment的输入法，当退出fragment or activity的时候</br>
     */
    public void hideCommentLayoutAndInputMethod() {
        if (mMainFeedFragment != null) {
            mMainFeedFragment.hideCommentLayoutAndInputMethod();
        }
    }

    /**
     * clean sub fragment data</br>
     */
    public void cleanAdapterData() {
        if (mMainFeedFragment != null) {
            mMainFeedFragment.cleanAdapterData();
        }
        if (mRecommendFragment != null) {
            mRecommendFragment.cleanAdapterData();
        }
    }

}
