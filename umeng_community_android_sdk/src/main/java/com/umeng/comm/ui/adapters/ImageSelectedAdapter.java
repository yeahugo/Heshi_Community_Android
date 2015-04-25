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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;

import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.utils.Log;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.adapters.viewparser.ImageSelectedParser;
import com.umeng.comm.ui.adapters.viewparser.ImageSelectedParser.ViewHolder;

import java.util.List;

/**
 * 发布消息时选中的图片预览的GridView适配器, 用户可以删除选中的图片.
 */
public class ImageSelectedAdapter extends CommonAdapter<String, ViewHolder> {

    /**
     * 当移除某张图片的时候，更新发送消息界面的图片Gridview信息
     */
    private FetchListener<List<String>> mListener;

    public ImageSelectedAdapter(Context context, List<String> data,
            FetchListener<List<String>> listener) {
        super(context, data, new ImageSelectedParser());
        this.mListener = listener;
    }

    /**
     * 显示删除某项的Dialog </br>
     * 
     * @param adapter
     * @param position
     */
    protected void showDeleteItemDialog(final int position) {
        String msg = ResFinder.getString("umeng_comm_delete_photo");
        String confirmText = ResFinder.getString("umeng_comm_text_confirm");
        String cancelText = ResFinder.getString("umeng_comm_text_cancel");

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(msg).setPositiveButton(confirmText,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mRemoveListener != null) {
                            mRemoveListener.onRemove(position, mData.get(position));
                        }
                        getDataSource().remove(position);
                        // 添加add图标
                        if (!mData.contains(Constants.ADD_IMAGE_PATH_SAMPLE)) {
                            mData.add(Constants.ADD_IMAGE_PATH_SAMPLE);
                        }
                        notifyDataSetChanged();
                        if (mListener != null) {
                            mListener.onComplete(mData);
                        }
                    }
                });

        builder.setNegativeButton(cancelText, null);
        builder.create().show();
    }

    /*
     * (non-Javadoc)
     * @see com.umeng.comm.adapters.CommonAdapter#setItemData(int,
     * java.lang.Object)
     */
    @Override
    protected void setItemData(final int position, ViewHolder holder, View rootView) {
        final String path = getItem(position);
        holder.imageView.setTag(path);
        Log.d("", "### postion = " + position + ", path = " + path);
        if (!path.equals(Constants.ADD_IMAGE_PATH_SAMPLE)) {
            holder.deleteImageView.setVisibility(View.VISIBLE);
            // 加载图片
            mImageLoader.displayImage(path, holder.imageView);
            mImageLoader.resume();
            // 删除按钮
            holder.deleteImageView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    showDeleteItemDialog(position);
                }
            });
        } else {
            holder.imageView.setImageDrawable(ResFinder.getDrawable("umeng_comm_add_image"));
            holder.deleteImageView.setVisibility(View.GONE);
        }

    }

    OnRemoveListener mRemoveListener;

    public void setOnRemoveListener(OnRemoveListener removeListener) {
        mRemoveListener = removeListener;
    }

    public static interface OnRemoveListener {
        public void onRemove(int position, String item);
    }

}
