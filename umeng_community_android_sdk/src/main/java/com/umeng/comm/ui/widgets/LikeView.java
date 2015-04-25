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

package com.umeng.comm.ui.widgets;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.Like;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.LoginResponse;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.core.utils.ResFinder.ResType;
import com.umeng.comm.ui.activities.UserInfoActivity;
import com.umeng.comm.ui.utils.textspan.AbsClickSpan;

import java.util.List;

/**
 * 在Feed ListView的每项feed中的赞视图,该视图包含一个红心图标,后面加上最多14个发出赞的,然后加上其他文本信息.
 * <p>
 * 该视图继承自{@link android.widget.TextView},使用
 * {@link android.text.SpannableStringBuilder}将图片和文本内存 连接在一起,形成图文混合内容.
 * 
 * @author mrsimple
 */
public class LikeView extends TextView {

    /**
     * LIKE图标的占位符
     */
    private static final String LIKE_IMAGE_TEXT_PLACEHOLDER = "p ";
    /**
     * 最多显示14个赞
     */
    private static final int MAX_LIKE_COUNT = 14;

    /**
     * @param context
     */
    public LikeView(Context context) {
        this(context, null);
    }

    public LikeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LikeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * 封装赞的TextView
     * 
     * @param str
     * @return
     */
    public void addLikes(List<Like> likes) {
        //
        this.setMovementMethod(LinkMovementMethod.getInstance());

        // 可追加的SpannableStringBuilder
        SpannableStringBuilder ssb = new SpannableStringBuilder(insertLikeIcon());
        int likeCount = likes.size();
        // 最多显示14个like
        int maxNum = Math.min(likeCount, MAX_LIKE_COUNT);
        // 起始点为LIKE占位符的长度,即LIKE图标加上一个空格的长度.
        int start = LIKE_IMAGE_TEXT_PLACEHOLDER.length();
        for (int i = 0; i < maxNum; i++) {
            Like like = likes.get(i);
            if (!isLikeValid(like)) {
                continue;
            }
            final CommUser user = like.creator;
            // 追加上用户的名字
            ssb.append(user.name);
            // 设置点击like创建者文字时的处理函数
            setClickTheLikeUser(ssb, start, user);
            // 以顿号相隔开, 最后一个不添加顿号
            if (i != maxNum - 1) {
                ssb.append("、");
            }
            start += user.name.length() + 1;
        }

        if (likeCount > MAX_LIKE_COUNT) {
            ssb.append(" ... ");
        }

        // 更新文本
        this.setText(ssb);
    } // end of addLikes

    /**
     * 插入Like view前面的图标,其中图标站一个字符的位置,另一个字符为空格.
     * 
     * @return
     */
    private SpannableString insertLikeIcon() {
        // 图片
        ImageSpan span = new ImageSpan(getContext(), ResFinder.getResourceId(ResType.DRAWABLE,
                "umeng_comm_like_icon"));
        SpannableString spanStr = new SpannableString(LIKE_IMAGE_TEXT_PLACEHOLDER);
        spanStr.setSpan(span, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        return spanStr;
    }

    private void setClickTheLikeUser(SpannableStringBuilder ssb, int start, final CommUser user) {
        ssb.setSpan(new AbsClickSpan() {

            @Override
            public void onClick(View widget) {
                clickTheLikeUserText(user);
            }

        }, start, start + user.name.length(), 0);
    }

    private void clickTheLikeUserText(final CommUser user) {
        CommonUtils.checkLoginAndFireCallback(getContext(),
                new SimpleFetchListener<LoginResponse>() {

                    @Override
                    public void onComplete(LoginResponse response) {
                        if (response.errCode == Constants.NO_ERROR) {
                            Intent intent = new Intent(getContext(), UserInfoActivity.class);
                            intent.putExtra(Constants.TAG_USER, user);
                            getContext().startActivity(intent);
                        } else {
                            ToastMsg.showShortMsgByResName(getContext(),
                                    "umeng_comm_login_failed");
                        }
                    }
                });
    }

    /**
     * like是否有效
     * 
     * @param like
     * @return 如果有效则返回true,否则返回false.
     */
    private boolean isLikeValid(Like like) {
        return like != null && like.creator != null && !TextUtils.isEmpty(like.creator.name);
    }
}
