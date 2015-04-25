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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.widget.TextView;

import com.umeng.comm.core.utils.ResFinder;

/**
 * 文字下面绘制一排圆点,并且左右滑动能够切换的TextView
 * 
 * @author mrsimple
 */
@SuppressLint("ClickableViewAccessibility")
public class SlidingTextView extends TextView implements OnTouchListener {

    int mCurrentIndex = 0;
    int mTouchSlop = 0;

    Paint mPaint = new Paint();
    Rect mTextRect = new Rect();
    /**
     * 圆点的数量
     */
    // int mCircleCount = 3;
    /**
     * 圆点与文字的margin值
     */
    int mCircleMarginTop = 20;
    /**
     * 圆点之间的margin
     */
    int mCircleMargin = 8;
    /**
     * 圆点的大小
     */
    int mCircleRadius = 6;
    /**
     * 触摸按下的x坐标
     */
    float mDownX = 0;
    /**
     * 当前x的位置,滑动手势时
     */
    float mCurX = 0;
    // /**
    // * 默认选中的项
    // */
    // private int mDefaultIndex = 0;
    /**
     * 适配器
     */
    SlidingAdapter mAdapter;

    /**
     * 索引切换Listener
     */
    OnIndexChangeListener mIndexChangeListener;

    public SlidingTextView(Context context) {
        super(context);
        initStuffs();
    }

    public SlidingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs);
        initStuffs();
    }

    public SlidingTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttrs(attrs);
        initStuffs();
    }

    private void initAttrs(AttributeSet attrs) {

        // TypedArray typedArray = getContext()
        // .obtainStyledAttributes(attrs, R.styleable.SlidingTextView);
        // mCircleRadius = (int)
        // typedArray.getDimension(R.styleable.SlidingTextView_circleRadius, 6);
        // mCircleMarginTop = (int) typedArray.getDimension(
        // R.styleable.SlidingTextView_circleMarginTop, 20);
        // mCircleMargin = (int) typedArray.getDimension(
        // R.styleable.SlidingTextView_circleMargin, 8);
        TypedArray typedArray = getContext()
                .obtainStyledAttributes(attrs, ResFinder.getStyleableArrts("SlidingTextView"));
        // mCircleRadius = (int) typedArray.getDimension(
        // ResFinder.getStyleableId("SlidingTextView_circleRadius"), 6);
        // mCircleMarginTop = (int) typedArray.getDimension(
        // ResFinder.getStyleableId("SlidingTextView_circleMarginTop"), 20);
        // mCircleMargin = (int) typedArray.getDimension(
        // ResFinder.getStyleableId("SlidingTextView_circleMargin"), 8);
        mCircleRadius = (int) typedArray.getDimension(0, 6);
        mCircleMarginTop = (int) typedArray.getDimension(1, 20);
        mCircleMargin = (int) typedArray.getDimension(2, 8);
        typedArray.recycle();

        Log.e(VIEW_LOG_TAG, "### circle " + mCircleRadius);
    }

    private void initStuffs() {
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.GREEN);

        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        this.setOnTouchListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int extraHeight = mCircleMarginTop + mCircleRadius * 2;
        setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight() + extraHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCircles(canvas);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        handleTouchEvent(event);
        return true;
    }

    /**
     * 计算圆点左右两边的padding,使得圆点居中
     * 
     * @return
     */
    private int calculateCirclePadding() {
        String text = getText().toString();
        getPaint().getTextBounds(text, 0, text.length(), mTextRect);
        int textWidth = mTextRect.right - mTextRect.left;
        int avaliableCircleWidth = textWidth - getPaddingLeft() - getPaddingRight();
        int count = mAdapter.getCount();
        int circleUseWidth = count * mCircleRadius;
        return (avaliableCircleWidth - circleUseWidth - count * mCircleMargin) / 2 - 3;
    }

    /**
     * 绘制圆点
     */
    private void drawCircles(Canvas canvas) {
        int circlePadding = calculateCirclePadding();
        int left = getPaddingLeft() + circlePadding;
        final int circleTop = mTextRect.bottom - mTextRect.top + mCircleMarginTop + getPaddingTop()
                + getPaddingBottom() + mCircleRadius * 2 + 10;

        Log.e(VIEW_LOG_TAG, "### left : " + left + ", top : " + circleTop + ", rect : " + mTextRect
                + ", margin : " + mCircleMargin);
        for (int i = 0; i < mAdapter.getCount(); i++) {
            if (mCurrentIndex == i) {
                mPaint.setColor(Color.BLACK);
            } else {
                mPaint.setColor(Color.LTGRAY);
            }
            canvas.drawCircle(left, circleTop, mCircleRadius, mPaint);
            Log.e(VIEW_LOG_TAG, "### draw  : " + left + ", top : " + circleTop
                    + ", circle margin : "
                    + mCircleMargin);
            left += mCircleRadius * 2 + mCircleMargin * 2;
        }
    }

    /**
     * 处理触摸事件
     * 
     * @param event
     */
    private void handleTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                break;

            case MotionEvent.ACTION_MOVE:
                mCurX = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                handleSliding();
                mDownX = 0;
                mCurX = 0;
                break;
            default:
                break;
        }
    }

    /**
     * 处理左右滑动
     */
    private void handleSliding() {

        int oldIndex = mCurrentIndex;

        // 左滑
        // if (mCurX - mDownX > 0 && mCurrentIndex > 0) {
        // mCurrentIndex = --mCurrentIndex % mAdapter.getCount();
        // } else if (mDownX - mCurX > 0) { // 右滑
        // mCurrentIndex = ++mCurrentIndex % mAdapter.getCount();
        // }

        mCurrentIndex = ++mCurrentIndex % mAdapter.getCount();
        if (oldIndex != mCurrentIndex) {
            changeText();
            if (mIndexChangeListener != null) {
                mIndexChangeListener.onChange(oldIndex, mCurrentIndex);
            }
        }
    }

    /**
     * 设置默认选中的文本</br>
     * 
     * @param pos
     */
    public void setDefaultSelectIndex(int pos) {
        if (pos < 0 || pos >= mAdapter.getCount()) {
            throw new IllegalArgumentException("pos must be between [0," + mAdapter.getCount()
                    + "]");
        }
        // mDefaultIndex = pos;
        mCurrentIndex = pos;
        setText(mAdapter.getTitle(mCurrentIndex));
    }

    public void selectItemWithIndex(int index) {
        if (index >= 0 && index != mCurrentIndex
                && index < mAdapter.getCount()) {
            mCurrentIndex = index;
            changeText();
        }
    }

    private void changeText() {
        setText(mAdapter.getTitle(mCurrentIndex));
        invalidate();
    }

    public void slidingToLeft() {
        if (mCurrentIndex > 0) {
            mCurrentIndex--;
            changeText();
        }
    }

    public void slidingToRight() {
        if (mCurrentIndex < mAdapter.getCount()) {
            mCurrentIndex++;
            changeText();
        }
    }

    public void setOnIndexChangeListener(OnIndexChangeListener listener) {
        mIndexChangeListener = listener;
    }

    public void setSlidingAdapter(SlidingAdapter adapter) {
        mAdapter = adapter;
        setDefaultSelectIndex(0);
    }

    /**
     * 索引切换的监听器
     * 
     * @author mrsimple
     */
    public static interface OnIndexChangeListener {
        public void onChange(int oldIndex, int index);
    }

    /**
     * 滑动适配器
     * 
     * @author mrsimple
     */
    public static interface SlidingAdapter {
        int getCount();

        String getTitle(int position);
    }

}
