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

package com.umeng.comm.ui.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.Comment;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.imageloader.ImgDisplayOption;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.LoginResponse;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.activities.UserInfoActivity;
import com.umeng.comm.ui.adapters.viewparser.FeedCommentViewParser;
import com.umeng.comm.ui.adapters.viewparser.FeedCommentViewParser.CommentViewHolder;
import com.umeng.comm.ui.utils.FeedViewUtils;
import com.umeng.comm.ui.widgets.NetworkImageView;

/**
 * @author mrsimple
 */
public class FeedCommentAdapter extends CommonAdapter<Comment, CommentViewHolder> {

    private String mColon;
    private String mReplyText;

    // private String mLoginUserId;

    public FeedCommentAdapter(Context context, List<Comment> data) {
        super(context, data, new FeedCommentViewParser());

        mColon = ResFinder.getString("umeng_comm_colon");
        mReplyText = ResFinder.getString("umeng_comm_reply");
//        mLoginUserId = CommConfig.getConfig().loginedUser.id;
    }

    private void renderCommentText(TextView textView, Comment comment) {
        // 设置评论昵称,普通形式为AA:评论内容,当回复回复某条评论时则是“AA回复BB”的形式
        String prefix = prepareCommentPrefix(comment);

        // 设置评论的内容
        textView.setText(prefix + comment.text);
        FeedViewUtils.renderFriendText(mContext, textView, prepareRelativeUsers(comment));
    }

    private void setCommentCreator(NetworkImageView imageView, Comment comment) {
        ImgDisplayOption option = ImgDisplayOption.getOptionByGender(comment.creator.gender);
        imageView.setImageUrl(comment.creator.iconUrl, option);
        // 设置头像的点击事件,跳转到用户个人主页
        setClickFriendIconListener(imageView, comment.creator);
    }

    /**
     * 评论或者回复评论时涉及到的用户,如果是普通评论则只涉及评论的创建者,或者是回复评论,那么还涉及到被回复的对象
     * 
     * @param comment
     * @return
     */
    private List<CommUser> prepareRelativeUsers(Comment comment) {
        List<CommUser> users = new ArrayList<CommUser>();
        users.add(comment.creator);
        if (isReplyCommemt(comment)) {
            users.add(comment.replyUser);
        }
        return users;
    }

    private boolean isReplyCommemt(Comment comment) {
        return comment.replyUser != null
                && !TextUtils.isEmpty(comment.replyUser.name);
    }

    private String prepareCommentPrefix(Comment comment) {
        String text = comment.creator.name;

        // 如果有回复用户且该用户不是自己
        if (isReplyCommemt(comment)) {
            text += mReplyText + comment.replyUser.name + mColon;
        } else {
            text += mColon;
        }

        return text;
    }

    /**
     * 判断评论用户是否是当前登录用户(自己)</br>
     * 
     * @param comment 评论
     * @return 如果评论用户是自己，返回TRUE；否则返回false
     */
    // private boolean isMyself(Comment comment) {
    // if (comment == null || comment.replyUser == null
    // || TextUtils.isEmpty(comment.replyUser.id)) {
    // return false;
    // }
    // return comment.replyUser.id.equals(mLoginUserId);
    // }

    /**
     * 设置点击评论中好友icon的逻辑，跳转至相关好友的个人中心
     * 
     * @param iconImageView 显示头像的ImageView
     * @param user 创建该评论的用户
     */
    private void setClickFriendIconListener(final ImageView iconImageView, final CommUser user) {
        iconImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 跳转用户中心前检查是否登录
                CommonUtils.checkLoginAndFireCallback(mContext,
                        new SimpleFetchListener<LoginResponse>() {

                            @Override
                            public void onComplete(LoginResponse response) {
                                if (response.errCode == Constants.NO_ERROR) {
                                    Intent intent = new Intent(mContext, UserInfoActivity.class);
                                    intent.putExtra(Constants.TAG_USER, user);
                                    mContext.startActivity(intent);
                                } else {
                                    ToastMsg.showShortMsgByResName(iconImageView.getContext(),
                                            "umeng_comm_login_failed");
                                }
                            }
                        });
            }
        });
    }

    @Override
    protected void setItemData(int position, CommentViewHolder holder, View rootView) {
        final Comment comment = getItem(position);
        // 渲染评论文本
        renderCommentText(holder.contentTextView, comment);
        // 设置评论创建者的头像和头像图标的点击事件
        setCommentCreator(holder.userHeaderImageView, comment);
    }
}
