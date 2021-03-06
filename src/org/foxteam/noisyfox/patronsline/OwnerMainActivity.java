package org.foxteam.noisyfox.patronsline;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class OwnerMainActivity extends SherlockActivity {

	private View mShopDetailLoadView;
	private View mShopDetailView;
	private ImageView mImageView_picture;
	private TextView mTextViewPopularity;
	private TextView mTextViewMark;

	private InformationShop mInformationShop = null;

	private ActionBar mActionBar;

	public static final int REQUESTCODE_NEW_SHOP = 1;
	public static final int REQUESTCODE_EDIT_SHOP = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_owner_main);

		mActionBar = getSupportActionBar();
		mActionBar.setIcon(R.drawable.title_icon_shop);

		mShopDetailLoadView = findViewById(R.id.shop_detail_load_view);
		mShopDetailView = findViewById(R.id.shop_detail_view);
		mImageView_picture = (ImageView) findViewById(R.id.imageView_picture);
		mTextViewPopularity = (TextView) findViewById(R.id.textView_popularity);
		mTextViewMark = (TextView) findViewById(R.id.textView_mark);

		mInformationShop = SessionManager.getCurrentSession().ownedShop.get(0);

		findViewById(R.id.button_view_food_order).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent i = new Intent();
						i.setClass(OwnerMainActivity.this,
								OwnerFoodOrderActivity.class);
						i.putExtra("sid", mInformationShop.sid);
						startActivity(i);
					}
				});

		findViewById(R.id.button_food_manage).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent i = new Intent();
						i.setClass(OwnerMainActivity.this,
								OwnerFoodManageActivity.class);
						i.putExtra("sid", mInformationShop.sid);
						startActivity(i);
					}
				});

		loadShopData();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Manage").setIcon(android.R.drawable.ic_menu_manage)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		Intent i = new Intent();
		i.setClass(OwnerMainActivity.this, OwnerShopEditActivity.class);
		i.putExtra("sid", mInformationShop.sid);
		i.putExtra("requestCode", REQUESTCODE_EDIT_SHOP);
		startActivityForResult(i, REQUESTCODE_EDIT_SHOP);

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
					// ???????????????????????????
					Intent i = new Intent();
					i.setClass(OwnerMainActivity.this,
							OwnerShopEditActivity.class);
					i.putExtra("sid", mInformationShop.sid);
					i.putExtra("requestCode", REQUESTCODE_NEW_SHOP);
					startActivityForResult(i, REQUESTCODE_NEW_SHOP);
				} else {
					// ???????????????
					showProgress(false);
					mActionBar.setTitle(mInformationShop.name);
					mTextViewPopularity.setText(String.format(
							getString(R.string.label_shop_popularity),
							mInformationShop.popularity));
					mTextViewMark.setText(String.format(
							getString(R.string.label_shop_mark),
							mInformationShop.mark));
					PictureManager.loadPicture(mInformationShop,
							mImageView_picture);
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

			mShopDetailLoadView.setVisibility(View.VISIBLE);
			mShopDetailLoadView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mShopDetailLoadView
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
			mShopDetailLoadView.setVisibility(show ? View.VISIBLE : View.GONE);
			mShopDetailView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
}
