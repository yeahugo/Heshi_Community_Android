/**
 *
 *	created by Mr.Simple, Dec 3, 20143:16:27 PM.
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.utils.FontUtils;
import com.umeng.comm.ui.utils.ViewFinder;
import com.umeng.comm.ui.widgets.RoundImageView;

public class ActiveUserViewParser implements ViewParser {

    @Override
    public View inflate(Context context, ViewGroup parent, boolean attachToRoot) {

        ViewFinder viewFinder = new ViewFinder(context, parent,
                ResFinder.getLayout("umeng_comm_active_user_item"));
        View convertView = viewFinder.getRootView();

        // view holder
        ActiveUserViewHolder viewHolder = new ActiveUserViewHolder();
        viewHolder.mImageView = viewFinder
                .findViewById(ResFinder.getId("umeng_comm_active_user_icon"));
        viewHolder.muserNameTextView = viewFinder.findViewById(ResFinder
                .getId("umeng_comm_active_user_name"));
        viewHolder.mToggleButton = viewFinder.findViewById(ResFinder
                .getId("umeng_comm_active_user_togglebutton"));
        viewHolder.mGenderImageView = viewFinder.findViewById(ResFinder
                .getId("umeng_comm_active_user_gender"));
        viewHolder.mMsgFansTextView = viewFinder.findViewById(ResFinder
                .getId("umeng_comm_active_user_msg"));
        
        viewHolder.mView = viewFinder.findViewById(ResFinder
                .getId("umeng_comm_active_user_layout"));
        
        // 设置tag
        convertView.setTag(viewHolder);
        FontUtils.changeTypeface(convertView);
        return convertView;
    }

    /**
     * @author mrsimple
     */
    public static class ActiveUserViewHolder {
        public RoundImageView mImageView;
        public TextView muserNameTextView;
        public ToggleButton mToggleButton;
        public ImageView mGenderImageView;
        public TextView mMsgFansTextView;
        public View mView;
    }
}
