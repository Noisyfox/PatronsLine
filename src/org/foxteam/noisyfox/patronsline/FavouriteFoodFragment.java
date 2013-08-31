package org.foxteam.noisyfox.patronsline;

import org.foxteam.noisyfox.patronsline.PictureManager.OnPictureGetListener;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class FavouriteFoodFragment extends SherlockListFragment {

	FoodAdapter mFoodAdapter = null;
	private GetBookmarkTask mGetBookmarkTask = null;

	InformationSession mInformationSession = null;
	private boolean mDataRefreshed = false;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d("msg", "onActivityCreated");

		mInformationSession = SessionManager.getCurrentSession();

		setEmptyText(getText(R.string.empty_text_no_favourite_food));
		setHasOptionsMenu(true);

		/*
		 * InformationFood food = new InformationFood(); food.name = "鸭血粉丝汤";
		 * food.fid = "AAA"; food.bookmark = true; food.price = 5.0f;
		 * food.special = true; food.photo = "AAAA"; food.photoBitmap =
		 * BitmapFactory.decodeResource(getResources(),
		 * R.drawable.pic_food_example); food.likes = 100; food.dislikes = 0;
		 * food.comments = 1;
		 * 
		 * mFoods.add(food); mFoods.add(food); mFoods.add(food);
		 * mFoods.add(food); mFoods.add(food); mFoods.add(food);
		 * mFoods.add(food); mFoods.add(food); mFoods.add(food);
		 * mFoods.add(food); mFoods.add(food); mFoods.add(food);
		 */

		mFoodAdapter = new FoodAdapter(getActivity(), getListView());
		setListAdapter(mFoodAdapter);

		if (!mDataRefreshed) {
			mDataRefreshed = true;
			refreshData();
		}

	}

	MenuItem mItem_refresh = null;

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		mItem_refresh = menu.add(R.string.menu_refresh);
		mItem_refresh.setIcon(R.drawable.ic_menu_refresh);
		mItem_refresh.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item == mItem_refresh) {
			refreshData();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d("msg", "onListItemClick");
		InformationBookmarkFood bookmarkFood = (InformationBookmarkFood) v
				.getTag();
		Intent intent = new Intent();
		intent.putExtra("fid", bookmarkFood.food.fid);
		intent.putExtra("sid", bookmarkFood.shop.sid);
		intent.setClass(getActivity(), ConsumerFoodDetailActivity.class);
		startActivity(intent);
	}

	private void refreshData() {
		if (mGetBookmarkTask != null) {
			return;
		}

		setListShown(false);

		Log.d("msg", "update");

		mInformationSession.bookmarkFood.clear();
		mGetBookmarkTask = new GetBookmarkTask();
		mGetBookmarkTask.execute();
	}

	class GetBookmarkTask extends AsyncTask<Void, Void, Void> {
		int errCode = -1;

		@Override
		protected Void doInBackground(Void... params) {
			errCode = SessionManager.getSessionManager().bookmark_list_food();

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (errCode == SessionManager.ERROR_OK) {
				mFoodAdapter.notifyDataSetChanged();

				// The list should now be shown.
				if (isResumed()) {
					setListShown(true);
				} else {
					setListShownNoAnimation(true);
				}
			} else {
				Toast.makeText(getActivity(), R.string.error_refresh_failure,
						Toast.LENGTH_SHORT).show();
			}
			mGetBookmarkTask = null;
		}

		@Override
		protected void onCancelled() {
			mGetBookmarkTask = null;
		}

	}

	class FoodAdapter extends BaseAdapter {

		private final ListView mListView;
		private final LayoutInflater mInflater;

		FoodAdapter(Context context, ListView listView) {
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mListView = listView;
		}

		@Override
		public int getCount() {
			return mInformationSession.bookmarkFood.size();
		}

		@Override
		public Object getItem(int position) {
			return mInformationSession.bookmarkFood.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			InformationBookmarkFood bookmarkFood = mInformationSession.bookmarkFood
					.get(position);
			final InformationFood food = bookmarkFood.food;

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_consumer_food,
						parent, false);
			}

			convertView.setTag(bookmarkFood);

			final ImageView imageView_food = (ImageView) convertView
					.findViewById(R.id.imageView_food);
			TextView textView_food_name = (TextView) convertView
					.findViewById(R.id.textView_food_name);
			TextView textView_food_price = (TextView) convertView
					.findViewById(R.id.textView_food_price);
			TextView textView_food_price_change = (TextView) convertView
					.findViewById(R.id.textView_food_price_change);
			ToggleButton toggleButton_star_is_bookmarked = (ToggleButton) convertView
					.findViewById(R.id.toggleButton_star_is_bookmarked);

			textView_food_name.setText(food.name);
			textView_food_price.setText(food.price + "元");
			textView_food_price_change.setText("");
			toggleButton_star_is_bookmarked.setChecked(food.bookmark);
			toggleButton_star_is_bookmarked.setTag(bookmarkFood);
			toggleButton_star_is_bookmarked
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						BookmarkDeleteTask mBookmarkDeleteTask = null;

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							if (!isChecked && mBookmarkDeleteTask == null) {
								mBookmarkDeleteTask = new BookmarkDeleteTask(
										buttonView);
								mBookmarkDeleteTask.execute();
							}
						}

						class BookmarkDeleteTask extends
								AsyncTask<Void, Void, Boolean> {
							private final CompoundButton mButtonView;

							BookmarkDeleteTask(CompoundButton buttonView) {
								mButtonView = buttonView;
							}

							@Override
							protected Boolean doInBackground(Void... params) {
								return SessionManager
										.getSessionManager()
										.bookmark_delete(
												((InformationBookmarkFood) mButtonView
														.getTag()).bfid, true) == SessionManager.ERROR_OK;
							}

							@Override
							protected void onPostExecute(Boolean result) {
								if (result) {
									Toast.makeText(
											getActivity(),
											R.string.msg_bookmark_delete_success,
											Toast.LENGTH_SHORT).show();
									mButtonView.setChecked(false);
									refreshData();
								} else {
									mButtonView.setChecked(true);
								}
								mBookmarkDeleteTask = null;
							}

							@Override
							protected void onCancelled() {
								mBookmarkDeleteTask = null;
								mButtonView.setChecked(true);
							}

						}
					});

			if (food.photoBitmap == null) {
				final String fid = food.fid;
				imageView_food.setTag(fid);
				PictureManager mPictureManager = new PictureManager();
				mPictureManager
						.setOnPictureGetListener(new OnPictureGetListener() {
							@Override
							public void onPictureGet(String pid,
									final Bitmap pic) {
								mListView.post(new Runnable() {
									@Override
									public void run() {
										InformationManager
												.obtainFoodInformation(fid).photoBitmap = pic;
										ImageView imageViewByTag = (ImageView) mListView
												.findViewWithTag(fid);
										if (imageViewByTag != null) {
											imageViewByTag.setImageBitmap(pic);
										}
									}
								});
							}
						});
				mPictureManager.getPicture(food.photo);
				// 设置为空
				imageView_food.setImageResource(R.drawable.pic_on_loading);
			} else {
				imageView_food.setImageBitmap(food.photoBitmap);
			}

			return convertView;
		}

	}

}