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
import android.view.View;

import com.umeng.comm.ui.adapters.viewparser.FriendItemViewParser;
import com.umeng.comm.ui.adapters.viewparser.FriendItemViewParser.ImgTextViewHolder;

/**
 * 
 */
public abstract class PickerAdapter<T> extends BackupAdapter<T,ImgTextViewHolder> {


    /**
     * @param friList
     */
    public PickerAdapter(Context context, List<T> friList) {
        super(context, friList,new FriendItemViewParser());
    }

    private void reset(ImgTextViewHolder viewHolder) {
        // viewHolder.mCheckBox.setChecked(false);
        viewHolder.mTextView.setText("");
        viewHolder.mDetailTextView.setText("");
    }

    /**
     * 填充每项的数据
     * 
     * @param viewHolder
     * @param item
     */
    public abstract void fillData(ImgTextViewHolder viewHolder, T item, int position);
    
    @Override
    protected void setItemData(int position, ImgTextViewHolder holder, View rootView) {
        // 重置view holder中各个view的状态, 避免复用问题
        reset(holder);
        T item = getItem(position);
        fillData(holder, item, position);
    }

} // end of PickerAdapter

