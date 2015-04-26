package com.umeng.community.example;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

import com.hefan.wewei.R;

public class SplashActivity extends Activity {

	private static final long DELAY_TIME = 500L;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		redirectByTime();
	}

	private void redirectByTime() {
//		new Handler().postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				startActivity(new Intent(SplashActivity.this,MainActivity.class));
////				AnimationUtil.finishActivityAnimation(SplashActivity.this);
//			}
//		}, DELAY_TIME);
		
		View view = findViewById(R.id.guide);
		AlphaAnimation animation = new AlphaAnimation(0.0f,1.0f);
		animation.setDuration(1000);
		animation.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationStart(Animation animation) {
				
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				startActivity(new Intent(SplashActivity.this,MainActivity.class));
				finish();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}});
		view.startAnimation(animation);
	}
}

