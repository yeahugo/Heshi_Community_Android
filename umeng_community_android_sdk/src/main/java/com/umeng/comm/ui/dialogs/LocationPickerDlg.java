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

package com.umeng.comm.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.umeng.comm.core.beans.LocationItem;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.nets.responses.LocationResponse;
import com.umeng.comm.core.utils.Log;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.adapters.PickerAdapter;
import com.umeng.comm.ui.adapters.viewparser.FriendItemViewParser.ImgTextViewHolder;
import com.umeng.comm.ui.location.LocationFinder;

/**
 * @author mrsimple
 */
public class LocationPickerDlg extends PickerDialog<LocationItem> {

    /**
     * 地理位置List
     */
    private List<LocationItem> mLocationItems = new ArrayList<LocationItem>();

    /**
     * @param context
     */
    public LocationPickerDlg(Context context) {
        this(context, 0);
    }

    /**
     * @param context
     * @param theme
     */
    public LocationPickerDlg(Context context, int theme) {
        super(context, theme);
        setContentView(this.createContentView());
    }

    @Override
    protected View createContentView() {
        View rootView = super.createContentView();
        mRefreshLvLayout.setOnLoadListener(null);
        return rootView;
    }

    @Override
    protected void setupAdater() {
        mAdapter = new PickerAdapter<LocationItem>(getContext(),
                mLocationItems) {

            @Override
            public void fillData(ImgTextViewHolder viewHolder, LocationItem item, int position) {
                viewHolder.mImageView.setVisibility(View.GONE);
                viewHolder.mTextView.setText(item.description);
                String text = ResFinder.getString("umeng_comm_text_dont_show_location");

                boolean isDefaultText = text.equals(item.description);
                if (mLocationItems.size() == 1 && isDefaultText) {
                    viewHolder.mDetailTextView.setVisibility(View.GONE);
                } else {
                    viewHolder.mDetailTextView.setText(item.detail);
                }
            }

        };
        mListView.setAdapter(mAdapter);

        mTitleTextView.setText(ResFinder.getString("umeng_comm_text_my_location"));
        int searchBtnResId = ResFinder.getId("search_ok_btn");
        // ok button is hide
        mRootView.findViewById(searchBtnResId).setVisibility(View.GONE);
    }

    /**
     * 
     */
    private void notifyDataSetChanged() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void setupLvOnItemClickListener() {
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 选中在position位置的项
                pickItemAtPosition(position);
            }

        });
    }

    @Override
    public void loadDataFromServer() {
        if (mLocation != null) {
            mRefreshLvLayout.setRefreshing(true);
            mSdkImpl.getLocationAddr(mLocation, mListener);
        }
    }

    @Override
    public void loadMore() {
        mRefreshLvLayout.setLoading(false);
    }

    /**
     * 设置我的位置信息
     * 
     * @param locationItems
     */
    public synchronized void setupMyLocation(final Location myLocation,
            final List<LocationItem> locationItems) {

        //
        resetLocationItems(locationItems);

        Log.d("", "### dialog location size : " + mLocationItems.size());

        mLocation = myLocation;
        //
        initMyLocation();
    }

    /**
     * @param locationItems
     */
    private void resetLocationItems(List<LocationItem> locationItems) {
        mLocationItems.clear();
        // first one
        String text = ResFinder.getString("umeng_comm_text_dont_show_location");
        LocationItem location = LocationItem.makeLocationItem(text, "", null);
        mLocationItems.add(location);
        mLocationItems.addAll(locationItems);

        if (locationItems != null && locationItems.size() > 0) {
            mSelectedItem = locationItems.get(0);
        }
    }

    /**
     * 初始化我的位置
     * 
     * @param location
     */
    private void initMyLocation() {
        if (mLocation == null) {
            // 获取我的位置
            LocationFinder.getInstance().findLocation(getContext(),
                    new FetchListener<Location>() {

                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onComplete(Location result) {
                            if (mLocation != null) {
                                mLocation = result;
                                // 获取详细的信息
                                mSdkImpl.getLocationAddr(mLocation, mListener);
                            }
                        } // end onComplete
                    });
        } else if (mLocation != null && mLocationItems.size() < 2) {// 默认会添加一个"不显示位置"的item,
                                                                    // 所以size小于2代表还没有获取到具体的位置信息
            // 获取详细的信息
            mSdkImpl.getLocationAddr(mLocation, mListener);
        } else {
            notifyDataSetChanged();
        }
    }

    /**
     * 
     */
    FetchListener<LocationResponse> mListener = new FetchListener<LocationResponse>() {

        @Override
        public void onStart() {

        }

        @Override
        public void onComplete(LocationResponse data) {
            mRefreshLvLayout.setRefreshing(false);
            //
            resetLocationItems(data.result);
            //
            notifyDataSetChanged();
        }
    };

}
