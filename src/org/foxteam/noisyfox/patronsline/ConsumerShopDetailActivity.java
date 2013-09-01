package org.foxteam.noisyfox.patronsline;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;

import android.os.Bundle;

public class ConsumerShopDetailActivity extends SherlockActivity {

	private InformationShop mInformationShop = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_consumer_shop_detail);

		String sid = getIntent().getStringExtra("sid");
		mInformationShop = InformationManager.obtainShopInformation(sid);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.consumer_shop_detail, menu);
		return true;
	}

}
