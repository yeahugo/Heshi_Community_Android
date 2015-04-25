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

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ToggleButton;

import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.activities.TopicDetailActivity;
import com.umeng.comm.ui.adapters.viewparser.TopicViewParser;
import com.umeng.comm.ui.adapters.viewparser.TopicViewParser.TopicViewHolder;
import com.umeng.comm.ui.adapters.viewparser.ViewParser;
import com.umeng.comm.ui.utils.FontUtils;

/**
 * 
 */
public class TopicAdapter extends BackupAdapter<Topic, TopicViewHolder> {

    private FollowListener<Topic> mListener;
    static final String TOPIC_TAG = "#";
    private static final String NULL = "null";

    /**
     * 
     */
    public TopicAdapter(Context context, List<Topic> topics) {
        super(context, topics, new TopicViewParser());
    }
    
    public TopicAdapter(Context context, List<Topic> topics,ViewParser viewParser) {
        super(context, topics, viewParser);
    }

//    protected TopicAdapter(Context context, List<Topic> data, ViewParser viewParser) {
//        
//    }

    @Override
    protected void setItemData(int position, TopicViewHolder holder, View rootView) {
        final Topic topic = getItem(position);
        holder.mTopicTv.setText(topic.name);
        String desc = topic.desc;
        if (TextUtils.isEmpty(desc) || desc.equals(NULL)) {
            desc = ResFinder.getString("umeng_comm_topic_no_desc");
        }
        holder.mDescTv.setText(desc);
        // 设置此话题是否被用户关注
        holder.mFollowedBtn.setChecked(topic.isFocused);
        // 设置点击话题的事件处理，跳转到话题详情页面
        holder.mTopicTv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                gotoTopicDetailActivity(topic);
            }
        });
        final ToggleButton tmpToggleButton = holder.mFollowedBtn;
        holder.mFollowedBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mListener.onFollowOrUnFollow(topic, tmpToggleButton, tmpToggleButton.isChecked());
            }
        });
        holder.mView.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                gotoTopicDetailActivity(topic);
            }
        });

        FontUtils.changeTypeface(rootView);
    }
    
    private void gotoTopicDetailActivity(Topic topic){
        Intent intent = new Intent(mContext, TopicDetailActivity.class);
        intent.putExtra(Constants.TAG_TOPIC, topic);
        mContext.startActivity(intent);
    }

    public void setFollowListener(FollowListener<Topic> listener) {
        this.mListener = listener;
    }

    public static interface FollowListener<T> {
        public void onFollowOrUnFollow(T t, ToggleButton toggleButton, boolean isFollow);
    }
}
