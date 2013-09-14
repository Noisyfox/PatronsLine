package org.foxteam.noisyfox.patronsline;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;

public class OwnerMainActivity extends Activity {

	private View mShopDeatilLoadView;
	private View mShopDetailView;
	InformationShop mInformationShop = null;

	public static final int REQUESTCODE_NEW_SHOP = 1;
	public static final int REQUESTCODE_EDIT_SHOP = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_owner_main);

		mShopDeatilLoadView = findViewById(R.id.shop_detail_load_view);
		mShopDetailView = findViewById(R.id.shop_detail_view);

		mInformationShop = SessionManager.getCurrentSession().ownedShop.get(0);

		loadShopData();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.owner_main, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			loadShopData();
		}
	}

	@Override
	protected void onDestroy() {
		PatronsLineApplication mMyApp = (PatronsLineApplication) this
				.getApplication();
		mMyApp.destroyLocationClient();
		mMyApp.destroyEngineManager();
		PictureManager.cacheSave();
		super.onDestroy();
	}

	LoadShopInformationTask mLoadShopInformationTask = null;

	private void loadShopData() {
		if (mLoadShopInformationTask != null) {
			return;
		}
		showProgress(true);
		mLoadShopInformationTask = new LoadShopInformationTask();
		mLoadShopInformationTask.execute();
	}

	private class LoadShopInformationTask extends
			AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			return SessionManager.getSessionManager().shop_detail(
					mInformationShop.sid) == SessionManager.ERROR_OK;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				if (TextUtils.isEmpty(mInformationShop.name)) {
					// 弹出创建店铺的界面
					Intent i = new Intent();
					i.setClass(OwnerMainActivity.this,
							OwnerShopEditActivity.class);
					i.putExtra("sid", mInformationShop.sid);
					i.putExtra("requestCode", REQUESTCODE_NEW_SHOP);
					startActivityForResult(i, REQUESTCODE_NEW_SHOP);
				} else {
					// 显示主界面
					showProgress(false);
				}
			}
			mLoadShopInformationTask = null;
		}

		@Override
		protected void onCancelled() {
			mLoadShopInformationTask = null;
		}

	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mShopDeatilLoadView.setVisibility(View.VISIBLE);
			mShopDeatilLoadView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mShopDeatilLoadView
									.setVisibility(show ? View.VISIBLE
											: View.GONE);
						}
					});

			mShopDetailView.setVisibility(View.VISIBLE);
			mShopDetailView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mShopDetailView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mShopDeatilLoadView.setVisibility(show ? View.VISIBLE : View.GONE);
			mShopDetailView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
}
