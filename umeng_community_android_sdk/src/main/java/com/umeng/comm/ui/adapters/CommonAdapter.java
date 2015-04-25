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

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.umeng.comm.core.imageloader.UMImageLoader;
import com.umeng.comm.core.sdkmanager.ImageLoaderManager;
import com.umeng.comm.ui.adapters.viewparser.ViewParser;

/**
 * 泛型适配器
 * 
 * @author mrsimple
 * @param <T> 泛型参数
 */
public abstract class CommonAdapter<T, H> extends BaseAdapter {

    protected List<T> mData = new LinkedList<T>();
    protected Context mContext = null;
    protected LayoutInflater mInflater = null;
    private ViewParser mViewParser;

    protected UMImageLoader mImageLoader = ImageLoaderManager.getInstance().getCurrentSDK();

    /**
     * 构造函数
     * 
     * @param context Context对象
     * @param data Adapter的数据
     */
    public CommonAdapter(Context context, List<T> data, ViewParser viewParser) {
        this.mData = data;
        this.mContext = context;
        this.mViewParser = viewParser;
        this.mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public T getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * // TODO : 这里考虑删除updateListViewData方法, 这里会导致数据源变化. mData = data; 更新数据源
     * 
     * @param data
     */
    public void updateListViewData(List<T> data) {
        if (data != null && data.size() > 0) {
            mData = data;
            // sort();
            notifyDataSetChanged();
        }
    }

    /**
     * 添加数据并更新数据源</br>
     * 
     * @param list
     */
    public void addData(List<T> list) {
        if (list != null && list.size() > 0) {
            mData.addAll(list);
            // sort();
            notifyDataSetChanged();
        }
    }

    /**
     * 添加数据并更新数据源</br>
     * 
     * @param T
     */
    public void addToFirst(T data) {
        mData.add(0, data);
        // sort();
        notifyDataSetChanged();
    }

    /**
     * 添加数据并更新数据源</br>
     * 
     * @param T
     */
    public void addToFirst(List<T> data) {
        if (data != null && data.size() > 0) {
            mData.addAll(0, data);
            // sort();
            notifyDataSetChanged();
        }
    }

    /**
     * 添加数据并更新数据源</br>
     * 
     * @param T
     */
    public void addData(T t) {
        if (t != null) {
            mData.add(t);
            // sort();
            notifyDataSetChanged();
        }
    }

    /**
     * @return
     */
    public List<T> getDataSource() {
        return mData;
    }

    // protected void sort(){}

    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = mViewParser.inflate(mContext, parent, false);
        }

        H holder = (H) view.getTag();
        setItemData(position, holder, view);
        return view;
    }

    protected abstract void setItemData(int position, H holder, View rootView);

}
