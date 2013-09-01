package org.foxteam.noisyfox.patronsline;

import java.util.List;

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

class UserShopAdapter extends BaseAdapter {

	private final ListView mListView;
	private final LayoutInflater mInflater;
	private final Context mContext;

	private List<InformationShop> mData;

	UserShopAdapter(Context context, ListView listView) {
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mListView = listView;
	}

	public void setData(List<InformationShop> data) {
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
				return onBookmarkShopChange(
						(InformationShop) mButtonView.getTag(), mIsChecked);
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (result) {
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
			}
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final InformationShop shop = mData.get(position);

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_consumer_shop,
					parent, false);
		}

		convertView.setTag(shop);

		final ImageView imageView_shop = (ImageView) convertView
				.findViewById(R.id.imageView_shop);
		TextView textView_shop_name = (TextView) convertView
				.findViewById(R.id.textView_shop_name);
		ToggleButton toggleButton_star_is_bookmarked = (ToggleButton) convertView
				.findViewById(R.id.toggleButton_star_is_bookmarked);

		textView_shop_name.setText(shop.name);

		toggleButton_star_is_bookmarked
				.setOnCheckedChangeListener(new MyOnCheckedChangeListener(shop.bookmark) /*{
					
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
											((InformationBookmarkShop) mButtonView
													.getTag()).bsid, false) == SessionManager.ERROR_OK;
						}

						@Override
						protected void onPostExecute(Boolean result) {
							if (result) {
								Toast.makeText(mContext,
										R.string.msg_bookmark_delete_success,
										Toast.LENGTH_SHORT).show();
								mButtonView.setChecked(false);
								mInformationSession.bookmarkShop
										.remove((InformationBookmarkShop) mButtonView
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
					
				}*/);

		toggleButton_star_is_bookmarked.setChecked(shop.bookmark);
		toggleButton_star_is_bookmarked.setTag(shop);

		if (shop.photoBitmap == null) {
			final String sid = shop.sid;
			imageView_shop.setTag(sid);
			PictureManager mPictureManager = new PictureManager();
			mPictureManager.setOnPictureGetListener(new OnPictureGetListener() {
				@Override
				public void onPictureGet(String pid, final Bitmap pic) {
					mListView.post(new Runnable() {
						@Override
						public void run() {
							InformationManager.obtainShopInformation(sid).photoBitmap = pic;
							ImageView imageViewByTag = (ImageView) mListView
									.findViewWithTag(sid);
							if (imageViewByTag != null) {
								imageViewByTag.setImageBitmap(pic);
							}
						}
					});
				}
			});
			mPictureManager.getPicture(shop.photo);
			// 设置为空
			imageView_shop.setImageResource(R.drawable.pic_on_loading);
		} else {
			imageView_shop.setImageBitmap(shop.photoBitmap);
		}

		return convertView;
	}

	public boolean onBookmarkShopChange(InformationShop shop,
			boolean addBookmark) {
		return true;
	}
}
