package org.foxteam.noisyfox.patronsline;

import java.io.IOException;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class RegisterActivity extends Activity {

	private static final int REQUESTCODE_LOAD_IMAGE = 1;

	private UserRegisterTask mRegisterTask = null;

	private View mRegisterFormView;
	private View mRegisterStatusView;
	private ImageView mAvatarVeiw;
	private Spinner mSexView;
	private EditText mUsernameView;
	private EditText mPasswordView;
	private EditText mPasswordConfirmView;
	private EditText mSchoolView;
	private EditText mRregionView;
	private CheckBox mShopOwnerView;

	private Bitmap mAvatar;
	private int mSex;
	private String mUsername;
	private String mPassword;
	private String mPasswordConfirm;
	private String mSchool;
	private String mRegion;
	private int mType = 0;

	private TextView mRegisterStatusMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		mRegisterFormView = findViewById(R.id.register_form);
		mRegisterStatusView = findViewById(R.id.register_status);
		mRegisterStatusMessageView = (TextView) findViewById(R.id.register_status_message);
		mAvatarVeiw = (ImageView) findViewById(R.id.imageView_avatar);
		mSexView = (Spinner) findViewById(R.id.spinner_sex);
		mUsernameView = (EditText) findViewById(R.id.username);
		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordConfirmView = (EditText) findViewById(R.id.password_confrim);
		mSchoolView = (EditText) findViewById(R.id.school);
		mRregionView = (EditText) findViewById(R.id.region);
		mShopOwnerView = (CheckBox) findViewById(R.id.checkBox_shop_owner);

		mAvatar = BitmapFactory.decodeResource(getResources(), R.drawable.head);
		mAvatarVeiw.setImageBitmap(mAvatar);

		findViewById(R.id.register_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptRegister();
					}
				});
		findViewById(R.id.button_choose_avatar).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent i = new Intent(
								Intent.ACTION_PICK,
								android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
						startActivityForResult(i, REQUESTCODE_LOAD_IMAGE);
					}
				});
		mSexView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long id) {
				switch (position) {
				case 0:
					mSex = 1;
					break;
				case 1:
					mSex = 2;
					break;
				case 2:
					mSex = 0;
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}

		});
		mShopOwnerView
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						mType = isChecked ? 1 : 0;
					}
				});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUESTCODE_LOAD_IMAGE && resultCode == RESULT_OK
				&& data != null) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };
			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			cursor.close();

			try {
				mAvatar = PictureManager.loadBitmap(picturePath);
				mAvatarVeiw.setImageBitmap(mAvatar);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			setResult(RESULT_CANCELED);
			finish();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	public void attemptRegister() {
		if (mRegisterTask != null) {
			return;
		}

		// 隐藏输入法
		((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(RegisterActivity.this
						.getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);

		// Reset errors.
		mUsernameView.setError(null);
		mPasswordView.setError(null);
		mPasswordConfirmView.setError(null);
		mSchoolView.setError(null);
		mRregionView.setError(null);

		// read values
		mUsername = mUsernameView.getText().toString();
		mPassword = mPasswordView.getText().toString();
		mPasswordConfirm = mPasswordConfirmView.getText().toString();
		mSchool = mSchoolView.getText().toString();
		mRegion = mRregionView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		if (!mPassword.equals(mPasswordConfirm)) {
			mPasswordConfirmView
					.setError(getString(R.string.error_password_missmatch));
			focusView = mPasswordConfirmView;
			cancel = true;
		}

		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		if (TextUtils.isEmpty(mUsername)) {
			mUsernameView.setError(getString(R.string.error_field_required));
			focusView = mUsernameView;
			cancel = true;
		} else if (mUsername.length() < 4) {
			mUsernameView.setError(getString(R.string.error_invalid_username));
			focusView = mUsernameView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mRegisterStatusMessageView
					.setText(R.string.register_progress_register);
			showProgress(true);
			mRegisterTask = new UserRegisterTask();
			mRegisterTask.execute();
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

			mRegisterStatusView.setVisibility(View.VISIBLE);
			mRegisterStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mRegisterStatusView
									.setVisibility(show ? View.VISIBLE
											: View.GONE);
						}
					});

			mRegisterFormView.setVisibility(View.VISIBLE);
			mRegisterFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mRegisterFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mRegisterStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	public class UserRegisterTask extends AsyncTask<String, Void, Void> {
		int errCode = -1;

		@Override
		protected Void doInBackground(String... params) {
			SessionManager sm = SessionManager.getSessionManager();

			errCode = sm.user_register(mUsername, mPassword, mSex, mType,
					mAvatar, mSchool, mRegion);

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mRegisterTask = null;
			showProgress(false);

			switch (errCode) {
			case SessionManager.ERROR_OK:
				setResult(RESULT_OK);
				finish();
				break;
			case SessionManager.ERROR_USER_NAME_DUPLICATE:
				mUsernameView
						.setError(getString(R.string.error_username_duplicate));
				mUsernameView.requestFocus();
				break;
			case SessionManager.ERROR_NETWORK_FAILURE:
				Toast.makeText(getApplication(),
						R.string.error_network_failure, Toast.LENGTH_SHORT)
						.show();
				break;
			default:
				Toast.makeText(getApplication(), R.string.error_internal,
						Toast.LENGTH_SHORT).show();
				break;
			}
		}

		@Override
		protected void onCancelled() {
			mRegisterTask = null;
			showProgress(false);
		}
	}

}
