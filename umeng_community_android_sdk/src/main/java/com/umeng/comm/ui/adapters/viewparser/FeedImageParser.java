///**
// * 
// */
//package com.umeng.comm.adapters.viewparser;
//
//import android.content.Context;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AbsListView.LayoutParams;
//import android.widget.ImageView.ScaleType;
//
//import com.umeng.comm.widgets.SquareImageView;
//
///**
// * 
// */
//public class FeedImageParser implements ViewParser {
//
//    @Override
//    public View inflate(Context context, ViewGroup parent, boolean attachToRoot) {
//        
//        LayoutParams mImageViewLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT
//                , ViewGroup.LayoutParams.MATCH_PARENT);
//        
//        SquareImageView imageView = new SquareImageView(context);
//        imageView.setScaleType(ScaleType.CENTER_CROP);
//        imageView.setLayoutParams(mImageViewLayoutParams);
//        
//        ViewHolder holder = new ViewHolder();
//        holder.imageView = imageView;
//        imageView.setTag(holder);
//        return imageView;
//    }
//    
//    public static class ViewHolder{
//        public SquareImageView imageView;
//    }
//
//}
