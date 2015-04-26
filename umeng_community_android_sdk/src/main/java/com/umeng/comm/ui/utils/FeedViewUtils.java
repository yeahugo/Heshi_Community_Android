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

package com.umeng.comm.ui.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.text.style.ImageSpan;

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.LoginResponse;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.R;
import com.umeng.comm.ui.activities.TopicDetailActivity;
import com.umeng.comm.ui.activities.UserInfoActivity;
import com.umeng.comm.ui.utils.textspan.AbsClickSpan;
import com.umeng.comm.ui.widgets.TextViewFixTouchConsume;

import com.keyboard.utils.EmoticonsUtils;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;

import java.util.regex.*;
import java.util.HashMap;

/**
 * 包装显示Feed的View
 * 
 * @author mrsimple
 */
public final class FeedViewUtils {

	private static Context mContext;
    public static final Pattern EMOTION_URL = Pattern.compile("\\[(\\S+?)\\]");
    private static String[] mSmileyToRes;

    static {
        mSmileyToRes = EmoticonsUtils.xhsemojiArray;
    }

    /**
     * 渲染话题跟好友</br>
     * 
     * @param activity
     * @param contentTextView
     * @param item
     */
    public static void parseTopicsAndFriends(
            final TextView contentTextView, FeedItem item) {
        contentTextView.setClickable(true);
        contentTextView.setMovementMethod(LinkMovementMethod.getInstance());
        // 消息文本内容
        contentTextView.setText(item.text);
        // 文本内容
        final String content = item.text;
        final Context context = contentTextView.getContext();

        SpannableStringBuilder contentSsb = new SpannableStringBuilder(content);
        // 添加话题
        renderTopics(context, item, contentSsb);
        // 渲染好友
        renderFriends(context, item, contentSsb);

//        // 渲染表情
        renderEmojis(context, item, contentSsb);
        // 多一个空格
        contentSsb.append(" ");
        //
        contentTextView.setText(contentSsb);
    }
    
    //构建正则表达式

