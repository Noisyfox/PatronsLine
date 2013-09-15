package org.foxteam.noisyfox.patronsline;

import org.foxteam.noisyfox.patronsline.PictureManager.OnPictureGetListener;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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

	InformationSession mInformationSession = null;

	PictureManager mAvatarPictureManager = new PictureManager();
	OnPictureGetListener mAvatarOnPictureGetListener = new OnPictureGetListener() {
		@Override
		public void onPictureGet(String pid, Bitmap pic) {
			InformationSession is = SessionManager.getCurrentSession();
			if (pic != null && is != null && is.user != null
					&& is.user.avatar == pid) {
				is.user.avatarBitmap = pic;
				ActionBar actionBar = getSupportActionBar();
				actionBar.setIcon(new BitmapDrawable(getResources(), pic));
			}
		}
	};

	@Override
	protected void onDestroy() {
		PatronsLineApplication mMyApp = (PatronsLineApplication) this
				.getApplication();
		mMyApp.destroyLocationClient();
		mMyApp.destroyEngineManager();
		PictureManager.cacheSave();
		super.onDestroy();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Sherlock_Light);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_consumer_main);
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();

		mViewPager = (ViewPager) findViewById(R.id.pager);

		mInformationSession = SessionManager.getCurrentSession();

		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle(mInformationSession.user.name);
		actionBar.setSubtitle("欢迎回来");
		actionBar.setIcon(R.drawable.avatar_empty);

		mAvatarPictureManager
				.setOnPictureGetListener(mAvatarOnPictureGetListener);
		mAvatarPictureManager.getPicture(mInformationSession.user.avatar);

		mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);
		mTabsAdapter.addTab(
				mTabHost.newTabSpec("favouriteFood").setIndicator(
						getText(R.string.tab_label_favourite_food)),
				ConsumerFavouriteFoodFragment.class, null);
		mTabsAdapter.addTab(
				mTabHost.newTabSpec("favouriteShop").setIndicator(
						getText(R.string.tab_label_favourite_shop)),
				ConsumerFavouriteShopFragment.class, null);
		mTabsAdapter.addTab(
				mTabHost.newTabSpec("listShop").setIndicator(
						getText(R.string.tab_label_list_shop)),
				ConsumerListShopFragment.class, null);

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
