package org.foxteam.noisyfox.patronsline;

import java.util.List;

import org.foxteam.noisyfox.patronsline.PictureManager.OnPictureGetListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
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
	private final Context mContext;

	private List<InformationFood> mData;

	UserFoodAdapter(Context context, ListView listView) {
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mListView = listView;
	}

	public void setData(List<InformationFood> data) {
		mData = data;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (mData == null) {
			return 0;
		} else {
			return mData.size();
		}
	}

	@Override
	public Object getItem(int position) {
		if (mData == null) {
			return null;
		} else {
			return mData.get(position);
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private class MyOnCheckedChangeListener implements OnCheckedChangeListener {

		private boolean mLstChecked;

		MyOnCheckedChangeListener(boolean isChecked) {
			mLstChecked = isChecked;
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (mLstChecked == isChecked)
				return;
			buttonView.setEnabled(false);
			new BookmarkChangeTask(buttonView, isChecked).execute();
		}

		class BookmarkChangeTask extends AsyncTask<Void, Void, Boolean> {
			CompoundButton mButtonView;
			boolean mIsChecked;

			BookmarkChangeTask(CompoundButton buttonView, boolean isChecked) {
				mButtonView = buttonView;
				mIsChecked = isChecked;
			}

			@Override
			protected Boolean doInBackground(Void... params) {
				return onBookmarkFoodChange(
						(InformationFood) mButtonView.getTag(), mIsChecked);
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (result) {
					((InformationFood) mButtonView.getTag()).bookmark = mIsChecked;
					mLstChecked = mIsChecked;
					Toast.makeText(
							mContext,
							mIsChecked ? R.string.information_bookmark_food_add_success
									: R.string.information_bookmark_food_delete_success,
							Toast.LENGTH_SHORT).show();
				} else {
					mButtonView.setChecked(mLstChecked);
					Toast.makeText(
							mContext,
							mIsChecked ? R.string.information_bookmark_food_add_failure
									: R.string.information_bookmark_food_delete_failure,
							Toast.LENGTH_SHORT).show();
				}
				mButtonView.setEnabled(true);
				notifyDataSetChanged();
			}
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		// InformationBookmarkFood bookmarkFood =
		// mInformationSession.bookmarkFood
		// .get(position);
		final InformationFood food = mData.get(position);

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_consumer_food,
					parent, false);
		}

		convertView.setTag(food);

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
		toggleButton_star_is_bookmarked
				.setOnCheckedChangeListener(new MyOnCheckedChangeListener(
						food.bookmark));
		toggleButton_star_is_bookmarked.setChecked(food.bookmark);
		toggleButton_star_is_bookmarked.setEnabled(true);
		toggleButton_star_is_bookmarked.setTag(food);

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

	public boolean onBookmarkFoodChange(InformationFood food,
			boolean addBookmark) {
		int result = 0;
		if (addBookmark) {
			result = SessionManager.getSessionManager().bookmark_add(food.fid,
					true);
		} else {
			InformationBookmarkFood bmff = null;
			InformationSession informationSession = SessionManager
					.getCurrentSession();
			if (informationSession.bookmarkFood.isEmpty()) {
				if (SessionManager.getSessionManager().bookmark_list_food() != SessionManager.ERROR_OK) {
					return false;
				} else {
					if (informationSession.bookmarkFood.isEmpty()) {
						return true;
					}
				}
			}
			for (InformationBookmarkFood bmf : informationSession.bookmarkFood) {
				if (bmf.food.fid == food.fid) {
					bmff = bmf;
					break;
				}
			}
			if (bmff == null) {
				return true;
			}
			informationSession.bookmarkFood.remove(bmff);
			Log.d("", "delete:" + bmff.bfid);
			result = SessionManager.getSessionManager().bookmark_delete(
					bmff.bfid, true);
		}
		return result == SessionManager.ERROR_OK;
	}
}
