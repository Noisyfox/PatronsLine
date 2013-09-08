package org.foxteam.noisyfox.patronsline;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
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

public class ListShopFragment extends SherlockListFragment {

	UserShopAdapter mShopAdapter = null;
	private GetShopListTask mGetShopListTask = null;

	InformationSession mInformationSession = null;
	private List<InformationShop> mShops = new ArrayList<InformationShop>();
	private boolean mDataRefreshed = false;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d("msg", "onActivityCreated");

		mInformationSession = SessionManager.getCurrentSession();

		setEmptyText(getText(R.string.empty_text_no_list_shop));
		setHasOptionsMenu(true);

		mShopAdapter = new UserShopAdapter(getActivity(), getListView());
		loadData();
		setListAdapter(mShopAdapter);

		if (!mDataRefreshed) {
			mDataRefreshed = true;
			refreshData();
		}

	}

	private void loadData() {
		mShops.clear();
		for (InformationShop is : mInformationSession.listShop) {
			mShops.add(is);
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
		InformationShop informationShop = (InformationShop) v.getTag();
		Intent intent = new Intent();
		intent.putExtra("sid", informationShop.sid);
		intent.setClass(getActivity(), ConsumerShopDetailActivity.class);
		startActivity(intent);
	}

	private void refreshData() {
		if (mGetShopListTask != null) {
			return;
		}

		setListShown(false);

		Log.d("msg", "update");

		mInformationSession.listShop.clear();
		mGetShopListTask = new GetShopListTask();
		mGetShopListTask.execute();
	}

	class GetShopListTask extends AsyncTask<Void, Void, Void> {
		int errCode = -1;

		@Override
		protected Void doInBackground(Void... params) {
			errCode = SessionManager.getSessionManager().shop_list();

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
				Toast.makeText(getActivity(),
						R.string.error_refresh_shoplist_failure,
						Toast.LENGTH_SHORT).show();
			}
			mGetShopListTask = null;
		}

		@Override
		protected void onCancelled() {
			mGetShopListTask = null;
		}

	}
}
