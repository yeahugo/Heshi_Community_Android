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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.adapters.viewparser.ImagePickerGvParser;
import com.umeng.comm.ui.adapters.viewparser.ImagePickerGvParser.ViewHolder;

/**
 * 在发布feed时选择图片的GridView适配器,选中图片的CheckBox会打上标识.
 */
public class ImagePickerGvAdapter extends CommonAdapter<String, ViewHolder> {

    /**
     * 存储被选中的图片地址
     */
    private List<String> mSelectImagePaths = new ArrayList<String>();

    /**
     * 最大被选中的图片数目。目前最大选中图片数不能超过9张
     */
    private static final int MAX_IMAGE_NUM = 9;

    public ImagePickerGvAdapter(Context context, List<String> list, Set<String> selectedImagePaths) {
        super(context, list, new ImagePickerGvParser());
        if (selectedImagePaths != null) {
            mSelectImagePaths.addAll(selectedImagePaths);
        }
    }

    /**
     * 加载图片
     * 
     * @param holder
     * @param item
     */
    private void loadImage(final ViewHolder holder, String item) {
        // 加载图片
        mImageLoader
                .displayImage(item, holder.imageView);
        mImageLoader.resume();
    }

    /**
     * 获取所有选中的图片地址</br>
     * 
     * @return 所有选择图片的地址
     */
    public List<String> getSelectImagePaths() {
        return mSelectImagePaths;
    }

    public void setSelectedImagePaths(Collection<String> paths) {
        mSelectImagePaths.clear();
        mSelectImagePaths.addAll(paths);
    }

    @Override
    protected void setItemData(int position, ViewHolder holder, View rootView) {
        final String item = getItem(position);
        holder.imageView.setTag(item);
        // 加载图片
        loadImage(holder, item);
        final CheckBox tmpCheckBox = holder.checkBox;
        // 点击每项的事件
        rootView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean status = tmpCheckBox.isChecked();
                if (!status) {
                    // 此时被选中
                    if (mSelectImagePaths.size() >= MAX_IMAGE_NUM) {
                        if ( !mSelectImagePaths.contains(Constants.ADD_IMAGE_PATH_SAMPLE) ) {
                            ToastMsg.showShortMsgByResName(mContext, "umeng_comm_image_overflow");
                            return;
                        }
                        mSelectImagePaths.remove(Constants.ADD_IMAGE_PATH_SAMPLE);
                    }
                    // 如果“+图片”存在且在末尾，则将新增的图片放在“+图片”的前面
                    mSelectImagePaths.add(item);
                    int index = mSelectImagePaths.indexOf(Constants.ADD_IMAGE_PATH_SAMPLE);
                    if (index >= 0 && index != mSelectImagePaths.size() - 1) {
                        mSelectImagePaths.add(mSelectImagePaths.remove(index));
                    }
                } else {
                    // 取消选中的图片
                    mSelectImagePaths.remove(item);
                }
                tmpCheckBox.setChecked(!status);
            }
        });

        // 如果是列表中包含该路径，那么代表该项为选中的.
        tmpCheckBox.setChecked(mSelectImagePaths.contains(item));

    }

}
