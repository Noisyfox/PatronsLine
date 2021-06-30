package org.foxteam.noisyfox.patronsline;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class OwnerFoodManageActivity extends SherlockListActivity {

	public static final int REQUESTCODE_NEW_FOOD = 1;
	public static final int REQUESTCODE_EDIT_FOOD = 2;

	private View mFoodDetailLoadView;
	private View mFoodDetailView;

	ActionMode mMode = null;

	private MenuItem mMenuItem_delete;
	private MenuItem mMenuItem_select_all;
	private MenuItem mMenuItem_deselect_all;
	private MenuItem mMenuItem_refresh;
	private MenuItem mMenuItem_add;
	private RefreshShopInformationTask mRefreshShopInformationTask = null;
	private FoodDeleteTask mFoodDeleteTask = null;
	private InformationShop mInformationShop;
	private LayoutInflater mInflater;
	private ManageFoodAdapter mOrderFoodAdapter = new ManageFoodAdapter();
	private AlertDialog mAlertDialog;
	ActionMode.Callback mCallback = new ActionMode.Callback() {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			mMenuItem_select_all = menu.add(R.string.menu_select_all);
			mMenuItem_select_all
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

			mMenuItem_deselect_all = menu.add(R.string.menu_deselect_all);
			mMenuItem_deselect_all
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

			mMenuItem_delete = menu.add(R.string.menu_delete_food);
			mMenuItem_delete.setIcon(android.R.drawable.ic_menu_delete)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			if (item == mMenuItem_select_all) {
				mOrderFoodAdapter.selectAll();
				mOnSelectedChangeListener.onSelectedChange(mOrderFoodAdapter
						.getCount());
			} else if (item == mMenuItem_deselect_all) {
				mOrderFoodAdapter.clearSelect();
				mOnSelectedChangeListener.onSelectedChange(0);
			} else if (item == mMenuItem_delete) {
				mAlertDialog.show();
			}
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mOrderFoodAdapter.clearSelect();
			mMode = null;
		}

	};
	private OnSelectedChangeListener mOnSelectedChangeListener = new OnSelectedChangeListener() {

		@Override
		public void onSelectedChange(int totalSelected) {
			if (totalSelected <= 0) {
				if (mMode != null) {
					mMode.finish();
					mMode = null;
				}
			} else {
				if (mMode == null) {
					mMode = startActionMode(mCallback);
				}
				mMenuItem_select_all
						.setVisible(totalSelected < mOrderFoodAdapter
								.getCount());
			}
		}

	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mMenuItem_add = menu.add(R.string.menu_add_food);
		mMenuItem_add.setIcon(android.R.drawable.ic_menu_add).setShowAsAction(
				MenuItem.SHOW_AS_ACTION_ALWAYS);

		mMenuItem_refresh = menu.add(R.string.menu_refresh);
		mMenuItem_refresh.setIcon(R.drawable.ic_menu_refresh).setShowAsAction(
				MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item == mMenuItem_add) {
			Intent i = new Intent();
			i.setClass(OwnerFoodManageActivity.this,
					OwnerFoodEditActivity.class);
			i.putExtra("sid", mInformationShop.sid);
			i.putExtra("requestCode", REQUESTCODE_NEW_FOOD);
			startActivityForResult(i, REQUESTCODE_NEW_FOOD);

		} else if (item == mMenuItem_refresh) {
			refresh();
		}
		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		InformationFood food = (InformationFood) v.getTag();
		Intent i = new Intent();
		i.setClass(OwnerFoodManageActivity.this, OwnerFoodEditActivity.class);
		i.putExtra("fid", food.fid);
		i.putExtra("requestCode", REQUESTCODE_EDIT_FOOD);
		startActivityForResult(i, REQUESTCODE_EDIT_FOOD);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			refresh();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_owner_food_manage);

		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		AlertDialog.Builder ab = new AlertDialog.Builder(
				OwnerFoodManageActivity.this);
		ab.setMessage(R.string.message_delete_comfirm);
		ab.setPositiveButton(R.string.button_ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (mFoodDeleteTask == null) {
					showProgress(true);
					mFoodDeleteTask = new FoodDeleteTask();
					List<InformationFood> foods = mOrderFoodAdapter
							.getSelected();
					String[] fids = new String[foods.size()];
					int c = 0;
					for (InformationFood f : foods) {
						fids[c] = f.fid;
						c++;
					}
					mFoodDeleteTask.execute(fids);
				}
			}
		});
		ab.setNegativeButton(R.string.button_cancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

			}
		});
		mAlertDialog = ab.create();
		mFoodDetailLoadView = findViewById(R.id.food_detail_load_view);
		mFoodDetailView = findViewById(R.id.food_detail_view);

		mInformationShop = InformationManager.obtainShopInformation(getIntent()
				.getStringExtra("sid"));

		mOrderFoodAdapter
				.setOnSelectedChangeListener(mOnSelectedChangeListener);
		mOrderFoodAdapter.setData(mInformationShop.foods);
		setListAdapter(mOrderFoodAdapter);
	}

	/**
	 * 一个支持多选的ListViewAdapter，实现了每一元素的复选框的响应
	 * @author Noisyfox
	 *
	 */
	class ManageFoodAdapter extends BaseAdapter {

		private List<InformationFood> mFoods = new ArrayList<InformationFood>();
		private boolean mFoods_selected[] = null;//复选框选中状态储存
		private int mSelectedCount = 0;
		private OnSelectedChangeListener mOnSelectedChangeListener = null;

		OnCheckedChangeListener listener = new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				//记录每个复选框的状态
				int position = (Integer) buttonView.getTag();
				if (mFoods_selected[position] != isChecked) {
					mFoods_selected[position] = isChecked;
					mSelectedCount += isChecked ? 1 : -1;
					if (mOnSelectedChangeListener != null) {
						mOnSelectedChangeListener
								.onSelectedChange(mSelectedCount);
					}
				}
			}

		};

		public int getSelectedCount() {
			return mSelectedCount;
		}

		//得到已选中的元素
		public List<InformationFood> getSelected() {
			List<InformationFood> selected = new ArrayList<InformationFood>();

			int loc = 0;
			for (InformationFood f : mFoods) {
				if (mFoods_selected[loc]) {
					selected.add(f);
				}
				loc++;
			}

			return selected;
		}

		public void selectAll() {
			if (mFoods_selected != null) {
				for (int i = 0; i < mFoods_selected.length; i++) {
					mFoods_selected[i] = true;
				}
			}
			mSelectedCount = mFoods_selected.length;
			this.notifyDataSetChanged();
		}

		public void clearSelect() {
			if (mFoods_selected != null) {
				for (int i = 0; i < mFoods_selected.length; i++) {
					mFoods_selected[i] = false;
				}
			}
			mSelectedCount = 0;
			this.notifyDataSetChanged();
		}

		public void setOnSelectedChangeListener(
				OnSelectedChangeListener listener) {
			mOnSelectedChangeListener = listener;
		}

		public void setData(List<InformationFood> foods) {
			mFoods.clear();
			mFoods_selected = null;

			for (InformationFood f : foods) {
				mFoods.add(f);
			}
			int size = mFoods.size();
			if (size != 0) {
				mFoods_selected = new boolean[size];
			}
			notifyDataSetChanged();
			if (mSelectedCount != 0) {
				mSelectedCount = 0;
				if (mOnSelectedChangeListener != null) {
					mOnSelectedChangeListener.onSelectedChange(0);
				}
			}
		}

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
				convertView = mInflater.inflate(
						R.layout.item_owner_food_manage, parent, false);
			}
			convertView.setTag(food);

			ImageView imageView_photo = (ImageView) convertView
					.findViewById(R.id.imageView_photo);
			TextView textView_name = (TextView) convertView
					.findViewById(R.id.textView_name);
			CheckBox checkBox_select = (CheckBox) convertView
					.findViewById(R.id.checkBox_select);
			checkBox_select.setTag(position);
			checkBox_select.setChecked(mFoods_selected[position]);

			checkBox_select.setOnCheckedChangeListener(listener);

			textView_name.setText(food.name);
			PictureManager.loadPicture(food, imageView_photo);

			return convertView;
		}

	}

	static interface OnSelectedChangeListener {
		void onSelectedChange(int totalSelected);
	}

	private void refresh() {
		if (mRefreshShopInformationTask == null) {
			showProgress(true);
			mRefreshShopInformationTask = new RefreshShopInformationTask();
			mRefreshShopInformationTask.execute();
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
			mOrderFoodAdapter.setData(mInformationShop.foods);
		}

	}

	private class FoodDeleteTask extends AsyncTask<String[], Void, Boolean> {

		@Override
		protected Boolean doInBackground(String[]... params) {
			return SessionManager.getSessionManager().food_delete(
					mInformationShop.sid, params[0]) == SessionManager.ERROR_OK;
		}

		@Override
		protected void onCancelled() {
			showProgress(false);
			mFoodDeleteTask = null;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			showProgress(false);
			mFoodDeleteTask = null;
			if (result) {
				mOrderFoodAdapter.setData(mInformationShop.foods);
			}
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
