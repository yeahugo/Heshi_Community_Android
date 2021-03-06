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
import android.text.TextUtils;
import android.view.View;

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.imageloader.ImgDisplayOption;
import com.umeng.comm.ui.adapters.viewparser.UserViewParser;
import com.umeng.comm.ui.adapters.viewparser.UserViewParser.UserViewHolder;

/**
 * 用于GridView或者ListView显示用户信息的适配器.
 * 
 * @author mrsimple
 */
public class UserAdapter extends CommonAdapter<CommUser, UserViewHolder> {

    public UserAdapter(Context context, List<CommUser> data) {
        super(context, data, new UserViewParser());
    }

    @Override
    protected void setItemData(int position, UserViewHolder holder, View rootView) {
        final CommUser user = getItem(position);
        holder.mTextView.setText(user.name);

        ImgDisplayOption option = ImgDisplayOption.getOptionByGender(user.gender);
        if (!TextUtils.isEmpty(user.iconUrl)) {
            mImageLoader.displayImage(user.iconUrl, holder.mImageView, option);
        } else {
            holder.mImageView.setImageResource(option.mLoadingResId);
        }
    }
}
