/**
 * 
 */
package com.umeng.comm.ui.adapters.viewparser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.widgets.SquareImageView;

/**
 * 
 */
public class ImageSelectedParser implements ViewParser {

    @Override
    public View inflate(Context context, ViewGroup parent, boolean attachToRoot) {
        
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewHolder holder = new ViewHolder();
        
        int layout = ResFinder.getLayout("umeng_comm_image_selected_item");
        int selectImageViewResId = ResFinder.getId("umeng_comm_image_selected");
        int deleteImageViewResId = ResFinder.getId("umeng_comm_image_delete");
        
        View view = inflater.inflate(layout, parent, false);
        holder.imageView = (SquareImageView) view.findViewById(selectImageViewResId);
        holder.deleteImageView = (ImageView) view.findViewById(deleteImageViewResId);
        holder.deleteImageView.setVisibility(View.VISIBLE);
        
        view.setTag(holder);
        
        return view;
    }
    
    public static class ViewHolder {
        public SquareImageView imageView;// 选中的图片
        public ImageView deleteImageView;// 选中图片右上方的删除图标
    }
    

}
