package com.bmob.pagingdemo;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Toast;

public class MainActivity extends Activity {

	PullToRefreshListView mPullToRefreshView;
	private ILoadingLayout loadingLayout;
	ListView mMsgListView;
	List<TestData> bankCards = new ArrayList<TestData>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Bmob.initialize(this, "");
		showToast("请记得将你的AppId填写在MainActivity的BmobSDK初始化方法中");
		
		initListView();
		findViewById(R.id.btn_insertTestData).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				createTestData();
			}
		});
		queryData(0, STATE_REFRESH);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void createTestData(){
		for (int i = 0; i < 20; i++) {
			TestData td = new TestData();
			td.setName("测试数据  "+i);
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
		// //滑动监听
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
	
	private int limit = 10;		// 每页的数据是10条
	private int curPage = 0;		// 当前页的编号，从0开始
	
	/**
	 * 分页获取数据
	 * @param page	页码
	 * @param actionType	ListView的操作类型（下拉刷新、上拉加载更多）
	 */
	private void queryData(final int page, final int actionType){
		Log.i("bmob", "pageN:"+page+" limit:"+limit+" actionType:"+actionType);
		
		BmobQuery<TestData> query = new BmobQuery<TestData>();
		query.setLimit(limit);			// 设置每页多少条数据
		query.setSkip(page*limit);		// 从第几条数据开始，
		query.findObjects(this, new FindListener<TestData>() {
			
			@Override
			public void onSuccess(List<TestData> arg0) {
				// TODO Auto-generated method stub
				
				if(arg0.size()>0){
					if(actionType == STATE_REFRESH){
						// 当是下拉刷新操作时，将当前页的编号重置为0，并把bankCards清空，重新添加
						curPage = 0;
						bankCards.clear();
					}
					
					// 将本次查询的数据添加到bankCards中
					for (TestData td : arg0) {
						bankCards.add(td);
					}
					
					// 这里在每次加载完数据后，将当前页码+1，这样在上拉刷新的onPullUpToRefresh方法中就不需要操作curPage了
					curPage++;
					showToast("第"+(page+1)+"页数据加载完成");
				}else if(actionType == STATE_MORE){
					showToast("没有更多数据了");
				}else if(actionType == STATE_REFRESH){
					showToast("没有数据");
				}
				mPullToRefreshView.onRefreshComplete();
			}
			
			@Override
			public void onError(int arg0, String arg1) {
				// TODO Auto-generated method stub
				showToast("查询失败:"+arg1);
				mPullToRefreshView.onRefreshComplete();
			}
		});
	}
	
	
	private class DeviceListAdapter extends BaseAdapter  {
		
		Context context;
		
		public DeviceListAdapter(Context context){
			this.context = context;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				
				convertView = LayoutInflater.from(context)
						.inflate(R.layout.list_item_bankcard, null);
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
			// TODO Auto-generated method stub
			return bankCards.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return bankCards.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

	}
	
	private void showToast(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

}
