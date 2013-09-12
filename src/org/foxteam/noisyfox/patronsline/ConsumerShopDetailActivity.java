package org.foxteam.noisyfox.patronsline;

import java.util.List;

import org.foxteam.noisyfox.patronsline.PictureManager.OnPictureGetListener;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ConsumerShopDetailActivity extends SherlockListActivity {

	private View mShopDeatilLoadView;
	private View mShopDetailView;

	private InformationShop mInformationShop = null;
	private List<InformationFood> mFoods = null;
	private TitledUserFoodAdapter mFoodAdapter = null;

	private BookmarkProcessTask mBookmarkProcessTack = null;
	private boolean mLastBookmarked = false;

	private View titleView;
	private ImageView imageView_photo;
	private TextView textView_name;
	private TextView textView_mark;
	private ToggleButton toggleBotton_bookmark;
	private RatingBar ratingBar_mark;
	private TextView textView_introduction;
	private TextView textView_address;
	private TextView textView_phone;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_consumer_shop_detail);

		mShopDeatilLoadView = findViewById(R.id.shop_detail_load_view);
		mShopDetailView = findViewById(R.id.shop_detail_view);

		String sid = getIntent().getStringExtra("sid");
		mInformationShop = InformationManager.obtainShopInformation(sid);
		mFoods = mInformationShop.foods;

		this.setTitle(mInformationShop.name);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		titleView = inflater.inflate(R.layout.item_title_consumer_shop,
				this.getListView(), false);

		imageView_photo = (ImageView) titleView
				.findViewById(R.id.imageView_photo);
		textView_name = (TextView) titleView.findViewById(R.id.textView_name);
		textView_mark = (TextView) titleView.findViewById(R.id.textView_mark);
		toggleBotton_bookmark = (ToggleButton) titleView
				.findViewById(R.id.toggleButton_star_is_bookmarked);
		ratingBar_mark = (RatingBar) titleView.findViewById(R.id.ratingBar1);
		textView_introduction = (TextView) titleView
				.findViewById(R.id.textView_introduction);
		textView_address = (TextView) titleView
				.findViewById(R.id.textView_address);
		textView_phone = (TextView) titleView.findViewById(R.id.TextView_phone);
		titleView.findViewById(R.id.imageButton_mapView).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent();
						intent.setClass(ConsumerShopDetailActivity.this,
								ConsumerNavigateActivity.class);
						intent.putExtra(
								ConsumerNavigateActivity.STR_SHOPADDRESS,
								mInformationShop.address);
						ConsumerShopDetailActivity.this.startActivity(intent);
					}
				});

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

				// imageView_photo;
				if (mInformationShop.photoBitmap != null) {
					imageView_photo
							.setImageBitmap(mInformationShop.photoBitmap);
				} else {
					PictureManager pm = new PictureManager();
					pm.setOnPictureGetListener(new OnPictureGetListener() {
						@Override
						public void onPictureGet(String pid, Bitmap pic) {
							if (pid == mInformationShop.photo && pic != null) {
								mInformationShop.photoBitmap = pic;
								imageView_photo
										.setImageBitmap(mInformationShop.photoBitmap);
							}
						}
					});
					pm.getPicture(mInformationShop.photo);
				}
				textView_name.setText(mInformationShop.name);
				textView_mark.setText(String.format(
						ConsumerShopDetailActivity.this
								.getString(R.string.text_format_shop_mark),
						mInformationShop.mark));
				// toggleBotton_bookmark;
				// ratingBar_mark;
				textView_introduction.setText(mInformationShop.introduction);
				textView_address.setText(mInformationShop.address);
				textView_phone.setText(mInformationShop.phone_num);

				mLastBookmarked = mInformationShop.bookmark;

				toggleBotton_bookmark.setChecked(mInformationShop.bookmark);
				toggleBotton_bookmark
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

				ratingBar_mark.setRating(mInformationShop.user_mark <= 0 ? 0
						: mInformationShop.user_mark / 2f);
				ratingBar_mark
						.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
							@Override
							public void onRatingChanged(RatingBar ratingBar,
									float rating, boolean fromUser) {
								if (!fromUser) {
									return;
								}
								Log.d("rate", "" + rating);
								int ratingI = (int) (rating * 2);
								new UsermarkProcessTask().execute(ratingI);
							}
						});

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

	private class UsermarkProcessTask extends AsyncTask<Integer, Void, Void> {

		@Override
		protected Void doInBackground(Integer... params) {
			SessionManager.getSessionManager().shop_mark(mInformationShop.sid,
					params[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			textView_mark.setText(String.format(ConsumerShopDetailActivity.this
					.getString(R.string.text_format_shop_mark),
					mInformationShop.mark));
		}

	}

	private class BookmarkProcessTask extends AsyncTask<Boolean, Void, Boolean> {
		boolean add = false;

		@Override
		protected Boolean doInBackground(Boolean... params) {
			add = params[0];
			int result = 0;
			if (add) {
				result = SessionManager.getSessionManager().bookmark_add(
						mInformationShop.sid, false);
			} else {
				InformationBookmarkShop bmsf = null;
				if (SessionManager.getCurrentSession().bookmarkShop.isEmpty()) {
					if (SessionManager.getSessionManager().bookmark_list_shop() != SessionManager.ERROR_OK) {
						return false;
					} else {
						if (SessionManager.getCurrentSession().bookmarkShop
								.isEmpty()) {
							return true;
						}
					}
				}
				for (InformationBookmarkShop bms : SessionManager
						.getCurrentSession().bookmarkShop) {
					if (bms.shop.sid == mInformationShop.sid) {
						bmsf = bms;
						break;
					}
				}
				if (bmsf == null) {
					return true;
				}
				SessionManager.getCurrentSession().bookmarkShop.remove(bmsf);
				result = SessionManager.getSessionManager().bookmark_delete(
						bmsf.bsid, false);
			}
			return result == SessionManager.ERROR_OK;
		}

		@Override
		protected void onCancelled() {
			toggleBotton_bookmark.setEnabled(true);
			mBookmarkProcessTack = null;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				Toast.makeText(
						getApplication(),
						add ? R.string.information_bookmark_shop_add_success
								: R.string.information_bookmark_shop_delete_success,
						Toast.LENGTH_SHORT).show();

				mLastBookmarked = add;
			} else {
				Toast.makeText(
						getApplication(),
						add ? R.string.information_bookmark_shop_add_failure
								: R.string.information_bookmark_shop_delete_failure,
						Toast.LENGTH_SHORT).show();

				toggleBotton_bookmark.setChecked(mLastBookmarked);
			}
			toggleBotton_bookmark.setEnabled(true);
			mBookmarkProcessTack = null;
		}

	}
}
