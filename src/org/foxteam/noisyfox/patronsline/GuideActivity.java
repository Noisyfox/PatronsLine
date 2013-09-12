package org.foxteam.noisyfox.patronsline;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;

@SuppressWarnings("deprecation")
public class GuideActivity extends Activity {

	final int[] mImageIds = { R.drawable.guide_1, R.drawable.guide_2,
			R.drawable.guide_3 };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide);

		final PageIndicatorView pageView = (PageIndicatorView) findViewById(R.id.pageView);
		final GuideGallery guideGallery = (GuideGallery) findViewById(R.id.gallery_feture);
		guideGallery.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long id) {
				// TODO Auto-generated method stub
				if (position >= mImageIds.length) {
					GuideActivity.this.finish();
				} else {
					pageView.setCurrentPage(position);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}

		});

		if (mImageIds != null) {
			pageView.setTotalPage(mImageIds.length);
			pageView.setCurrentPage(0);
			guideGallery.setAdapter(new ImageAdapter());
		} else {
			this.finish();
		}

	}

	class ImageAdapter extends BaseAdapter {
		Bitmap[] bitmaps = new Bitmap[mImageIds.length];

		ImageAdapter() {
			for (int i = 0; i < mImageIds.length; i++) {
				bitmaps[i] = BitmapFactory.decodeResource(getResources(),
						mImageIds[i]);
			}
		}

		@Override
		public int getCount() {
			return mImageIds.length + 1;
		}

		@Override
		public Object getItem(int position) {
			if (position < mImageIds.length)
				return mImageIds[position];

			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Bitmap image = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(GuideActivity.this).inflate(
						R.layout.item_gallery, null);
				Gallery.LayoutParams params = new Gallery.LayoutParams(
						Gallery.LayoutParams.FILL_PARENT,
						Gallery.LayoutParams.FILL_PARENT);
				convertView.setLayoutParams(params);
				if (position < mImageIds.length)
					image = bitmaps[position];

				convertView.setTag(image);
			} else {
				image = (Bitmap) convertView.getTag();
			}

			ImageView imageView = (ImageView) convertView
					.findViewById(R.id.gallery_image);
			imageView.setImageBitmap(image);
			imageView.setScaleType(ImageView.ScaleType.FIT_XY);

			return convertView;
		}

	}

	@Override
	public void finish() {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor e = pref.edit();
		// e.putBoolean("firstRun", false);
		e.commit();

		Intent intent = new Intent();
		intent.setClass(this, LoginActivity.class);
		startActivity(intent);

		super.finish();
	}

}
