/**
 * 
 */
package com.umeng.comm.ui.adapters.viewparser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.widgets.SquareImageView;

/**
 * 
 */
public class ImagePickerGvParser implements ViewParser {

    @Override
    public View inflate(Context context, ViewGroup parent, boolean attachToRoot) {
        
        int layout = ResFinder.getLayout("umeng_comm_select_images_gv_item");
        int imageViewResId = ResFinder.getId("umeng_comm_sel_imageview");
        int checkboxResId = ResFinder.getId("umeng_comm_sel_checkbox");

        ViewHolder holder = new ViewHolder();
        View view = LayoutInflater.from(context).inflate(layout,
                parent, false);
        
        holder.imageView = (SquareImageView) view.findViewById(imageViewResId);
        holder.checkBox = (CheckBox) view.findViewById(checkboxResId);
        
        view.setTag(holder);
        
        return view;
    }
    
    public static class ViewHolder {
        public SquareImageView imageView;
        public CheckBox checkBox;
    }

}
