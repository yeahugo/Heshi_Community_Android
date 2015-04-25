/**
 * 
 */

package com.umeng.comm.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.listeners.Listeners.OnResultListener;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.dialogs.RecommendTopicDialog;
import com.umeng.comm.ui.fragments.RecommendUserFragment;

/**
 * 发现的Activity
 */
public class FindActivity extends BaseFragmentActivity implements OnClickListener {

    private CommUser mUser;
    private String mContainerClass;
    private RecommendTopicDialog mRecommendDialog;
    private RecommendUserFragment mRecommendUserFragment;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(ResFinder.getLayout("umeng_comm_find_layout"));
        findViewById(ResFinder.getId("umeng_comm_title_back_btn")).setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_comm_topic_recommend")).setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_comm_user_recommend")).setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_comm_usercenter_recommend")).setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_comm_setting_recommend")).setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_comm_title_setting_btn")).setVisibility(View.GONE);
        TextView textView = (TextView) findViewById(ResFinder.getId("umeng_comm_title_tv"));
        textView.setText(ResFinder.getString("umeng_comm_find"));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        mUser = getIntent().getExtras().getParcelable(Constants.TAG_USER);
        mContainerClass = getIntent().getExtras().getString(Constants.TYPE_CLASS);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == ResFinder.getId("umeng_comm_title_back_btn")) { // 返回事件
            finish();
        } else if (id == ResFinder.getId("umeng_comm_topic_recommend")) { // 话题推荐
            showRecommendTopic();
        } else if (id == ResFinder.getId("umeng_comm_user_recommend")) { // 用户推荐
            showRecommendUserFragment();
        } else if (id == ResFinder.getId("umeng_comm_usercenter_recommend")) { // 个人中心
            gotoUserInfoActivity();
        } else if (id == ResFinder.getId("umeng_comm_setting_recommend")) {// 设置页面
            Intent setting = new Intent(this, SettingActivity.class);
            setting.putExtra(Constants.TYPE_CLASS, mContainerClass);
            startActivity(setting);
        }
    }

    /**
     * 跳转到用户中心Activity</br>
     */
    private void gotoUserInfoActivity() {
        Intent intent = new Intent(FindActivity.this, UserInfoActivity.class);
        if (mUser == null) {// 来自开发者外部调用的情况
            intent.putExtra(Constants.TAG_USER, CommConfig.getConfig().loginedUser);
        } else {
            intent.putExtra(Constants.TAG_USER, mUser);
        }
        // intent.putExtra(Constants.TYPE_CLASS, mContainerClass); //
        // 设置页面需要此参数，由于个人中心设置被移到此页面，暂时不传递该参数
        startActivity(intent);
    }

    /**
     * 显示推荐话题的Dialog</br>
     */
    private void showRecommendTopic() {
        if (mRecommendDialog == null) {
            mRecommendDialog = new RecommendTopicDialog(this,
                    ResFinder.getStyle("umeng_comm_dialog_fullscreen"));
            mRecommendDialog.setSaveButtonInVisiable();
        }
        mRecommendDialog.show();
    }

    /**
     * 显示推荐用户fragment</br>
     */
    private void showRecommendUserFragment() {
        if (mRecommendUserFragment == null) {
            mRecommendUserFragment = new RecommendUserFragment();
            mRecommendUserFragment.setSaveButtonInvisiable();
            mRecommendUserFragment.setOnResultListener(new OnResultListener() {

                @Override
                public void onResult(int status) {
                    findViewById(ResFinder.getId("umeng_comm_find_baset")).setVisibility(View.VISIBLE);
                    findViewById(ResFinder.getId("container")).setVisibility(View.GONE);
                }
            });
        }
        findViewById(ResFinder.getId("umeng_comm_find_baset")).setVisibility(View.GONE);
        int contaniner = ResFinder.getId("container");
        findViewById(contaniner).setVisibility(View.VISIBLE);
        addFragment(contaniner, mRecommendUserFragment);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( keyCode == KeyEvent.KEYCODE_BACK && findViewById(ResFinder.getId("container")).getVisibility() == View.VISIBLE ) {
            findViewById(ResFinder.getId("umeng_comm_find_baset")).setVisibility(View.VISIBLE);
            findViewById(ResFinder.getId("container")).setVisibility(View.GONE);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
