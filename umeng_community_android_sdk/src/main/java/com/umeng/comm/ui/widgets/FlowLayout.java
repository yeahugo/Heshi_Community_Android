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

import android.content.Context;
import android.graphics.Paint.FontMetrics;
import android.text.Layout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * 
 */
public class FlowLayout extends ViewGroup {

    private int width = 0;
    private int mViewHeight = 0;// 当前viewgroup的高度
    private int mMarginLeft = 8;// 子控件距离左边8
    private int mMarginBottom = 8;// 子控件距离底部8
    /**
     * 是否显示剩余的view。由于其container的高度限制为150dp，为了防止显示的话题太长无法完全容纳的情况
     */
    private boolean isShowView = true;

    /**
     * @param context
     * @param attrs
     */
    public FlowLayout(Context context) {
        super(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        isShowView = true;

        measureChildren(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        mViewHeight = getMeasuredHeight() - getPaddingBottom() - getPaddingTop() - mMarginBottom;
        int count = getChildCount();
        if (width == 0 || mViewHeight == 0 || count == 0) {
            return;
        }
        hideViewIndex = -1;
        int height = 0;
        View childView = null;
        int lineMaxHeight = 0;
        int remainSpace = width;
        int vPadding = 0;
        int hPadding = 0;
        for (int i = 0; i < count; i++) {
            childView = getChildAt(i);
            vPadding = childView.getPaddingBottom() + childView.getPaddingTop();
            hPadding = childView.getPaddingLeft() + childView.getPaddingRight();
            remainSpace = remainSpace - childView.getMeasuredWidth() - mMarginLeft - hPadding;
            if (remainSpace >= 0) {
                lineMaxHeight = Math.max(lineMaxHeight, childView.getMeasuredHeight() + vPadding);
            } else {
                remainSpace = width - childView.getMeasuredWidth() - mMarginLeft - hPadding;
                height += (lineMaxHeight + mMarginBottom);
                // 新的一行的高度
                lineMaxHeight = childView.getMeasuredHeight() + vPadding;
                // 检查新的一行的高度是否能显示完整
                if (height + lineMaxHeight > mViewHeight && isShowView) {
                    isShowView = false;
                    // 如果第i个view换行时，其高度超过container的限制，设置其前一个view不可见。目的是为“更多”view留下显示空间
                    // getChildAt(i).setVisibility(View.GONE);
                    hideViewIndex = i;
                }
            }
        }
        // 最后一行的高度
        height += lineMaxHeight + vPadding + mMarginBottom + 5;
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height);
    }

    private int hideViewIndex = -1;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        View view = null;
        // 需要支持padding, 因此左边的起始位置是paddingLeft, top的起始位置是paddingTop.
        int px = getPaddingLeft(); // 布局控件相对于父控件的x坐标
        int py = getPaddingTop();// 布局控件相对于父控件的y坐标
        int hPadding = 0; // 子视图padding feft、padding right的大小
        int vPadding = 0;// 子视图padding top、padding bottom的大小
        int currentLineMaxHeight = 0;// 当前行中某个子视图的最大高
        for (int i = 0; i < count; i++) {
            view = (TextView) getChildAt(i);
            if (view.getVisibility() == View.GONE) {
                continue;
            }
            hPadding = view.getPaddingLeft() + view.getPaddingRight();
            vPadding = view.getPaddingTop() + view.getPaddingBottom();
            currentLineMaxHeight = Math.max(currentLineMaxHeight, view.getMeasuredHeight()
                    + vPadding);
            int tmpSpace = px + view.getMeasuredWidth() + mMarginLeft + mMarginLeft;
            if (hideViewIndex != -1 && i >= hideViewIndex) {
                return;
            }
            if (tmpSpace <= width || i == 0) {
                view.layout(px, py, px + view.getMeasuredWidth() + hPadding,
                        py + view.getMeasuredHeight() + vPadding);
                px = px + view.getMeasuredWidth() + hPadding + mMarginLeft;
            } else {
                // 显示新的一行
                py = py + currentLineMaxHeight + mMarginBottom;
                currentLineMaxHeight = view.getMeasuredHeight() + vPadding;
                // 如果话题名超过ViewGroup的宽度，则设置其最大宽度为ViewGroup的宽度，显示话题名将出现省略号
                int right = view.getMeasuredWidth() + hPadding;
                if (right > width - mMarginLeft) {
                    right = width - mMarginLeft;
                    // TODO 显示省略号时paddingRight参数偏小，暂时设置其paddingRight为1.5倍
                    int paddingLeft = view.getPaddingLeft();
                    int paddingTop = view.getPaddingTop();
                    int paddingRight = view.getPaddingRight();
                    int paddingBottom = view.getPaddingBottom();
                    view.setPadding(paddingLeft, paddingTop, (int) (paddingRight * 1.5),
                            paddingBottom);
                }
                view.layout(getPaddingLeft(), py, right,
                        py + view.getMeasuredHeight() + vPadding);
                px = view.getMeasuredWidth() + hPadding + mMarginLeft;
            }
        }
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        int count = getChildCount();
        if (count <= 0) {
            return 0;
        }
        TextPaint textPaint = ((TextView) getChildAt(0)).getPaint();
        TextView textView = null;
        int desire = 0;
        for (int i = 0; i < count; i++) {
            textView = (TextView) getChildAt(i);
            int width = (int) (Layout.getDesiredWidth(textView.getText(), textPaint)) + 1;
            desire = Math.max(desire, width);
        }
        return desire;
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        int count = getChildCount();
        if (count <= 0) {
            return 0;
        }
        if (count == 1 ) {
            TextView textView = (TextView) getChildAt(0);
            FontMetrics fm = textView.getPaint().getFontMetrics();
            int minH = (int)Math.ceil(fm.descent - fm.top) + 2;
            return minH;
        } else {
            return super.getSuggestedMinimumHeight();
        }
    }
}
