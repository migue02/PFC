package com.example.mipatternrecognition;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.KeyPoint;

import pfc.bd.ObjetoDataSource;
import pfc.obj.Objeto;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


public class ReconocimientoObjeto extends Activity implements CvCameraViewListener2 {
	
	private static final String TAG = "ReconocimientoObjeto::Activity";
	
	private CameraBridgeViewBase mOpenCvCameraView;
	private boolean buscandoObjeto;
	private boolean patronAdquirido = false;
	private boolean encontrado = false;
	private Mat mGray;
	private Mat mRgba;
	private ObjetoDataSource datasource;
	private MatOfKeyPoint keypoints_obj = new MatOfKeyPoint();
	private Mat descriptores_obj = new Mat();
	private KeyPoint[] listaKP_obj;
	
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
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_reconocimiento_objeto);
		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.surface_view);
		mOpenCvCameraView.setCvCameraViewListener(this);
		datasource = new ObjetoDataSource(this);
		datasource.open();

	}
	
	public void onCancelarClick(View v){
		Intent myIntent = new Intent(ReconocimientoObjeto.this,
				Reconocimiento.class);
		finish();
		startActivity(myIntent);
	}
	
	public void onCapturarClick(View v){
		patronAdquirido = true;
		listaKP_obj = FindFeatures(mGray.getNativeObjAddr(),
				mRgba.getNativeObjAddr(), descriptores_obj.getNativeObjAddr());
		final Dialog dialog = new Dialog(ReconocimientoObjeto.this);
		dialog.setContentView(R.layout.activity_dialog_objeto);
		dialog.setTitle("¿Desea guardar este objeto?");

		// set the custom dialog components - image and button
		// convert to bitmap:
		Bitmap bm = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(),Bitmap.Config.ARGB_8888);
        org.opencv.android.Utils.matToBitmap(mRgba, bm);
		ImageView image = (ImageView) dialog.findViewById(R.id.imageObjeto);
		image.setImageBitmap(bm);
		
		final EditText edtNombre = (EditText) findViewById(R.id.edtNombre);
		
		Button btnAceptar = (Button) dialog.findViewById(R.id.btnAceptar);
		// if button is clicked, close the custom dialog
		btnAceptar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				patronAdquirido = false;
				String keyString, desString;
				
				
				keypoints_obj = new MatOfKeyPoint(listaKP_obj);
				keyString = Utils.keypointsToJson(keypoints_obj);
				desString = Utils.matToJson(descriptores_obj);
				Objeto obj = datasource.createObjeto("Objeto"
						.toString(), keyString, desString);
				//id = obj.getId();
				Log.w("Tag", " Size "+listaKP_obj.length+ "\n" + keyString);
				descriptores_obj.release();
				keypoints_obj.release();
				dialog.dismiss();
			}
		});
		
		Button btnCancelar = (Button) dialog.findViewById(R.id.btnCancelar);
		// if button is clicked, close the custom dialog
		btnCancelar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				descriptores_obj.release();
				keypoints_obj.release();
				patronAdquirido = true;
			}
		});		
		dialog.show();
	}
	
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		if (patronAdquirido) {
			mRgba = inputFrame.rgba();
			mGray = inputFrame.gray();
			
			
 
			
		} else if (buscandoObjeto) {
			mRgba = inputFrame.rgba();
			mGray = inputFrame.gray();
			/*Objeto obj2 = datasource.getObjeto(id);
				
			for (int i=1;i<NumObjetos;i++){						
				obj2 = datasource.getObjeto(i);
				descriptores_obj = Utils.matFromJson(obj2.getDescriptores());
				keypoints_obj = Utils.keypointsFromJson(obj2
						.getKeypoints());
				
				encontrado = FindObject(mGray.getNativeObjAddr(),
						mRgba.getNativeObjAddr(), keypoints_obj.toArray(),
						descriptores_obj.getNativeObjAddr());
				
				Log.w("Tag",obj2.getKeypoints());
				
				descriptores_obj.release();
				keypoints_obj.release();
			}*/
		} else
			mRgba = inputFrame.rgba();
		if (encontrado)
			buscandoObjeto = false;
		return mRgba;
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

	@Override
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
			Intent myIntent = new Intent(ReconocimientoObjeto.this,
					Reconocimiento.class);
			finish();
			startActivity(myIntent);
			return true;

		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.reconocimiento_objeto, menu);
		return true;
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
