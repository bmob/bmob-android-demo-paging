package com.handmark.pulltorefresh.library.internal;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.handmark.pulltorefresh.library.R;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class WaterFallView extends ScrollView implements View.OnClickListener {

	private boolean inited = false;
	private int SCREEN_HEIGHT_PX;

	private Adapter adapter;
	private ArrayList<View> views;

	private LinearLayout column1;
	private LinearLayout column2;
	private OnItemVisibilityChangeListener onItemVisibilityChangeListener;
	private OnItemClickListener onItemClickListener;

	public WaterFallView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public WaterFallView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public WaterFallView(Context context) {
		super(context);
		init(context);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	private void init(Context context) {
		if (inited)
			return;

		ViewGroup innerView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.water_fall_view_inner, null);
		this.addView(innerView);

		//
		column1 = (LinearLayout) innerView.findViewById(R.id.column1);
		column2 = (LinearLayout) innerView.findViewById(R.id.column2);

		//
		WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		windowManager.getDefaultDisplay().getMetrics(dm);
		SCREEN_HEIGHT_PX = dm.heightPixels;
		//
		views = new ArrayList<View>();

		inited = true;
	}

	@Override
	protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
		super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);

		int maxTop = scrollY - SCREEN_HEIGHT_PX * 1;
		int maxBottom = scrollY + SCREEN_HEIGHT_PX + SCREEN_HEIGHT_PX * 1;

		int size = views.size();
		if (size < 1)
			return;

		for (int position = 0; position < size; position++) {
			View view;
			if (position % 2 == 0) {
				int index = position / 2;
				view = column1.getChildAt(index);
			} else {
				int index = (position - 1) / 2;
				view = column2.getChildAt(index);
			}

			int top = view.getTop();
			int bottom = view.getBottom();

			int visibility;
			if (top > maxBottom || bottom < maxTop) {
				visibility = View.INVISIBLE;
			} else {
				visibility = View.VISIBLE;
			}

			if (view.getVisibility() != visibility) {
				view.setVisibility(visibility);

				if (onItemVisibilityChangeListener != null)
					onItemVisibilityChangeListener.onVisibilityChange(this, position, view, visibility);
			}
		}

	}

	public void setAdapter(Adapter adapter) {
		if (this.adapter != adapter) {
			adapter.setWaterFallView(this);
			this.adapter = adapter;

			// REFRESH VIEWS
			if (column1 != null)
				column1.removeAllViews();

			if (column2 != null)
				column2.removeAllViews();

			views.clear();

			// ADD VIEWS
			for (int position = 0; position < adapter.getCount(); position++) {
				View view = adapter.createView(position, getContext());
				View itemView = createItemView(position, view);
				addView(position, itemView);
			}
		}
	}

	public Adapter getAdapter() {
		return adapter;
	}

	private View createItemView(int position, View view) {

		RelativeLayout parent = new RelativeLayout(getContext());
		parent.setTag(position);
		parent.setOnClickListener(this);

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		parent.addView(view, params);

		return parent;
	}

	private void addView(int position, View view) {

		LinearLayout column;
		if ((position % 2) == 0) {
			column = column1;
		} else {
			column = column2;
		}

		column.addView(view);
		views.add(view);
	}

	@Override
	public void onClick(View v) {
		int position = (Integer) v.getTag();
		if (onItemClickListener != null) {
			onItemClickListener.onItemClick(this, position, v);
		}
	}

	/**
	 * ONLY SUPPORT INCREASE DATA CHANGED
	 */
	private void onDataChanged() {
		if (adapter != null) {
			if (adapter.getCount() > views.size()) {
				int position = views.size();
				Log.i("life","onDataChanged");
				for (; position < adapter.getCount(); position++) {
					View view = adapter.createView(position, getContext());
					View itemView = createItemView(position, view);
					addView(position, itemView);
				}
			}
		}
	}

	public OnItemVisibilityChangeListener getOnItemVisibilityChangeListener() {
		return onItemVisibilityChangeListener;
	}

	public void setOnItemVisibilityChangeListener(OnItemVisibilityChangeListener onItemVisibilityChangeListener) {
		this.onItemVisibilityChangeListener = onItemVisibilityChangeListener;
	}

	public static interface OnItemVisibilityChangeListener {
		public void onVisibilityChange(View wallView, int position, View view, int visibility);
	}

	public abstract static class Adapter {

		private WaterFallView waterFallView;

		public abstract int getCount();

		public abstract int getItemId(int position);

		public abstract Object getItem(int position);

		public abstract View createView(int position, Context context);

		/**
		 * ONLY SUPPORT INCREASE
		 */
		public void notifyDataChanged() {
			waterFallView.onDataChanged();
		}

		public WaterFallView getWaterFallView() {
			return waterFallView;
		}

		public void setWaterFallView(WaterFallView waterFallView) {
			this.waterFallView = waterFallView;
		}

	}

	public OnItemClickListener getOnItemClickListener() {
		return onItemClickListener;
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		this.onItemClickListener = onItemClickListener;
	}

	public static interface OnItemClickListener {
		public void onItemClick(WaterFallView waterFallView, int position, View view);
	}

}
