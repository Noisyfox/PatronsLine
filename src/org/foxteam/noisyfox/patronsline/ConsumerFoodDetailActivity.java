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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ConsumerFoodDetailActivity extends Activity {

	private InformationFood mInformationFood = null;
	private InformationShop mInformationShop = null;
	private boolean isFromShop = false;

	private BookmarkProcessTask mBookmarkProcessTack = null;
	private boolean mLastBookmarked = false;

	private View mFoodDeatilLoadView;
	private View mFoodDetailView;

	private Button mBackToShopView;
	private ImageView mFoodImageView;
	private TextView mFoodSpecialView;
	private TextView mFoodNameView;
	private TextView mFoodPriceView;
	private ToggleButton mFoodBookmarkView;
	private TextView mFoodLikesView;
	private TextView mFoodDislikeView;
	private TextView mFoodIntroView;
	private ExpandableListView mFoodCommentsView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_consumer_food_detail);

		mFoodDeatilLoadView = findViewById(R.id.food_detail_load_view);
		mFoodDetailView = findViewById(R.id.food_detail_view);

		mBackToShopView = (Button) findViewById(R.id.button_back_to_shop);
		mFoodImageView = (ImageView) findViewById(R.id.imageView_food);
		mFoodSpecialView = (TextView) findViewById(R.id.textView_food_special);
		mFoodNameView = (TextView) findViewById(R.id.textView_food_name);
		mFoodPriceView = (TextView) findViewById(R.id.textView_food_price);
		mFoodBookmarkView = (ToggleButton) findViewById(R.id.toggleButton_star_is_bookmarked);
		mFoodLikesView = (TextView) findViewById(R.id.textView_food_likes);
		mFoodDislikeView = (TextView) findViewById(R.id.textView_food_dislikes);
		mFoodIntroView = (TextView) findViewById(R.id.textView_food_introduction);
		mFoodCommentsView = (ExpandableListView) findViewById(R.id.expandableListView_comments);

		isFromShop = getIntent().getBooleanExtra("fromShop", false);
		String fid = getIntent().getStringExtra("fid");
		mInformationFood = InformationManager.obtainFoodInformation(fid);
		mInformationShop = InformationManager
				.obtainShopInformation(mInformationFood.sid);

		mFoodBookmarkView.setOnCheckedChangeListener(null);

		showProgress(true);
		new LoadFoodInformationTask().execute();
	}

	public class LoadFoodInformationTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			if (SessionManager.getSessionManager().food_detail(
					mInformationFood.fid) != SessionManager.ERROR_OK) {
				return false;
			}
			if (SessionManager.getSessionManager().shop_detail(
					mInformationShop.sid) != SessionManager.ERROR_OK) {
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				mBackToShopView.setText("<  " + mInformationShop.name);
				if (isFromShop) {
					mBackToShopView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							finish();
						}
					});
				} else {
					mBackToShopView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent();
							intent.putExtra("sid", mInformationShop.sid);
							intent.setClass(ConsumerFoodDetailActivity.this,
									ConsumerShopDetailActivity.class);
							startActivity(intent);
						}
					});
				}
				PictureManager.loadPicture(mInformationFood, mFoodImageView);
				mFoodSpecialView
						.setVisibility(mInformationFood.special ? View.VISIBLE
								: View.GONE);
				mFoodNameView.setText(mInformationFood.name);
				mFoodPriceView.setText(String.format("%.1f",
						mInformationFood.price));
				mLastBookmarked = mInformationFood.bookmark;
				mFoodBookmarkView.setChecked(mInformationFood.bookmark);
				mFoodBookmarkView
						.setOnCheckedChangeListener(new OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(
									CompoundButton buttonView, boolean isChecked) {
								if (mLastBookmarked == isChecked)
									return;

								if (mBookmarkProcessTack != null) {
									return;
								} else {
									buttonView.setEnabled(false);
									mBookmarkProcessTack = new BookmarkProcessTask();
									mBookmarkProcessTack.execute(isChecked);
								}
							}
						});
				mFoodLikesView.setText(String.valueOf(mInformationFood.likes));
				mFoodDislikeView.setText(String
						.valueOf(mInformationFood.dislikes));
				if (TextUtils.isEmpty(mInformationFood.introduction)) {
					mFoodIntroView
							.setText(R.string.information_no_introduction);
				} else {
					mFoodIntroView.setText(mInformationFood.introduction);
				}
				// mFoodCommentsView;
				showProgress(false);
			}
		}

	}

	class BookmarkProcessTask extends AsyncTask<Boolean, Void, Boolean> {
		boolean add = false;

		@Override
		protected Boolean doInBackground(Boolean... params) {
			add = params[0];
			int result = 0;
			if (add) {
				result = SessionManager.getSessionManager().bookmark_add(
						mInformationFood.fid, true);
			} else {
				InformationBookmarkFood bmff = null;
				if (SessionManager.getCurrentSession().bookmarkFood.isEmpty()) {
					if (SessionManager.getSessionManager().bookmark_list_food() != SessionManager.ERROR_OK) {
						return false;
					} else {
						if (SessionManager.getCurrentSession().bookmarkFood
								.isEmpty()) {
							return true;
						}
					}
				}
				for (InformationBookmarkFood bmf : SessionManager
						.getCurrentSession().bookmarkFood) {
					if (bmf.food.fid == mInformationFood.fid) {
						bmff = bmf;
						break;
					}
				}
				if (bmff == null) {
					return true;
				}
				SessionManager.getCurrentSession().bookmarkFood.remove(bmff);
				result = SessionManager.getSessionManager().bookmark_delete(
						bmff.bfid, true);
			}
			return result == SessionManager.ERROR_OK;
		}

		@Override
		protected void onCancelled() {
			mFoodBookmarkView.setEnabled(true);
			mBookmarkProcessTack = null;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				Toast.makeText(
						getApplication(),
						add ? R.string.information_bookmark_food_add_success
								: R.string.information_bookmark_food_delete_success,
						Toast.LENGTH_SHORT).show();

				mLastBookmarked = add;
			} else {
				Toast.makeText(
						getApplication(),
						add ? R.string.information_bookmark_food_add_failure
								: R.string.information_bookmark_food_delete_failure,
						Toast.LENGTH_SHORT).show();

				mFoodBookmarkView.setChecked(mLastBookmarked);
			}
			mFoodBookmarkView.setEnabled(true);
			mBookmarkProcessTack = null;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.consumer_food_detail, menu);
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

			mFoodDeatilLoadView.setVisibility(View.VISIBLE);
			mFoodDeatilLoadView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mFoodDeatilLoadView
									.setVisibility(show ? View.VISIBLE
											: View.GONE);
						}
					});

			mFoodDetailView.setVisibility(View.VISIBLE);
			mFoodDetailView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mFoodDetailView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mFoodDeatilLoadView.setVisibility(show ? View.VISIBLE : View.GONE);
			mFoodDetailView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

}
