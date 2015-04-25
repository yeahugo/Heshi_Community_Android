/**
 *
 *	created by Mr.Simple, Nov 24, 20144:50:53 PM.
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

public class FriendItemViewParser implements ViewParser {

    @Override
    public View inflate(Context context, ViewGroup parent, boolean attachToRoot) {

        int layout = ResFinder.getLayout("umeng_comm_at_friend_lv_item");
        int iconResId = ResFinder.getId("umeng_comm_friend_picture");
        int nameResId = ResFinder.getId("umeng_comm_friend_name");
        int otherInfoResId = ResFinder.getId("umeng_comm_other_info");
        ImgTextViewHolder viewHolder = new ImgTextViewHolder();
        // 加载item 布局
        // View convertView = LayoutInflater.from(context).inflate(
        // layout, parent, attachToRoot);

        ViewFinder viewFinder = new ViewFinder(context, parent, layout);
        View convertView = viewFinder.getRootView();

        viewHolder = new ImgTextViewHolder();
        // 查找views
        viewHolder.mImageView = viewFinder.findViewById(iconResId);
        viewHolder.mTextView = viewFinder.findViewById(nameResId);
        viewHolder.mDetailTextView = viewFinder.findViewById(otherInfoResId);
        // 设置tag
        convertView.setTag(viewHolder);

        //
        FontUtils.changeTypeface(convertView);

        return convertView;
    }

    /**
     * 包含imageview和textview的ViewHolder
     * 
     * @author mrsimple
     */
    public static final class ImgTextViewHolder {
        public RoundImageView mImageView;
        public TextView mTextView;
        public TextView mDetailTextView;
        // public CheckBox mCheckBox;
    }

}
