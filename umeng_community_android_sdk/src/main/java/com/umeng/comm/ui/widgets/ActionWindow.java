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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ToggleButton;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.beans.Like;
import com.umeng.comm.core.utils.ResFinder;

/**
 * @author mrsimple
 */
public class ActionWindow {

    Context mContext;

    private PopupWindow mWindow;
    private ToggleButton mLikeToggleButton;
    private ImageView mCommentImageView;
    private ImageView mForwardImageView;
    private FeedItem mFeedItem;
    public boolean isFeedLiked = false;
    public String likedId = "";

    /**
     * @param context
     * @param item
     */
    public ActionWindow(Context context) {
        mContext = context;
        // 设置内容
        initContentView(context);
    }

    /**
     * 
     */
    private void initContentView(Context context) {
        int layout = ResFinder.getLayout("umeng_comm_action_layout");
        int likeButtonResId = ResFinder.getId("umeng_comm_like_btn");
        int commentButtonResId = ResFinder.getId("umeng_comm_comment_btn");
        int forwardButtonResId = ResFinder.getId("umeng_comm_forward_btn");
        View actionView = LayoutInflater.from(context).inflate(layout, null);
        mLikeToggleButton = (ToggleButton) actionView.findViewById(likeButtonResId);
        mCommentImageView = (ImageView) actionView.findViewById(commentButtonResId);
        mForwardImageView = (ImageView) actionView.findViewById(forwardButtonResId);

        //
        mWindow = new PopupWindow(actionView, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mWindow.setFocusable(true);
        mWindow.setOutsideTouchable(true);
    }

    public void setFeedItem(FeedItem feedItem) {
        mFeedItem = feedItem;
        isFeedLiked = false;
        likedId = "";
        updateLikeBtnState();
    }

    private void updateLikeBtnState() {
        final CommUser me = CommConfig.getConfig().loginedUser;
        for (Like likeItem : mFeedItem.likes) {
            if (likeItem.creator.id.equals(me.id)) {
                likedId = likeItem.id;
                isFeedLiked = true;
                break;
            }
        }
        mLikeToggleButton.setChecked(isFeedLiked);
    }

    public void setCommentOnClickListener(OnClickListener listener) {
        mCommentImageView.setOnClickListener(listener);
    }

    public void setLikeOnClickListener(OnClickListener listener) {
        mLikeToggleButton.setOnClickListener(listener);
    }

    /**
     * @param listener
     */
    public void setForwardOnClickListener(OnClickListener listener) {
        mForwardImageView.setOnClickListener(listener);
    }

    /**
     * @param parent
     * @param gravity
     * @param x
     * @param y
     */
    public void showAtLocation(View parent, int gravity, int x, int y) {
        mWindow.showAtLocation(parent, gravity, x, y);
    }

    public void showAsDropDown(View anchor) {
        showAsDropDown(anchor, 0, 0);
    }

    /**
     * <p>
     * Display the content view in a popup window anchored to the bottom-left
     * corner of the anchor view offset by the specified x and y coordinates. If
     * there is not enough room on screen to show the popup in its entirety,
     * this method tries to find a parent scroll view to scroll. If no parent
     * scroll view can be scrolled, the bottom-left corner of the popup is
     * pinned at the top left corner of the anchor view.
     * </p>
     * <p>
     * If the view later scrolls to move <code>anchor</code> to a different
     * location, the popup will be moved correspondingly.
     * </p>
     *
     * @param anchor the view on which to pin the popup window
     * @see #dismiss()
     */
    public void showAsDropDown(View anchor, int xoff, int yoff) {
        mWindow.showAsDropDown(anchor, xoff, yoff);
    }

    /**
     * @return
     */
    public int getWindowWidth() {
        mWindow.getContentView().measure(0, 0);
        return mWindow.getContentView().getMeasuredWidth();
    }

    /**
     * 关闭PopupWindow窗口</br>
     */
    public void dismiss() {
        if (mWindow != null && mWindow.isShowing()) {
            mWindow.dismiss();
        }
    }

}
