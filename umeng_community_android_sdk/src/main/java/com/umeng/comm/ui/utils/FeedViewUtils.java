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

import android.R;
import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
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
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.activities.TopicDetailActivity;
import com.umeng.comm.ui.activities.UserInfoActivity;
import com.umeng.comm.ui.utils.textspan.AbsClickSpan;
import com.umeng.comm.ui.widgets.TextViewFixTouchConsume;

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

	private static Pattern mPattern;
	private static Context mContext;
	private static String[] mSmileyTexts;
	private static HashMap<String, Integer> mSmileyToRes;  

//	public static final int[] DEFAULT_SMILEY_RES_IDS = {  
//        R.drawable.aini,  
//        R.drawable.aoteman,  
//        R.drawable.baibai,  
//        R.drawable.baobao,  
//        R.drawable.beiju,  
//        R.drawable.beishang,  
//        R.drawable.bianbian,  
//        R.drawable.bishi,  
//        R.drawable.bizui,  
//        R.drawable.buyao,  
//        R.drawable.chanzui,  
//    };
	
//	private static HashMap<String, Integer> buildSmileyToRes() {  
//        if (DEFAULT_SMILEY_RES_IDS.length != mSmileyTexts.length) {  
//            throw new IllegalStateException("Smiley resource ID/text mismatch");  
//        }  
//  
//        HashMap<String, Integer> smileyToRes = new HashMap<String, Integer>(mSmileyTexts.length);  
//        for (int i = 0; i < mSmileyTexts.length; i++) {  
//            smileyToRes.put(mSmileyTexts[i], DEFAULT_SMILEY_RES_IDS[i]);  
//        }  
//  
//        return smileyToRes;  
//    } 
	
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
//        renderEmojis(context, item, contentSsb);
        // 多一个空格
        contentSsb.append(" ");
        //
        contentTextView.setText(contentSsb);
    }
    
    //构建正则表达式
    private static Pattern buildPattern() {
        StringBuilder patternString = new StringBuilder(mSmileyTexts.length * 3);
        patternString.append('(');
        for (String s : mSmileyTexts) {
            patternString.append(Pattern.quote(s));
            patternString.append('|');
        }
        patternString.replace(patternString.length() - 1, patternString.length(), ")");

        return Pattern.compile(patternString.toString());
    }
    
//    private static void renderEmojis(Context context, FeedItem feedItem, SpannableStringBuilder contentSsb)
//    {
//        Spannable spannable = contentSsb.newSpannable(feedItem.text);
//        char c = str.charAt(0);
//        Pattern p = Pattern.compile("([\ud83d\ude01-\ud83d\ude45])");
//        Matcher m = p.matcher(str);
//        while (m.find()) {
//            if (mSpannables.get(m.group()) == null) {
//                Bitmap b = BitmapFactory.decodeResource(myApp.getAppContext().getResources(), R.drawable.u0033);
//                ImageSpan imp = new ImageSpan(Bitmap.createScaledBitmap(b, 70, 70, false));
//                mSpannables.put(m.group(), imp);
//            }
//            spannable.setSpan(mSpannables.get(m.group()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        }
//        return spannable;
//    }
//        mPattern = buildPattern();
//    	Matcher matcher = mPattern.matcher(feedItem.text);
//    	while(matcher.find()){
//    		int resId = mSmileyToRes.get(matcher.group());
//    		contentSsb.setSpan(new ImageSpan(mContext, resId),matcher.start(), matcher.end(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//    	}
//    }

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
