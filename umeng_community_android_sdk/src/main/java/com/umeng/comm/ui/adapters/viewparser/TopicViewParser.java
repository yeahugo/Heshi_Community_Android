/**
 *
 *	created by Mr.Simple, Dec 16, 201411:12:07 AM.
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
import android.widget.ToggleButton;

import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.utils.ViewFinder;

public class TopicViewParser implements ViewParser {

    @Override
    public View inflate(Context context, ViewGroup parent, boolean attachToRoot) {

        int layout = ResFinder.getLayout("umeng_comm_followed_topic_lv_item");
        // View convertView = LayoutInflater.from(context).inflate(layout,
        // parent,
        // false);

        ViewFinder viewFinder = new ViewFinder(context, parent, layout);
        View convertView = viewFinder.getRootView();

        TopicViewHolder viewHolder = new TopicViewHolder();
        viewHolder.mTopicTv = viewFinder.findViewById(ResFinder
                .getId("umeng_comm_topic_tv"));
        viewHolder.mDescTv = viewFinder.findViewById(ResFinder
                .getId("umeng_comm_topic_desc_tv"));
        viewHolder.mFollowedBtn = viewFinder.findViewById(ResFinder
                .getId("umeng_comm_topic_togglebutton"));
        viewHolder.mView = viewFinder.findViewById(ResFinder.getId("umeng_comm_layout"));
        // 设置Tag
        convertView.setTag(viewHolder);

        return convertView;
    }

    /**
     * @author mrsimple
     */
    public static class TopicViewHolder {
        public TextView mTopicTv;
        public TextView mDescTv;
        public ToggleButton mFollowedBtn;
        public View mView;
    }
}
