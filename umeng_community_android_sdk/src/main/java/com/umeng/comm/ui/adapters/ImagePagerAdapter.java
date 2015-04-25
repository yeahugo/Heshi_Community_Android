/**
 * 
 */

package com.umeng.comm.ui.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.umeng.comm.core.beans.ImageItem;
import com.umeng.comm.core.imageloader.ImgDisplayOption;
import com.umeng.comm.core.imageloader.UMImageLoader;
import com.umeng.comm.core.imageloader.UMImageLoader.ImageLoadingListener;
import com.umeng.comm.core.sdkmanager.ImageLoaderManager;
import com.umeng.comm.core.utils.DeviceUtils;
import com.umeng.comm.core.utils.Log;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.widgets.ScaleImageView;

/**
 * 
 */
public class ImagePagerAdapter extends PagerAdapter {

    private List<ImageItem> mPaths = new ArrayList<ImageItem>();
    UMImageLoader mImageLoader;
    public boolean isPreView = false;//是否是预览
    private OnDismissListener mListener;
    public ImagePagerAdapter(List<ImageItem> paths) {

        if (paths != null && paths.size() > 0) {
            mPaths.addAll(paths);
        }
        mImageLoader = ImageLoaderManager.getInstance().getCurrentSDK();
    }

    @Override
    public int getCount() {
        return mPaths.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // ScaleImageView imageView = new
        // ScaleImageView(container.getContext());
//        String url = null;
        View view = createView(container.getContext(), mPaths.get(position).middleImageUrl);
        container.addView(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        return view;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    /**
     * 确保创建的View被销毁
     */
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public void addImagePaths(List<ImageItem> paths) {
        mPaths.addAll(paths);
        notifyDataSetChanged();
    }

    public void cleanCache() {
        mPaths.clear();
        notifyDataSetChanged();
    }

    private View createView(Context context, String url) {
        
        ScaleImageView imageView = new ScaleImageView(context);
        RelativeLayout.LayoutParams imageLayoutParams = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        imageLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        imageView.setLayoutParams(imageLayoutParams);
        imageView.setOndismissListener(mListener);
        // 如果是预览情况，则直接查看图片
        if ( isPreView ) {
            mImageLoader.displayImage(url, imageView);
            return imageView;
        }
        
        RelativeLayout relativeLayout = new RelativeLayout(context);

        ProgressBar progressBar = new ProgressBar(context);
        int size = DeviceUtils.dp2px(context, 40);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar.setLayoutParams(params);
        int color = ResFinder.getColor("umeng_comm_topic_tip_bg");
        progressBar.getIndeterminateDrawable().setColorFilter(color,Mode.SRC_IN);

//        ScaleImageView imageView = new ScaleImageView(context);
//        RelativeLayout.LayoutParams imageLayoutParams = new RelativeLayout.LayoutParams(
//                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//        imageLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//        imageView.setLayoutParams(imageLayoutParams);
        // 绑定大图
        bindImage(imageView, progressBar, url);
        relativeLayout.addView(imageView);

        relativeLayout.addView(progressBar, params);
        return relativeLayout;
    }

    /**
     * 绑定图片</br>
     * 
     * @param imageView
     * @param item
     * @param source 是否是原图
     */
    private void bindImage(ImageView imageView, final ProgressBar progressBar, String url) {
        // 设置原图
        Bitmap bitmap = getSourceBitmap(url, getSize(imageView));
        ImgDisplayOption option = null;
        if (bitmap == null) {
            option = ImgDisplayOption.getCommonDisplayOption();
        } else {
            imageView.setImageBitmap(bitmap);
            option = new ImgDisplayOption();
        }

        new ImgDisplayOption();
        mImageLoader.displayImage(url, imageView, option,
                new ImageLoadingListener() {

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        // 显示加载的Dialog
                        progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view) {

                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        // 关闭显示的Dialog
                        progressBar.setVisibility(View.GONE);
                        if (view == null) {
                            Log.d("", "### image view 为空");
                            return;
                        }
                        ImageView tmpImageView = (ImageView) view;
                        if (isUriEqualsWithImageViewTag(tmpImageView.getTag(), imageUri)
                                && loadedImage != null) {
                            tmpImageView.setImageBitmap(loadedImage);
                        }
                    }
                });
    }

    /**
     * 根据image设置宽高。如果是wrap_content,match_parent则返回宽高250</br>
     * 
     * @param imageView
     * @return
     */
    private Point getSize(ImageView imageView) {
        Point size = new Point();
        if (imageView.getWidth() > 0) {
            size.x = imageView.getWidth();
            size.y = imageView.getHeight();
        } else {
            size.x = size.y = 250;
        }
        return size;
    }

    /**
     * 根据url获取本地cache的原图。如果不存在则返回null</br>
     * 
     * @param url 图片的url
     * @return
     */
    private Bitmap getSourceBitmap(String url, Point size) {
        String uriPath = Uri.parse(url).getPath();
        return ImageLoaderManager.getInstance().getCurrentSDK().loadBitmapFromCache(uriPath, size);
    }

    private boolean isUriEqualsWithImageViewTag(Object tag, String url) {
        return tag != null && !TextUtils.isEmpty(url) && tag.equals(url);
    }
    
    public void setOnDismissListener(OnDismissListener listener ){
        mListener = listener;
    }

}
