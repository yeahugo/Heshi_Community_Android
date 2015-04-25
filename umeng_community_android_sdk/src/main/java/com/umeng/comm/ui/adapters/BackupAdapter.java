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

import com.umeng.comm.core.utils.Log;
import com.umeng.comm.ui.adapters.viewparser.ViewParser;

public abstract class BackupAdapter<T,H> extends CommonAdapter<T,H> {

    List<T> mBackupData = new ArrayList<T>();

    /**
     * @param context
     * @param data
     */
    public BackupAdapter(Context context, List<T> data,ViewParser viewParser) {
        super(context, data,viewParser);
    }

    @Override
    public void updateListViewData(java.util.List<T> data) {
        backupData();
        if (data != null) {
            mData.clear();
            mData.addAll(data);
            notifyDataSetChanged();
        }
        
    }

    /**
     * 保存原始数据列表,用于搜索功能
     */
    public void backupData() {
        if (mBackupData.size() == 0) {
            mBackupData.clear();
            mBackupData.addAll(mData);
            Log.d("", "### backup : " + mBackupData.toString());
        }
    }

    /**
     * 回复原始数据,与backupData配套使用
     */
    public void restoreData() {
        mData.clear();
        mData.addAll(mBackupData);
        mBackupData.clear();
        notifyDataSetChanged();
    }

}
