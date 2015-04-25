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
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.umeng.comm.core.utils.Log;
import com.umeng.comm.core.utils.ResFinder;

/**
 * @author mrsimple
 */
public class RefreshLvLayout extends RefreshLayout<ListView> {

    /**
     * ListView的加载中footer
     */
    protected View mFooterView;

    Runnable mFooterRunnable = new Runnable() {

        @Override
        public void run() {
            if (mFooterView == null) {
                return;
            }
            if (canAddFooter()) {
                mAbsListView.addFooterView(mFooterView);
            } else {
                if (mAbsListView.getAdapter() instanceof HeaderViewListAdapter) {
                    Log.d(VIEW_LOG_TAG, "### 移除footer ");
                    mAbsListView.removeFooterView(mFooterView);
                } else {
                    Log.d(VIEW_LOG_TAG, "### 隐藏footer ");
                    mFooterView.setVisibility(View.GONE);
                }
            }
        } // end run
    };

    public RefreshLvLayout(Context context) {
        this(context, null);
    }

    public RefreshLvLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void getRefreshView() {
        super.getRefreshView();
        setupFooter();
    }

    private boolean canAddFooter() {
        return isLoading && mAbsListView.getFooterViewsCount() == 0;
    }

    /**
     * 为ListView添加FooterView, 必须要在{@link #findRefreshViewById(int)}调用之后再调用该方法
     * 
     * @param layout fooer的布局
     */
    public void setFooterView(int layout) {
        mFooterView = LayoutInflater.from(getContext()).inflate(layout,
                null, false);
        if (mAbsListView != null && mAbsListView.getFooterViewsCount() == 0) {
            mAbsListView.addFooterView(mFooterView);
        }
    }

    private void setupFooter() {
        if (mAbsListView != null && mAbsListView.getFooterViewsCount() == 0) {
            if (mFooterView == null) {
                setDefaultFooterView();
            }
            mAbsListView.addFooterView(mFooterView);
        }

        if (mAdapter != null && mAbsListView != null) {
            mAbsListView.setAdapter(mAdapter);
            if (mAbsListView.getFooterViewsCount() > 0) {
                mAbsListView.removeFooterView(mFooterView);
            }
        }
    }

    ListAdapter mAdapter;

    public void setAdapter(ListAdapter adapter) {
        mAdapter = adapter;
    }

    public void setDefaultFooterView() {
        setFooterView(ResFinder.getLayout("umeng_comm_listview_footer"));
    }

    @Override
    public void setLoading(boolean loading) {
        super.setLoading(loading);

        this.post(mFooterRunnable);

        mYDown = 0;
        mLastY = 0;
    }
}
