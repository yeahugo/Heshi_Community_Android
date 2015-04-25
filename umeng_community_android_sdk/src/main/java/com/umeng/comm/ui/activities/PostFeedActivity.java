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

package com.umeng.comm.ui.activities;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.CommUser.Gender;
import com.umeng.comm.core.beans.CommUser.Permisson;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.beans.ImageItem;
import com.umeng.comm.core.beans.LocationItem;
import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.FeedItemResponse;
import com.umeng.comm.core.nets.responses.LocationResponse;
import com.umeng.comm.core.utils.DeviceUtils;
import com.umeng.comm.core.utils.Log;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.adapters.ImageSelectedAdapter;
import com.umeng.comm.ui.adapters.ImageSelectedAdapter.OnRemoveListener;
import com.umeng.comm.ui.broadcastreceiver.NotifyBroadcastReceiver;
import com.umeng.comm.ui.dialogs.AtFriendDialog;
import com.umeng.comm.ui.dialogs.LocationPickerDlg;
import com.umeng.comm.ui.fragments.PhotoPickerFragment;
import com.umeng.comm.ui.fragments.TopicPickerFragment;
import com.umeng.comm.ui.fragments.TopicPickerFragment.ResultListener;
import com.umeng.comm.ui.location.LocationFinder;
import com.umeng.comm.ui.notifycation.PostNotifycation;
import com.umeng.comm.ui.utils.FeedMemento;
import com.umeng.comm.ui.utils.FeedViewUtils;
import com.umeng.comm.ui.widgets.FeedEditText;
import com.umeng.comm.ui.widgets.TopicTipView;

/**
 * @author mrsimple
 */
public class PostFeedActivity extends BaseFragmentActivity implements OnClickListener {

    /**
     * 内容编辑框，最多300字
     */
    protected FeedEditText mEditText;

    FrameLayout mFragmentLatout;

    /**
     * 选择的图片的GridView
     */
    protected GridView mGridView;
    /**
     * 保存已经选择的图片的路径
     */
    protected List<String> mImagePaths = new LinkedList<String>();
    /**
     * 显示已经选择的图片的Adapter
     */
    private ImageSelectedAdapter mImageSelectedAdapter;
    /**
     * 启动拍照的requestCode
     */
    private static final int REQUEST_IMAGE_CAPTURE = 123;
    /**
     * 位置
     */
    protected Location mLocation;
    /**
     * 通过拍照获取到的图片地址
     */
    private String mNewImagePath;
    /**
     * 已选择的话题
     */
    protected List<Topic> mSelecteTopics = new ArrayList<Topic>();

    /**
     * 保存已经@的好友
     */
    protected List<CommUser> mSelectFriends = new ArrayList<CommUser>();
    /**
     * 我的位置TextView
     */
    protected TextView mLocationTv;
    /**
     * 选择好友的dialog
     */
    private AtFriendDialog mAtFriendDlg;
    /**
     * 地理位置选择dialog
     */
    private LocationPickerDlg mLocationPickerDlg;
    /**
     * 保存地理位置的list
     */
    protected List<LocationItem> mLocationItems = new ArrayList<LocationItem>();
    /**
     * 选择图片的Fragment
     */
    private PhotoPickerFragment mPhotoFragment = new PhotoPickerFragment();;
    /**
     * 选择话题的Fragment
     */
    private TopicPickerFragment mTopicFragment;
    /**
     * 选择话题的ToggleButton
     */
    private ToggleButton mTopicButton;
    /**
     * 选择图片的ToggleButton
     */
    private ToggleButton mPhotoButton;
    /**
     * 地理位置的icon
     */
    private ImageView mLocIcon;

    private View mLocationLayout;
    /**
     * 加载地理位置时的Progress
     */
    private ProgressBar mLocProgressBar;

    protected String TAG = PostFeedActivity.class.getSimpleName();

    /**
     * 是否是发布失败重新发布
     */
    private boolean isRepost = false;

    private static final String CHAR_WELL = "#";
    private static final String CHAR_AT = "@";
    protected boolean isForwardFeed = false;

    protected TopicTipView mTopicTipView;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        setContentView(ResFinder.getLayout("umeng_comm_post_feed_layout"));
        setFragmentContainerId(ResFinder.getId("umeng_comm_select_layout"));
        initViews();

        Bundle extraBundle = getIntent().getExtras();
        if (extraBundle == null) {
            return;
        }

