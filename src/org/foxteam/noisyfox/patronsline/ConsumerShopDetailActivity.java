package org.foxteam.noisyfox.patronsline;

import java.util.List;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class ConsumerShopDetailActivity extends SherlockListActivity {

	private View mShopDeatilLoadView;
	private View mShopDetailView;

	private InformationSession mInformationSession = null;
	private InformationShop mInformationShop = null;
	private List<InformationFood> mFoods = null;
	private TitledUserFoodAdapter mFoodAdapter = null;
	private View titleView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_consumer_shop_detail);

		mShopDeatilLoadView = findViewById(R.id.shop_detail_load_view);
		mShopDetailView = findViewById(R.id.shop_detail_view);

		mInformationSession = SessionManager.getCurrentSession();

		String sid = getIntent().getStringExtra("sid");
		mInformationShop = InformationManager.obtainShopInformation(sid);
		mFoods = mInformationShop.foods;

		this.setTitle(mInformationShop.name);

		LayoutInflater inflater = (LayoutInflater) ConsumerShopDetailActivity.this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		titleView = inflater.inflate(R.layout.item_title_consumer_shop, null,
				false);

		mFoodAdapter = new TitledUserFoodAdapter(this, getListView()) {

			@Override
			public View getTitleView(View convertView, ViewGroup parent) {
				return titleView;
			}

		};
		mFoodAdapter.setData(mFoods);
		this.setListAdapter(mFoodAdapter);

		showProgress(true);
		new LoadShopInformationTask().execute();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (position == 0 || v.getTag() == null) {
			super.onListItemClick(l, v, position, id);
		} else {
			InformationFood food = (InformationFood) v.getTag();
			Intent intent = new Intent();
			intent.putExtra("fid", food.fid);
			intent.putExtra("fromShop", true);
			intent.setClass(this, ConsumerFoodDetailActivity.class);
			startActivity(intent);
		}
	}

	public class LoadShopInformationTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			return SessionManager.getSessionManager().shop_detail(
					mInformationShop.sid) == SessionManager.ERROR_OK;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				mFoodAdapter.setData(mFoods);
				showProgress(false);
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.consumer_shop_detail, menu);
		return true;
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
