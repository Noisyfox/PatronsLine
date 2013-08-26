package org.foxteam.noisyfox.patronsline;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;

public class SplashScreenActivity extends Activity {
	private Thread mSplashThread = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);

		final ImageView splashImageView = (ImageView) findViewById(R.id.SplashImageView);
		splashImageView.setBackgroundResource(R.drawable.flag);
		final AnimationDrawable frameAnimation = (AnimationDrawable) splashImageView
				.getBackground();
		splashImageView.post(new Runnable() {
			@Override
			public void run() {
				frameAnimation.start();
			}
		});

		final SplashScreenActivity sPlashScreen = this;

		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		final boolean firstRun = pref.getBoolean("firstRun", true);

		// The thread to wait for splash screen events
		mSplashThread = new Thread() {
			@Override
			public void run() {
				try {
					synchronized (this) {
						// Wait given period of time or exit on touch
						wait(2000);
					}
				} catch (InterruptedException ex) {
				}

				finish();

				// Run next activity
				Intent intent = new Intent();
				if (firstRun) {
					intent.setClass(sPlashScreen, GuideActivity.class);
				} else {
					intent.setClass(sPlashScreen, LoginActivity.class);
				}
				startActivity(intent);
				// stop();
			}
		};

		mSplashThread.start();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent evt) {
		if (evt.getAction() == MotionEvent.ACTION_DOWN) {
			synchronized (mSplashThread) {
				mSplashThread.notifyAll();
			}
		}
		return true;
	}

}
