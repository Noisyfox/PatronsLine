package org.foxteam.noisyfox.patronsline;

import java.io.IOException;

import org.foxteam.noisyfox.patronsline.PictureManager.OnPictureGetListener;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
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

public class OwnerFoodEditActivity extends Activity {
	private static final int REQUESTCODE_LOAD_IMAGE = 1;

	private View mFoodEditSaveView;
	private View mFoodEditView;
	private TextView mTextView_save_message;
	private ImageView mImageView_picture;
	private Button mButton_change_picture;
	private EditText mEditText_food_name;
	private EditText mEditText_food_price;
	private CheckBox mCheckBox_food_special;
	private EditText mEditText_food_introduction;
	private Button mButton_save;

	private String sid;
	private String fid;
	private int requestCode;
	private boolean isPictureChanged = false;

	private Bitmap mPicture;
	private String mFoodName;
	private float mFoodPrice;
	private boolean mFoodSpecial;
	private String mFoodIntroduction;

	private SaveTask mSaveTask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_owner_food_edit);

		mFoodEditSaveView = findViewById(R.id.save_status);
		mFoodEditView = findViewById(R.id.edit_form);
		mTextView_save_message = (TextView) findViewById(R.id.save_status_message);
		mImageView_picture = (ImageView) findViewById(R.id.imageView_picture);
		mButton_change_picture = (Button) findViewById(R.id.button_change_picture);
		mEditText_food_name = (EditText) findViewById(R.id.editText_food_name);
		mEditText_food_price = (EditText) findViewById(R.id.editText_food_price);
		mCheckBox_food_special = (CheckBox) findViewById(R.id.checkBox_food_special);
		mEditText_food_introduction = (EditText) findViewById(R.id.editText_food_introduction);
		mButton_save = (Button) findViewById(R.id.button_save);

		mPicture = BitmapFactory.decodeResource(getResources(),
				R.drawable.picture_empty);

		Intent i = getIntent();
		sid = i.getStringExtra("sid");
		fid = i.getStringExtra("fid");
		requestCode = i.getIntExtra("requestCode",
				OwnerMainActivity.REQUESTCODE_EDIT_SHOP);

		if (requestCode == OwnerFoodManageActivity.REQUESTCODE_NEW_FOOD) {
			mTextView_save_message.setText(R.string.edit_progress_create_food);
			mButton_save.setText(R.string.edit_action_create_food);
			setTitle(R.string.title_activity_owner_food_edit_create);
			isPictureChanged = true;
		} else {
			mTextView_save_message.setText(R.string.edit_progress_save_food);
			mButton_save.setText(R.string.edit_action_save_food);
			setTitle(R.string.title_activity_owner_food_edit_edit);
			// 加载数据
			final InformationFood food = InformationManager
					.obtainFoodInformation(fid);
			if (food.photoBitmap != null) {
				mImageView_picture.setImageBitmap(food.photoBitmap);
				mPicture = food.photoBitmap;
			} else {
				PictureManager pm = new PictureManager();
				pm.setOnPictureGetListener(new OnPictureGetListener() {
					@Override
					public void onPictureGet(String pid, Bitmap pic) {
						food.photoBitmap = pic;
						if (!isPictureChanged) {
							mImageView_picture.setImageBitmap(food.photoBitmap);
							mPicture = pic;
						}
					}
				});
				pm.getPicture(food.photo);
			}
			mEditText_food_name.setText(food.name);
			mEditText_food_price.setText(String.format("%.1f", food.price));
			mCheckBox_food_special.setChecked(food.special);
			mEditText_food_introduction.setText(food.introduction);
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

			mFoodEditSaveView.setVisibility(View.VISIBLE);
			mFoodEditSaveView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mFoodEditSaveView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mFoodEditView.setVisibility(View.VISIBLE);
			mFoodEditView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mFoodEditView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mFoodEditSaveView.setVisibility(show ? View.VISIBLE : View.GONE);
			mFoodEditView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	public void attemptSave() {
		if (mSaveTask != null) {
			return;
		}

		// 隐藏输入法
		((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(OwnerFoodEditActivity.this
						.getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);

		// Reset errors.
		mEditText_food_name.setError(null);
		mEditText_food_price.setError(null);
		mEditText_food_introduction.setError(null);

		// read values
		mFoodName = mEditText_food_name.getText().toString();
		String foodPrice = mEditText_food_price.getText().toString();
		mFoodIntroduction = mEditText_food_introduction.getText().toString();
		mFoodSpecial = mCheckBox_food_special.isChecked();

		boolean cancel = false;
		View focusView = null;

		if (TextUtils.isEmpty(foodPrice)) {
			mEditText_food_price
					.setError(getString(R.string.error_field_required));
			focusView = mEditText_food_price;
			cancel = true;
		} else {
			mFoodPrice = Float.valueOf(foodPrice);
			if (mFoodPrice < 0) {
				mEditText_food_price
						.setError(getString(R.string.error_price_too_low));
				focusView = mEditText_food_price;
				cancel = true;
			}
		}

		if (TextUtils.isEmpty(mFoodName)) {
			mEditText_food_name
					.setError(getString(R.string.error_field_required));
			focusView = mEditText_food_name;
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
			if (requestCode == OwnerFoodManageActivity.REQUESTCODE_NEW_FOOD) {
				return sm.food_create(sid, mFoodName, mFoodIntroduction,
						mFoodPrice, mPicture, mFoodSpecial);
			} else {
				return sm.food_modify(fid, mFoodName, mFoodIntroduction,
						mFoodPrice, isPictureChanged ? mPicture : null,
						mFoodSpecial);
			}
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
					Toast.makeText(OwnerFoodEditActivity.this,
							R.string.error_create_failure_food,
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(OwnerFoodEditActivity.this,
							R.string.error_save_failure_food,
							Toast.LENGTH_SHORT).show();
				}
			}
			showProgress(false);
			mSaveTask = null;
		}

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
}
