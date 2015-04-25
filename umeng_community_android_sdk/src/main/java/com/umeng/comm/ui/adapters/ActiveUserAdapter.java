/**
 * 
 */

package com.umeng.comm.ui.adapters;

import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.CommUser.Gender;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.imageloader.ImgDisplayOption;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ResFinder.ResType;
import com.umeng.comm.ui.activities.UserInfoActivity;
import com.umeng.comm.ui.adapters.TopicAdapter.FollowListener;
import com.umeng.comm.ui.adapters.viewparser.ActiveUserViewParser;
import com.umeng.comm.ui.adapters.viewparser.ActiveUserViewParser.ActiveUserViewHolder;

/**
 * 
 */
public class ActiveUserAdapter extends
        CommonAdapter<CommUser, ActiveUserViewParser.ActiveUserViewHolder> {

    private FollowListener<CommUser> mFollowListener;
    private static String mFeedsStr;
    private static String mFansStr;
    private static final String DIVIDER = " / ";
    private boolean isFromFindPage;// 是否来自于发现页面。如果来自发现页面，则需要单独的逻辑处理

    /**
     * @param context
     * @param data
     */
    public ActiveUserAdapter(Context context, List<CommUser> data) {
        super(context, data, new ActiveUserViewParser());
        mFeedsStr = ResFinder.getString("umeng_comm_feeds_num");
        mFansStr = ResFinder.getString("umeng_comm_fans_num");
    }

    @Override
    protected void setItemData(int position, final ActiveUserViewHolder holder, View rootView) {
        final CommUser user = mData.get(position);
        // 用户昵称
        holder.muserNameTextView.setText(user.name);
        // 用户头像
        ImgDisplayOption option = ImgDisplayOption.getOptionByGender(user.gender);
        if (!TextUtils.isEmpty(user.iconUrl)) {
            mImageLoader.displayImage(user.iconUrl, holder.mImageView, option);
        } else {
            holder.mImageView.setImageResource(option.mLoadingResId);
        }

        // 用户性别
        if (user.gender == Gender.MALE) {
            holder.mGenderImageView.setImageResource(ResFinder.getResourceId(ResType.DRAWABLE,
                    "umeng_comm_gender_male"));
        } else {
            holder.mGenderImageView.setImageResource(ResFinder.getResourceId(ResType.DRAWABLE,
                    "umeng_comm_gender_female"));
        }

        // 设置消息数跟粉丝数
        holder.mMsgFansTextView.setText(buildMsgFansStr(user.extraData));

        // 设置关注状态跟点击事件
        holder.mToggleButton.setChecked(user.extraData.getBoolean(Constants.IS_FOCUSED));
        holder.mToggleButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mFollowListener.onFollowOrUnFollow(user, holder.mToggleButton,
                        holder.mToggleButton.isChecked());
            }
        });

        if (isFromFindPage) {
            rootView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    ComponentName componentName = new ComponentName(mContext,
                            UserInfoActivity.class);
                    intent.setComponent(componentName);
                    intent.putExtra(Constants.TAG_USER, user);
                    ((Activity) mContext).startActivity(intent);
                }
            });
        }

    }

    /**
     * 
     * 是否来自于发现页面</br>
     * @param fromFindPage
     */
    public void setFromFindPage( boolean fromFindPage){
        isFromFindPage = fromFindPage;
    }
    
    private String buildMsgFansStr(Bundle bundle) {
        StringBuilder builder = new StringBuilder(mFeedsStr);
        builder.append(bundle.getInt(Constants.MY_FEED_COUNT));
        builder.append(DIVIDER).append(mFansStr);
        builder.append(bundle.getInt(Constants.MY_FANS_COUNT));
        return builder.toString();
    }

    public void setFollowListener(FollowListener<CommUser> listener) {
        this.mFollowListener = listener;
    }

}
