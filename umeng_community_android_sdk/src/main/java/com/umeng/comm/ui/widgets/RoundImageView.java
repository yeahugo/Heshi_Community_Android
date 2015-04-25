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

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * 圆形ImageView,用于显示用户头像
 * 
 * @author mrsimple
 */
public class RoundImageView extends SquareImageView {

    Paint mMaskPaint = new Paint();

    Path mMaskPath;

    int mTargetAPI = 8;

    PorterDuffXfermode mXfermode = new
            PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

    float mCornerRadius;

    private RectF mRectF = new RectF();

    public RoundImageView(Context context) {
        super(context);
        initPaint();
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public RoundImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initPaint();
    }

    @TargetApi(11)
    private void initPaint() {
        mTargetAPI = Build.VERSION.SDK_INT;
        if (systemApiHigherThanHoneycomb()) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            this.mMaskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            this.mMaskPaint.setAntiAlias(true);
        }

    }

    /**
     * 系统是否大于sdk 11
     * 
     * @return 如果大于或者等于sdk 11则返回true,否则返回false.
     */
    private boolean systemApiHigherThanHoneycomb() {
        return mTargetAPI >= Build.VERSION_CODES.HONEYCOMB;
    }

    private void generateMaskPath(int width, int height) {
        this.mMaskPath = new Path();
        this.mMaskPath.addRoundRect(new RectF(0.0F, 0.0F, width, height), width, width,
                Path.Direction.CW);
        // 反向清除canvas上的其他数据
        this.mMaskPath.setFillType(Path.FillType.INVERSE_WINDING);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);

        // 系统小于API 11和drawable 是 BitmapDrawable时设置mMaskPaint为相应的属性
        if (drawable instanceof BitmapDrawable && !systemApiHigherThanHoneycomb()) {
            // 获取drawable的画笔
            mMaskPaint = ((BitmapDrawable) drawable).getPaint();
            mMaskPaint.setAntiAlias(true);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mCornerRadius = mSize.x;
        generateMaskPath(mSize.x, mSize.x);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 大于API 11则使用更高效的Path绘制方法
        if (systemApiHigherThanHoneycomb()) {
            // 保存当前layer的透明橡树到离屏缓冲区。并新创建一个透明度爲255的新layer
            int saveCount = canvas.saveLayerAlpha(0.0F, 0.0F, canvas.getWidth(),
                    canvas.getHeight(),
                    255, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
            super.onDraw(canvas);
            if (this.mMaskPath != null) {
                canvas.drawPath(this.mMaskPath, this.mMaskPaint);
            }
            canvas.restoreToCount(saveCount);
        } else if (mCornerRadius > 0) {
            mRectF.set(0, 0, getWidth(), getHeight());
            int saveCount = canvas.saveLayer(mRectF, null,
                    Canvas.MATRIX_SAVE_FLAG
                            | Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
                            | Canvas.FULL_COLOR_LAYER_SAVE_FLAG
                            | Canvas.CLIP_TO_LAYER_SAVE_FLAG);

            canvas.drawARGB(0, 0, 0, 0);
            mMaskPaint.setColor(Color.BLACK);
            canvas.drawRoundRect(mRectF, mCornerRadius, mCornerRadius,
                    mMaskPaint);

            Xfermode oldMode = mMaskPaint.getXfermode();
            mMaskPaint.setXfermode(mXfermode);
            super.onDraw(canvas);
            mMaskPaint.setXfermode(oldMode);
            canvas.restoreToCount(saveCount);
        }
    }
}
