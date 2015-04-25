/**
 *
 *	created by Mr.Simple, Dec 16, 201411:17:58 AM.
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
import android.widget.TextView;

import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.utils.FontUtils;
import com.umeng.comm.ui.utils.ViewFinder;
import com.umeng.comm.ui.widgets.RoundImageView;

public class FeedCommentViewParser implements ViewParser {

    @Override
    public View inflate(Context context, ViewGroup parent, boolean attachToRoot) {
        ViewFinder viewFinder = new ViewFinder(context, parent,
                ResFinder.getLayout("umeng_comm_msg_comment_item"));
        View convertView = viewFinder.getRootView();
        //
        CommentViewHolder holder = new CommentViewHolder();
        holder.contentTextView = viewFinder.findViewById(ResFinder
                .getId("umeng_comm_msg_comment_content"));
        holder.userHeaderImageView = viewFinder.findViewById(ResFinder
                .getId("umeng_comm_msg_comment_header"));
        // set tag
        convertView.setTag(holder);

        // 修改字体
        FontUtils.changeTypeface(convertView);
        return convertView;
    }

    /**
     * 评论Item的view holder
     */
    public static class CommentViewHolder {
        public RoundImageView userHeaderImageView;
        public TextView contentTextView;
    }
}
