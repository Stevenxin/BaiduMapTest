package com.baidumaptest;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MySenorOrientation implements SensorEventListener {

	private SensorManager mSensorManager;
	private Context mcontext;
	private Sensor mSensor;

	private float lastx;

	// 构造方法
	public MySenorOrientation(Context context) {
		this.mcontext = context;
	}

	@SuppressWarnings("deprecation")
	public void start() {
		mSensorManager = (SensorManager) mcontext
				.getSystemService(Context.SENSOR_SERVICE);
		if (mSensorManager != null) {

			// 获得方向传感器
			mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		}
		if (mSensor != null) {
			mSensorManager.registerListener(this, mSensor,
					SensorManager.SENSOR_DELAY_UI);
		}

	}

	public void stop() {
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
			float x = event.values[SensorManager.DATA_X];
			if (Math.abs(x - lastx) > 1.0) {
				if(mOnOrientationListener!=null){
					mOnOrientationListener.OnOrientationChanged(x);
				}
			}
			lastx = x;

		}

	}

	private OnOrientationListener mOnOrientationListener;

	public void setOnOrientationListener(
			OnOrientationListener mOnOrientationListener) {
		this.mOnOrientationListener = mOnOrientationListener;
	}

	
	
	public interface OnOrientationListener {
		void OnOrientationChanged(float x);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

}
