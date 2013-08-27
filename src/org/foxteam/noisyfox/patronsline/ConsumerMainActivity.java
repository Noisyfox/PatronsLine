package org.foxteam.noisyfox.patronsline;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class ConsumerMainActivity extends SherlockFragmentActivity {
	TabHost mTabHost;
	ViewPager mViewPager;
	TabsAdapter mTabsAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Sherlock_Light);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_consumer_main);
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();

		mViewPager = (ViewPager) findViewById(R.id.pager);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle("Noisyfox");
		actionBar.setIcon(R.drawable.head);
		actionBar.setSubtitle("欢迎回来");

		mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);
		mTabsAdapter.addTab(
				mTabHost.newTabSpec("favouriteFood").setIndicator(
						getText(R.string.tab_label_favourite_food)),
				FavouriteFoodFragment.class, null);

		if (savedInstanceState != null) {
			mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.consumer_main, menu);
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("tab", mTabHost.getCurrentTabTag());
	}

	// -----------------------------------------------------------------------------------
	public static class FavouriteFoodFragment extends SherlockListFragment {

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			setHasOptionsMenu(true);

			setEmptyText(getText(R.string.empty_text_no_favourite_food));

			setListShown(false);

			List<InformationFood> mFoods = new ArrayList<InformationFood>();
			FoodAdapter mFoodAdapter = new FoodAdapter(mFoods);

			this.setListAdapter(mFoodAdapter);
		}

		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			MenuItem item = menu.add(R.string.menu_refresh);
			item.setIcon(R.drawable.ic_menu_refresh);
			item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}

		class FoodAdapter extends BaseAdapter {

			private List<InformationFood> mFoods = null;
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

			FoodAdapter(List<InformationFood> foods) {
				mFoods = foods;
				mPictureManager.setOnPictureGetListener(mOnPictureGetListener);
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
					convertView = LayoutInflater.from(getActivity()).inflate(
							R.layout.item_consumer_food, null);
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
}
