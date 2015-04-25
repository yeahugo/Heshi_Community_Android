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
import android.widget.TextView;

import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.utils.FontUtils;
import com.umeng.comm.ui.utils.ViewFinder;
import com.umeng.comm.ui.widgets.RoundImageView;

public class UserViewParser implements ViewParser {

    @Override
    public View inflate(Context context, ViewGroup parent, boolean attachToRoot) {

        ViewFinder viewFinder = new ViewFinder(context, parent,
                ResFinder.getLayout("umeng_comm_friends_item"));
        View convertView = viewFinder.getRootView();

        // view holder
        UserViewHolder viewHolder = new UserViewHolder();
        viewHolder.mImageView = viewFinder
                .findViewById(ResFinder.getId("umeng_comm_user_picture"));
        viewHolder.mTextView = viewFinder.findViewById(ResFinder
                .getId("umeng_comm_user_name"));
        // 设置tag
        convertView.setTag(viewHolder);
        FontUtils.changeTypeface(convertView);
        return convertView;
    }

    /**
     * @author mrsimple
     */
    public static class UserViewHolder {
        public RoundImageView mImageView;
        public TextView mTextView;
    }
}
