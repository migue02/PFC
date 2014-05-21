package com.example.mipatternrecognition;

import java.util.ArrayList;
import java.util.Arrays;

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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class ReconocimientoObjeto extends Activity implements CvCameraViewListener2 {
	
	private static final String TAG = "ReconocimientoObjeto::Activity";
	
	private CameraBridgeViewBase mOpenCvCameraView;
	private boolean buscandoObjeto = false;
	private int nObjeto = -1;
	private Mat mGray;
	private Mat mRgba;
	private Mat aux;
	private ObjetoDataSource datasource;
	private MatOfKeyPoint keypoints_obj = new MatOfKeyPoint();
	private Mat descriptores_obj = new Mat();
	private KeyPoint[] listaKP_obj;
	private ArrayList<Objeto> objetos;
	private ArrayList<Mat> matsDescriptores; 
	private ArrayList<MatOfKeyPoint> matsKeyPoints;
	private int[] colsArray;
	private int[] rowsArray;
	private String nombre;
	
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
		
		objetos = datasource.getAllObjetos();
		rellenar();
		
	}
	
	private void rellenar(){
		int inicio=0;
		if (colsArray == null){
			colsArray = new int[objetos.size()];
			rowsArray = new int[objetos.size()];
			matsDescriptores = new ArrayList<Mat>(objetos.size());
			matsKeyPoints = new ArrayList<MatOfKeyPoint>(objetos.size());
		}else{
			inicio=objetos.size()-1;
			colsArray = Arrays.copyOf(colsArray, objetos.size());
			rowsArray = Arrays.copyOf(rowsArray, objetos.size());
		}
		for(int i=inicio;i<objetos.size(); i++){
			colsArray[i] = objetos.get(i).getCols();
			rowsArray[i] = objetos.get(i).getRows();
			
			Mat tempaddr1=Utils.matFromJson(objetos.get(i).getDescriptores());
            matsDescriptores.add(tempaddr1);
			
            MatOfKeyPoint tempaddr2=Utils.keypointsFromJson(objetos.get(i).getKeypoints());
    		matsKeyPoints.add(tempaddr2);
		}
		
        long[] tempobjadr1 = new long[matsDescriptores.size()]; 
        long[] tempobjadr2 = new long[matsKeyPoints.size()]; 
        int[] cols = new int[matsKeyPoints.size()];
        int[] rows = new int[matsKeyPoints.size()];
        for (int i=0;i<matsDescriptores.size();i++)
        {
            Mat tempaddr1=matsDescriptores.get(i);
            tempobjadr1[i]= tempaddr1.getNativeObjAddr();
            
            MatOfKeyPoint tempaddr2=matsKeyPoints.get(i);
            tempobjadr2[i]= tempaddr2.getNativeObjAddr();
            
            cols[i] = objetos.get(i).getCols();
            rows[i] = objetos.get(i).getRows();
        }

		TrainDescriptors(tempobjadr1, tempobjadr2, cols, rows);
	}
	
	public void onReconocerClick(View v){
		buscandoObjeto=true;
		nObjeto=-1;
	}
	
	public void onCancelarClick(View v){
		if (!buscandoObjeto){
			Intent myIntent = new Intent(ReconocimientoObjeto.this,
					Reconocimiento.class);
			finish();
			startActivity(myIntent);
		}else{
			buscandoObjeto=false;
			nObjeto=-1;
		}
	}
	
	public void onCapturarClick(View v){
		aux = mRgba.clone();
		FindFeatures(mGray.getNativeObjAddr(),
				aux.getNativeObjAddr(), descriptores_obj.getNativeObjAddr(), keypoints_obj.getNativeObjAddr());
		if (!keypoints_obj.empty() || !descriptores_obj.empty()) {
			final Dialog dialog = new Dialog(ReconocimientoObjeto.this);
			dialog.setContentView(R.layout.activity_dialog_objeto);
			dialog.setTitle("¿Desea guardar este objeto?");
	
			// set the custom dialog components - image and button
			// convert to bitmap:
			Bitmap bm = Bitmap.createBitmap(aux.cols(), aux.rows(),Bitmap.Config.ARGB_8888);
	        org.opencv.android.Utils.matToBitmap(aux, bm);
			ImageView image = (ImageView) dialog.findViewById(R.id.imageObjeto);
			image.setImageBitmap(bm);			
			
			final EditText edtNombre = (EditText) dialog.findViewById(R.id.edtNombre);
			
			Button btnAceptar = (Button) dialog.findViewById(R.id.btnAceptar);
			// if button is clicked, close the custom dialog
			btnAceptar.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					String keyString, desString;				
					//keypoints_obj = new MatOfKeyPoint(listaKP_obj);
					keyString = Utils.keypointsToJson(keypoints_obj);
					desString = Utils.matToJson(descriptores_obj);
					Objeto obj=datasource.createObjeto(edtNombre.getText().toString(), keyString, desString, aux.cols(), aux.rows());
					objetos.add(obj);
					rellenar();
					//id = obj.getId();
					descriptores_obj.release();
					keypoints_obj.release();
					dialog.dismiss();
					aux.release();
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
					objetos.clear();
					aux.release();
				}
			});		
			dialog.show();
		}else
			Toast.makeText(this, "Es necesario capturar de nuevo el objeto", Toast.LENGTH_SHORT).show();
	}
	
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		if (!buscandoObjeto || (buscandoObjeto && nObjeto == -1)){
			mRgba = inputFrame.rgba();
			mGray = inputFrame.gray();

			if (buscandoObjeto) {
					
				FindObjects(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr());
				if (nObjeto!=-1){
					nombre=objetos.get(nObjeto).getNombre();
					ReconocimientoObjeto.this.runOnUiThread(new Runnable() {
					    public void run() {
					    	Toast.makeText(getApplicationContext(), "Encontrado el objeto "+nombre, Toast.LENGTH_LONG).show();
					    }
					});
				}
			}
		}
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
		colsArray = null;
		rowsArray = null;
		//matsDescriptores = null;
		//matsKeyPoints = null;
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		mRgba = new Mat(height, width, CvType.CV_8UC4);
		aux = new Mat(height, width, CvType.CV_8UC4);
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
					MainActivity.class);
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
	
	public native void FindFeatures(long matAddrGr, long matAddrRgba,
			long matAddrDescriptores, long matAddrKeyPoints);
	public native int FindObjects(long matAddrGray, long matAddrRgba);
	
	public native int TrainDescriptors(long[] descriptors, long[] keyPoints, int[] cols, int[] rows);
	
//	public native int FindObjects(long matAddrGray, long matAddrRgba, long[] matsKeyPoints,
//			long[] matsDescriptores, int[] colsArray, int[] rowsArray, boolean esPrimeraVez);
	
//	public native void/*KeyPoint[]*/ FindFeatures(long matAddrGr, long matAddrRgba,
//			long matAddrDescriptores, long matAddrKeyPoints);
//
//	public native boolean FindObject(long matAddrGr, long matAddrRgba,
//			long matAddrKeyPoints/*KeyPoint[] matAddrKeypoints*/, long matAddrDescriptores, int cols, int rows);
//	
//	public native boolean FindFeatures2(long matAddrGr, long matAddrRgba,
//			long matAddrDescriptores);
//
//	public native boolean FindObject2(long matAddrGr, long matAddrRgba,
//			long matAddrDescriptores);

}
