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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.widget.TextView;

import com.umeng.comm.core.beans.ImageItem;
import com.umeng.comm.core.imageloader.cache.ImageCache;
import com.umeng.comm.core.imageloader.utils.Md5Helper;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.DeviceUtils;
import com.umeng.comm.core.utils.Log;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.adapters.ImagePagerAdapter;

/**
 * 图片浏览器
 * 
 * @author mrsimple
 */
public class ImageBrowser extends Dialog /* implements OnGestureListener */{

    // private int mCusIndex = 0;

    ProgressDialog mLoadingDialog;
    private ViewPager mViewPager;
    private ImagePagerAdapter mAdapter;
    private List<ImageItem> mImageList;
    private TextView mImagePosTextView;
    private static final String DIVIDER = "/";
    private boolean mPreView = false;
    private Activity mActivity;

    /**
     * @param context
     */
    public ImageBrowser(Context context) {
        this(context, false);
    }

    public ImageBrowser(Context context, boolean isPreView) {
        super(context, android.R.style.Theme_Black_NoTitleBar);
        mPreView = isPreView;
        initContentView();
        int dialogStyle = ResFinder.getStyle("umeng_comm_dialog_wrap_content");
        mLoadingDialog = new ProgressDialog(context, dialogStyle);
        getWindow().setWindowAnimations(ResFinder.getStyle("umeng_comm_image_browser"));
        mActivity = (Activity) context;

    }