        isRepost = extraBundle.getBoolean(Constants.POST_FAILED, false);
        // 发送失败的重新发送
        if (isRepost) {
            prepareRepostData();
        }

        // 从话题详情页面进入到发送新鲜事页面
        Topic mTopic = extraBundle.getParcelable(Constants.TAG_TOPIC);
        if (mTopic != null) {
            mSelecteTopics.add(mTopic);
            mEditText.insertTopics(mSelecteTopics);
            startFadeOutAnimForTopicTipView();
        }
        
        //从签到按钮进入发送新鲜事页面
        Topic locationTopic = extraBundle.getParcelable("location");
        if (locationTopic != null){
            mSelecteTopics.add(locationTopic);
            mEditText.insertTopics(mSelecteTopics);
            startFadeOutAnimForTopicTipView();
            
            getMyLocation();
        }
    }

    /**
     * 初始化相关View
     */
    protected void initViews() {

        // 发送和回退按钮
        findViewById(ResFinder.getId("umeng_comm_post_ok_btn")).setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_comm_post_back_btn")).setOnClickListener(this);

        mLocProgressBar = (ProgressBar) findViewById(ResFinder.getId(
                "umeng_comm_post_loc_progressbar"));

        mLocIcon = (ImageView) findViewById(ResFinder.getId("umeng_comm_post_loc_icon"));
        mLocIcon.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                getMyLocation();
            }
        });
        mLocationTv = (TextView) findViewById(ResFinder.getId("umeng_comm_location_text"));
        mLocationLayout = findViewById(
                ResFinder.getId("umeng_community_loc_layout"));

        initEditView();
        // 以下四个按钮分别是选择话题、添加图片、选择位置、@好友
        findViewById(ResFinder.getId("umeng_comm_take_photo_btn")).setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_comm_select_location_btn")).setOnClickListener(
                this);
        findViewById(ResFinder.getId("umeng_comm_at_friend_btn")).setOnClickListener(this);

        mPhotoButton = (ToggleButton) findViewById(ResFinder
                .getId("umeng_comm_add_image_btn"));
        mPhotoButton.setOnClickListener(this);
        mTopicButton = (ToggleButton) findViewById(ResFinder.getId(
                "umeng_comm_pick_topic_btn"));
        mTopicButton.setOnClickListener(this);

        mFragmentLatout = (FrameLayout)
                findViewById(ResFinder.getId("umeng_comm_select_layout"));
        mGridView = (GridView) findViewById(ResFinder.getId("umeng_comm_prev_images_gv"));
        initSelectedImageAdapter(taskResultListener);
        mTopicTipView = (TopicTipView) findViewById(ResFinder.getId("umeng_comm_topic_tip"));
        if (CommConfig.getConfig().loginedUser.gender == Gender.FEMALE) {// 根据性别做不同的提示
            mTopicTipView.setText(ResFinder.getString("umeng_comm_topic_tip_female"));
        }
        if (!isForwardFeed) {
            startAnimationForTopicTipView();
        }
    }

    /**
     * 为话题提示VIew绑定动画</br>
     */
    private void startAnimationForTopicTipView() {
        int timePiece = 500;
        int repeatCount = 4;
        int startDeny = 50;
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 10, 0);
        translateAnimation.setRepeatMode(Animation.REVERSE);
        // translateAnimation.setStartOffset(startDeny * repeatCount+timePiece);
        translateAnimation.setRepeatCount(Integer.MAX_VALUE);
        translateAnimation.setDuration(timePiece);

        // int deny = (timePiece+1) * repeatCount + startDeny *( repeatCount +
        // 1);
        // AlphaAnimation alphaAnimationOut = new AlphaAnimation(1.0f, 0);
        // alphaAnimationOut.setDuration(timePiece);
        // alphaAnimationOut.setStartOffset(startDeny * repeatCount + timePiece
        // + repeatCount * (timePiece + 2));

        AlphaAnimation alphaAnimationIn = new AlphaAnimation(0, 1.0f);
        alphaAnimationIn.setDuration(timePiece);
        alphaAnimationIn.setStartOffset(startDeny * repeatCount);

        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(alphaAnimationIn);
        animationSet.addAnimation(translateAnimation);
        // animationSet.addAnimation(alphaAnimationOut);
        // animationSet.setFillAfter(true);
        mTopicTipView.startAnimation(animationSet);
    }

    /**
     * 启动淡出动画</br>
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startFadeOutAnimForTopicTipView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (mTopicTipView.getAlpha() < 0.1f) {
                return;
            }
        }

        AlphaAnimation alphaAnimationOut = new AlphaAnimation(1.0f, 0);
        alphaAnimationOut.setDuration(300);
        alphaAnimationOut.setFillAfter(true);
        mTopicTipView.startAnimation(alphaAnimationOut);
    }

    /**
     * 准备重新发送的数据，从“备忘录”中恢复发送失败的数据。</br>
     */
    private void prepareRepostData() {

        FeedItem feedItem = FeedMemento.restoreMemento(getApplicationContext());
        mEditText.setText(feedItem.text);
        mLocationTv.setText(feedItem.locationAddr);
        mImagePaths.clear();

        int count = feedItem.imageUrls.size();
        for (int i = 0; i < count; i++) {
            // 图片
            mImagePaths.add(feedItem.imageUrls.get(i).originImageUrl);
        }
        // 图片
        // mImagePaths.addAll(feedItem.imageUrls);
        if (mImagePaths.size() < 9) {
            mImagePaths.add(Constants.ADD_IMAGE_PATH_SAMPLE);
        }
        mImageSelectedAdapter.notifyDataSetChanged();
        // 好友
        mSelectFriends.addAll(feedItem.atFriends);
        // 话题
        mSelecteTopics.addAll(feedItem.topics);
        // TODO : 在这里需要设置话题和@好友不可点击.
        FeedViewUtils.parseTopicsAndFriends(mEditText, feedItem);

        // 设置光标位置
        mEditText.setSelection(mEditText.getText().length());
    }

    /**
     * 位置是否已经初始化</br>
     * 
     * @return
     */
    private boolean isLocationInited() {
        return mLocation != null && mLocationItems.size() > 1;
    }

    /**
     * 改变位置布局状态跟文本内容</br>
     */
    private void changeLocLayoutState() {
        mLocProgressBar.setVisibility(View.GONE);
        mLocIcon.setVisibility(View.VISIBLE);
        // 设置我的位置,我的位置放在第1个索引的位置
        if (mLocationItems.size() > 0 && mLocation != null) {
            mLocationTv.setText(mLocationItems.get(0).detail);
        } else {
            mLocationTv.setText(ResFinder.getString("umeng_comm_fetching_loc_failed"));
        }
    }

    /**
     * 获取地理位置信息。
     */
    protected void getMyLocation() {
        // 如果地理位置信息已经初始化，则不再重复获取
        if (isLocationInited()) {
            return;
        }
        mLocProgressBar.setVisibility(View.VISIBLE);
        mLocIcon.setVisibility(View.GONE);
        mLocationTv.setText(ResFinder.getString("umeng_comm_fetching_loc"));
        // 获取位置,得到位置以后获取位置的具体地址以及周边的地址
        LocationFinder.getInstance().findLocation(this,
                new SimpleFetchListener<Location>() {

                    @Override
                    public void onComplete(Location result) {
                        mLocation = result;

                        if (mLocation != null) {
                            // 获取详细的信息
                            getLocationDetailAddr();
                        } else {
                            // 修改位置信息的状态
                            changeLocLayoutState();
                        }

                    }
                });
    }

    /**
     * 获取地理位置详细信息</br>
     */
    private void getLocationDetailAddr() {
        mSdkImpl.getLocationAddr(mLocation,
                new SimpleFetchListener<LocationResponse>() {

                    @Override
                    public void onComplete(LocationResponse response) {
                        mLocationItems.clear();
                        mLocationItems.addAll(response.result);
                        // 修改位置信息的状态
                        changeLocLayoutState();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        // 获取我的位置
        getMyLocation();
        showInputMethod(mEditText);

        mImageLoader.resume();
    }

    private void showKeyboard() {
        mFragmentLatout.setVisibility(View.GONE);
        showInputMethod(mEditText);
    }

    /**
     * 初始化EditView并设置回调</br>
     */
    private void initEditView() {

        mEditText = (FeedEditText) findViewById(ResFinder.getId(
                "umeng_comm_post_msg_edittext"));
        mEditText.setFocusableInTouchMode(true);
        mEditText.requestFocus();
        mEditText.setMinimumHeight(DeviceUtils.dp2px(this, 150));

        mEditText.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mEditText.mCursorIndex = mEditText.getSelectionStart();
                mFragmentLatout.setVisibility(View.GONE);
                mTopicButton.setChecked(false);
                mPhotoButton.setChecked(false);
            }
        });

        mEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1) {
                    String newChar = s.subSequence(start, start + count).toString();
                    // 转发时不显示话题
                    if (CHAR_WELL.equals(newChar) && !isForwardFeed) {
                        showTopicFragment();
                    } else if (CHAR_AT.equals(newChar)) {
                        showAtFriendsDialog();
                    }
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
     * 从TextView获取位置text。该值是在获取地理位置信息时设置。</br>
     * 
     * @return
     */
    protected String getLocationAddr() {
        String locString = mLocationTv.getText().toString().trim();
        String fetchFailed = ResFinder.getString("umeng_comm_fetching_loc_failed");
        String fetching = ResFinder.getString("umeng_comm_fetching_loc");
        String dontShowLoc = ResFinder.getString("umeng_comm_text_dont_show_location");
        // 该判断需要重构
        if (locString.equals(fetchFailed)
                || locString.equals(fetching)
                || locString.equals("")
                || locString.equals(dontShowLoc)
                || mLocationLayout.getVisibility() == View.INVISIBLE) {
            return "";
        }
        return locString;
    }

    /**
     * 准备feed数据</br>
     */
    protected FeedItem prepareFeed() {
        FeedItem mNewFeed = new FeedItem();
        mNewFeed.text = mEditText.getText().toString().trim();
        mNewFeed.locationAddr = getLocationAddr();
        mNewFeed.location = mLocation;

        // 移除添加图标
        mImagePaths.remove(Constants.ADD_IMAGE_PATH_SAMPLE);

        for (String url : mImagePaths) {
            // 图片地址
            mNewFeed.imageUrls.add(new ImageItem("", "", url));
        }

        // 话题
        mNewFeed.topics.addAll(mSelecteTopics);
        // @好友
        mNewFeed.atFriends.addAll(mSelectFriends);
        // 发表的用户
        mNewFeed.creator = CommConfig.getConfig().loginedUser;
        mNewFeed.type = mNewFeed.creator.permisson == Permisson.ADMIN ? 1 : 0;
        Log.d(TAG, " @@@ my new Feed = " + mNewFeed);
        return mNewFeed;
    }

    /**
     * 清除状态</br>
     */
    public void clearState() {
        mEditText.setText("");
        mEditText.mAtMap.clear();
        mEditText.mTopicMap.clear();
        mImagePaths.clear();
        mTopicButton.setChecked(false);
        mPhotoButton.setChecked(false);
    }

    /**
     * 检查分享内容是否有效</br>
     * 
     * @return
     */
    protected boolean hasContent() {
        return mEditText.getText().toString().trim().length() > 0;
    }

    /**
     * 发布新的feed</br>
     */
    private void executePostFeed() {

        // 发布feed
        final FeedItem feedItem = prepareFeed();
        doPostFeed(feedItem);
        // 保存这次要提交的数据，用于发送失败时的重新发送
        FeedMemento.createMemento(getApplicationContext(), feedItem);

        // 清除状态
        clearState();

        final String title = ResFinder.getString("umeng_comm_send_ing");
        PostNotifycation.showPostNotifycation(this, title, feedItem.text);
    }

    protected void doPostFeed(final FeedItem feedItem) {
        mSdkImpl.postFeed(feedItem, new SimpleFetchListener<FeedItemResponse>() {

            @Override
            public void onComplete(FeedItemResponse response) {
                postFeedResponse(response, response.result);
            }
        });
    }

    protected void postFeedResponse(FeedItemResponse response, FeedItem feedItem) {
        final Context appContext = getApplicationContext();
        if (handlerResponse(response)) {
            return;
        }

        if (response.errCode == Constants.NO_ERROR) {
            ToastMsg.showShortMsg(appContext,
                    ResFinder.getString("umeng_comm_send_success"));

            PostNotifycation.clearPostNotifycation(appContext);
            FeedMemento.clear(appContext);
            // 发送广播
            sendPostSuccessBroadcast(appContext, feedItem);
        } else {
            String failedTitle = ResFinder.getString(
                    "umeng_comm_send_failed");
            PostNotifycation.showPostNotifycation(appContext, failedTitle,
                    feedItem.text);
        }
    }

    /**
     * 发送广播
     * 
     * @param context
     */
    protected void sendPostSuccessBroadcast(Context context, FeedItem feedItem) {
        Intent intent = new Intent(NotifyBroadcastReceiver.FEED_POSTED);
        intent.putExtra(Constants.FEED, feedItem);
        context.sendBroadcast(intent);
    }

    /**
     * 发送feed。对发送的feed做验证、发送feed、关闭输入法、跳转到主feed页面</br>
     */
    private void postFeed() {
        if (!hasContent()) {
            ToastMsg.showShortMsg(this, ResFinder.getString("umeng_comm_no_content"));
            return;
        }
        executePostFeed();
        hideInputMethod(mEditText);
        if (isRepost) {
            mSdkImpl.openCommunity(this);
        }
        finish();
    }

    /**
     * 用户点击back按钮。</br>
     */
    private void dealBackLogic() {
        mFragmentLatout.setVisibility(View.GONE);
        hideInputMethod(mEditText);
        if (isRepost) {
            mSdkImpl.openCommunity(this);
        }
        this.finish();
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (ResFinder.getId("umeng_comm_post_ok_btn") == id) { // 点击发送按钮
            postFeed();
        } else if (ResFinder.getId("umeng_comm_post_back_btn") == id) { // 点击back按钮
            dealBackLogic();
        } else if (ResFinder.getId("umeng_comm_take_photo_btn") == id) { // 拍照按钮
            this.takePhoto();
            changeButtonStatus(false, false);
        } else if (ResFinder.getId("umeng_comm_select_location_btn") == id) { // 选择位置
            showLocPickerDlg();
            changeButtonStatus(false, false);
        } else if (ResFinder.getId("umeng_comm_add_image_btn") == id) { // 添加图片
            showPickPhotoFragment();
            changeButtonStatus(true, false);
        } else if (ResFinder.getId("umeng_comm_at_friend_btn") == id) { // @好友
            showAtFriendsDialog();
            changeButtonStatus(false, false);
        } else if (ResFinder.getId("umeng_comm_pick_topic_btn") == id) { // 选择话题
            // executeDismissAnimForTopicTip();
            showTopicFragment();
            changeButtonStatus(false, true);
        }
    }

    // private void executeDismissAnimForTopicTip() {
    // mTopicTipView.clearAnimation();
    // AlphaAnimation animation = new AlphaAnimation(1.0f, 0);
    // animation.setDuration(50);
    // animation.setAnimationListener(new AnimationListener() {
    //
    // @Override
    // public void onAnimationStart(Animation animation) {
    // }
    //
    // @Override
    // public void onAnimationRepeat(Animation animation) {
    // }
    //
    // @Override
    // public void onAnimationEnd(Animation animation) {
    // mTopicTipView.setVisibility(View.GONE);
    // }
    // });
    // mTopicTipView.startAnimation(animation);
    // }

    /**
     * 设置PhotoButton跟TopicButton的选中状态</br>
     * 
     * @param photoSelected
     * @param topicSelected
     */
    private void changeButtonStatus(boolean photoSelected, boolean topicSelected) {
        mPhotoButton.setChecked(photoSelected);
        mTopicButton.setChecked(topicSelected);
    }

    /**
     * 显示选择图片的Fragment
     */
    private void showPickPhotoFragment() {
        mFragmentLatout.setVisibility(View.VISIBLE);
        hideInputMethod(mEditText);

        // if (mImageLoader instanceof DefaultImageLoader) {
        // DefaultImageLoader imageLoader = ((DefaultImageLoader) mImageLoader);
        // imageLoader.setLoadPolicy(new SerialLoadPolicy(imageLoader));
        // }
        mPhotoFragment.setSelectedImagePaths(mImagePaths);
        this.showFragment(mPhotoFragment);

        // 监听器
        mPhotoFragment.mOnClickListener = hideFragmentClickListener;
        mPhotoFragment.setImagePathListener(new FetchListener<List<String>>() {

            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(List<String> paths) {
                Iterator<String> iterator = paths.iterator();
                String path = "";
                while (iterator.hasNext()) {
                    path = iterator.next();
                    if (!mImagePaths.contains(path)) {
                        mImagePaths.add(path);
                    }
                }

                int index = mImagePaths.indexOf(Constants.ADD_IMAGE_PATH_SAMPLE);
                if (index >= 0 && index != mImagePaths.size() - 1) {
                    mImagePaths.add(mImagePaths.remove(index));
                }

                if (mImagePaths.size() >= 9) {
                    mImagePaths.remove(Constants.ADD_IMAGE_PATH_SAMPLE);
                }
                mImageSelectedAdapter.updateListViewData(mImagePaths);

                //
                showKeyboard();
            }
        });

    }

    /**
     * 显示选择话题的Fragment</br>
     */
    private void showTopicFragment() {
        mFragmentLatout.setVisibility(View.VISIBLE);
        hideInputMethod(mEditText);

        if (mTopicFragment == null) {
            mTopicFragment = new TopicPickerFragment();
        }
        showFragment(mTopicFragment);
        // 新增话题的回调
        mTopicFragment.addTopicListener(new ResultListener<Topic>() {

            @Override
            public void onRemove(Topic topic) {
                mEditText.removeTopic(topic);
            }

            @Override
            public void onAdd(Topic topic) {
                Log.d(TAG, "### topic = " + topic);
                if (!mEditText.mTopicMap.containsValue(topic)) {
                    removeChar('#');
                    List<Topic> topics = new ArrayList<Topic>();
                    topics.add(topic);
                    mEditText.insertTopics(topics);
                    mSelecteTopics.add(topic);
                    startFadeOutAnimForTopicTipView();
                }

                showKeyboard();
            }
        });

        // 删除话题时的回调
        mEditText.setTopicListener(new ResultListener<Topic>() {

            @Override
            public void onRemove(Topic topic) {
                mTopicFragment.uncheckTopic(topic);
                if (mEditText.mTopicMap.size() == 0 && !isForwardFeed) {
                    startAnimationForTopicTipView();
                }
            }

            @Override
            public void onAdd(Topic topic) {

            }
        });

    }

    // 隐藏Fragment的回调
    OnClickListener hideFragmentClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            mFragmentLatout.setVisibility(View.GONE);
        }
    };

    /**
     * 删除图片的回调
     */
    SimpleFetchListener<List<String>> taskResultListener = new SimpleFetchListener<List<String>>() {
        public void onComplete(List<String> data) {

            mImagePaths = data;
            List<String> selectedImage = mPhotoFragment.getSelectImagePaths();
            // 判断是否有删除的图片
            int size = mImagePaths.size();
            if (size < 9 && size != selectedImage.size() - 1) {
                Iterator<String> it = selectedImage.iterator();
                while (it.hasNext()) {
                    String imgPath = it.next();
                    if (!mImagePaths.contains(imgPath)) {
                        it.remove();
                    }
                }
                if (mPhotoFragment.getAdapter() != null) {
                    mPhotoFragment.getAdapter().notifyDataSetChanged();
                }
            }
        }
    };

    /**
     * 已选择图片显示的Adapter
     * 
     * @param listener 删除某张图片的回调
     */
    private void initSelectedImageAdapter(
            FetchListener<List<String>> listener) {
        // 默认显示一张“添加图片”的图片
        mImagePaths.add(Constants.ADD_IMAGE_PATH_SAMPLE);
        mImageSelectedAdapter = new ImageSelectedAdapter(PostFeedActivity.this,
                mImagePaths, listener);
        // 删除某张图片
        mImageSelectedAdapter.setOnRemoveListener(new OnRemoveListener() {

            @Override
            public void onRemove(int position, String item) {
                mPhotoFragment.removeImage(item);
            }
        });
        // 设置选择item时得背景为透明
        mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mGridView.setAdapter(mImageSelectedAdapter);
        mGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {
                String clickImageUrl = mImagePaths.get(position);
                boolean isfakeUrl = Constants.ADD_IMAGE_PATH_SAMPLE.equals(clickImageUrl);
                if (isfakeUrl) { // 如果触发的是添加图片事件，则显示选择图片的Fragment
                    showPickPhotoFragment();
                }
            }
        });

    }

    /**
     * 显示选择地理位置的Dialog</br>
     */
    private void showLocPickerDlg() {

        if (mLocationPickerDlg == null) {
            mLocationPickerDlg = new LocationPickerDlg(this, ResFinder.getStyle(
                    "umeng_comm_dialog_fullscreen"));
        }
        mLocationPickerDlg.setOwnerActivity(PostFeedActivity.this);
        mLocationPickerDlg.setupMyLocation(mLocation, mLocationItems);
        // 数据获取监听器
        mLocationPickerDlg.setDataListener(new SimpleFetchListener<LocationItem>() {

            @Override
            public void onComplete(LocationItem data) {
                if (data != null && !TextUtils.isEmpty(data.detail)) {
                    mLocationLayout.setVisibility(View.VISIBLE);
                    // 地理位置数据
                    mLocationTv.setText(data.detail);
                } else {
                    mLocationLayout.setVisibility(View.INVISIBLE);
                }

                // 显示输入框
                showKeyboard();
            }
        });
        mLocationPickerDlg.show();
    }

    /**
     * 显示@好友列表的Dialog</br>
     */
    private void showAtFriendsDialog() {

        if (mAtFriendDlg == null) {
            mAtFriendDlg = new AtFriendDialog(PostFeedActivity.this, ResFinder.getStyle(
                    "umeng_comm_dialog_fullscreen"));
        }
        mAtFriendDlg.setOwnerActivity(PostFeedActivity.this);
        // 数据获取监听器
        mAtFriendDlg.setDataListener(new SimpleFetchListener<CommUser>() {

            @Override
            public void onComplete(CommUser data) {

                if (data != null) {
                    removeChar('@');
                    mSelectFriends.add(data);
                    // 插入数据
                    mEditText.atFriends(mSelectFriends);
                }
                // // 显示输入框
                showKeyboard();
            }
        });

        mEditText.setResultListener(new ResultListener<CommUser>() {

            @Override
            public void onAdd(CommUser t) {

            }

            @Override
            public void onRemove(CommUser friend) {
                mSelectFriends.remove(friend);
            }
        });
        mAtFriendDlg.show();
    }

    /**
     * 移除字符</br>
     * 
     * @param c
     */
    private void removeChar(char c) {
        Editable editable = mEditText.getText();
        if (editable.length() <= 0) {
            return;
        }
        if (editable.charAt(editable.length() - 1) == c) {
            editable.delete(editable.length() - 1, editable.length());
        }
    }

    /**
     * 启动系统拍照功能
     */
    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        ComponentName componentName = takePictureIntent.
                resolveActivity(getPackageManager());
        if (componentName == null) { // 无拍照的App
            return;
        }
        // Create the File where the photo should go.
        // If you don't do you may get a crash in some devices.
        File photoFile = null;
        try {
            photoFile = createImageFile();
            Uri fileUri = Uri.fromFile(photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (IOException ex) {
            // Error occurred while creating the File
            ex.printStackTrace();
            ToastMsg.showShortMsg(this, "There was a problem saving the photo...");
        }
    }

    /**
     * Creates the image file to which the image must be saved.
     * 
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        SimpleDateFormat dateFormat = (SimpleDateFormat) SimpleDateFormat.getInstance();
        dateFormat.applyPattern("yyyyMMdd_HHmmss");
        String timeStamp = dateFormat.format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        // 检测目录是否存在
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        File image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
                );

        mNewImagePath = image.getAbsolutePath();
        return image;
    }

    /**
     * Add the picture to the photo gallery. Must be called on all camera images
     * or they will disappear once taken.
     */
    protected void addPhotoToGallery() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File imgFile = new File(mNewImagePath);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mNewImagePath, options);
        // 图片的有效性判断
        if (options.outWidth < 10 && options.outHeight < 10) {
            imgFile.delete();
            return;
        }
        Uri contentUri = Uri.fromFile(imgFile);
        mediaScanIntent.setData(contentUri);
        // 更新媒体库
        PostFeedActivity.this.sendBroadcast(mediaScanIntent);
        mImagePaths.remove(Constants.ADD_IMAGE_PATH_SAMPLE);
        if (mImagePaths.size() < 9) {
            mImagePaths.add(contentUri.toString());
            if (mImagePaths.size() < 9) {
                mImagePaths.add(Constants.ADD_IMAGE_PATH_SAMPLE);
            }
        } else {
            ToastMsg.showShortMsg(this, ResFinder.getString("umeng_comm_image_overflow"));
        }
        mImageSelectedAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        mFragmentLatout.setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 将拍照得到的图片添加到gallery中, 并且显示到GridView中
        addPhotoToGallery();
        super.onActivityResult(requestCode, resultCode, data);
    }

}
