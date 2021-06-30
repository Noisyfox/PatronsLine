package org.foxteam.noisyfox.patronsline;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * 在普通的ListAdapter基础上增加了标题显示，可以在列表的第一个位置显示不同的标题布局
 * @author Noisyfox
 *
 */
public abstract class TitledUserFoodAdapter extends UserFoodAdapter {

	TitledUserFoodAdapter(Context context, ListView listView) {
		super(context, listView);
	}

	@Override
	public final int getCount() {
		return super.getCount() + 1;
	}

	@Override
	public final Object getItem(int position) {
		if (position == 0) {
			return null;
		} else {
			return super.getItem(position - 1);
		}
	}

	@Override
	public final long getItemId(int position) {
		if (position == 0) {
			return 0;
		} else {
			return super.getItemId(position - 1) + 1;
		}
	}

	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {
		if (position == 0) {
			return getTitleView(convertView, parent);
		} else {
			return super.getView(position - 1, convertView, parent);
		}
	}

	public abstract View getTitleView(View convertView, ViewGroup parent);

}
