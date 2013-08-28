package org.foxteam.noisyfox.patronsline;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.widget.TabHost;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;

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
		mTabsAdapter.addTab(
				mTabHost.newTabSpec("favouriteFood2").setIndicator(
						getText(R.string.tab_label_favourite_food)),
				FavouriteFoodFragment.class, null);
		mTabsAdapter.addTab(
				mTabHost.newTabSpec("favouriteFood3").setIndicator(
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

}
