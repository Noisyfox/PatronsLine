package org.foxteam.noisyfox.patronsline;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class FavouriteShopFragment extends SherlockListFragment {

	UserShopAdapter mShopAdapter = null;
	private GetBookmarkTask mGetBookmarkTask = null;

	InformationSession mInformationSession = null;
	private List<InformationShop> mShops = new ArrayList<InformationShop>();
	private boolean mDataRefreshed = false;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d("msg", "onActivityCreated");

		mInformationSession = SessionManager.getCurrentSession();

		setEmptyText(getText(R.string.empty_text_no_favourite_shop));
		setHasOptionsMenu(true);

		mShopAdapter = new UserShopAdapter(getActivity(), getListView()){

			@Override
			public boolean onBookmarkShopChange(InformationShop shop,
					boolean addBookmark) {
				int result = 0;
				if (addBookmark) {
					result = SessionManager.getSessionManager().bookmark_add(
							shop.sid, false);
				} else {
					InformationBookmarkShop bmsf = null;
					if (mInformationSession.bookmarkFood.isEmpty()) {
						if (SessionManager.getSessionManager()
								.bookmark_list_shop() != SessionManager.ERROR_OK) {
							return false;
						} else {
							if (mInformationSession.bookmarkShop.isEmpty()) {
								return true;
							}
						}
					}
					for (InformationBookmarkShop bms : mInformationSession.bookmarkShop) {
						if (bms.shop.sid == shop.sid) {
							bmsf = bms;
							break;
						}
					}
					if (bmsf == null) {
						return true;
					}
					mInformationSession.bookmarkShop.remove(bmsf);
					Log.d("","delete:" + bmsf.bsid);
					result = SessionManager.getSessionManager()
							.bookmark_delete(bmsf.bsid, false);
				}
				return result == SessionManager.ERROR_OK;
			}
			
		};
		loadData();
		setListAdapter(mShopAdapter);

		if (!mDataRefreshed) {
			mDataRefreshed = true;
			refreshData();
		}

	}

	private void loadData() {
		mShops.clear();
		for (InformationBookmarkShop ibs : mInformationSession.bookmarkShop) {
			mShops.add(ibs.shop);
		}
		mShopAdapter.setData(mShops);
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
				loadData();

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
}
