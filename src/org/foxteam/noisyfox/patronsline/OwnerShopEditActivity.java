package org.foxteam.noisyfox.patronsline;

import java.io.IOException;

import org.foxteam.noisyfox.patronsline.PictureManager.OnPictureGetListener;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class OwnerShopEditActivity extends Activity {
	private static final int REQUESTCODE_LOAD_IMAGE = 1;

	private View mShopEditSaveView;
	private View mShopEditView;
	private TextView mTextView_save_message;
	private ImageView mImageView_picture;
	private Button mButton_change_picture;
	private EditText mEditText_shop_name;
	private EditText mEditText_shop_address;
	private EditText mEditText_shop_phone;
	private EditText mEditText_shop_introduction;
	private Button mButton_save;

	private String sid;
	private int requestCode;
	private boolean isPictureChanged = false;

	private Bitmap mPicture;
	private String mShopName;
	private String mShopAddress;
	private String mShopPhone;
	private String mShopIntroduction;

	private SaveTask mSaveTask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_owner_shop_edit);

		mShopEditSaveView = findViewById(R.id.save_status);
		mShopEditView = findViewById(R.id.edit_form);
		mTextView_save_message = (TextView) findViewById(R.id.save_status_message);
		mImageView_picture = (ImageView) findViewById(R.id.imageView_picture);
		mButton_change_picture = (Button) findViewById(R.id.button_change_picture);
		mEditText_shop_name = (EditText) findViewById(R.id.editText_shop_name);
		mEditText_shop_address = (EditText) findViewById(R.id.editText_shop_address);
		mEditText_shop_phone = (EditText) findViewById(R.id.editText_shop_phone);
		mEditText_shop_introduction = (EditText) findViewById(R.id.editText_shop_introduction);
		mButton_save = (Button) findViewById(R.id.button_save);

		mPicture = BitmapFactory.decodeResource(getResources(),
				R.drawable.picture_empty);

		Intent i = getIntent();
		sid = i.getStringExtra("sid");
		requestCode = i.getIntExtra("requestCode",
				OwnerMainActivity.REQUESTCODE_EDIT_SHOP);

		if (requestCode == OwnerMainActivity.REQUESTCODE_NEW_SHOP) {
			mTextView_save_message.setText(R.string.edit_progress_create_shop);
			mButton_save.setText(R.string.edit_action_create_shop);
			setTitle(R.string.title_activity_owner_shop_edit_create);
		} else {
			mTextView_save_message.setText(R.string.edit_progress_save_shop);
			mButton_save.setText(R.string.edit_action_save_shop);
			setTitle(R.string.title_activity_owner_shop_edit_edit);
			// 加载数据
			final InformationShop shop = InformationManager
					.obtainShopInformation(sid);
			if (shop.photoBitmap != null) {
				mImageView_picture.setImageBitmap(shop.photoBitmap);
				mPicture = shop.photoBitmap;
				isPictureChanged = true;
			} else {
				PictureManager pm = new PictureManager();
				pm.setOnPictureGetListener(new OnPictureGetListener() {
					@Override
					public void onPictureGet(String pid, Bitmap pic) {
						shop.photoBitmap = pic;
						if (!isPictureChanged) {
							mImageView_picture.setImageBitmap(shop.photoBitmap);
							mPicture = pic;
						}
					}
				});
				pm.getPicture(shop.photo);
			}
			mEditText_shop_name.setText(shop.name);
			mEditText_shop_address.setText(shop.address);
			mEditText_shop_phone.setText(shop.phone_num);
			mEditText_shop_introduction.setText(shop.introduction);
		}
		mButton_change_picture.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent i = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, REQUESTCODE_LOAD_IMAGE);
			}
		});
		mButton_save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptSave();
			}
		});
	}

	public void attemptSave() {
		if (mSaveTask != null) {
			return;
		}

		// 隐藏输入法
		((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(OwnerShopEditActivity.this
						.getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);

		// Reset errors.
		mEditText_shop_name.setError(null);
		mEditText_shop_address.setError(null);
		mEditText_shop_phone.setError(null);
		mEditText_shop_introduction.setError(null);

		// read values
		mShopName = mEditText_shop_name.getText().toString();
		mShopAddress = mEditText_shop_address.getText().toString();
		mShopPhone = mEditText_shop_phone.getText().toString();
		mShopIntroduction = mEditText_shop_introduction.getText().toString();

		boolean cancel = false;
		View focusView = null;

		if (TextUtils.isEmpty(mShopPhone)) {
			mEditText_shop_phone
					.setError(getString(R.string.error_field_required));
			focusView = mEditText_shop_phone;
			cancel = true;
		} else if (mShopPhone.length() < 8) {
			mEditText_shop_phone
					.setError(getString(R.string.error_invalid_phone));
			focusView = mEditText_shop_phone;
			cancel = true;
		}

		if (TextUtils.isEmpty(mShopAddress)) {
			mEditText_shop_address
					.setError(getString(R.string.error_field_required));
			focusView = mEditText_shop_address;
			cancel = true;
		}

		if (TextUtils.isEmpty(mShopName)) {
			mEditText_shop_name
					.setError(getString(R.string.error_field_required));
			focusView = mEditText_shop_name;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			showProgress(true);
			mSaveTask = new SaveTask();
			mSaveTask.execute();
		}
	}

	public class SaveTask extends AsyncTask<Void, Void, Integer> {

		@Override
		protected Integer doInBackground(Void... params) {
			SessionManager sm = SessionManager.getSessionManager();
			return sm.shop_modify(sid, mShopName, mShopAddress,
					mShopIntroduction, mShopPhone, isPictureChanged ? mPicture
							: null);
		}

		@Override
		protected void onCancelled() {
			mSaveTask = null;
			showProgress(false);
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (result == SessionManager.ERROR_OK) {
				setResult(RESULT_OK);
				finish();
			} else {
				if (requestCode == OwnerMainActivity.REQUESTCODE_NEW_SHOP) {
					Toast.makeText(OwnerShopEditActivity.this,
							R.string.error_create_failure_shop,
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(OwnerShopEditActivity.this,
							R.string.error_save_failure_shop,
							Toast.LENGTH_SHORT).show();
				}
			}
			showProgress(false);
			mSaveTask = null;
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& requestCode == OwnerMainActivity.REQUESTCODE_NEW_SHOP) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
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
				mPicture = PictureManager.loadBitmap(picturePath);
				isPictureChanged = true;
				mImageView_picture.setImageBitmap(mPicture);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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

			mShopEditSaveView.setVisibility(View.VISIBLE);
			mShopEditSaveView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mShopEditSaveView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mShopEditView.setVisibility(View.VISIBLE);
			mShopEditView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mShopEditView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mShopEditSaveView.setVisibility(show ? View.VISIBLE : View.GONE);
			mShopEditView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
}