    /**
     * 初始化内容视图
     */
    private void initContentView() {
        setContentView(ResFinder.getLayout("umeng_comm_img_browser_layout"));

        View saveView = findViewById(ResFinder.getId("umeng_comm_save_img_tv"));
        if (mPreView) {// 如果是预览，则不显示保存按钮
            saveView.setVisibility(View.GONE);
        } else {
            saveView.setOnClickListener(
                    new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            saveImage();
                        }
                    });
        }

        mImagePosTextView = (TextView) findViewById(ResFinder.getId("umeng_comm_current_pos"));
        mViewPager = (ViewPager) findViewById(ResFinder.getId("viewPager"));
        mAdapter = new ImagePagerAdapter(null);
        mAdapter.setOnDismissListener(new android.content.DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                dismiss();
            }
        });
        mAdapter.isPreView = mPreView;
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int page) {
                mImagePosTextView.setText((page + 1) + DIVIDER + mImageList.size());
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
    }

    // private void saveImage() {
    // FileOutputStream fos = null;
    // try {
    // // TODO 保存哪一种规格 ?
    // String imageMd5 =
    // Md5Helper.toMD5(mImageList.get(mCusIndex).originImageUrl);
    // File imgFile = new File(getCacheDir() + File.separator
    // + imageMd5 + ".png");
    // imgFile.createNewFile();
    // fos = new FileOutputStream(imgFile);
    // ViewGroup viewGroup = (ViewGroup)
    // mViewPager.getChildAt(mViewPager.getCurrentItem());
    // ImageView imageView = (ImageView) viewGroup.getChildAt(0);
    // Bitmap bitmap = ((BitmapDrawable) (imageView.getDrawable())).getBitmap();
    // bitmap.compress(CompressFormat.PNG, 100, fos);
    // fos.flush();
    // ToastMsg.showShortMsg(getContext(),
    // ResFinder.getString("umeng_comm_save_pic_success")
    // + imgFile.getAbsolutePath());
    // } catch (FileNotFoundException e) {
    // ToastMsg.showShortMsg(getContext(),
    // ResFinder.getString("umeng_comm_save_pic_failed"));
    // e.printStackTrace();
    // } catch (IOException e) {
    // e.printStackTrace();
    // ToastMsg.showShortMsg(getContext(),
    // ResFinder.getString("umeng_comm_save_pic_failed"));
    // } finally {
    // DiskLruCache.closeQuietly(fos);
    // }
    // }

    // private void saveImage() {
    // BufferedWriter writer = null;
    // BufferedReader reader = null;
    // try {
    // String url = mImageList.get(mViewPager.getCurrentItem()).middleImageUrl;
    // String fileName = Md5Helper.toMD5(url);
    // File imgFile = new File(getCacheDir() + File.separator
    // + fileName + ".png");
    // // imgFile.createNewFile();
    // writer = new BufferedWriter(new OutputStreamWriter(new
    // FileOutputStream(imgFile)));
    //
    // InputStream inputStream =
    // ImageCache.getInstance().getInputStream(fileName);
    // reader = new BufferedReader(new InputStreamReader(inputStream));
    // int len = 0;
    // char[] buffer = new char[4096];
    // while (( len = reader.read(buffer) ) != -1 ) {
    // writer.write(buffer, 0, len);
    // }
    // writer.flush();
    // galleryAddPic(imgFile);
    // ToastMsg.showShortMsg(getContext(),
    // ResFinder.getString("umeng_comm_save_pic_success")
    // + imgFile.getAbsolutePath());
    // } catch (FileNotFoundException e) {
    // ToastMsg.showShortMsg(getContext(),
    // ResFinder.getString("umeng_comm_save_pic_failed"));
    // e.printStackTrace();
    // } catch (IOException e) {
    // e.printStackTrace();
    // ToastMsg.showShortMsg(getContext(),
    // ResFinder.getString("umeng_comm_save_pic_failed"));
    // } finally {
    // DiskLruCache.closeQuietly(writer);
    // DiskLruCache.closeQuietly(reader);
    // }
    // }

    private void saveImage() {
        BufferedOutputStream out = null;
        BufferedInputStream in = null;
        try {
            String url = mImageList.get(mViewPager.getCurrentItem()).middleImageUrl;
            String fileName = Md5Helper.toMD5(url);
            File imgFile = new File(getCacheDir() + File.separator
                    + fileName + ".png");
            in = new BufferedInputStream(ImageCache.getInstance().getInputStream(fileName),
                    8 * 1024);
            out = new BufferedOutputStream(new FileOutputStream(imgFile), 8 * 1024);
            byte[] buffer = new byte[4 * 1024];
            int len = 0;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
            galleryAddPic(imgFile);
            ToastMsg.showShortMsg(getContext(),
                    ResFinder.getString("umeng_comm_save_pic_success")
                            + imgFile.getAbsolutePath());
        } catch (FileNotFoundException e) {
            ToastMsg.showShortMsg(getContext(),
                    ResFinder.getString("umeng_comm_save_pic_failed"));
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            ToastMsg.showShortMsg(getContext(),
                    ResFinder.getString("umeng_comm_save_pic_failed"));
        } finally {
            CommonUtils.closeSilently(out);
            CommonUtils.closeSilently(in);
        }
    }

    private void galleryAddPic(File savedFile) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(savedFile);
        mediaScanIntent.setData(contentUri);
        mActivity.sendBroadcast(mediaScanIntent);
    }

    private String getCacheDir() throws IOException {
        Context context = getContext();
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Log.d("", "### context : " + context + ", dir = " + context.getExternalCacheDir());
            cachePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }

        File cacheFile = new File(cachePath + File.separator + DeviceUtils.getAppName(context));
        if (!cacheFile.exists()) {
            cacheFile.mkdir();
        }
        return cacheFile.getAbsolutePath();
    }

    /**
     * @param url 图片url地址
     */
    public void setImageList(List<ImageItem> images, int curPos) {
        mImageList = images;
        mAdapter.addImagePaths(images);
        mViewPager.setCurrentItem(curPos);
        mImagePosTextView.setText((curPos + 1) + DIVIDER + mImageList.size());
    }

    /**
     * @param url 图片url地址
     */
    public void setImageStringList(List<String> images, int curPos) {
        List<ImageItem> imageItems = new ArrayList<ImageItem>();
        for (int i = 0; i < images.size(); i++) {
            String url = images.get(i);
            imageItems.add(new ImageItem(url, url, url));
        }

        setImageList(imageItems, curPos);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mAdapter.cleanCache();
    }

    public static interface OnDismissListener {
        void onDismiss();
    }

}
