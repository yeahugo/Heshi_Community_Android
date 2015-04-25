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

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Selection;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.CommUser.Gender;
import com.umeng.comm.core.beans.Source;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.constants.HttpProtocol;
import com.umeng.comm.core.db.DbHelper;
import com.umeng.comm.core.db.DbHelperFactory;
import com.umeng.comm.core.imageloader.ImgDisplayOption;
import com.umeng.comm.core.listeners.Listeners.CommListener;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.nets.Response;
import com.umeng.comm.core.nets.responses.LoginResponse;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.LoginHelper;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ResFinder.ResType;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.activities.BaseFragmentActivity;
import com.umeng.comm.ui.activities.GuideActivity;
import com.umeng.comm.ui.broadcastreceiver.NotifyBroadcastReceiver;
import com.umeng.comm.ui.utils.ViewFinder;
import com.umeng.comm.ui.widgets.SquareImageView;

/**
 * 用户设置Fragment
 */
public class UserSettingFragment extends FontFragment implements OnClickListener {

    private EditText mNickNameEtv;
    private TextView mGendertTextView;
    private SquareImageView mIconImg;
    private CommUser mUser;
    private Dialog mDialog;
    private Gender mGender;
    private boolean isFirstSetting = false;// 是否第一次登录跳转到设置页面
    public boolean isRegisterUserNameInvalid = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        int layout = ResFinder.getLayout("umeng_comm_account_setting");
        View rootView = inflater.inflate(layout, container, false);
        mUser = mUser == null ? CommConfig.getConfig().loginedUser : mUser;
        mGender = mUser.gender;
        initViews(rootView);
        return rootView;
    }

    /**
     * 获取用户设置页面的Fragment</br>
     * 
     * @return
     */
    public static UserSettingFragment getUserSettingFragment() {
        return new UserSettingFragment();
    }

    /**
     * 通过外部设置用户信息
     * 
     * @param user
     */
    public void setUser(CommUser user) {
        mUser = user;
    }

    public void setFirstSetting(boolean isFirstSetting) {
        this.isFirstSetting = isFirstSetting;
    }

    /**
     * 初始化相关视图控件
     */
    private void initViews(View rootView) {

        mViewFinder = new ViewFinder(rootView);

        int userIconResId = ResFinder.getId("umeng_comm_user_icon");
        int nameEditResId = ResFinder.getId("umeng_comm_nickname_edt");
        int genderTextResId = ResFinder.getId("umeng_comm_gender_textview");
        mIconImg = mViewFinder.findViewById(userIconResId);
        mIconImg.setOnClickListener(this);

        // 初始化昵称
        mNickNameEtv = mViewFinder.findViewById(nameEditResId);
        if (!TextUtils.isEmpty(mUser.name)) {
            mNickNameEtv.setText(mUser.name);
            Selection.setSelection(mNickNameEtv.getText(), mNickNameEtv.length());
        }

        // 初始化性别
        mGendertTextView = mViewFinder.findViewById(genderTextResId);
        String genderStr = ResFinder.getString("umeng_comm_male");
        if (mUser.gender == Gender.FEMALE) {
            genderStr = ResFinder.getString("umeng_comm_female");
            changeDefaultIcon(Gender.FEMALE);
        }

        if (!TextUtils.isEmpty(mUser.iconUrl)) {
            mImageLoader.reset();
            mImageLoader.displayImage(mUser.iconUrl, mIconImg, getDisplayOption(mGender));
            mImageLoader.resume();
        }

        mGendertTextView.setText(genderStr);
        mGendertTextView.setOnClickListener(this);

    }

    /**
     * @param bmp
     */
    public void showClipedBitmap(Bitmap bmp) {
        if (bmp != null) {
            mIconImg.setImageBitmap(bmp);
        }
    }

    /**
     * 检查检查昵称、年龄数据是否正确</br>
     * 
     * @return
     */
    private boolean checkData() {
        String name = mNickNameEtv.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            ToastMsg.showShortMsgByResName(getActivity(), "umeng_comm_user_center_no_name");
            return false;
        }

        boolean result = CommonUtils.isUserNameValid(name);
        if (!result) {
            ToastMsg.showShortMsg(getActivity(), ResFinder.getString("umeng_comm_user_name_tips"));
        }
        return result;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        int userIconResId = ResFinder.getId("umeng_comm_user_icon");
        int genderTextViewResId = ResFinder.getId("umeng_comm_gender_textview");
        int maleViewResId = ResFinder.getId("umeng_comm_gender_textview_male");
        int femalViewResId = ResFinder.getId("umeng_comm_gender_textview_femal");
        if (id == userIconResId) {
            if ( isRegisterUserNameInvalid ) {
                ToastMsg.showShortMsgByResName(getActivity(), "umeng_comm_before_save");
            } else {
                selectProfile();
            }
        } else if (id == genderTextViewResId) {
            // 显示选择性别的dialog
            showGenderDialog();
        } else if (id == maleViewResId) {
            String maleStr = ResFinder.getString("umeng_comm_male");
            mGendertTextView.setText(maleStr);
            closeDialog();
            changeDefaultIcon(Gender.MALE);
        } else if (id == femalViewResId) {
            String femalStr = ResFinder.getString("umeng_comm_female");
            mGendertTextView.setText(femalStr);
            closeDialog();
            changeDefaultIcon(Gender.FEMALE);
        }
    }

    /**
     * 根据性别切换用户默认头像。该行为仅仅发生在用户没有头像的情况下</br>
     * 
     * @param gender 用户性别
     */
    private void changeDefaultIcon(Gender gender) {
        mGender = gender;
        // 头像为空的情况下才设置
        if (TextUtils.isEmpty(mUser.iconUrl)) {
            int resId = 0;
            if (gender == Gender.MALE) {
                resId = ResFinder.getResourceId(ResType.DRAWABLE, "umeng_comm_male");
            } else {
                resId = ResFinder.getResourceId(ResType.DRAWABLE, "umeng_comm_female");
            }
            mIconImg.setImageResource(resId);
        }
    }

    private ImgDisplayOption getDisplayOption(Gender gender) {
        ImgDisplayOption displayOption = new ImgDisplayOption();
        int resId = ResFinder.getResourceId(ResType.DRAWABLE, "umeng_comm_male");
        if (gender == Gender.FEMALE) {
            resId = ResFinder.getResourceId(ResType.DRAWABLE, "umeng_comm_female");
        }
        displayOption.setLoadingResId(resId).setLoadFailedResId(resId);
        return displayOption;
    }

    /**
     * 从相册中选择头像</br>
     */
    private void selectProfile() {
        Intent pickImageIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageIntent.setType("image/png;image/jpeg");
        getActivity().startActivityForResult(pickImageIntent, Constants.PIC_SELECT);
    }

    /**
     * 显示选择性别的Dialog</br>
     */
    private void showGenderDialog() {
        int style = ResFinder.getStyle("customDialog");
        int layout = ResFinder.getLayout("umeng_comm_gender_select");
        int femalResId = ResFinder.getId("umeng_comm_gender_textview_femal");
        int maleResId = ResFinder.getId("umeng_comm_gender_textview_male");
        mDialog = new Dialog(getActivity(), style);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(getActivity()).inflate(layout,
                null, false);
        mDialog.setContentView(view);
        mDialog.setCanceledOnTouchOutside(true);
        view.findViewById(femalResId).setOnClickListener(this);
        view.findViewById(maleResId).setOnClickListener(this);
        mDialog.show();
    }

    /**
     * 注册或者更新用户信息
     */
    public void registerOrUpdateUserInfo() {
        boolean flag = checkData();
        if (!flag) {
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(ResFinder.getString("umeng_comm_update_user_info"));
        if (isRegisterUserNameInvalid) {
            register(progressDialog);
        } else {
            updateUserInfo(progressDialog);
        }
    }

    private void register(final ProgressDialog dialog) {
        mUser.name = mNickNameEtv.getText().toString().trim();
        mUser.gender = mGender;
        mSdkImpl.register(mUser, new FetchListener<LoginResponse>() {

            @Override
            public void onStart() {
                dialog.show();
            }

            @Override
            public void onComplete(LoginResponse response) {
                dialog.dismiss();
                if (response.errCode == 0) {
                    Source source = mUser.source;
                    mUser = response.result;
                    LoginHelper.loginSuccess(getActivity(), mUser, source);
                    Intent intent = new Intent(getActivity(),GuideActivity.class);
                    getActivity().startActivity(intent);
                    getActivity().finish();
                }

                showResponseToast(response);
            }
        });
    }

    /**
     * 更新用户信息</br>
     */
    private void updateUserInfo(final ProgressDialog progressDialog) {
        final CommUser tmpUser = new CommUser();
        tmpUser.name = mNickNameEtv.getText().toString();
        tmpUser.gender = mGender;

        mSdkImpl.updateUserProfile(tmpUser, new CommListener() {

            @Override
            public void onStart() {
                progressDialog.show();
            }

            @Override
            public void onComplete(Response data) {
                progressDialog.dismiss();
                // if (data.errCode == Constants.NO_ERROR) {
                // mUser.gender = mGender;
                // mUser.name = tmpUser.name;
                // mUser.iconUrl = CommConfig.getConfig().loginedUser.iconUrl;
                // //
                // updateUserDB();
                // //
                // CommonUtils.saveLoginUserInfo(getActivity(), mUser);
                // //
                // sendBroadcast();
                // //
                // ToastMsg.showShortMsgByResName(getActivity(),
                // "umeng_comm_update_info_success");
                // if (isFirstSetting) {
                // isFirstSetting = false;
                // getActivity().finish();
                // }
                // } else if (data.errCode == Constants.SENSITIVE_ERR_CODE) {
                // ToastMsg.showShortMsgByResName(getActivity(),
                // "umeng_comm_username_sensitive");
                // } else {
                // ToastMsg.showShortMsgByResName(getActivity(),
                // "umeng_comm_update_userinfo_failed");
                // }

                saveUserInfo(data, tmpUser);
                showResponseToast(data);
                if (data.errCode == Constants.NO_ERROR) {
                    sendBroadcast();
                }
            }

            private void sendBroadcast() {
                Intent intent = new Intent(NotifyBroadcastReceiver.USER_INFO_UPDATED);
                intent.putExtra(HttpProtocol.USER_INFO_KEY, mUser);
                getActivity().sendBroadcast(intent);
            }

        });
    }

    /**
     * 
     * 保存用户信息</br>
     * @param data
     * @param tmpUser
     */
    private void saveUserInfo(Response data, CommUser tmpUser) {
        if (data.errCode == Constants.NO_ERROR) {
            mUser.gender = mGender;
            mUser.name = tmpUser.name;
            mUser.iconUrl = CommConfig.getConfig().loginedUser.iconUrl;
            updateUserDB();
            CommonUtils.saveLoginUserInfo(getActivity(), mUser);
            if (isFirstSetting) {
                isFirstSetting = false;
                Intent intent = new Intent(getActivity(),GuideActivity.class);
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        }
    }

    /**
     * 
     * 根据错误码Toast操作</br>
     * @param data
     */
    private void showResponseToast(Response data) {
        if (data.errCode == Constants.NO_ERROR) {
            ToastMsg.showShortMsgByResName(getActivity(), "umeng_comm_update_info_success");
        } else if (data.errCode == Constants.SENSITIVE_ERR_CODE) { // 昵称含有敏感词
            ToastMsg.showShortMsgByResName(getActivity(),
                    "umeng_comm_username_sensitive");
        } else if (data.errCode == Constants.ERR_CODE_USER_NAME_DUPLICATE) { //昵称重复
            ToastMsg.showShortMsgByResName(getActivity(),
                    "umeng_comm_duplicate_name");
        } else {
            ToastMsg.showShortMsgByResName(getActivity(),
                    "umeng_comm_update_userinfo_failed");
        }
    }

    /**
     * 检查昵称的合法性,确保在第一次登录时昵称不合法导致的问题.
     * 
     * @return
     */
    public boolean checkUserName() {
        String name = mNickNameEtv.getText().toString().trim();
        return CommonUtils.isUserNameValid(name);
    }

    /**
     * 将最新的用户数据更新到数据库</br>
     */
    private void updateUserDB() {
        DbHelper<CommUser> helper = DbHelperFactory.getUserDbHelper(getActivity());
        helper.insert(mUser);
    }

    /**
     * 关闭Dialog</br>
     */
    private void closeDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    /**
     * 隐藏输入法</br>
     */
    public void hideInputMethod() {
        ((BaseFragmentActivity) getActivity()).hideInputMethod(mNickNameEtv);
    }

}
