package org.foxteam.noisyfox.patronsline;

import java.util.ArrayList;
import java.util.List;

import org.foxteam.noisyfox.patronsline.SessionManager.ShopListOrder;

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
import com.actionbarsherlock.view.SubMenu;

public class ConsumerListShopFragment extends SherlockListFragment {

	UserShopAdapter mShopAdapter = null;
	private GetShopListTask mGetShopListTask = null;

	InformationSession mInformationSession = null;
	private List<InformationShop> mShops = new ArrayList<InformationShop>();
	private boolean mDataRefreshed = false;

	private ShopListOrder mOrder = ShopListOrder.rank;

	private static final int ITEM_ID_NEWEST = 1;
	private static final int ITEM_ID_ACTIVITY = 2;
	private static final int ITEM_ID_RANK = 3;

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
		SubMenu sub = menu.addSubMenu(R.string.menu_order_shop);
		sub.add(0, ITEM_ID_NEWEST, 0, R.string.menu_sub_shop_newest);
		sub.add(0, ITEM_ID_ACTIVITY, 0, R.string.menu_sub_shop_activity);
		sub.add(0, ITEM_ID_RANK, 0, R.string.menu_sub_shop_rank);
		sub.getItem().setShowAsAction(
				MenuItem.SHOW_AS_ACTION_ALWAYS
						| MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		mItem_refresh = menu.add(R.string.menu_refresh);
		mItem_refresh.setIcon(R.drawable.ic_menu_refresh);
		mItem_refresh.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item == mItem_refresh) {
			refreshData();
			return true;
		} else {
			if (item.getItemId() == android.R.id.home || item.getItemId() == 0) {
				return false;
			}
			switch (item.getItemId()) {
			case ITEM_ID_NEWEST:
				mOrder = ShopListOrder.newest;
				break;
			case ITEM_ID_ACTIVITY:
				mOrder = ShopListOrder.activity;
				break;
			case ITEM_ID_RANK:
				mOrder = ShopListOrder.rank;
				break;
			}
			refreshData();
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

	class GetShopListTask extends AsyncTask<Void, Void, Integer> {

		@Override
		protected Integer doInBackground(Void... params) {
			return SessionManager.getSessionManager().shop_list(mOrder);
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (result == SessionManager.ERROR_OK) {
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
