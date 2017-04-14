package com.bmob.pagingdemo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class MainActivity extends Activity {

	PullToRefreshListView mPullToRefreshView;
	private ILoadingLayout loadingLayout;
	ListView mMsgListView;
	List<TestData> bankCards = new ArrayList<TestData>();
    String lastTime = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Bmob.initialize(this, "");

		initListView();
		findViewById(R.id.btn_insertTestData1).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						createTestData1();
					}
				});
		findViewById(R.id.btn_insertTestData2).setOnClickListener(
				new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						createTestData2();
					}
				});
		findViewById(R.id.btn_insertTestData3).setOnClickListener(
				new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						createTestData3();
					}
				});
		queryData(0, STATE_REFRESH);
	}


	private void createTestData1() {
		for (int i = 0; i < 10; i++) {
			TestData td = new TestData();
			td.setName("测试数据A  " + i);
			td.save(this);
		}
	}
	
	private void createTestData2() {
		for (int i = 0; i < 10; i++) {
			TestData td = new TestData();
			td.setName("测试数据B  " + i);
			td.save(this);
		}
	}
	
	private void createTestData3() {
		for (int i = 0; i < 10; i++) {
			TestData td = new TestData();
			td.setName("测试数据C  " + i);
			td.save(this);
		}
	}

	private void initListView() {
		mPullToRefreshView = (PullToRefreshListView) findViewById(R.id.list);
		loadingLayout = mPullToRefreshView.getLoadingLayoutProxy();
		loadingLayout.setLastUpdatedLabel("");
		loadingLayout
				.setPullLabel(getString(R.string.pull_to_refresh_bottom_pull));
		loadingLayout
				.setRefreshingLabel(getString(R.string.pull_to_refresh_bottom_refreshing));
		loadingLayout
				.setReleaseLabel(getString(R.string.pull_to_refresh_bottom_release));
		// 滑动监听
		mPullToRefreshView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

				if (firstVisibleItem == 0) {
					loadingLayout.setLastUpdatedLabel("");
					loadingLayout
							.setPullLabel(getString(R.string.pull_to_refresh_top_pull));
					loadingLayout
							.setRefreshingLabel(getString(R.string.pull_to_refresh_top_refreshing));
					loadingLayout
							.setReleaseLabel(getString(R.string.pull_to_refresh_top_release));
				} else if (firstVisibleItem + visibleItemCount + 1 == totalItemCount) {
					loadingLayout.setLastUpdatedLabel("");
					loadingLayout
							.setPullLabel(getString(R.string.pull_to_refresh_bottom_pull));
					loadingLayout
							.setRefreshingLabel(getString(R.string.pull_to_refresh_bottom_refreshing));
					loadingLayout
							.setReleaseLabel(getString(R.string.pull_to_refresh_bottom_release));
				}
			}
		});

		// 下拉刷新监听
		mPullToRefreshView
				.setOnRefreshListener(new OnRefreshListener2<ListView>() {

					@Override
					public void onPullDownToRefresh(
							PullToRefreshBase<ListView> refreshView) {
						// 下拉刷新(从第一页开始装载数据)
						queryData(0, STATE_REFRESH);
					}

					@Override
					public void onPullUpToRefresh(
							PullToRefreshBase<ListView> refreshView) {
						// 上拉加载更多(加载下一页数据)
						queryData(curPage, STATE_MORE);
					}
				});

		mMsgListView = mPullToRefreshView.getRefreshableView();
		// 再设置adapter
		mMsgListView.setAdapter(new DeviceListAdapter(this));
	}

	private static final int STATE_REFRESH = 0;// 下拉刷新
	private static final int STATE_MORE = 1;// 加载更多

	private int limit = 10; // 每页的数据是10条
	private int curPage = 0; // 当前页的编号，从0开始

	/**
	 * 分页获取数据
	 * 
	 * @param page
	 *            页码
	 * @param actionType
	 *            ListView的操作类型（下拉刷新、上拉加载更多）
	 */
	private void queryData(int page, final int actionType) {
		Log.i("bmob", "pageN:" + page + " limit:" + limit + " actionType:"
				+ actionType);

		BmobQuery<TestData> query = new BmobQuery<>();
		// 按时间降序查询
		query.order("-createdAt");
		int count = 0;
		// 如果是加载更多
		if (actionType == STATE_MORE) {
			// 处理时间查询
			Date date = null;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				date = sdf.parse(lastTime);
				Log.i("0414", date.toString());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			// 只查询小于等于最后一个item发表时间的数据
			query.addWhereLessThanOrEqualTo("createdAt", new BmobDate(date));
			// 跳过之前页数并去掉重复数据
			query.setSkip(page * count + 1);
		} else {
			// 下拉刷新
			page = 0;
			query.setSkip(page);
		}
		// 设置每页数据个数
		query.setLimit(limit);
		// 查找数据
		query.findObjects(MainActivity.this, new FindListener<TestData>() {
			@Override
			public void onSuccess(List<TestData> list) {
				if (list.size() > 0) {
					
					if (actionType == STATE_REFRESH) {
						// 当是下拉刷新操作时，将当前页的编号重置为0，并把bankCards清空，重新添加
						curPage = 0;
						bankCards.clear();
						// 获取最后时间
						lastTime = list.get(list.size() - 1).getCreatedAt();
					}

					// 将本次查询的数据添加到bankCards中
					for (TestData td : list) {
						bankCards.add(td);
					}

					// 这里在每次加载完数据后，将当前页码+1，这样在上拉刷新的onPullUpToRefresh方法中就不需要操作curPage了
					curPage++;
//					 showToast("第"+(page+1)+"页数据加载完成");
				} else if (actionType == STATE_MORE) {
					showToast("没有更多数据了");
				} else if (actionType == STATE_REFRESH) {
					showToast("没有数据");
				}
				mPullToRefreshView.onRefreshComplete();
			}

			@Override
			public void onError(int arg0, String arg1) {
				showToast("查询失败:" + arg1);
				mPullToRefreshView.onRefreshComplete();
			}
		});
	}

	private class DeviceListAdapter extends BaseAdapter {

		Context context;

		public DeviceListAdapter(Context context) {
			this.context = context;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {

				convertView = LayoutInflater.from(context).inflate(
						R.layout.list_item_bankcard, null);
				holder = new ViewHolder();
				holder.tv_cardNumber = (TextView) convertView
						.findViewById(R.id.tv_cardNumber);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			TestData td = (TestData) getItem(position);

			holder.tv_cardNumber.setText(td.getName());
			return convertView;
		}

		class ViewHolder {
			TextView tv_cardNumber;
		}

		@Override
		public int getCount() {
			return bankCards.size();
		}

		@Override
		public Object getItem(int position) {
			return bankCards.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

	}

	private void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

}
