package org.foxteam.noisyfox.patronsline;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.widget.TabHost;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;

public class ConsumerMainActivity extends SherlockFragmentActivity {
	TabHost mTabHost;
	ViewPager mViewPager;
	TabsAdapter mTabsAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Sherlock);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_consumer_main);
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();

		mViewPager = (ViewPager) findViewById(R.id.pager);

		mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);
		mTabsAdapter.addTab(
				mTabHost.newTabSpec("favouriteFood").setIndicator(
						"FavouriteFood"), FavouriteFoodFragment.class, null);

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

	public static class FavouriteFoodFragment extends SherlockListFragment {

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			setEmptyText("No phone numbers");

			setListShown(false);
		}

	}
}