    public static int getResId(String resName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    
    private static void renderEmojis(Context context, FeedItem feedItem, SpannableStringBuilder contentSsb)
    {
        Matcher matcher = EMOTION_URL.matcher(feedItem.text);

        while(matcher.find()){
            String matchString = matcher.group(0);
            Log.d("Debug:::::::",matchString);
            for (String resNameString : mSmileyToRes){
                String[] textArray = resNameString.split(",");
                String emojiName = textArray[1];
                if (emojiName.equalsIgnoreCase(matchString)){
                    String emojiId = textArray[0];
                    Log.d("resName ",emojiId);
                    emojiId = emojiId.substring(0,emojiId.length()-4);
                    int resourceId= getResId(emojiId,R.drawable.class);
                    if (resourceId > 0)
                    {
                        Drawable dr = context.getResources().getDrawable(resourceId);
                        dr.setBounds(0, 0, dr.getIntrinsicWidth()*4/5, dr.getIntrinsicHeight()*4/5);
                        ImageSpan imageSpan = new ImageSpan(dr);
                        contentSsb.setSpan(imageSpan,matcher.start(), matcher.end(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        break;
                    }
                }
            }
        }
    }

    /**
     * 渲染好友文本</br>
     * 
     * @param activity
     * @param friends
     * @param contentSsb
     * @param content
     */
    private static void renderFriends(Context context, FeedItem feedItem,
            SpannableStringBuilder contentSsb) {
        String name = null;
        // int start = 0;
        for (CommUser friend : feedItem.atFriends) {
            name = "@" + friend.name;
            List<DecorationItem> items = findTagsInText(feedItem.text, name);
            for (DecorationItem decoratorItem : items) {
                makeStringClickable(contentSsb, decoratorItem.start, decoratorItem.text,
                        new UserClickSpan(
                                context, friend));
            }
        }
    }

    private static List<DecorationItem> findTagsInText(String fullString, String tag) {
        int lastIndex = 0;
        List<DecorationItem> decoratorItems = new LinkedList<DecorationItem>();
        while (lastIndex != -1) {
            lastIndex = fullString.indexOf(tag, lastIndex);
            if (lastIndex != -1) {
                decoratorItems.add(new DecorationItem(lastIndex, tag));
                lastIndex += tag.length();
            }
        }

        return decoratorItems;
    }

    /**
     * 渲染评论中</br>
     * 
     * @param activity
     * @param textView
     * @param users
     */
    public static void renderFriendText(Context context, TextView textView, List<CommUser> users) {
        textView.setClickable(true);
        textView.setMovementMethod(TextViewFixTouchConsume.LocalLinkMovementMethod.getInstance());
        String content = textView.getText().toString();
        SpannableStringBuilder contentSsb = new SpannableStringBuilder(content);
        String name = "";
        // int start = -1;
        for (CommUser user : users) {
            name = user.name;

            List<DecorationItem> items = findTagsInText(content, name);
            for (DecorationItem decorationItem : items) {
                makeStringClickable(contentSsb, decorationItem.start, decorationItem.text,
                        new UserClickSpan(context, user));
            }
        }
        textView.setText(contentSsb);
    }

    /**
     * 渲染话题文本</br>
     * 
     * @param activity
     * @param topics
     * @param contentSsb
     * @param content
     */
    private static void renderTopics(final Context context, FeedItem feedItem,
            SpannableStringBuilder contentSsb) {
        for (final Topic topic : feedItem.topics) {
            String name = topic.name;
            if (TextUtils.isEmpty(name)) {
                continue;
            }

            // start = feedItem.text.indexOf(name);
            // if (start >= 0) {
            // makeStringClickable(contentSsb, start, name, new
            // TopicClickSpan(context, topic));
            // }

            List<DecorationItem> items = findTagsInText(feedItem.text, name);
            for (DecorationItem decoratorItem : items) {
                makeStringClickable(contentSsb, decoratorItem.start, decoratorItem.text,
                        new TopicClickSpan(context, topic));
            }

        }
    }

    /**
     * @param tv
     * @param start
     * @param text
     * @param clickableSpan
     */
    private static void makeStringClickable(SpannableStringBuilder contentSsb, int start,
            final String text,
            ClickableSpan clickableSpan) {
        contentSsb.setSpan(clickableSpan, start, start + text.length(), 0);
    }

    static class TopicClickSpan extends AbsClickSpan {

        Topic mTopic;
        Context mContext;

        public TopicClickSpan(Context context, Topic topic) {
            mTopic = topic;
            mContext = context;
        }

        @Override
        public void onClick(final View widget) {
            CommonUtils.checkLoginAndFireCallback(mContext,
                    new SimpleFetchListener<LoginResponse>() {

                        @Override
                        public void onComplete(LoginResponse response) {
                            if (response.errCode == Constants.NO_ERROR) {
                                Intent intent = new Intent(mContext,
                                        TopicDetailActivity.class);
                                intent.putExtra(Constants.TAG_TOPIC, mTopic);
                                mContext.startActivity(intent);
                            } else {
                                ToastMsg.showShortMsgByResName(widget.getContext(),
                                        "umeng_comm_login_failed");
                            }
                        }
                    });
        }
    }

    static class UserClickSpan extends AbsClickSpan {
        CommUser mUser;
        Context mContext;

        public UserClickSpan(Context context, CommUser user) {
            mUser = user;
            mContext = context;
        }

        @Override
        public void onClick(final View widget) {
            CommonUtils.checkLoginAndFireCallback(mContext,
                    new SimpleFetchListener<LoginResponse>() {

                        @Override
                        public void onComplete(LoginResponse response) {
                            if (response.errCode == Constants.NO_ERROR) {
                                Intent intent = new Intent(mContext,
                                        UserInfoActivity.class);
                                intent.putExtra(Constants.TAG_USER, mUser);
                                mContext.startActivity(intent);
                            } else {
                                ToastMsg.showShortMsgByResName(widget.getContext(),
                                        "umeng_comm_login_failed");
                            }
                        }
                    });
        }

    }

    static class DecorationItem {
        int start;
        String text;

        public DecorationItem(int start, String text) {
            this.start = start;
            this.text = text;
        }
    }

}
