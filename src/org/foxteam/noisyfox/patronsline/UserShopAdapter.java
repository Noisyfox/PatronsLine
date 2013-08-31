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

class UserShopAdapter extends BaseAdapter {

	private final ListView mListView;
	private final LayoutInflater mInflater;
	private final InformationSession mInformationSession;
	private final Context mContext;

	UserShopAdapter(Context context, ListView listView) {
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mListView = listView;
		mInformationSession = SessionManager.getCurrentSession();
	}

	@Override
	public int getCount() {
		return mInformationSession.bookmarkShop.size();
	}

	@Override
	public Object getItem(int position) {
		return mInformationSession.bookmarkShop.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		InformationBookmarkShop bookmarkShop = mInformationSession.bookmarkShop
				.get(position);
		final InformationShop shop = bookmarkShop.shop;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_consumer_shop,
					parent, false);
		}

		convertView.setTag(bookmarkShop);

		final ImageView imageView_shop = (ImageView) convertView
				.findViewById(R.id.imageView_shop);
		TextView textView_shop_name = (TextView) convertView
				.findViewById(R.id.textView_shop_name);
		ToggleButton toggleButton_star_is_bookmarked = (ToggleButton) convertView
				.findViewById(R.id.toggleButton_star_is_bookmarked);

		textView_shop_name.setText(shop.name);
		toggleButton_star_is_bookmarked.setChecked(shop.bookmark);
		toggleButton_star_is_bookmarked.setTag(bookmarkShop);

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
				});

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
}
