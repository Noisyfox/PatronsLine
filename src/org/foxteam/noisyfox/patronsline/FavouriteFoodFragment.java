package org.foxteam.noisyfox.patronsline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class FavouriteFoodFragment extends SherlockListFragment {

	List<InformationFood> mFoods = new ArrayList<InformationFood>();
	FoodAdapter mFoodAdapter = null;
	GetBookmarkTask mGetBookmarkTask = null;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d("msg", "onActivityCreated");

		setEmptyText(getText(R.string.empty_text_no_favourite_food));
		setHasOptionsMenu(true);

		mFoodAdapter = new FoodAdapter(getActivity());

		setListAdapter(mFoodAdapter);

		InformationFood food = new InformationFood();
		food.name = "鸭血粉丝汤";
		food.fid = "AAA";
		food.bookmark = true;
		food.price = 5.0f;
		food.special = true;
		food.photo = "AAAA";
		food.photoBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.pic_food_example);
		food.likes = 100;
		food.dislikes = 0;
		food.comments = 1;

		mFoods.add(food);
		mFoods.add(food);
		mFoods.add(food);
		mFoods.add(food);
		mFoods.add(food);
		mFoods.add(food);
		mFoods.add(food);
		mFoods.add(food);
		mFoods.add(food);
		mFoods.add(food);
		mFoods.add(food);
		mFoods.add(food);

		refreshData();
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
		super.onListItemClick(l, v, position, id);
	}

	private void refreshData() {
		setListShown(false);

		Log.d("msg", "update");

		new GetBookmarkTask().execute("", "");
	}

	class GetBookmarkTask extends
			AsyncTask<String, Void, List<InformationFood>> {

		@Override
		protected List<InformationFood> doInBackground(String... params) {
			Map<Object, Object> arguments = new HashMap<Object, Object>();
			arguments.put("type", "food");
			arguments.put("uid", params[0]);
			arguments.put("session", params[1]);
			String jsonString = NetworkHelper.doHttpRequest(
					NetworkHelper.STR_SERVER_URL, arguments.entrySet());
			if (jsonString != null) {

			}

			return null;
		}

		@Override
		protected void onPostExecute(List<InformationFood> result) {

			result = mFoods;

			mFoodAdapter.setData(result);

			// The list should now be shown.
			if (isResumed()) {
				setListShown(true);
			} else {
				setListShownNoAnimation(true);
			}
		}

	}

	class FoodAdapter extends BaseAdapter {

		private final LayoutInflater mInflater;
		private List<InformationFood> mFoods = new ArrayList<InformationFood>();
		private PictureManager mPictureManager = new PictureManager();
		private OnPictureGetListener mOnPictureGetListener = new OnPictureGetListener() {
			@Override
			public void onPictureGet(String pid, Bitmap pic) {
				if (pic == null) {
					return;
				}

				boolean shouldRefresh = false;
				synchronized (mFoods) {
					for (InformationFood food : mFoods) {
						if (food.photo.equals(pid)) {
							food.photoBitmap = pic;
							shouldRefresh = true;
							break;
						}
					}
				}
				if (shouldRefresh) {
					FoodAdapter.this.notifyDataSetChanged();
				}
			}
		};

		FoodAdapter(Context context) {
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mPictureManager.setOnPictureGetListener(mOnPictureGetListener);
		}

		void setData(List<InformationFood> foods) {
			mFoods.clear();
			if (foods != null) {
				for (InformationFood f : foods) {
					mFoods.add(f);
				}
			}
			this.notifyDataSetChanged();
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
			InformationFood food = mFoods.get(position);

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_consumer_food,
						parent, false);
			}

			convertView.setTag(food);

			ImageView imageView_food = (ImageView) convertView
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
			toggleButton_star_is_bookmarked.setChecked(food.bookmark);
			if (food.photoBitmap == null) {
				mPictureManager.getPicture(food.photo);
				// 设置为空
				imageView_food.setImageResource(R.drawable.pic_on_loading);
			} else {
				imageView_food.setImageBitmap(food.photoBitmap);
			}

			return convertView;
		}

	}

}