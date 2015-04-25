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

package com.umeng.comm.ui.fragments;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.Comment;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.beans.ForwardFeedItem;
import com.umeng.comm.core.beans.ImageItem;
import com.umeng.comm.core.beans.Like;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.db.AbsDBHelper;
import com.umeng.comm.core.db.DbHelperFactory;
import com.umeng.comm.core.db.cmd.DeleteCommand;
import com.umeng.comm.core.db.cmd.InsertCommand;
import com.umeng.comm.core.imageloader.ImgDisplayOption;
import com.umeng.comm.core.listeners.Listeners.CommListener;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.CommentResponse;
import com.umeng.comm.core.nets.responses.LoginResponse;
import com.umeng.comm.core.nets.responses.SimpleResponse;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ResFinder.ResType;
import com.umeng.comm.core.utils.TimeUtils;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.activities.FeedDetailActivity.OnItemCommentClickListener;
import com.umeng.comm.ui.activities.ForwardActivity;
import com.umeng.comm.ui.activities.UserInfoActivity;
import com.umeng.comm.ui.adapters.CommonAdapter;
import com.umeng.comm.ui.adapters.FeedCommentAdapter;
import com.umeng.comm.ui.adapters.FeedImageAdapter;
import com.umeng.comm.ui.adapters.viewparser.FeedItemViewParser;
import com.umeng.comm.ui.adapters.viewparser.FeedItemViewParser.FeedItemViewHolder;
import com.umeng.comm.ui.adapters.viewparser.ViewParser;
import com.umeng.comm.ui.utils.FeedViewUtils;
import com.umeng.comm.ui.widgets.ActionWindow;
import com.umeng.comm.ui.widgets.ImageBrowser;
import com.umeng.comm.ui.widgets.LikeView;
import com.umeng.comm.ui.widgets.NetworkImageView;
import com.umeng.comm.ui.widgets.WrapperGridView;
import com.umeng.comm.ui.widgets.WrapperListView;

/**
 * 该类是某条Feed的详情页面,使用FeedItemViewParser解析单项的Feed数据.
 * 
 * @author mrsimple
 */
public class FeedDetailFragment extends FontFragment {

    protected View mRootView;

    /**
     * 用户当前选中的feed
     */
    protected FeedItem mCurFeedItem = new FeedItem();

    /**
     * 赞、评论、转发的view
     */
    protected ActionWindow mActionWindow;

    /**
     * 消息流中的图片浏览器
     */
    protected ImageBrowser mImageBrowser;

    /**
     * 评论的ListView
     */
    protected WrapperListView mCommentListView;
    /**
     * 点击评论某项时的回调。用于在评论的EditView中显示回复XXX
     */
    private OnItemCommentClickListener mCommentClickListener;
    /**
     * 点击的评论的item view
     */
    protected View mClickItemView;

    /**
     * feed 每项视图的解析器
     */
    protected ViewParser mViewParser = new FeedItemViewParser();
    /**
     * 当前点击的消息流的某一项
     */
    protected int mCurFeedItemIndex = 0;
    /**
     * 弹出评论EditVIew时，某项feed的滚动距离
     */
    protected int mScrollDis = 0;
    /**
     * 对某人进行回复。用在评论的时候显示在EditText中
     */
    protected CommUser mReplyUser;

    FeedItemViewHolder mCurViewHolder;

    /**
     * 
     */
    Map<WrapperListView, String> mCommentLvMap = new HashMap<WrapperListView, String>();

    /**
     * 
     */
    protected Map<String, WeakReference<FeedItemViewHolder>> mViewHolderMap = new HashMap<String, WeakReference<FeedItemViewHolder>>();

    // 由于开发者可能直接使用Fragment，在退出登录的时候，我们需要回到该Activity
    protected String mContainerClass = null;

    /**
     * 创建一个FeedDetailFragment对象，在feedDetailActivity使用
     * 
     * @param feedItem
     * @return
     */
    public static FeedDetailFragment newFeedDetailFragment(FeedItem feedItem) {
        FeedDetailFragment fragment = new FeedDetailFragment();
        fragment.mCurFeedItem = feedItem;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mContainerClass = getActivity().getClass().getName();
        mRootView = mViewParser.inflate(getActivity(), container, false);
        // 初始化ActionButton(赞、评论、转发)、浏览图片的Dialog
        initWidgets();
        mCurViewHolder = (FeedItemViewHolder) mRootView.getTag();
        mCurViewHolder.mDialogButton.setVisibility(View.GONE);
        // 更新视图
        updateFeed(mCurFeedItem);
        return mRootView;
    }

    public void updateFeed(FeedItem feedItem) {
        mCurFeedItem = feedItem;

        Log.e("", "###new feed : " + feedItem.text);
        // 填充数据
        setFeedItemData(mRootView, mCurFeedItem, 0);
        // 设置一些点击监听器
        initButtonsClickListener(mCurViewHolder, mCurFeedItem, 0);
        mCommentListView = mCurViewHolder.mCommentsListView;
    }

    /**
     * 
     */
    protected void initWidgets() {
        mActionWindow = new ActionWindow(getActivity());
        mImageBrowser = new ImageBrowser(getActivity());
    }

