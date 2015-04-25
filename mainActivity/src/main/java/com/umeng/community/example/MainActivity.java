package com.umeng.community.example;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.hefan.wewei.R;
import com.umeng.comm.core.CommunitySDK;
import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.impl.CommunityFactory;
import com.umeng.comm.core.sdkmanager.ImageLoaderManager;
import com.umeng.comm.core.sdkmanager.LoginSDKManager;
import com.umeng.comm.core.sdkmanager.PushSDKManager;
import com.umeng.comm.login.sso.UMQQSsoHandler;
import com.umeng.comm.push.UmengPushImpl;
import com.umeng.comm.ui.activities.FeedDetailActivity;
import com.umeng.comm.ui.fragments.CommunityMainFragment;
import com.umeng.community.example.custom.MyLoginImpl;
import com.umeng.community.example.custom.UILImageLoader;
import com.umeng.login.controller.UMAuthService;
import com.umeng.login.controller.UMLoginServiceFactory;

public class MainActivity extends FragmentActivity {

	static CommunitySDK mCommSDK = null;
	/**
	 * 是否使用Fragment的方式集成
	 */
	private boolean isUseFragment = false;
	private CommunityMainFragment mFeedsFragment;

	// TODO
	String tempId = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mCommSDK = CommunityFactory.getCommSDK(getApplicationContext());

		// 使用Fragment的方式集成
		isUseFragment = true;
		if (isUseFragment) {
			mFeedsFragment = new CommunityMainFragment();
			mFeedsFragment.setBackButtonVisibility(View.INVISIBLE);
			// mFeedsFragment.setNavTitleVisibility(View.GONE);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, mFeedsFragment).commit();
		} else {
			if (savedInstanceState == null) {
				getSupportFragmentManager().beginTransaction()
						.add(R.id.container, new PlaceholderFragment())
						.commit();
			}
		}
		// // 在初始化CommunitySDK之前配置推送和登录等组件
		useSocialLogin();
		useMyPushComponent();
		// useMyImageLoader();

		// 登录自定义
		// userCustomLogin();

	}

	/**
	 * 自定义自己的登录系统
	 */
	protected void useSocialLogin() {
		// 管理器
		LoginSDKManager manager = CommConfig.getConfig().getLoginSDKManager();
		// 用户自定义的登录
		UMAuthService mLogin = UMLoginServiceFactory
				.getLoginService("umeng_login_impl");
		// 将登录实现注入到sdk中,key为umeng_login
		manager.addImpl("umeng_login", mLogin);
		// 使用该登录实现
		manager.useThis("umeng_login");
		String appId = "100424468";
		String appKey = "c7394704798a158208a74ab60104f0ba";
		// SSO 设置
		// mLogin.getConfig().setSsoHandler(new SinaSsoHandler());
		new UMQQSsoHandler(this, appId, appKey).addToSocialSDK();

	}

	protected void userCustomLogin() {
		// 管理器
		LoginSDKManager manager = CommConfig.getConfig().getLoginSDKManager();
		// 将登录实现注入到sdk中,key为umeng_login
		manager.addImpl(MyLoginImpl.class.getSimpleName(), new MyLoginImpl());
		// 使用该登录实现
		manager.useThis(MyLoginImpl.class.getSimpleName());
	}

	/**
	 * 自定义自己的ImageLoader
	 */
	protected void useMyImageLoader() {
		//
		final String imageLoadKey = UILImageLoader.class.getSimpleName();
		// 使用第三方ImageLoader库,添加到sdk manager中, 并且使用useThis来使用该加载器.
		ImageLoaderManager manager = CommConfig.getConfig()
				.getImageLoaderManager();
		manager.addImpl(imageLoadKey, new UILImageLoader(this));
		manager.useThis(imageLoadKey);
	}

	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// if ( isUseFragment && mFeedsFragment.onKeyDown(keyCode, event)) {
	// return true;
	// }
	// return super.onKeyDown(keyCode, event);
	// }

	/**
	 * 自定义自己的推送系统
	 */
	protected void useMyPushComponent() {
		final String pushClz = UmengPushImpl.class.getSimpleName();
		// 推送实现
		PushSDKManager.getInstance().addImpl(pushClz, new UmengPushImpl());
		// 使用这个推送实现
		PushSDKManager.getInstance().useThis(pushClz);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		ViewStub mListStub;
		ListView mListView;
		TextView mTextView;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			Button detailButton = (Button) rootView.findViewById(R.id.comm_btn);
			detailButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent gotoFeedIntent = new Intent(getActivity(),
							FeedDetailActivity.class);
					getActivity().startActivity(gotoFeedIntent);
				}
			});
			// detailButton.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),
			// "SourceHanSansCN-Light.otf"));
			Button msgButton = (Button) rootView
					.findViewById(R.id.msg_listview_btn);
			msgButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mCommSDK.openCommunity(getActivity());
				}
			});

			Typeface typeface = Typeface.createFromAsset(getActivity()
					.getAssets(), "fonts/lantinghei-font.TTF");
			msgButton.setTypeface(typeface);

			// 设置字体
			mCommSDK.getConfig().setTypeface(typeface);

			return rootView;
		} // end of
	}

	@Override
	protected void onResume() {
		super.onResume();
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			boolean fromLogout = bundle
					.getBoolean(Constants.FROM_COMMUNITY_LOGOUT);
			Log.d("", "######## from community logout " + fromLogout);
			// 属于友盟微社区“退出登录”的情况，此时你需要处理自己的逻辑（比如显示MainFeedsFragment）
		} else {
			Log.d("",
					"######## from first start activity or don't override onNewIntent method,please refer to demo onNewIntent method...");
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

}
