package org.foxteam.noisyfox.patronsline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class OwnerFoodOrderActivity extends SherlockListActivity {
	private static final int ITEM_ID_QUANT = 1;
	private static final int ITEM_ID_RATIO = 2;
	private View mFoodDetailLoadView;
	private View mFoodDetailView;

	private MenuItem mMenuItemRefresh;

	private RefreshShopInformationTask mRefreshShopInformationTask = null;
	private int order = ITEM_ID_QUANT;
	private OrderFoodAdapter mOrderFoodAdapter = new OrderFoodAdapter();
	private List<InformationFood> mFoods = new ArrayList<InformationFood>();
	private InformationShop mInformationShop;
	private static Comparator<InformationFood> mFoodQuantComparator = new FoodQuantComparator();
	private static Comparator<InformationFood> mFoodRatioComparator = new FoodRatioComparator();
	private LayoutInflater mInflater;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_owner_food_order);

		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mFoodDetailLoadView = findViewById(R.id.food_detail_load_view);
		mFoodDetailView = findViewById(R.id.food_detail_view);

		mInformationShop = InformationManager.obtainShopInformation(getIntent()
				.getStringExtra("sid"));

		loadData();
		setListAdapter(mOrderFoodAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		SubMenu sub = menu.addSubMenu(R.string.menu_order);
		sub.add(0, ITEM_ID_QUANT, 0, R.string.menu_sub_quantitative);
		sub.add(0, ITEM_ID_RATIO, 0, R.string.menu_sub_ratio);
		sub.getItem().setShowAsAction(
				MenuItem.SHOW_AS_ACTION_ALWAYS
						| MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		mMenuItemRefresh = menu.add(R.string.menu_refresh);
		mMenuItemRefresh.setIcon(R.drawable.ic_menu_refresh).setShowAsAction(
				MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item == mMenuItemRefresh) {
			if (mRefreshShopInformationTask != null) {
				return true;
			}
			showProgress(true);
			mRefreshShopInformationTask = new RefreshShopInformationTask();
			mRefreshShopInformationTask.execute();
		} else {
			if (item.getItemId() == android.R.id.home || item.getItemId() == 0) {
				return false;
			}
			order = item.getItemId();
			if (mRefreshShopInformationTask == null) {
				loadData();
			}
		}
		return true;
	}

	private void loadData() {
		mFoods.clear();

		for (InformationFood f : mInformationShop.foods) {
			mFoods.add(f);
		}
		switch (order) {
		case ITEM_ID_QUANT:
			Collections.sort(mFoods, mFoodQuantComparator);
			break;
		case ITEM_ID_RATIO:
			Collections.sort(mFoods, mFoodRatioComparator);
			break;
		}
		mOrderFoodAdapter.notifyDataSetChanged();
	}

	class OrderFoodAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mFoods.size();
		}

		@Override
		public Object getItem(int position) {
			return mFoods.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final InformationFood food = mFoods.get(position);

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_owner_food_order,
						parent, false);
			}
			convertView.setTag(food);

			ImageView imageView_photo = (ImageView) convertView
					.findViewById(R.id.imageView_food_photo);
			TextView textView_name = (TextView) convertView
					.findViewById(R.id.textView_food_name);
			TextView textView_likes = (TextView) convertView
					.findViewById(R.id.textView_food_likes);
			TextView textView_dislikes = (TextView) convertView
					.findViewById(R.id.textView_food_dislikes);

			textView_name.setText(food.name);
			textView_likes.setText(food.likes + "");
			textView_dislikes.setText(food.dislikes + "");
			PictureManager.loadPicture(food, imageView_photo);

			return convertView;
		}

	}

	//食物列表排序方式--按数量
	static class FoodQuantComparator implements Comparator<InformationFood> {

		@Override
		public int compare(InformationFood lhs, InformationFood rhs) {
			if (lhs.likes == rhs.likes)
				return 0;
			return lhs.likes < rhs.likes ? 1 : -1;
		}

	}

	//食物列表排序方式--按比例
	static class FoodRatioComparator implements Comparator<InformationFood> {

		@Override
		public int compare(InformationFood lhs, InformationFood rhs) {
			float lr = lhs.likes == 0 ? 0f
					: ((float) lhs.likes / (lhs.dislikes + lhs.likes));
			float rr = rhs.likes == 0 ? 0f
					: ((float) rhs.likes / (rhs.dislikes + rhs.likes));
			if (lr == rr)
				return 0;
			return lr < rr ? 1 : -1;
		}

	}

	private class RefreshShopInformationTask extends
			AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			return SessionManager.getSessionManager().shop_detail(
					mInformationShop.sid) == SessionManager.ERROR_OK;
		}

		@Override
		protected void onCancelled() {
			showProgress(false);
			mRefreshShopInformationTask = null;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			showProgress(false);
			mRefreshShopInformationTask = null;
			loadData();
		}

	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mFoodDetailLoadView.setVisibility(View.VISIBLE);
			mFoodDetailLoadView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mFoodDetailLoadView
									.setVisibility(show ? View.VISIBLE
											: View.GONE);
						}
					});

			mFoodDetailView.setVisibility(View.VISIBLE);
			mFoodDetailView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mFoodDetailView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mFoodDetailLoadView.setVisibility(show ? View.VISIBLE : View.GONE);
			mFoodDetailView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
}
