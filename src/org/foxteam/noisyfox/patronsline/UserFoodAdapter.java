package org.foxteam.noisyfox.patronsline;

import org.foxteam.noisyfox.patronsline.PictureManager.OnPictureGetListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

class UserFoodAdapter extends BaseAdapter {

	private final ListView mListView;
	private final LayoutInflater mInflater;
	private final InformationSession mInformationSession;
	private final Context mContext;

	UserFoodAdapter(Context context, ListView listView) {
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mListView = listView;
		mInformationSession = SessionManager.getCurrentSession();
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
								Toast.makeText(mContext,
										R.string.msg_bookmark_delete_success,
										Toast.LENGTH_SHORT).show();
								mButtonView.setChecked(false);
								mInformationSession.bookmarkFood
										.remove((InformationBookmarkFood) mButtonView
												.getTag());
								notifyDataSetChanged();
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
			mPictureManager.setOnPictureGetListener(new OnPictureGetListener() {
				@Override
				public void onPictureGet(String pid, final Bitmap pic) {
					mListView.post(new Runnable() {
						@Override
						public void run() {
							InformationManager.obtainFoodInformation(fid).photoBitmap = pic;
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