    /**
     * 设置操作栏和图片浏览的点击监听器
     * 
     * @param viewHolder
     * @param position
     */
    protected void initButtonsClickListener(final FeedItemViewHolder viewHolder,
            final FeedItem feedItem,
            final int position) {

        // 操作栏的点击操作
        viewHolder.mActionButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mCurViewHolder = viewHolder;
                // 保存评论的ListView。只能放在这里，放在setCommentOnClickListener内部将导致刷新或者转发后，
                // 评论一条后将到下一条消息流的评论中，暂时不知道什么原因导致。
                mCurFeedItem = feedItem;
                mCommentListView = viewHolder.mCommentsListView;

                Log.e(getTag(),
                        "### init comment list view : "
                                + (mCommentListView != null ? mCommentListView.hashCode() : -1));
                clickActionButton(viewHolder, position);
            }
        });

        // 设置Action点击评论的的回调
        mActionWindow.setCommentOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mActionWindow.dismiss();

                Log.e(getTag(), "### setCommentOnClickListener,  feed id : " + mCurFeedItem.id
                        + ", text = "
                        + mCurFeedItem.text);

                clickCommentButton(viewHolder, position);
            }
        });
        // 设置Action点击Like的的回调
        mActionWindow.setLikeOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mActionWindow.dismiss();
                Log.e(getTag(), "### comment feed id : " + mCurFeedItem.id + ", text = "
                        + mCurFeedItem.text
                        + ", position = " + position);

                if (mActionWindow.isFeedLiked) {
                    postUnlike(mCurFeedItem.id, mActionWindow.likedId);
                } else {
                    // 执行like操作
                    postLike(mCurFeedItem.id);
                }
            }
        });
        // 设置Action点击转发的的回调
        mActionWindow.setForwardOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mActionWindow.dismiss();
                if (!checkFeeditem(mCurFeedItem)) {
                    ToastMsg.showShortMsg(getActivity(),
                            ResFinder.getString("umeng_comm_origin_feed_delete"));
                    return;
                }
                //
                gotoForwardActivity(mCurFeedItem);
            }
        });

        // 使用ViewStub加载，如果之前的项没有图片，不显示GridView，此时GridView为null，不需要设置相应的时间
        if (viewHolder.mImageGv != null) {
            // 图片GridView
            viewHolder.mImageGv.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                    List<ImageItem> images = feedItem.imageUrls;
                    // 被转发的feed的图片
                    if (feedItem instanceof ForwardFeedItem) {
                        images = ((ForwardFeedItem) feedItem).forwardItem.imageUrls;
                    }
                    mImageBrowser.setImageList(images, pos);
                    mImageBrowser.show();
                }
            });
        }

    }

    private boolean checkFeeditem(FeedItem feedItem) {
        if (feedItem instanceof ForwardFeedItem) {
            ForwardFeedItem forwardFeedItem = (ForwardFeedItem) feedItem;
            return forwardFeedItem.forwardItem.status < 2;
        }

        return true;
    }

    /**
     * 点击action button
     */
    protected void clickActionButton(final FeedItemViewHolder viewHolder, final int position) {
        // 点击操作栏图标
        CommonUtils.checkLoginAndFireCallback(getActivity(),
                new SimpleFetchListener<LoginResponse>() {

                    @Override
                    public void onComplete(LoginResponse response) {

                        if (response.errCode == Constants.NO_ERROR) {
                            performActionButtonClick(viewHolder, position);
                        } else {
                            ToastMsg.showShortMsgByResName(getActivity(),
                                    "umeng_comm_login_failed");
                        }
                    }
                });
    }

    /**
     * 点击某项评论时的事件处理
     * 
     * @param viewHolder
     * @param position
     */
    // protected void clickCommentListItem(FeedItemViewHolder viewHolder, int
    // position) {
    // int footerCount = mCommentListView.getFooterViewsCount();
    // int clickIndex = mCommentListView.getCount() - footerCount - 1;
    // if (clickIndex >= 0) {
    // mClickItemView = mCommentListView.getChildAt(clickIndex);
    // }
    // // 此处+1的目的是让其索引无效，评论某人的时候无预设置的文本
    // showCommentLayout(clickIndex + 1);
    // }

    /**
     * 点击Action Button时的评论按钮
     * 
     * @param viewHolder
     * @param position
     */
    private void clickCommentButton(FeedItemViewHolder viewHolder, int position) {
        // 此处+1的目的是让其索引无效，评论某人的时候无预设置的文本
        // showCommentLayout(position);
        int clickCommentPos = 0;
        View itemView = null;
        WrapperListView commentListView = mCurViewHolder.mCommentsListView;

        if (commentListView != null && commentListView.getCount() > 0) {
            // 检测是否是footer
            int footerCount = commentListView.getFooterViewsCount();
            clickCommentPos = commentListView.getCount() - 1;
            if (footerCount > 0) {
                clickCommentPos = clickCommentPos - footerCount;
            }
            itemView = commentListView.getChildAt(clickCommentPos);
        }

        performClickCommentItem(commentListView, itemView, clickCommentPos, true);
    }

    /**
     * 根据评论的内容、评论者等信息创建一条评论
     * 
     * @param commentText 评论的内容
     * @return
     */
    private Comment createComment(String commentText) {
        final Comment comment = new Comment();
        comment.feedId = mCurFeedItem.id;
        comment.text = commentText;
        comment.creator = CommConfig.getConfig().loginedUser;
        comment.replyUser = mReplyUser;

        Log.e(getTag(), "### createComment, feed id = " + mCurFeedItem.id + ", text = "
                + mCurFeedItem.text);
        return comment;
    }

    /**
     * 发送评论
     */
    public void postComment(String text, final CommListener listener) {
        final Comment comment = createComment(text);
        FetchListener<SimpleResponse> wrapListener = new FetchListener<SimpleResponse>() {

            @Override
            public void onStart() {
                if (listener != null) {
                    listener.onStart();
                }

                ToastMsg.showShortMsg(getActivity(), "评论中...");
            }

            @Override
            public void onComplete(SimpleResponse response) {

                if (listener != null) {
                    listener.onComplete(response);
                }

                // 判断用户是否被禁言
                if (response.errCode == Constants.USER_FORBIDDEN_ERR_CODE) {
                    ToastMsg.showShortMsg(getActivity(),
                            ResFinder.getString("umeng_comm_user_unusable"));
                    return;
                }

                postCommentSuccess(comment, response);
            }
        };

        // 将holder存到map中, 更新评论时显示到别的ListView上的问题
        mViewHolderMap.put(comment.feedId, new WeakReference<FeedItemViewHolder>(mCurViewHolder));

        // 发布评论
        mSdkImpl.postComment(comment, wrapListener);

    }

    /**
     * 通过FeedId来找对应的ViewHolder
     * 
     * @param feedId
     * @return
     */
    private FeedItemViewHolder findViewHolderByFeedId(String feedId) {
        return mViewHolderMap.containsKey(feedId) ? mViewHolderMap
                .get(feedId).get()
                : mCurViewHolder;
    }

    protected void postCommentSuccess(Comment comment,
            SimpleResponse response) {
        String toastResName = "umeng_comm_post_comment_failed";
        if (response.errCode == Constants.NO_ERROR) {
            toastResName = "umeng_comm_post_comment_success";
            FeedItemViewHolder viewHolder = findViewHolderByFeedId(comment.feedId);
            // 获取对应的评论列表
            WrapperListView commentListView = viewHolder != null ? inflateCommentListView(viewHolder)
                    : null;
            FeedItem feedItem = findFeedWithId(comment.feedId);
            int size = feedItem.comments.size();
            if (  size > 0 ) {
                comment.nextPageUrl = feedItem.comments.get(size-1).nextPageUrl;
            }
            // 保存评论数据
            savePostedComment(comment, response.id);
            updateCommentListView(commentListView,feedItem , comment);
        }

        ToastMsg.showShortMsgByResName(getActivity(), toastResName);
    }

    /**
     * 保存已经发送成功的评论到DB
     * 
     * @param comment 评论
     * @param commId 该条评论的id
     */
    protected void savePostedComment(Comment comment, String commId) {
        comment.id = commId;
        // 保存该条评论数据到DB
        DbHelperFactory.getCommentDbHelper(getActivity())
                .insert(comment);
    }

    /**
     * 更新评论的ListView</br>
     * 
     * @param comment
     */
    @SuppressWarnings("unchecked")
    protected void updateCommentListView(WrapperListView commentListView, FeedItem feedItem,
            Comment comment) {
        feedItem.comments.add(comment);
        if (commentListView != null) {
            commentListView.setVisibility(View.VISIBLE);
            initCommentListView(commentListView);
            commentListView.getCommAdapter().addData(comment);
            commentListView.getCommAdapter().notifyDataSetChanged();
        }
    }

    /**
     * 设置点击评论某项时的回调</br>
     * 
     * @param listener
     */
    public void setCommentBtnClickListener(OnItemCommentClickListener listener) {
        mCommentClickListener = listener;
    }

    /**
     * 默认为显示赞、评论、转发三个图标的布局的操作
     */
    protected void performActionButtonClick(FeedItemViewHolder viewHolder, final int position) {
        showActionWindow(viewHolder);
    }

    /**
     * 显示Action Button</br>
     * 
     * @param viewHolder
     */
    protected void showActionWindow(FeedItemViewHolder viewHolder) {

        // 设置被点击的项
        mActionWindow.setFeedItem(mCurFeedItem);
        // action 宽度
        int windowWidth = mActionWindow.getWindowWidth();
        int xOff = windowWidth <= 0 ? -210 : (-windowWidth);
        // 显示的位置,以viewHolder.mActionBarView为锚点显示,分别向左和向上偏移
        mActionWindow.showAsDropDown(viewHolder.mActionButton, xOff - 20,
                -viewHolder.mActionButton.getHeight() - 20);
    }

    /**
     * 跳转到转发页面</br>
     */
    private void gotoForwardActivity(FeedItem item) {
        Intent forwardIntent = new Intent(getActivity(), ForwardActivity.class);
        forwardIntent.putExtra(Constants.FEED, item);
        getActivity().startActivity(forwardIntent);
    }

    /**
     * 用户点赞操作</br>
     * 
     * @param feedId 该条feed的id
     */
    private void postLike(final String feedId) {

        SimpleFetchListener<SimpleResponse> listener = new SimpleFetchListener<SimpleResponse>() {

            @Override
            public void onComplete(SimpleResponse response) {

                // 判断用户是否被禁言
                if (response.errCode == Constants.USER_FORBIDDEN_ERR_CODE) {
                    ToastMsg.showShortMsg(getActivity(),
                            ResFinder.getString("umeng_comm_user_unusable"));
                    return;
                }

                if (!TextUtils.isEmpty(response.id)) {
                    likeSuccess(feedId, response.id);
                } else if (Constants.LIKED_CODE == response.errCode) {
                    ToastMsg.showShortMsgByResName(getActivity(), "umeng_comm_liked");
                } else {
                    ToastMsg.showShortMsg(getActivity(),
                            ResFinder.getString("umeng_comm_like_failed"));
                }
            }
        };
        mSdkImpl.postLike(feedId, listener);

    }

    private void postUnlike(final String feedId, final String likeId) {
        SimpleFetchListener<SimpleResponse> listener = new SimpleFetchListener<SimpleResponse>() {

            @Override
            public void onComplete(SimpleResponse response) {
                // 判断用户是否被禁言
                if (response.errCode == Constants.USER_FORBIDDEN_ERR_CODE) {
                    ToastMsg.showShortMsg(getActivity(),
                            ResFinder.getString("umeng_comm_user_unusable"));
                    return;
                }

                if (response.errCode == 0) {
                    unlikeSuccess(feedId, likeId);
                } else {
                    ToastMsg.showShortMsg(getActivity(),
                            ResFinder.getString("umeng_comm_cancel_like_failed"));
                }
            }
        };
        mSdkImpl.postUnLike(feedId, likeId, listener);
    }

    protected void likeSuccess(String feedId, String likeId) {
        Like like = new Like();
        CommUser likeUser = CommConfig.getConfig().loginedUser;
        like.id = likeId;
        like.feedId = feedId;
        like.creator = likeUser;
        // mCurFeedItem.likes.add(like);

        // 通过feed id找到feed
        final FeedItem targetFeedItem = findFeedWithId(feedId);
        targetFeedItem.likes.add(like);
        targetFeedItem.likeCount++;
        // 更新ListView
        updateLikeView(targetFeedItem);

        InsertCommand<Like> insertCommand = new InsertCommand<Like>(
                DbHelperFactory.getLikeDbHelper(getActivity()), like);
        insertCommand.execute();

    }

    /**
     * TODO : 当在删除中时用户点击了其他的feed, 那么mCurFeedItem就不是原来的那条feed了，这里需要修改.
     * 
     * @param feedId
     * @param likeId
     */
    protected void unlikeSuccess(String feedId, String likeId) {
        //
        Like like = new Like();
        CommUser likeUser = CommConfig.getConfig().loginedUser;
        like.id = likeId;
        like.feedId = feedId;
        like.creator = likeUser;

        // 通过feed id找到目标feed
        final FeedItem targetFeed = findFeedWithId(feedId);
        targetFeed.likes.remove(like);
        targetFeed.likeCount--;
        // 更新Like视图
        updateLikeView(targetFeed);

        // 删除参数
        Map<String, String> whereMap = new HashMap<String, String>();
        whereMap.put(AbsDBHelper.ID, likeId);
        // 删除like
        DeleteCommand<Like> deleteCommand = new DeleteCommand<Like>(
                DbHelperFactory.getLikeDbHelper(getActivity()), whereMap);
        deleteCommand.execute();

    }

    protected FeedItem findFeedWithId(String feedId) {
        return mCurFeedItem.id.equals(feedId) ? mCurFeedItem : new FeedItem();
    }

    /**
     * 更新赞View</br>
     */
    private void updateLikeView(FeedItem feedItem) {
        if (mCurViewHolder != null) {
            setLikeView(mCurViewHolder, feedItem);
            // 更新like的背景视图
            mCurViewHolder.mLikeLayout.requestLayout();
            mCurViewHolder.mLikeLayout.invalidate();
            mCurViewHolder.mLikeView.invalidate();

        }
    }

    /**
     * 填充消息流ListView每项的数据
     * 
     * @param viewHolder
     * @param item
     */
    protected void setFeedItemData(final View convertView, final FeedItem feedItem,
            final int position) {

        if (TextUtils.isEmpty(feedItem.id)) {
            return;
        }

        final FeedItemViewHolder viewHolder = getViewHolderFromConvertView(convertView);
        // 设置基础信息
        setBaseFeeditemInfo(viewHolder, feedItem);
        // 设置feed图片
        setFeedImages(convertView, feedItem);
        // 转发的feed
        if (feedItem instanceof ForwardFeedItem) {
            final ForwardFeedItem forwardItem = (ForwardFeedItem) feedItem;
            // 转发视图
            setForwardViewVisibility(viewHolder, forwardItem);
            // 设置转发视图的数据
            setForwardItemData(viewHolder, forwardItem);
        } else {
            // 设置普通类型feed的item view的可见性
            setCommFeedViewVisibility(viewHolder, feedItem);
        }

        // 添加赞
        setLikeView(viewHolder, feedItem);
        // 设置评论列表
        setCommentListView(viewHolder, feedItem, position);
    }

    /**
     * 从view重获取设置的tag（FeedItemViewHolder）</br>
     * 
     * @param convertView
     * @return
     */
    private FeedItemViewHolder getViewHolderFromConvertView(View convertView) {
        return (FeedItemViewHolder) convertView.getTag();
    }

    /**
     * 显示feed item中的图片
     * 
     * @param convertView convertView
     * @param imageList 图片列表
     */
    protected void showFeedImageGv(View convertView, List<ImageItem> imageList) {

        final FeedItemViewHolder viewHolder = getViewHolderFromConvertView(convertView);
        // 显示转发的布局
        viewHolder.mForwardLayout.setVisibility(View.VISIBLE);
        //
        if (viewHolder.mGrideViewStub.getVisibility() == View.GONE) {
            viewHolder.mGrideViewStub.setVisibility(View.VISIBLE);
            int imageGvResId = ResFinder.getId("umeng_comm_msg_gridview");
            viewHolder.mImageGv = (WrapperGridView) convertView
                    .findViewById(imageGvResId);
            viewHolder.mImageGv.hasScrollBar = true;
        }

        viewHolder.mImageGv.setBackgroundColor(Color.TRANSPARENT);
        viewHolder.mImageGv.setVisibility(View.VISIBLE);

        // adapter
        FeedImageAdapter gridviewAdapter = new FeedImageAdapter(getActivity(), imageList);
        // 设置图片
        viewHolder.mImageGv.setAdapter(gridviewAdapter);
        // 计算列数
        viewHolder.mImageGv.updateColumns(3);
    }

    /**
     * 隐藏feed 中的图片布局，并清空其数据，避免复用
     */
    private void hideFeedImageGv(FeedItemViewHolder viewHolder) {
        if (viewHolder.mImageGv != null) {
            viewHolder.mImageGv.setAdapter(new
                    FeedImageAdapter(getActivity(), new LinkedList<ImageItem>()));
            viewHolder.mImageGv.setVisibility(View.GONE);
        }
    }

    /**
     * TODO : 将Holder当做参数在所有需要的函数中传递,去掉相应的字段,避免出现问题 inflate 评论的View
     * 
     * @param viewHolder
     */
    protected WrapperListView inflateCommentListView(FeedItemViewHolder viewHolder) {
        if (viewHolder.mCommentViewStub.getVisibility() == View.GONE
                && viewHolder.mCommentsListView == null) {
            viewHolder.mCommentsListView = (WrapperListView) viewHolder.mCommentViewStub
                    .inflate();
            viewHolder.mCommentsListView.setVisibility(View.VISIBLE);

            // 加载评论列表
            viewHolder.mCommentsListView.setAdapter(new FeedCommentAdapter(getActivity(),
                    new ArrayList<Comment>()));
        }

        mCommentListView = viewHolder.mCommentsListView;
        return mCommentListView;
    }

    /**
     * 设置评论列表的可见性和数据
     * 
     * @param viewHolder
     * @param item
     * @param position
     */
    private void setCommentListView(FeedItemViewHolder viewHolder,
            FeedItem feedItem, final int position) {
        // 评论数量大于0则显示评论列表,否则隐藏
        if (feedItem.comments.size() > 0) {
            // 加载评论列表
            inflateCommentListView(viewHolder);
            // 初始化Comment ListView
            initCommentListView(viewHolder.mCommentsListView);
            //
            addCommentListViewFooter(viewHolder.mCommentsListView, feedItem.comments, position);
            // 显示评论view并填充数据
            showCommentView(viewHolder.mCommentsListView, feedItem, position);
        } else if (viewHolder.mCommentsListView != null) {
            CommonAdapter<?, ?> adapter = viewHolder.mCommentsListView.getCommAdapter();
            if (adapter != null) {
                adapter.getDataSource().clear();
                adapter.notifyDataSetChanged();
            }

            View footer = getFooterFromViewTag(viewHolder.mCommentsListView);
            if (footer != null) {
                viewHolder.mCommentsListView.setTag(null);
                // TODO 如果移除footer也要将tag设置为空 TODO TODO
                viewHolder.mCommentsListView.removeFooterView(footer);
            }

            viewHolder.mCommentsListView.setVisibility(View.GONE);
        }

        // 将评论列表存在map中
        mCommentLvMap.put(viewHolder.mCommentsListView, feedItem.id);
    }

    private void initCommentListView(WrapperListView commentListView) {
        commentListView.setVisibility(View.VISIBLE);
        int bgColor = ResFinder.getResourceId(ResType.COLOR,
                "umeng_comm_user_center_title_color");
        commentListView
                .setBackgroundResource(bgColor);
        commentListView.hasScrollBar = true;
    }

    /**
     * 设置转发feed的视图的可见性
     */
    private void setForwardViewVisibility(FeedItemViewHolder viewHolder, ForwardFeedItem item) {

        // 显示转发视图
        viewHolder.mForwardLayout.setVisibility(View.VISIBLE);
        viewHolder.mForwardLayout.setPadding(10, 10, 10, 10);
        if (viewHolder.mImageGv != null) {
            viewHolder.mImageGv.setPadding(10, 2, 10, 10);
        }

        // 转发视图的背景
        viewHolder.mForwardLayout.setBackgroundDrawable(ResFinder
                .getDrawable("umeng_comm_forward_bg"));
        // 被转发的文本
        viewHolder.mForwardTextView.setVisibility(View.VISIBLE);

        // 隐藏位置图标
        viewHolder.mLocation.setVisibility(View.GONE);
        viewHolder.mLocImageView.setVisibility(View.GONE);

    }

    /**
     * 设置feed 中图片的显示。如果有图片则显示；否则隐藏</br>
     * 
     * @param convertView
     * @param item
     */
    private void setFeedImages(View convertView, FeedItem item) {
        if (item.getImages() != null && item.getImages().size() > 0) {
            showFeedImageGv(convertView, item.getImages());
        } else {
            hideFeedImageGv(getViewHolderFromConvertView(convertView));
        }
    }

    /**
     * 设置feedItem的基本信息（头像，昵称，内容、位置）</br>
     * 
     * @param viewHolder
     * @param feedItem
     */
    private void setBaseFeeditemInfo(FeedItemViewHolder viewHolder, FeedItem feedItem) {
        // 设置feed类型图标
        setTypeIcon(viewHolder, feedItem);
        // 用户头像
        setupUserIcon(viewHolder.mUserIcon, feedItem.creator);
        // 昵称
        viewHolder.mUserNameTv.setText(feedItem.creator.name);
        // 更新时间
        Date date = new Date(Long.parseLong(feedItem.publishTime));
        viewHolder.mUpdateTime.setText(TimeUtils.format(date));
        // feed的文本内容
        FeedViewUtils.parseTopicsAndFriends(viewHolder.mTextView, feedItem);

        // 地理位置信息
        if (TextUtils.isEmpty(feedItem.locationAddr)) {
            viewHolder.mLocation.setVisibility(View.GONE);
            viewHolder.mLocImageView.setVisibility(View.GONE);
        } else {
            viewHolder.mLocation.setVisibility(View.VISIBLE);
            viewHolder.mLocImageView.setVisibility(View.VISIBLE);
            viewHolder.mLocation.setText(feedItem.locationAddr);
        }

        // 内容为空时Text隐藏布局,这种情况出现在转发时没有文本的情况
        if (TextUtils.isEmpty(feedItem.text)) {
            viewHolder.mTextView.setVisibility(View.GONE);
        } else {
            viewHolder.mTextView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 设置feed 类型的icon</br>
     * 
     * @param viewHolder
     * @param feedItem
     */
    private void setTypeIcon(FeedItemViewHolder viewHolder, FeedItem feedItem) {
        // if (isMyFeed(feedItem)) {
        // viewHolder.mFeedTypeIcon.setVisibility(View.INVISIBLE);
        // } else {
        Drawable drawable = null;
        if (feedItem.type == FeedItem.ANNOUNCEMENT_FEED) {
            // 设置feed类型图标
            drawable = ResFinder.getDrawable("umeng_comm_announce");
            viewHolder.mFeedTypeIcon.setVisibility(View.VISIBLE);
            viewHolder.mFeedTypeIcon.setImageDrawable(drawable);
        } else {
            // 设置feed类型图标 [ 目前只标识公告类型 ]
            // drawable = ResFinder.getDrawable("umeng_comm_friends");
            viewHolder.mFeedTypeIcon.setVisibility(View.INVISIBLE);
        }
        // }

    }

    /**
     * 设置转发的数据
     * 
     * @param viewHolder 视图Holder
     * @param item 转发的feed item
     */
    private void setForwardItemData(FeedItemViewHolder viewHolder, ForwardFeedItem item) {

        // @原始feed的创建者
        atOriginFeedCreator(item.forwardItem);
        // 大于等于2表示该feed已经被删除
        if (item.forwardItem.status >= FeedItem.STATUS_SPAM || isDeleted(item.forwardItem)) {
            viewHolder.mForwardTextView.setGravity(Gravity.CENTER);
            viewHolder.mForwardTextView.setText(ResFinder.getString("umeng_comm_feed_deleted"));

            if (viewHolder.mImageGv != null) {
                viewHolder.mImageGv.setVisibility(View.GONE);
            }

            // 删除被转发的feed
            deleteInvalidateFeed(item.forwardItem);
        } else {
            viewHolder.mForwardTextView.setGravity(Gravity.LEFT | Gravity.CENTER);
            // 解析被转发的@和话题
            FeedViewUtils.parseTopicsAndFriends(viewHolder.mForwardTextView,
                    item.forwardItem);

            if (viewHolder.mImageGv != null) {
                viewHolder.mImageGv.setVisibility(View.VISIBLE);
            }
        }

    }

    /**
     * 判断该feed是否被删除，本地[目前暂时按照从方法判断]</br>
     * 
     * @param item
     * @return
     */
    private boolean isDeleted(FeedItem item) {
        if (TextUtils.isEmpty(item.publishTime)) {
            return true;
        }
        return false;
    }

    /**
     * @param feedItem
     */
    protected void deleteInvalidateFeed(FeedItem feedItem) {
        Map<String, String> wheres = new HashMap<String, String>();
        wheres.put(AbsDBHelper.ID, feedItem.id);
        DeleteCommand<FeedItem> deleteCommand = new DeleteCommand<FeedItem>(
                DbHelperFactory.getFeedDbHelper(getActivity()), wheres);
        deleteCommand.execute();
    }

    /**
     * 被转发的原始feed的创建者在转发时会被@,因此将其名字设置到文本中,然后将其添加到@的好友中.
     * 
     * @param feedItem
     */
    protected void atOriginFeedCreator(FeedItem feedItem) {
        String contextText = feedItem.text;
        // @前缀
        final String atPrefix = "@" + feedItem.creator.name + ": ";
        if (!contextText.contains(atPrefix)) {
            feedItem.text = atPrefix + contextText;
            feedItem.atFriends.add(feedItem.creator);
        }
    }

    /**
     * 设置普通feed视图的可见性
     * 
     * @param viewHolder
     */
    private void setCommFeedViewVisibility(FeedItemViewHolder viewHolder, FeedItem item) {

        // 修改转发视图的背景为透明
        viewHolder.mForwardLayout.setBackgroundColor(Color.TRANSPARENT);
        // viewHolder.mForwardLayout.setVisibility(View.GONE);
        viewHolder.mForwardLayout.setPadding(0, 0, 0, 0);
        if (viewHolder.mImageGv != null) {
            viewHolder.mImageGv.setPadding(0, 0, 0, 0);
        }
        // 隐藏转发视图
        viewHolder.mForwardTextView.setVisibility(View.GONE);

        // 显示时间视图
        viewHolder.mUpdateTime.setVisibility(View.VISIBLE);
        // 昵称
        viewHolder.mUserNameTv.setVisibility(View.VISIBLE);
        // 加载头像视图设置为可见
        viewHolder.mUserIcon.setVisibility(View.VISIBLE);
    }

    /**
     * 设置用户头像
     * 
     * @param userIconImageView 用户头像的SquareImageView
     * @param iconUrl 用户头像的url
     */
    private void setupUserIcon(final NetworkImageView userIconImageView,
            final CommUser user) {

        if (user == null || userIconImageView == null || mImageLoader == null) {
            return;
        }

        ImgDisplayOption option = ImgDisplayOption.getOptionByGender(user.gender);
        userIconImageView.setImageUrl(user.iconUrl, option);
        //
        userIconImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 跳转用户中心前检查是否登录
                gotoUserInfoActivity(user);
            }
        });
    }

    /**
     * 跳转到个人信息页面。该接口只在外部由开发者调用.
     * 
     * @param user 目标用户
     */
    public void gotoUserInfoActivity() {
        gotoUserInfoActivity(null);
    }

    /**
     * 跳转到个人中心。</br>
     * 
     * @param user
     */
    protected void gotoUserInfoActivity(final CommUser user) {
        CommonUtils.checkLoginAndFireCallback(getActivity(),
                new SimpleFetchListener<LoginResponse>() {

                    @Override
                    public void onComplete(LoginResponse response) {
                        if (response.errCode != Constants.NO_ERROR) {
                            ToastMsg.showShortMsgByResName(getActivity(),
                                    "umeng_comm_login_failed");
                            return;
                        }

                        Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                        if (user == null) {// 来自开发者外部调用的情况
                            intent.putExtra(Constants.TAG_USER, CommConfig.getConfig().loginedUser);
                        } else {
                            intent.putExtra(Constants.TAG_USER, user);
                        }
                        intent.putExtra(Constants.TYPE_CLASS, mContainerClass);
                        getActivity().startActivity(intent);
                    }
                });
    }

    /**
     * @param tv
     * @param item
     */
    private void setLikeView(FeedItemViewHolder viewHolder, FeedItem item) {
        List<Like> likes = item.likes;
        final LikeView likeView = viewHolder.mLikeView;
        final View divideView = viewHolder.mDivideView;
        likeView.setText("");
        if (likes.size() > 0) {
            viewHolder.mLikeLayout.setVisibility(View.VISIBLE);
            viewHolder.mLikeCountTv.setText(String.valueOf(item.likeCount));
            likeView.setVisibility(View.VISIBLE);
            divideView.setVisibility(View.VISIBLE);
            // 添加like
            likeView.addLikes(likes);
        } else {
            likeView.setVisibility(View.GONE);
            divideView.setVisibility(View.GONE);
            viewHolder.mLikeLayout.setVisibility(View.GONE);
            viewHolder.mLikeView.setVisibility(View.GONE);
        }

    }

    /**
     * 点击评论某项
     * 
     * @param commListView 评论的ListView
     * @param itemView 点击的View
     * @param position 点击的位置
     * @param clickBtn 是否来自于Action Buton的点击
     */
    protected void performClickCommentItem(final WrapperListView commListView,
            final View itemView, final int position, boolean clickBtn) {
        mClickItemView = itemView;
        showCommentLayout(position, clickBtn);
    }

    /*
     * 显示输入评论内容的布局
     */
    protected void showCommentLayout(int pos, boolean fromClickBtn) {
        if (mCommentClickListener != null) {
            String text = getCommentHint(pos, fromClickBtn);
            mCommentClickListener.onItemClick(text);
        }
    }

    /**
     * 拼接都某条评论评论的文本提示。比如“回复xxx：”</br>
     * 
     * @param pos
     * @return
     */
    protected String getCommentHint(int pos, boolean fromClickBtn) {
        String text = "";
        if (fromClickBtn || pos >= mCommentListView.getCommAdapter().getCount()) {
            mReplyUser = null;
            return text;
        }
        Comment comm = getCommentAtPosition(pos);
        mReplyUser = comm.creator;
        text = getReplyPrefix();
        return text;
    }

    private Comment getCommentAtPosition(int position) {
        return (Comment) mCommentListView.getItemAtPosition(position);
    }

    /**
     * 显示评论列表</br>
     * 
     * @param holder
     */
    private void showCommentView(final WrapperListView commentListView,
            final FeedItem feedItem, final int position) {
        FeedCommentAdapter adapter = (FeedCommentAdapter) commentListView.getCommAdapter();
        if (adapter != null) {
            adapter.getDataSource().clear();
            adapter.getDataSource().addAll(feedItem.comments);
            adapter.notifyDataSetChanged();
        }
        // 点击某条评论则是回复某人的评论
        commentListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int pos, long id) {

                mCurFeedItem = feedItem;
                mCurFeedItemIndex = position + 1;
                mCommentListView = commentListView;

                // 点击了某个评论项
                clickCommentItem(commentListView, view, pos);

            }
        });

    }

    /**
     * 点击了某个评论项进行回复,自己不能回复自己,但是自己能回复自己发布的feed.
     * 
     * @param commentListView
     * @param view
     * @param CommentPosition
     */
    private void clickCommentItem(final WrapperListView commentListView, final View view,
            final int commentPosition) {
        // 检查是否登录
        CommonUtils.checkLoginAndFireCallback(getActivity(),
                new SimpleFetchListener<LoginResponse>() {

                    @Override
                    public void onComplete(LoginResponse response) {
                        if (response.errCode != Constants.NO_ERROR) {
                            ToastMsg.showShortMsgByResName(getActivity(),
                                    "umeng_comm_login_failed");
                            return;
                        }
                        performClickCommentItem(commentListView, view, commentPosition, false);
                    }
                });
    }

    // private boolean isMyComment(Comment comment) {
    // return comment != null &&
    // comment.creator.id.equals(CommConfig.getConfig().loginedUser.id);
    // }

    /**
     * 如果存在加载更多评论，则显示footer；否则移除footer不显示</br>
     * 
     * @param commentListView
     * @param adapter
     * @param comments
     */
    protected void addCommentListViewFooter(final WrapperListView commentListView,
            final List<Comment> comments, final int pos) {
        View footerView = getFooterFromViewTag(commentListView);
        // 避免footer复用
        String nextPage = parseCommentNextPage(comments);
        // 如果有下一页地址,那么需要添加footer
        if (!TextUtils.isEmpty(nextPage)) {
            if (footerView != null) {
                footerView.setVisibility(View.VISIBLE);
            } else {
                commentListView.requestLayout();
                footerView = setupCommentFooter(commentListView, comments, pos);
            }
        } else if (footerView != null) {
            commentListView.setTag(null);
            commentListView.removeFooterView(footerView);
        }

        if (footerView != null && !TextUtils.isEmpty(nextPage)) {
            // 设置footer的点击事件
            setClickFooterListener(commentListView, footerView, pos);
        }
    }

    /**
     * 设置评论footer的点击事件
     * 
     * @param commentListView
     * @param footerView
     * @param nextPage
     * @param pos
     */
    private void setClickFooterListener(final WrapperListView commentListView,
            final View footerView, final int pos) {
        // 点击事件
        footerView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 只有在用户已经登录的情况下，才可以加载更多的评论
                CommonUtils.checkLoginAndFireCallback(getActivity(),
                        new SimpleFetchListener<LoginResponse>() {

                            @Override
                            public void onComplete(LoginResponse response) {
                                if (response.errCode != Constants.NO_ERROR) {
                                    ToastMsg.showShortMsgByResName(getActivity(),
                                            "umeng_comm_login_failed");
                                    return;
                                }

                                mCommentListView = commentListView;
                                // 解析下一页地址
                                @SuppressWarnings("unchecked")
                                String nextPage = parseCommentNextPage(commentListView
                                        .getCommAdapter().getDataSource());
                                loadMoreComments(nextPage, pos,
                                        footerView);
                            }
                        });
            }
        });
    }

    View getFooterFromViewTag(View view) {
        if (view == null) {
            return null;
        }
        Object footer = view.getTag();
        if (footer instanceof View) {
            return (View) footer;
        }

        return null;
    }

    private String parseCommentNextPage(List<Comment> comments) {
        return comments.size() > 0 ? comments.get(comments.size() - 1).nextPageUrl : "";
    }

    /**
     * 创建加载更多的 footerView</br>
     * 
     * @param adapter
     * @param comments
     * @return
     */
    private View setupCommentFooter(final WrapperListView commentListView,
            final List<Comment> comments, final int pos) {

        final View footerView = inflateCommentFooter(commentListView);
        // 清除原来ListView的adapter问题 TODO : 不一定
        CommonAdapter<?, ?> commentAdapter = commentListView.getCommAdapter();
        @SuppressWarnings("unchecked")
        FeedCommentAdapter adapter = new FeedCommentAdapter(getActivity(), Collections.EMPTY_LIST);
        commentListView.setAdapter(adapter);

        // 将footer设置到ListView的tag中
        commentListView.setTag(footerView);
        commentListView.addFooterView(footerView, null, false);
        commentListView.setAdapter(commentAdapter);
        commentAdapter.notifyDataSetChanged();
        return footerView;
    }

    private View inflateCommentFooter(ViewGroup parent) {
        int footerViewResId = ResFinder.getLayout("umeng_comm_msg_comment_footer");
        return LayoutInflater.from(getActivity()).inflate(
                footerViewResId, parent, false);
    }

    /**
     * 根据url加载更多评论</br>
     * 
     * @param url 评论的url
     */
    private void loadMoreComments(final String url, final int pos, final View view) {
        final int loadMoreResId = ResFinder.getId("umeng_comm_load_more");
        final int progressBarResId = ResFinder.getId("umeng_comm_loadding_pb");
        final View progressBar = view.findViewById(progressBarResId);
        progressBar.setVisibility(View.VISIBLE);
        final View loadMoreView = view.findViewById(loadMoreResId);
        loadMoreView.setVisibility(View.GONE);
        // 获取下一页数据
        mSdkImpl.fetchNextPageData(url, CommentResponse.class,
                new SimpleFetchListener<CommentResponse>() {

                    @SuppressWarnings("unchecked")
                    @Override
                    public void onComplete(CommentResponse response) {
                        progressBar.setVisibility(View.GONE);
                        loadMoreView.setVisibility(View.VISIBLE);

                        List<Comment> oldComments = mCommentListView.getCommAdapter()
                                .getDataSource();
                        // 移除可能重复的评论
                        List<Comment> comments = removeExsitItems(oldComments, response.result);
                        if (comments.size() > 0) {
                            // 此时需要判断是否url为空，来决定是否显示footer
                            String nextPage = parseCommentNextPage(comments);
                            // 如果没有下一页了,那么隐藏footer
                            hideFooterIfNextPageIsEmpty(nextPage);
                            // 加载完成
                            loadMoreComplete(comments, url, pos);
                        }
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private void loadMoreComplete(final List<Comment> comments, String url, int pos) {
        // 将评论添加到feed中
        FeedItem feedItem = findFeedWithId(comments.get(0).feedId);
        feedItem.comments.removeAll(comments);
        feedItem.comments.addAll(comments);

        final WrapperListView commentListView = findListViewWithFeedId(feedItem.id);
        if (commentListView != null) {
            commentListView.getCommAdapter().addData(comments);
        }
        // 将数据保存到数据库
        saveCommentsToDB(comments);
    }

    private WrapperListView findListViewWithFeedId(String feedId) {
        Set<Entry<WrapperListView, String>> set = mCommentLvMap.entrySet();
        Iterator<Entry<WrapperListView, String>> iterator = set.iterator();
        while (iterator.hasNext()) {
            Entry<WrapperListView, String> entry = iterator.next();
            if (entry.getValue().equals(feedId)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void hideFooterIfNextPageIsEmpty(String nextPage) {
        if (TextUtils.isEmpty(nextPage)) {
            View footerView = getFooterFromViewTag(mCommentListView);
            if (footerView != null) {
                mCommentListView.setTag(null);
                mCommentListView.removeFooterView(footerView);
            }
        }
    }

    protected <T> List<T> removeExsitItems(List<T> oldItems, List<T> newItems) {
        // 去掉在本地已经存在的feeds
        oldItems.removeAll(newItems);
        return newItems;
    }

    /**
     * 保存评论到数据库中</br>
     * 
     * @param comments
     */
    private void saveCommentsToDB(final List<Comment> comments) {
        InsertCommand<Comment> insertCommand = new InsertCommand<Comment>(
                DbHelperFactory.getCommentDbHelper(getActivity()), comments);
        insertCommand.execute();
    }

    /**
     * 获取被点击的评论item View</br>
     * 
     * @return
     */
    public View getClickView() {
        return mClickItemView;
    }

    /**
     * 获取显示在EditText中显示的评论文本。不如：回复XXX</br>
     * 
     * @return
     */
    private String getReplyPrefix() {
        if (mReplyUser == null) {
            return "";
        }

        String replyText = ResFinder.getString("umeng_comm_reply");
        String colon = ResFinder.getString("umeng_comm_colon");
        return replyText + mReplyUser.name + colon;
    }

}
