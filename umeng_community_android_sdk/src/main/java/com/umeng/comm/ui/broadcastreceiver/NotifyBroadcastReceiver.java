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

package com.umeng.comm.ui.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 广播接收器,当用户修改个人信息、发布新的feed时将会发出相应的广播,通知相应的界面及时更新数据。[ 类似于事件总线 ]
 * 
 * @author mrsimple
 */
public class NotifyBroadcastReceiver extends BroadcastReceiver {

    /**
     * 发布新的feed的通知,使得ListView滚动到顶部
     */
    public static final String FEED_POSTED = "umeng.comm.posted.action";

    /**
     * 发布新的feed的通知,使得ListView滚动到顶部
     */
    public static final String FEED_DELETED = "umeng.comm.deleted.action";

    /**
     * 关注某个用户
     */
    public static final String FOLLOWED = "umeng.comm.followed.action";
    
    /**
     * 取消关注某个用户
     */
    public static final String CANCEL_FOLLOWED = "umeng.comm.cancel.followed.action";

    /**
     * 关注某个话题
     */
    public static final String TOPIC_FOLLOWED = "umeng.comm.topic.followed.action";

    /**
     * 取消关注某个话题
     */
    public static final String CANCEL_TOPIC_FOLLOWED = "umeng.comm.cancel.topic.followed.action";

    /**
     * 用户信息更改
     */
    public static final String USER_INFO_UPDATED = "umeng.comm.user.info.action";
    
    /**
     * 话题的状态改变。比如关注or取消关注
     */
    public static final String TOPIC_UPDATE = "umeng.comm.topic.info.action";

    /**
     * 
     */
    NotifyListener mNotifyListener;

    public NotifyBroadcastReceiver(NotifyListener listener) {
        mNotifyListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mNotifyListener != null) {
            mNotifyListener.onNotify(intent);
        }
    }

    public void setNotifyListener(NotifyListener listener) {
        mNotifyListener = listener;
    }

    /**
     * @author mrsimple
     */
    public static interface NotifyListener {
        public void onNotify(Intent intent);
    }

}
