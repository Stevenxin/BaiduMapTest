package com.baidumaptest;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidumaptest.MySenorOrientation.OnOrientationListener;

import android.location.Location;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

public class MainActivity extends Activity {

	private MapView mMapView;

	private BaiduMap mBaiduMap;
	// 定位相关
	private LocationClient mLocationClient;

	private MyLocationListner mlocationListner;

	private boolean isFirstIn = true;

	private Context context = MainActivity.this;
	private double mLatitude;
	private double mLongtude;
	// 自定义定位图标
	private BitmapDescriptor mDescriptor;
	private MySenorOrientation mySenorOrientation;
	private float mCurrentX;
	private LocationMode mlocationMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 去除标签栏

		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要再setContentView方法之前实现
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_main);

		// 初始化布局
		initview();
		// 初始化定位
		initlocation();
	}

	private void initlocation() {
		mLocationClient = new LocationClient(this);
		mlocationListner = new MyLocationListner();
		// 注册绑定
		mLocationClient.registerLocationListener(mlocationListner);

		// 定位信息详细设置
		LocationClientOption option = new LocationClientOption();
		option.setCoorType("bd09ll");// 设置坐标类型
		option.setIsNeedAddress(true);// 设置是否需要地址信息，默认为无地址
		option.setOpenGps(true);// 是否打开gps进行定位
		option.setScanSpan(1000);// 设置扫描间隔，单位是毫秒
		mLocationClient.setLocOption(option);
		// 初始化图标
		mDescriptor = BitmapDescriptorFactory
				.fromResource(R.drawable.navi_map_gps_locked);

		mySenorOrientation = new MySenorOrientation(context);
		mySenorOrientation
				.setOnOrientationListener(new OnOrientationListener() {

					public void OnOrientationChanged(float x) {
						mCurrentX = x;

					}
				});

	}

	private void initview() {
		mMapView = (MapView) findViewById(R.id.id_bmapView);
		mBaiduMap = mMapView.getMap();
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);// 打开地图表示在500密左右
		mBaiduMap.setMapStatus(msu);

	}

	@Override
	protected void onResume() {
		super.onResume();
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		mMapView.onResume();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		// 开启定位
		mBaiduMap.setMyLocationEnabled(true);
		if (!mLocationClient.isStarted()) {
			mLocationClient.start();
		}
		// 开启方向传感器
		mySenorOrientation.start();

	}

	@Override
	protected void onPause() {
		super.onPause();
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		mMapView.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		// 停止定位
		mBaiduMap.setMyLocationEnabled(false);
		mLocationClient.stop();
		// 停止方向传感器
		mySenorOrientation.stop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		mMapView.onDestroy();
	}

	// 完成menu点击菜单布局
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);// 视图转换器
		return true;
	}

	// 对menu中菜单进行点击监听
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.id_common:// 正常地图
			mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);

			break;
		case R.id.id_map_site:// 卫星地图
			mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
			break;
		case R.id.id_map_traffic:// 交通图
			if (mBaiduMap.isTrafficEnabled()) {
				mBaiduMap.setTrafficEnabled(false);
				item.setTitle("实施交通(off)");

			} else {
				mBaiduMap.setTrafficEnabled(true);
				item.setTitle("实施交通(On)");
			}
			break;
		case R.id.id_map_location:// 卫星地图
			centerToMylocation();// 设置传递到当前位置的动画效果

			break;

		case R.id.id_map_normal:// 普通模式
			mlocationMode = LocationMode.NORMAL;

			break;

		case R.id.id_map_follow:// 跟随模式
			mlocationMode = LocationMode.FOLLOWING;
			break;

		case R.id.id_map_cmpass:// 罗盘模式
			mlocationMode = LocationMode.COMPASS;
			break;

		}
		return super.onOptionsItemSelected(item);
	}

	/*
	 * 定位到我的位置
	 */
	private void centerToMylocation() {
		LatLng latLng = new LatLng(mLatitude, mLongtude);
		MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
		mBaiduMap.animateMapStatus(msu);
	}

	private class MyLocationListner implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			// 事件监听
			MyLocationData data = new MyLocationData.Builder()//
					.direction(mCurrentX)//
					.accuracy(location.getRadius())//
					.latitude(location.getLatitude())//
					.longitude(location.getLongitude()).build();

			mBaiduMap.setMyLocationData(data);
			// 设置自定义指向箭头样式
			MyLocationConfiguration configuration = new MyLocationConfiguration(
					mlocationMode, true, mDescriptor);

			mBaiduMap.setMyLocationConfigeration(configuration);

			// 更新经纬度
			mLatitude = location.getLatitude();
			mLongtude = location.getLongitude();

			if (isFirstIn) {// 是否第一次进入地图定位
				LatLng latLng = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);// 生成地图状态将要发生的变化

				mBaiduMap.animateMapStatus(msu);// 设置传递到当前位置的动画效果
				isFirstIn = false;
				Toast.makeText(context, location.getAddrStr(),
						Toast.LENGTH_SHORT).show();

			}

		}
	}

}
