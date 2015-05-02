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
	// ��λ���
	private LocationClient mLocationClient;

	private MyLocationListner mlocationListner;

	private boolean isFirstIn = true;

	private Context context = MainActivity.this;
	private double mLatitude;
	private double mLongtude;
	// �Զ��嶨λͼ��
	private BitmapDescriptor mDescriptor;
	private MySenorOrientation mySenorOrientation;
	private float mCurrentX;
	private LocationMode mlocationMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// ȥ����ǩ��

		// ��ʹ��SDK�����֮ǰ��ʼ��context��Ϣ������ApplicationContext
		// ע��÷���Ҫ��setContentView����֮ǰʵ��
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_main);

		// ��ʼ������
		initview();
		// ��ʼ����λ
		initlocation();
	}

	private void initlocation() {
		mLocationClient = new LocationClient(this);
		mlocationListner = new MyLocationListner();
		// ע���
		mLocationClient.registerLocationListener(mlocationListner);

		// ��λ��Ϣ��ϸ����
		LocationClientOption option = new LocationClientOption();
		option.setCoorType("bd09ll");// ������������
		option.setIsNeedAddress(true);// �����Ƿ���Ҫ��ַ��Ϣ��Ĭ��Ϊ�޵�ַ
		option.setOpenGps(true);// �Ƿ��gps���ж�λ
		option.setScanSpan(1000);// ����ɨ��������λ�Ǻ���
		mLocationClient.setLocOption(option);
		// ��ʼ��ͼ��
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
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);// �򿪵�ͼ��ʾ��500������
		mBaiduMap.setMapStatus(msu);

	}

	@Override
	protected void onResume() {
		super.onResume();
		// ��activityִ��onResumeʱִ��mMapView. onResume ()��ʵ�ֵ�ͼ�������ڹ���
		mMapView.onResume();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		// ������λ
		mBaiduMap.setMyLocationEnabled(true);
		if (!mLocationClient.isStarted()) {
			mLocationClient.start();
		}
		// �������򴫸���
		mySenorOrientation.start();

	}

	@Override
	protected void onPause() {
		super.onPause();
		// ��activityִ��onPauseʱִ��mMapView. onPause ()��ʵ�ֵ�ͼ�������ڹ���
		mMapView.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		// ֹͣ��λ
		mBaiduMap.setMyLocationEnabled(false);
		mLocationClient.stop();
		// ֹͣ���򴫸���
		mySenorOrientation.stop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// ��activityִ��onDestroyʱִ��mMapView.onDestroy()��ʵ�ֵ�ͼ�������ڹ���
		mMapView.onDestroy();
	}

	// ���menu����˵�����
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);// ��ͼת����
		return true;
	}

	// ��menu�в˵����е������
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.id_common:// ������ͼ
			mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);

			break;
		case R.id.id_map_site:// ���ǵ�ͼ
			mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
			break;
		case R.id.id_map_traffic:// ��ͨͼ
			if (mBaiduMap.isTrafficEnabled()) {
				mBaiduMap.setTrafficEnabled(false);
				item.setTitle("ʵʩ��ͨ(off)");

			} else {
				mBaiduMap.setTrafficEnabled(true);
				item.setTitle("ʵʩ��ͨ(On)");
			}
			break;
		case R.id.id_map_location:// ���ǵ�ͼ
			centerToMylocation();// ���ô��ݵ���ǰλ�õĶ���Ч��

			break;

		case R.id.id_map_normal:// ��ͨģʽ
			mlocationMode = LocationMode.NORMAL;

			break;

		case R.id.id_map_follow:// ����ģʽ
			mlocationMode = LocationMode.FOLLOWING;
			break;

		case R.id.id_map_cmpass:// ����ģʽ
			mlocationMode = LocationMode.COMPASS;
			break;

		}
		return super.onOptionsItemSelected(item);
	}

	/*
	 * ��λ���ҵ�λ��
	 */
	private void centerToMylocation() {
		LatLng latLng = new LatLng(mLatitude, mLongtude);
		MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
		mBaiduMap.animateMapStatus(msu);
	}

	private class MyLocationListner implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			// �¼�����
			MyLocationData data = new MyLocationData.Builder()//
					.direction(mCurrentX)//
					.accuracy(location.getRadius())//
					.latitude(location.getLatitude())//
					.longitude(location.getLongitude()).build();

			mBaiduMap.setMyLocationData(data);
			// �����Զ���ָ���ͷ��ʽ
			MyLocationConfiguration configuration = new MyLocationConfiguration(
					mlocationMode, true, mDescriptor);

			mBaiduMap.setMyLocationConfigeration(configuration);

			// ���¾�γ��
			mLatitude = location.getLatitude();
			mLongtude = location.getLongitude();

			if (isFirstIn) {// �Ƿ��һ�ν����ͼ��λ
				LatLng latLng = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);// ���ɵ�ͼ״̬��Ҫ�����ı仯

				mBaiduMap.animateMapStatus(msu);// ���ô��ݵ���ǰλ�õĶ���Ч��
				isFirstIn = false;
				Toast.makeText(context, location.getAddrStr(),
						Toast.LENGTH_SHORT).show();

			}

		}
	}

}
