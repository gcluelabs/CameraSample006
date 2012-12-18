package com.gclue.CameraSample;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.LinearLayout;

public class CameraSample extends Activity implements SensorEventListener,
		LocationListener {
	private MyView mView;
	private boolean mRegisteredSensor;
	private SensorManager mSensorManager = null;
	private LocationManager lm;
	private LinearLayout.LayoutParams arLayoutParams;
	private WebView webView;
	private LinearLayout arLayout;
	private LocationManager mLocationManager;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		CameraView mCamera = new CameraView(this);
		setContentView(mCamera);

		mView = new MyView(this);
		addContentView(mView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mRegisteredSensor = false;

		
		// WebView
		webView = new WebView(this);
		webView.loadUrl("http://www.pref.kyoto.jp/");

		// Web用のLayoutParams
		arLayoutParams = new LinearLayout.LayoutParams(150, 150);
		arLayoutParams.setMargins(0, 10, 0, 0);
		
		// WebViewを張り付けるLayout
		arLayout = new LinearLayout(this);
		arLayout.addView(webView, arLayoutParams);
		
		// WebViewを張り付けたLayoutを画面にはりつけ　
		addContentView(arLayout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		// LocationManagerでGPSの値を取得するための設定
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// 値が変化した際に呼び出されるリスナーの追加
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
	}

	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
			Log.i("SENSOR", "yaw:" + event.values[0]);
			Log.i("SENSOR", "picth:" + event.values[1]);
			Log.i("SENSOR", "roll:" + event.values[2]);
			mView.onOrientationChanged((int) event.values[0],(int) event.values[1], (int) event.values[2]);
			
			// WebViewを位置を移動
			arLayoutParams.setMargins((int) event.values[0] * 10, 10, 10, 10);
			// Layoutを更新
			arLayout.updateViewLayout(webView, arLayoutParams);
			
		} else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			Log.i("SENSOR", "dx:" + event.values[0]);
			Log.i("SENSOR", "dy:" + event.values[1]);
			Log.i("SENSOR", "dz:" + event.values[2]);
			mView.onAcclerometerChanged((int) event.values[0],(int) event.values[1], (int) event.values[2]);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		List < Sensor > sensors_orientation = mSensorManager.getSensorList(Sensor.TYPE_ORIENTATION);

		if (sensors_orientation.size() > 0) {
			Sensor sensor_orientation = sensors_orientation.get(0);
			mRegisteredSensor = mSensorManager.registerListener(this,
														sensor_orientation, 
														SensorManager.SENSOR_DELAY_FASTEST);
		}

		List < Sensor > sensors_accelerometer = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);

		if (sensors_accelerometer.size() > 0) {
			Sensor sensor_accelerometer = sensors_accelerometer.get(0);
			mRegisteredSensor = mSensorManager.registerListener(this,
														sensor_accelerometer, 
														SensorManager.SENSOR_DELAY_FASTEST);
		}
	}

	@Override
	protected void onPause() {
		if (mRegisteredSensor) {
			mSensorManager.unregisterListener(this);
			mRegisteredSensor = false;
		}
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
	public void onDestroy() {
		super.onDestroy();
		if (mSensorManager != null) {
			mSensorManager.unregisterListener(this);
		}
		if (mLocationManager != null) {
			mLocationManager.removeUpdates(this);
		}
	}
   
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		mView.onGpsChanged(location.getLatitude(), location.getLongitude());
	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}
	
}

/**
 * CameraView
 */
class CameraView extends SurfaceView implements SurfaceHolder.Callback {
	Context mContext;
	Camera mCamera;

	CameraView(Context context) {
		super(context);
		mContext = context;

		SurfaceHolder mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

	}

	/**
	 * Surfaceに変化があった場合に呼ばれる
	 */
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewSize(width, height);
		// mCamera.setParameters(parameters);
		mCamera.startPreview();
	}

	/**
	 * Surfaceが生成された際に呼ばれる
	 */
	public void surfaceCreated(SurfaceHolder holder) {
		mCamera = Camera.open();
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (Exception exception) {
			mCamera.release();
			mCamera = null;
		}
	}

	/**
	 * Surfaceが破棄された場合に呼ばれる
	 */
	public void surfaceDestroyed(SurfaceHolder holder) {
		mCamera.stopPreview();
		mCamera = null;
	}
}

/**
 * オーバーレイ描画用のクラス
 */
class MyView extends View {
	private int mDx;
	private int mDy;
	private int mDz;

	private int mYaw;
	private int mRoll;
	private int mPitch;

	private double mLat;
	private double mLon;

	private int mCurX;
	private int mCurY;

	/**
	 * コンストラクタ
	 * 
	 * @param c
	 */
	public MyView(Context c) {
		super(c);
		setFocusable(true);
	}

	/**
	 * 描画処理
	 */
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		/* 背景色を設定 */
		canvas.drawColor(Color.TRANSPARENT);

		/* 描画するための線の色を設定 */
		Paint mPaint = new Paint();
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setARGB(255, 255, 255, 100);

		/* 文字を描画 */
		canvas.drawText("curX:" + mCurX, 20, 20, mPaint);
		canvas.drawText("curY:" + mCurY, 80, 20, mPaint);

		canvas.drawText("mDx:" + mDx, 20, 40, mPaint);
		canvas.drawText("mDy:" + mDy, 80, 40, mPaint);
		canvas.drawText("mDz:" + mDz, 140, 40, mPaint);

		canvas.drawText("mYaw:" + mYaw, 20, 60, mPaint);
		canvas.drawText("mRoll:" + mRoll, 80, 60, mPaint);
		canvas.drawText("mPitch:" + mPitch, 140, 60, mPaint);

		canvas.drawText("mLat:" + mLat, 20, 80, mPaint);
		canvas.drawText("mLon:" + mLon, 160, 80, mPaint);
	}

	public void onAcclerometerChanged(int dx, int dy, int dz) {
		mDx = dx;
		mDy = dy;
		mDz = dz;
		invalidate();
	}

	public void onOrientationChanged(int yaw, int roll, int pitch) {
		mYaw = yaw;
		mRoll = roll;
		mPitch = pitch;
		invalidate();
	}

	public void onGpsChanged(double lat, double lon) {
		mLat = lat;
		mLon = lon;
		invalidate();
	}

	/**
	 * タッチイベント
	 */
	public boolean onTouchEvent(MotionEvent event) {

		/* X,Y座標の取得 */
		mCurX = (int) event.getX();
		mCurY = (int) event.getY();
		/* 再描画の指示 */
		invalidate();

		return true;
	}
}