package org.foxteam.noisyfox.patronsline;

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

public class FavouriteFoodFragment extends SherlockListFragment {

	UserFoodAdapter mFoodAdapter = null;
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

		mFoodAdapter = new UserFoodAdapter(getActivity(), getListView());
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
}