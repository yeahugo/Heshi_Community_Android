/**
 * 
 */

package com.umeng.comm.ui.adapters;

import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;

import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.utils.DeviceUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.activities.TopicDetailActivity;
import com.umeng.comm.ui.adapters.TopicAdapter.FollowListener;
import com.umeng.comm.ui.adapters.viewparser.ActiveUserViewParser;
import com.umeng.comm.ui.adapters.viewparser.ActiveUserViewParser.ActiveUserViewHolder;

/**
 * 推荐话题的Adapter
 */
public class RecommendTopicAdapter extends BackupAdapter<Topic, ActiveUserViewHolder> {

    private int mTopicColor = 0;

    /**
     * 推荐话题的显示样式跟推荐用户的样式相同
     * 
     * @param context
     * @param topics
     */
    public RecommendTopicAdapter(Context context, List<Topic> topics) {
        super(context, topics, new ActiveUserViewParser());
        mFeedsStr = ResFinder.getString("umeng_comm_feeds_num");
        mFansStr = ResFinder.getString("umeng_comm_fans_num");
        mTopicColor = ResFinder.getColor("umeng_comm_text_topic_light_color");
    }

    private FollowListener<Topic> mListener;
    static final String TOPIC_TAG = "#";
    // private static final String NULL = "null";
    private static String mFeedsStr;
    private static String mFansStr;
    private static final String DIVIDER = " / ";
    private boolean isFromFindPage = false;// 是否来自于发现页面。对于来自发现页面需要单独处理，

    @Override
    protected void setItemData(int position, final ActiveUserViewHolder holder, View rootView) {
        final Topic topic = getItem(position);
        // 用户昵称
        holder.muserNameTextView.setText(topic.name);
        holder.muserNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        // 隐藏用户头像
        holder.mImageView.setVisibility(View.GONE);
        // ImgDisplayOption option =
        // ImgDisplayOption.getOptionByGender(user.gender);
        // if (!TextUtils.isEmpty(user.iconUrl)) {
        // mImageLoader.displayImage(user.iconUrl, holder.mImageView, option);
        // } else {
        // holder.mImageView.setImageResource(option.mLoadingResId);
        // }

        // 隐藏用户性别view
        holder.mGenderImageView.setVisibility(View.GONE);
        // if ( user.gender == Gender.MALE ) {
        // holder.mGenderImageView.setImageResource(ResFinder.getResourceId(ResType.DRAWABLE,
        // "umeng_comm_gender_male"));
        // } else {
        // holder.mGenderImageView.setImageResource(ResFinder.getResourceId(ResType.DRAWABLE,
        // "umeng_comm_gender_female"));
        // }

        // 设置消息数跟粉丝数
        holder.mMsgFansTextView.setText(buildMsgFansStr(topic.feedCount, topic.fansCount));

        // 设置关注状态跟点击事件
        holder.mToggleButton.setChecked(topic.isFocused);
        holder.mToggleButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mListener.onFollowOrUnFollow(topic, holder.mToggleButton,
                        holder.mToggleButton.isChecked());
            }
        });

        if (isFromFindPage) {
            holder.muserNameTextView.setTextColor(mTopicColor);
            rootView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    gotoTopicDetailPage(topic);
                }
            });
        }

        int left = holder.mView.getLeft() + DeviceUtils.dp2px(mContext, 4);
        int top = holder.mView.getLeft();
        int right = holder.mView.getLeft();
        int bottom = holder.mView.getLeft();

        holder.mView.setPadding(left, top, right, bottom);
    }

    /**
     * 跳转到话题详情页面</br>
     * 
     * @param topic
     */
    private void gotoTopicDetailPage(Topic topic) {
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName(mContext, TopicDetailActivity.class);
        intent.setComponent(componentName);
        intent.putExtra(Constants.TAG_TOPIC, topic);
        ((Activity) mContext).startActivity(intent);
    }

    private String buildMsgFansStr(long feedCount, long fansCount) {
        StringBuilder builder = new StringBuilder(mFeedsStr);
        builder.append(feedCount);
        builder.append(DIVIDER).append(mFansStr);
        builder.append(fansCount);
        return builder.toString();
    }

    public void setFollowListener(FollowListener<Topic> listener) {
        this.mListener = listener;
    }

    /**
     * 设置是否来自于发送页面</br>
     * 
     * @param fromFind
     */
    public void setFromFindPage(boolean fromFind) {
        isFromFindPage = fromFind;
    }

    // public static interface FollowListener<T> {
    // public void onFollowOrUnFollow(T t, ToggleButton toggleButton, boolean
    // isFollow);
    // }

}
