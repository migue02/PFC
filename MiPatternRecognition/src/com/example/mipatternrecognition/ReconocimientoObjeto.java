package com.example.mipatternrecognition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
	private EditText edtNombre;
	private ImageView image;
	private TextToSpeech ttobj;
	private LinearLayout layoutBotones;
	private SurfaceView surfaceView;
	
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
		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.surfaceView);
		mOpenCvCameraView.setCvCameraViewListener(this);
		datasource = new ObjetoDataSource(this);
		datasource.open();
		
		/*layoutBotones = (LinearLayout) findViewById(R.id.layoutBotones);
		surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
		LinearLayout.LayoutParams params = 
				new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		
		params.setMargins(0, surfaceView.getHeight() - layoutBotones.getHeight(), 0, 0);
		
		layoutBotones.setLayoutParams(params);*/

		//objetos = datasource.getAllObjetos();
		//rellenar();
		
		ttobj=new TextToSpeech(getApplicationContext(), 
	      new TextToSpeech.OnInitListener() {
	      @Override
	      public void onInit(int status) {
	         if(status != TextToSpeech.ERROR){
	             ttobj.setLanguage(Locale.UK);
	            }				
	         }
	      });
		
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
			
			Mat tempMat=Utils.matFromJson(objetos.get(i).getDescriptores());
            matsDescriptores.add(tempMat);
			
            MatOfKeyPoint tempMatKeyPoint=Utils.keypointsFromJson(objetos.get(i).getKeypoints());
    		matsKeyPoints.add(tempMatKeyPoint);
		}
		
        long[] tempAddrDesc = new long[matsDescriptores.size()]; 
        long[] tempAddrKeyP = new long[matsKeyPoints.size()]; 
        int[] cols = new int[matsKeyPoints.size()];
        int[] rows = new int[matsKeyPoints.size()];
        for (int i=0;i<matsDescriptores.size();i++)
        {
            Mat tempaddr1=matsDescriptores.get(i);
            tempAddrDesc[i]= tempaddr1.getNativeObjAddr();
            
            MatOfKeyPoint tempaddr2=matsKeyPoints.get(i);
            tempAddrKeyP[i]= tempaddr2.getNativeObjAddr();
            
            cols[i] = objetos.get(i).getCols();
            rows[i] = objetos.get(i).getRows();
        }

        RellenarObjetos(tempAddrDesc, tempAddrKeyP, cols, rows);
	}
	
	public void onReconocerClick(View v){
		buscandoObjeto=true;
		nObjeto=-1;
	}
	
	public void onCancelarClick(View v){
		if (!buscandoObjeto){
			Intent myIntent = new Intent(ReconocimientoObjeto.this,
					MainActivity.class);
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
			image = (ImageView) dialog.findViewById(R.id.imageObjeto);
			image.setImageBitmap(bm);			
			
			edtNombre = (EditText) dialog.findViewById(R.id.edtNombre);
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
	
	public class MyRunnable implements Runnable {
		public int nObjetoActual;
		public MyRunnable(int nObjetoActual){
			this.nObjetoActual=nObjetoActual;
		}
		@Override
		  public void run() {
			nObjeto=FindObjects(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr(),nObjetoActual);
		  }
		} 
	
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		if (!buscandoObjeto || (buscandoObjeto && nObjeto == -1)){
			mRgba = inputFrame.rgba();
			mGray = inputFrame.gray();

			if (buscandoObjeto) {
				if (InicializaEscenario(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr())){
					ExecutorService executor = Executors.newFixedThreadPool(objetos.size());
			    	for (int i = 0; i < objetos.size() || nObjeto != -1; i++) {
		    	      Runnable findObjectThread = new MyRunnable(i);
		    	      executor.execute(findObjectThread);
		    	    }
			    	// This will make the executor accept no new threads
			        // and finish all existing threads in the queue
			        executor.shutdown();
			        // Wait until all threads are finish
			        while (!executor.isTerminated()){}
			        LiberaEscenario();
					if (nObjeto!=-1){
						nombre=objetos.get(nObjeto).getNombre();
						ReconocimientoObjeto.this.runOnUiThread(new Runnable() {
						    public void run() {
						    	ttobj.speak(nombre, TextToSpeech.QUEUE_FLUSH, null);
						    	Toast.makeText(getApplicationContext(), "Encontrado el objeto "+nombre, Toast.LENGTH_SHORT).show();
						    }
						});
						nObjeto=-1;
					}
				}
			}
		}
		return mRgba;
	}
	
	@Override
	public void onPause() {
		datasource.close();
		LiberaObjetos();
		if(ttobj !=null){
			ttobj.stop();
			ttobj.shutdown();
		}
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}
	
	@Override
	public void onResume() {
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);
		datasource.open();
		objetos = datasource.getAllObjetos();
		rellenar();
		super.onResume();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
		colsArray = null;
		rowsArray = null;
		matsDescriptores = null;
		matsKeyPoints = null;
		edtNombre = null;
		image=null;
		LiberaObjetos();
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
			if (!buscandoObjeto){
				Intent myIntent = new Intent(ReconocimientoObjeto.this,
						MainActivity.class);
				finish();
				startActivity(myIntent);
				return true;
			}else{
				buscandoObjeto=false;
				nObjeto=-1;
				return false;
			}
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
	
	public native int FindObjects(long matAddrGray, long matAddrRgba, int i);
	
	public native boolean InicializaEscenario(long matAddrGray, long matAddrRgba);
	
	public native int LiberaEscenario();
	
	public native int RellenarObjetos(long[] descriptors, long[] keyPoints, int[] cols, int[] rows);
	
	public native int LiberaObjetos();

}
