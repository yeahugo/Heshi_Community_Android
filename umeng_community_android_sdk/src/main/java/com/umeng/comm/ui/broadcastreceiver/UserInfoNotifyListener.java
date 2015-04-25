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

import android.content.Intent;
import android.os.Bundle;

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.constants.HttpProtocol;
import com.umeng.comm.ui.broadcastreceiver.NotifyBroadcastReceiver.NotifyListener;

/**
 * 用户信息修改的监听器
 * 
 * @author mrsimple
 */
public abstract class UserInfoNotifyListener implements NotifyListener {

    protected CommUser newUser(Intent intent) {
        Bundle extra = intent.getExtras();
        if (extra != null && extra.containsKey(HttpProtocol.USER_INFO_KEY)) {
            return (CommUser) extra.get(HttpProtocol.USER_INFO_KEY);
        }

        return null;
    }
}
