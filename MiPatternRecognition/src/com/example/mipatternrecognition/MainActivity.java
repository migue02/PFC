package com.example.mipatternrecognition;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import pfc.bd.MySQLiteHelper;
import pfc.bd.Objeto;
import pfc.bd.ObjetoDataSource;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final String TAG = "Reconocimiento::MainActivity";
	private ObjetoDataSource datasource;
	private ImageView selectedImage;

	// Buttons
	private Button comenzarRec;
	private Button btn1;
	
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

		setContentView(R.layout.activity_main);
		
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);

		datasource = new ObjetoDataSource(this);		
		datasource.open();
		
		
		
		//List<Objeto> lista_objectos = datasource.getAllObjetos();

		comenzarRec = (Button) findViewById(R.id.btnComenzar);
		btn1 = (Button) findViewById(R.id.button1);
		

		/*if (lista_objectos.size() > 0) {
			// make a mat and draw something
			Mat m = Utils.matFromJson(lista_objectos.get(0).getKeypoints());

			// convert to bitmap:
			Bitmap bm = Bitmap.createBitmap(m.cols(), m.rows(),
					Bitmap.Config.ARGB_8888);
			org.opencv.android.Utils.matToBitmap(m, bm);

			// find the imageview and draw it!
			selectedImage.setImageBitmap(bm);
		}*/

		/* Listener Captura Objeto Button */
		comenzarRec.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.i(TAG, "called ReconocimientoActivity");
				Intent myIntent = new Intent(MainActivity.this,
						Reconocimiento.class);
				finish();
				startActivity(myIntent);

			}
		});
		
		/* Listener Captura Objeto Button */
		btn1.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				datasource.deleteTableObjeto();
				
			}
		});

	}

	@Override
	protected void onResume() {
		datasource.open();
		super.onResume();		
	}

	public void toast(Objeto obj){
		Toast.makeText(this, "Id ="+obj.getId() , Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected void onPause() {
		datasource.close();
		super.onPause();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			finish();
			return true;

		}

		return super.onKeyDown(keyCode, event);
	}

}
