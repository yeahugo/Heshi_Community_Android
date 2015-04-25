/**
 *
 *	created by Mr.Simple, Nov 12, 201411:43:19 AM.
 *	Copyright (c) 2014, hehonghui@umeng.com All Rights Reserved.
 *
 *                #####################################################
 *                #                                                   #
 *                #                       _oo0oo_                     #   
 *                #                      o8888888o                    #
 *                #                      88" . "88                    #
 *                #                      (| -_- |)                    #
 *                #                      0\  =  /0                    #   
 *                #                    ___/`---'\___                  #
 *                #                  .' \\|     |# '.                 #
 *                #                 / \\|||  :  |||# \                #
 *                #                / _||||| -:- |||||- \              #
 *                #               |   | \\\  -  #/ |   |              #
 *                #               | \_|  ''\---/''  |_/ |             #
 *                #               \  .-\__  '-'  ___/-. /             #
 *                #             ___'. .'  /--.--\  `. .'___           #
 *                #          ."" '<  `.___\_<|>_/___.' >' "".         #
 *                #         | | :  `- \`.;`\ _ /`;.`/ - ` : | |       #
 *                #         \  \ `_.   \_ __\ /__ _/   .-` /  /       #
 *                #     =====`-.____`.___ \_____/___.-`___.-'=====    #
 *                #                       `=---='                     #
 *                #     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~   #
 *                #                                                   #
 *                #               佛祖保佑         永无BUG              #
 *                #                                                   #
 *                #####################################################
 */

package com.umeng.comm.ui.adapters.viewparser;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.utils.FontUtils;
import com.umeng.comm.ui.utils.ViewFinder;
import com.umeng.comm.ui.widgets.LikeView;
import com.umeng.comm.ui.widgets.RoundImageView;
import com.umeng.comm.ui.widgets.WrapperGridView;
import com.umeng.comm.ui.widgets.WrapperListView;

/**
 * ListView的Feed Item View解析器. ( 将试图的显示和解析解耦, 便于测试, 也便于复用. )
 * 
 * @author mrsimple
 */
public class FeedItemViewParser implements ViewParser {

    @Override
    public View inflate(Context context, ViewGroup parent, boolean attachToRoot) {

        int layout = ResFinder.getLayout("umeng_comm_feed_lv_item");
        int feedTypeResId = ResFinder.getId("feed_type_img_btn");
        int userIconResId = ResFinder.getId("user_portrait_img_btn");
        int userNameResId = ResFinder.getId("umeng_comm_msg_user_name");
        int textResId = ResFinder.getId("umeng_comm_msg_text");
        int timeResId = ResFinder.getId("umeng_comm_msg_time_tv");
        int locResId = ResFinder.getId("umeng_comm_msg_location");
        int locTextResId = ResFinder.getId("umeng_comm_msg_location_text");
        int gvStubResId = ResFinder.getId("umeng_comm_msg_images_gv_viewstub");
        int commentBtnResId = ResFinder.getId("msg_comment_btn");
        int commentStubResId = ResFinder.getId("umeng_comm_msg_comment_normal_stub");
        int likeResId = ResFinder.getId("umeng_comm_like_tv");
        int dividerResId = ResFinder.getId("umeng_comm_divide_line_1");
        int forwardgvResId = ResFinder.getId("forward_image_gv_layout");
        int forwardTextResId = ResFinder.getId("forard_text_tv");
        //
        FeedItemViewHolder viewHolder = new FeedItemViewHolder();
        // item view
        // View convertView = LayoutInflater.from(context).inflate(layout,
        // parent, attachToRoot);

        ViewFinder viewFinder = new ViewFinder(context, parent, layout);
        View convertView = viewFinder.getRootView();

        // 公告或者好友feed的图标
        viewHolder.mFeedTypeIcon = viewFinder.findViewById(feedTypeResId);
        // 用户头像
        viewHolder.mUserIcon = viewFinder.findViewById(userIconResId);

        // 发布该消息的昵称
        viewHolder.mUserNameTv = viewFinder
                .findViewById(userNameResId);
        // 文本内容
        viewHolder.mTextView = viewFinder.findViewById(textResId);
        // 更新时间
        viewHolder.mUpdateTime = viewFinder.findViewById(timeResId);

        // 位置图标
        viewHolder.mLocImageView = viewFinder.findViewById(locResId);
        // 地理位置
        viewHolder.mLocation = viewFinder.findViewById(locTextResId);
        /**
         * 九宫格图片的View Stub
         */
        viewHolder.mGrideViewStub = viewFinder.findViewById(gvStubResId);
        // 操作栏, 点击时出现一个包含转发、评论、赞功能的popupwindow
        viewHolder.mActionButton = viewFinder.findViewById(commentBtnResId);

        // 评论列表的ViewStub
        viewHolder.mCommentViewStub = viewFinder.findViewById(commentStubResId);
        // 赞列表
        viewHolder.mLikeView = (LikeView) viewFinder.findViewById(likeResId);
        viewHolder.mLikeLayout = viewFinder.findViewById(ResFinder.getId("umeng_comm_like_layout"));

        viewHolder.mLikeCountTv = viewFinder.findViewById(ResFinder
                .getId("umeng_comm_like_count_tv"));

        // 赞与评论分割线
        viewHolder.mDivideView = viewFinder.findViewById(dividerResId);
        // 转发时候的text和图片gv
        viewHolder.mForwardLayout = viewFinder.findViewById(forwardgvResId);
        // 转发文本内容
        viewHolder.mForwardTextView = viewFinder.findViewById(forwardTextResId);
        // 弹出举报、删除feed对话框的按钮
        viewHolder.mDialogButton = viewFinder
                .findViewById(ResFinder.getId("umeng_comm_dialog_btn"));
        // 设置tag
        convertView.setTag(viewHolder);
        //
        FontUtils.changeTypeface(convertView);
        return convertView;
    }

    /**
     * @author mrsimple
     */
    public static class FeedItemViewHolder {
        // 公告或者好友feed的图标
        public ImageView mFeedTypeIcon;
        // 用户头像图标
        public RoundImageView mUserIcon;
        // 昵称
        public TextView mUserNameTv;
        // feed的文本
        public TextView mTextView;
        // 更新时间
        public TextView mUpdateTime;
        // 位置图标
        public ImageView mLocImageView;
        // 位置的文本信息
        public TextView mLocation;
        // feed的图片GridView
        public WrapperGridView mImageGv;
        // 图片GridView的ViewStub
        public ViewStub mGrideViewStub;
        // 赞、评论、转发的触发按钮
        public ImageView mActionButton;
        // 消息流页面的评论ListView
        public WrapperListView mCommentsListView;
        // 评论ListView的ViewStub
        public ViewStub mCommentViewStub;
        // 消息流页面赞
        public LikeView mLikeView;
//
        public TextView mLikeCountTv;
        public View mLikeLayout;
        // 分割线
        public View mDivideView;
        // 转发的根视图, 包含被转发的文本和图片gridview
        public View mForwardLayout;
        // 被转发的文本
        public TextView mForwardTextView;
        //
        public ImageButton mDialogButton;
    }

}
