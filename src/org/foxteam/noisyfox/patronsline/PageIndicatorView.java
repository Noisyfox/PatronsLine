package org.foxteam.noisyfox.patronsline;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class PageIndicatorView extends View {
	Rect r = new Rect();
	Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
	private String TAG = "PageIndicatorView";
	private int mCurrentPage = -1;
	private int mTotalPage = 0;
	Bitmap mBmp_point = BitmapFactory.decodeResource(getResources(),
			R.drawable.feature_point);
	Bitmap mBmp_point_cur = BitmapFactory.decodeResource(getResources(),
			R.drawable.feature_point_cur);

	public PageIndicatorView(Context context) {
		super(context);
	}

	public PageIndicatorView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setTotalPage(int nPageNum) {
		mTotalPage = nPageNum;
		if (mCurrentPage >= mTotalPage)
			mCurrentPage = mTotalPage - 1;
	}

	public int getCurrentPage() {
		return mCurrentPage;
	}

	public void setCurrentPage(int nPageIndex) {
		if (nPageIndex < 0 || nPageIndex >= mTotalPage)
			return;

		if (mCurrentPage != nPageIndex) {
			mCurrentPage = nPageIndex;
			this.invalidate();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Log.d(TAG, "on draw...");
		paint.setStyle(Paint.Style.FILL);
		// paint.setColor(Color.BLACK);

		this.getDrawingRect(r);

		int iconWidth = 22;
		int iconHeight = 22;
		int space = 12;

		int x = (r.width() - (iconWidth * mTotalPage + space * (mTotalPage - 1))) / 2;
		int y = (r.height() - iconHeight) / 2;

		for (int i = 0; i < mTotalPage; i++) {

			r.left = x;
			r.top = y;
			r.right = x + iconWidth;
			r.bottom = y + iconHeight;

			Bitmap bmp = null;
			if (i == mCurrentPage) {
				bmp = mBmp_point_cur;
			} else {
				bmp = mBmp_point;
			}

			// canvas.drawCircle(x + iconWidth / 2f, y + iconHeight / 2f, 4.5f,
			// paint);
			canvas.drawBitmap(bmp, null, r, paint);

			x += iconWidth + space;

		}

	}

}
