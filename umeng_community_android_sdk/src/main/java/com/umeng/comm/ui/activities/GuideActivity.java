/**
 * 
 */

package com.umeng.comm.ui.activities;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;

import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.dialogs.RecommendTopicDialog;
import com.umeng.comm.ui.fragments.RecommendUserFragment;

/**
 * 用户首次注册成功并修改用户信息后，将进行话题跟活跃用户的引导
 */
public class GuideActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(ResFinder.getLayout("umeng_comm_guide_activity"));
        showTopicDialog();
    }

    /**
     * 显示话题引导页面</br>
     */
    private void showTopicDialog() {
        RecommendTopicDialog topicRecommendDialog = new RecommendTopicDialog(this,
                ResFinder.getStyle("umeng_comm_dialog_fullscreen"));
        topicRecommendDialog.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                showRecommendUserFragment();
            }
        });
        topicRecommendDialog.show();
    }

    private void showRecommendUserFragment() {
        int container = ResFinder.getId("umeng_comm_guide_container");
        setFragmentContainerId(container);
        RecommendUserFragment recommendUserFragment = new RecommendUserFragment();
        addFragment(container, recommendUserFragment);
    }

}
