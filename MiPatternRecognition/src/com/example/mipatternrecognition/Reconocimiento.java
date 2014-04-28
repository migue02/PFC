package com.example.mipatternrecognition;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.KeyPoint;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;

import pfc.bd.MySQLiteHelper;
import pfc.bd.ObjetoDataSource;
import pfc.obj.Objeto;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class Reconocimiento extends Activity implements CvCameraViewListener2 {

	private static final String TAG = "Reconocimiento::Activity";
	private static final int VIEW_MODE_SURF_DETECTION = 0; // SURF (nonfree
															// module)
	private static final int VIEW_MODE_NORMAL = 8;

	private static final int SURF_DETECTION = 0; // SURF (nonfree module)

	private CameraBridgeViewBase mOpenCvCameraView;
	private MenuItem mItemSurfFeaturesDetection = null;
	private MenuItem mItemViewModeNormal = null;
	private boolean buscandoObjeto;
	private int mViewMode;
	private Mat mGray;
	private Mat mRgba;
	private Button buttonVistaNormal;
	private Button buttonCapturaObjeto;
	private Button buttonEncuentraObjeto;
	private boolean patronAdquirido = false;
	private boolean encontrado = false;
	private int DETECTION_TYPE = SURF_DETECTION; // CHOOOSEN KIND OF DETECTION
	private ObjetoDataSource datasource;
	private MatOfKeyPoint keypoints_obj = new MatOfKeyPoint();
	private Mat descriptores_obj = new Mat();
	private KeyPoint[] listaKP_obj;
	// private Mat aux=new Mat();
	private EditText editNombre;
	private long id;
	private CheckBox CBKeyPoints;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");

				// Load native library after(!) OpenCV initialization
				System.loadLibrary("opencv_java");
				System.loadLibrary("nonfree");
				System.loadLibrary("mipattern_recognition");

				mOpenCvCameraView.enableView();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");

		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.activity_reconocimiento);

		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		mViewMode = VIEW_MODE_SURF_DETECTION;

		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.surface_view);
		buttonCapturaObjeto = (Button) findViewById(R.id.buttonCapturaObjeto);
		buttonEncuentraObjeto = (Button) findViewById(R.id.buttonEncuentraObjeto);
		buttonVistaNormal = (Button) findViewById(R.id.buttonVistaNormal);
		editNombre = (EditText) findViewById(R.id.editNombre);
		CBKeyPoints = (CheckBox) findViewById(R.id.checkBoxKeyPoints);

		mOpenCvCameraView.setCvCameraViewListener(this);

		datasource = new ObjetoDataSource(this);
		datasource.open();

		/* Listener Captura Objeto Button */
		buttonCapturaObjeto.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				patronAdquirido = true;
			}
		});

		/* Listener Encuentra Objeto Button */
		buttonEncuentraObjeto.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				buscandoObjeto = true;
			}
		});

		/* Listener Vista Normal Button */
		buttonVistaNormal.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				buscandoObjeto = false;
				patronAdquirido = false;
				encontrado = false;
			}
		});

	}

	@Override
	public void onPause() {
		datasource.close();
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume() {
		datasource.open();
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		mRgba = new Mat(height, width, CvType.CV_8UC4);
		mGray = new Mat(height, width, CvType.CV_8UC1);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		Log.i(TAG, "called onCreateOptionsMenu");
		mItemSurfFeaturesDetection = menu.add("Surf Feature Detecction");
		mItemViewModeNormal = menu.add("Normal View");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item == mItemSurfFeaturesDetection) {
			mViewMode = VIEW_MODE_SURF_DETECTION;
			DETECTION_TYPE = SURF_DETECTION;
		}
		return true;
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		if (patronAdquirido) {

			mRgba = inputFrame.rgba();
			mGray = inputFrame.gray();
			patronAdquirido = false;
			String keyString, desString;
			
			if (CBKeyPoints.isChecked()){
				listaKP_obj = FindFeatures(mGray.getNativeObjAddr(),
						mRgba.getNativeObjAddr(), descriptores_obj.getNativeObjAddr());
				
				keypoints_obj = new MatOfKeyPoint(listaKP_obj);
				keyString = Utils.keypointsToJson(keypoints_obj);
				desString = Utils.matToJson(descriptores_obj);
				Objeto obj = datasource.createObjeto(editNombre.getText()
						.toString(), keyString, desString);
				id = obj.getId();
				Log.w("Tag", " Size "+listaKP_obj.length+ "\n" + keyString);
				descriptores_obj.release();
				keypoints_obj.release();
			}else{			
				FindFeatures2(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr(),
						descriptores_obj.getNativeObjAddr());
				desString = Utils.matToJson(descriptores_obj);
				Objeto obj = datasource.createObjeto(editNombre.getText()
						.toString(), "", desString);
				id = obj.getId();
				descriptores_obj.release();
			}

		} else if (buscandoObjeto) {
			final int viewMode = mViewMode;
			switch (viewMode) {
			case VIEW_MODE_SURF_DETECTION:
				// input frame has RGBA format
				mRgba = inputFrame.rgba();
				mGray = inputFrame.gray();
				Objeto obj2 = datasource.getObjeto(id);
				if (CBKeyPoints.isChecked()){
					
					//for (int i=1;i<4;i++){						
						//obj2 = datasource.getObjeto(i);
						descriptores_obj = Utils.matFromJson(obj2.getDescriptores());
						keypoints_obj = Utils.keypointsFromJson(obj2
								.getKeypoints());
						
						encontrado = FindObject(mGray.getNativeObjAddr(),
								mRgba.getNativeObjAddr(), keypoints_obj.toArray(),
								descriptores_obj.getNativeObjAddr());
						
						Log.w("Tag",obj2.getKeypoints());
						
						descriptores_obj.release();
						keypoints_obj.release();
					//}
					
				}else{
					descriptores_obj = Utils.matFromJson(obj2.getDescriptores());
	
					Log.w("Estrella",
							"Id= " + obj2.getId() + " col= " + descriptores_obj.cols()
									+ " rows= " + descriptores_obj.rows());
	
					encontrado = FindObject2(mGray.getNativeObjAddr(),
							mRgba.getNativeObjAddr(), descriptores_obj.getNativeObjAddr());
				}
				break;
			}
		} else
			mRgba = inputFrame.rgba();
		if (encontrado)
			buscandoObjeto = false;
		return mRgba;
	}

	@Override
	public void onCameraViewStopped() {
		// TODO Auto-generated method stub
		mGray.release();
		mRgba.release();
		descriptores_obj.release();
		keypoints_obj.release();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			Log.i(TAG, "called MainActivity");
			Intent myIntent = new Intent(Reconocimiento.this,
					MainActivity.class);
			finish();
			startActivity(myIntent);
			return true;

		}

		return super.onKeyDown(keyCode, event);
	}

	public native KeyPoint[] FindFeatures(long matAddrGr, long matAddrRgba,
			long matAddrDescriptores);

	public native boolean FindObject(long matAddrGr, long matAddrRgba,
			KeyPoint[] matAddrKeypoints, long matAddrDescriptores);
	
	public native boolean FindFeatures2(long matAddrGr, long matAddrRgba,
			long matAddrDescriptores);

	public native boolean FindObject2(long matAddrGr, long matAddrRgba,
			long matAddrDescriptores);
	
}
