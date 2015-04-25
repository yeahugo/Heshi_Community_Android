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

package com.umeng.comm.ui.fragments;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.GridView;

import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.imageloader.utils.ImageScaner;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.utils.Log;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.adapters.ImagePickerGvAdapter;
import com.umeng.comm.ui.widgets.ImageBrowser;

/**
 * 发表feed时的图片选择Fragment
 * 
 * @author mrsimple
 */
public class PhotoPickerFragment extends FontFragment implements OnClickListener {

    private GridView mGridView;
    private ImagePickerGvAdapter mImageGroupAdapter;

    /**
     * 用户已将选择的图片列表路径。该路径为重发布消息页面带过来的已经选择的图片路径。
     * 比如用户第一次选择3张图片，再次进行选择时，已经选择的这三张图片不应该再次被选择。
     */
    private Set<String> mImagepaths = new HashSet<String>();
    /**
     * 存储被选中的图片地址
     */
    private List<String> mSelectImagePaths = new ArrayList<String>();
    private FetchListener<List<String>> mFetchDataListener;
    public OnClickListener mOnClickListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mImageLoader.reset();

        int layout = ResFinder.getLayout("umeng_comm_image_browser");
        View rootView = inflater.inflate(layout, container, false);
        initView(rootView);
        return rootView;
    }

    /**
     * 初始化相关视图控件</br>
     */
    @SuppressLint("NewApi")
    private void initView(View rootView) {
        int cancelButtonResId = ResFinder.getId("umeng_comm_image_cancel");
        int confirmButtonResId = ResFinder.getId("umeng_comm_image_confirm");
        int gridViewResId = ResFinder.getId("umeng_comm_image_browser_gridview");
        rootView.findViewById(cancelButtonResId).setOnClickListener(this);
        rootView.findViewById(confirmButtonResId).setOnClickListener(this);
        mGridView = (GridView) rootView.findViewById(gridViewResId);
        mGridView.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                    mImageLoader.resume();
                } else {
                    mImageLoader.pause();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                    int totalItemCount) {

            }
        });
        // 适配器
        mImageGroupAdapter = new ImagePickerGvAdapter(getActivity(), new LinkedList<String>(),
                mImagepaths);
        mGridView.setAdapter(mImageGroupAdapter);

        mGridView.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                    mImageLoader.resume();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                    int totalItemCount) {
                mImageLoader.pause();
            }
        });

        mImageLoader.resume();

        ImageScaner imageScaner = new ImageScaner(getActivity(),
                new SimpleFetchListener<String>() {
                    @Override
                    public void onComplete(String result) {
                        appendNewImage(result);
                    }
                });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            imageScaner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            imageScaner.execute();
        }
    }

    /**
     * 初始化adapter</br>
     * 
     * @param paths 整个SD卡上文件的路径列表
     */
    private void appendNewImage(String newUri) {
        if (TextUtils.isEmpty(newUri)) {
            Log.e(getTag(), "don't scan image...");
            return;
        }

        mImageGroupAdapter.addData(newUri);
    }

    /**
     * 获取照片选择的Adapter
     * 
     * @return
     */
    public ImagePickerGvAdapter getAdapter() {
        return mImageGroupAdapter;
    }

    /**
     * 设置获取图片路径的回调
     * 
     * @param listener
     */
    public void setImagePathListener(FetchListener<List<String>> listener) {
        mFetchDataListener = listener;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        int previewButtonResId = ResFinder.getId("umeng_comm_image_cancel");
        int confirmButtonResId = ResFinder.getId("umeng_comm_image_confirm");

        if (id == previewButtonResId) { // 预览
            dealPreviewlBtnLogic();
        } else if (id == confirmButtonResId) { // 确认
            dealConfirmBtnLogin(v);
        }
    }

    /**
     * 执行点击取消按钮的逻辑</br>
     */
    private void dealPreviewlBtnLogic() {
        ImageBrowser imageBrowser = new ImageBrowser(getActivity(),true);
        List<String> imageList = new ArrayList<String>();
        imageList.addAll(mImageGroupAdapter.getSelectImagePaths());
        // 移除“+”图片
        if (imageList.contains(Constants.ADD_IMAGE_PATH_SAMPLE)) {
            imageList.remove(Constants.ADD_IMAGE_PATH_SAMPLE);
        }

        if (imageList.size() > 0) {
            // imageBrowser.setImageList(imageList, 0);
            imageBrowser.setImageStringList(imageList, 0);
            imageBrowser.show();
        } else {
            ToastMsg.showShortMsgByResName(getActivity(), "umeng_comm_not_choose_image");
        }
    }

    /**
     * 处理点击确认按钮时的逻辑</br>
     */
    private void dealConfirmBtnLogin(View view) {
        mSelectImagePaths = mImageGroupAdapter.getSelectImagePaths();
        // dismiss();
        mOnClickListener.onClick(view);
        if (mFetchDataListener != null) {
            mFetchDataListener.onComplete(mSelectImagePaths);
        }
    }

    /**
     * 获取用户选中的图片的地址集合</br>
     * 
     * @return 选中图片的路径集合
     */
    public List<String> getSelectImagePaths() {
        return mSelectImagePaths;
    }

    public void setSelectedImagePaths(List<String> paths) {
        mImagepaths.removeAll(paths);
        mImagepaths.addAll(paths);
        if (mImageGroupAdapter != null) {
            mImageGroupAdapter.setSelectedImagePaths(mImagepaths);
            mImageGroupAdapter.notifyDataSetChanged();
        }
    }

    public void removeImage(String path) {
        if (mImageGroupAdapter != null) {
            mImageGroupAdapter.getSelectImagePaths().remove(path);
            mImageGroupAdapter.getDataSource().remove(path);
            mImageGroupAdapter.notifyDataSetChanged();
        }

    }

    /**
     * 获取已经选择的所有图片的路径</br>
     * 
     * @param sets
     */
    public void setImageSelectedPaths(Set<String> sets) {
        if (sets != null && sets.size() > 0) {
            mImagepaths.addAll(sets);
        }
    }
}
