package org.foxteam.noisyfox.patronsline;

import org.foxteam.noisyfox.patronsline.PictureManager.OnPictureGetListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class FavouriteShopFragment extends SherlockListFragment {

	ShopAdapter mShopAdapter = null;
	private GetBookmarkTask mGetBookmarkTask = null;

	InformationSession mInformationSession = null;
	private boolean mDataRefreshed = false;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d("msg", "onActivityCreated");

		mInformationSession = SessionManager.getCurrentSession();

		setEmptyText(getText(R.string.empty_text_no_favourite_shop));
		setHasOptionsMenu(true);

		mShopAdapter = new ShopAdapter(getActivity(), getListView());
		setListAdapter(mShopAdapter);

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
			errCode = SessionManager.getSessionManager().bookmark_list_shop();

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (errCode == SessionManager.ERROR_OK) {
				mShopAdapter.notifyDataSetChanged();

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

	class ShopAdapter extends BaseAdapter {

		private final ListView mListView;
		private final LayoutInflater mInflater;

		ShopAdapter(Context context, ListView listView) {
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mListView = listView;
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

			if (shop.photoBitmap == null) {
				final String sid = shop.sid;
				imageView_shop.setTag(sid);
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
												.obtainShopInformation(sid).photoBitmap = pic;
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
}
