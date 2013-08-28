package org.foxteam.noisyfox.patronsline;

import android.os.Bundle;
import android.view.KeyEvent;
import android.app.Activity;

public class RegisterActivity extends Activity {

	public static final int RESULT_CANCLE = 1;
	public static final int RESULT_SUCCESS = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			setResult(RESULT_CANCLE);
			finish();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

}
