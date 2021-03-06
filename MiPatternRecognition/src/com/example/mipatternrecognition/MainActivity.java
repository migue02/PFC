package com.example.mipatternrecognition;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import pfc.bd.AlumnoDataSource;
import pfc.bd.EjercicioDataSource;
import pfc.bd.MySQLiteHelper;
import pfc.bd.ObjetoDataSource;
import pfc.bd.ResultadoDataSource;
import pfc.bd.SerieEjerciciosDataSource;
import pfc.obj.Alumno;
import pfc.obj.Ejercicio;
import pfc.obj.Objeto;
import pfc.obj.Resultado;
import pfc.obj.SerieEjercicios;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends Activity {

	private static final String TAG = "Reconocimiento::MainActivity";
	private ObjetoDataSource datasourceObjeto;
	private EjercicioDataSource datasourceEjercicio;
	private SerieEjerciciosDataSource datasourceSerieEjercicios;
	//private ResultadoDataSource datasourceResultado;
	//private AlumnoDataSource datasourceAlumno;
	private SerieEjercicios serie;
	
	// Buttons
	private Button comenzarRec;
	private Button btn1;
	private ListView lv;

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

		// -------
		// Objetos
		// -------
		datasourceObjeto = new ObjetoDataSource(this);
		datasourceObjeto.open();
		List<Objeto> lista_objectos = datasourceObjeto.getAllObjetos();
		ArrayAdapter<Objeto> adapterObjetos = new ArrayAdapter<Objeto>(this,
				android.R.layout.simple_list_item_1, lista_objectos);
		lv = (ListView)findViewById(R.id.listaObjetos);
		lv.setAdapter(adapterObjetos);
		
		// ---------
		// Ejercicio
		// ---------
		datasourceEjercicio = new EjercicioDataSource(this);
		datasourceEjercicio.open();
		//datasourceEjercicio.dropTableEjercicios();
		
		//final Ejercicio id1 = datasourceEjercicio.createEjercicio("Pelota y telefono", 
		//		new ArrayList<Integer>(){{add(1);add(2);add(3);}},"Pelota y telefono namas",3.5);
		//final Ejercicio id2 = datasourceEjercicio.createEjercicio("Pelota y raqueta", 
		//		new ArrayList<Integer>(){{add(2);add(3);add(4);}},"Pelota y raqueta namas",5);
		//final Ejercicio id3 = datasourceEjercicio.createEjercicio("Pito y tipo", 
		//		new ArrayList<Integer>(){{add(0);add(1);add(2);}},"Pito y tipo namas",1.5);
		
		List<Ejercicio> lista_ejercicios = datasourceEjercicio.getAllEjercicios();
		
		ArrayAdapter<Ejercicio> adapterEjercicios = new ArrayAdapter<Ejercicio>(this,
				android.R.layout.simple_list_item_1, lista_ejercicios);
		
		lv = (ListView)findViewById(R.id.listaEjercicios);
		lv.setAdapter(adapterEjercicios);
		
		// ---------------
		// SerieEjercicios
		// ---------------
		datasourceSerieEjercicios = new SerieEjerciciosDataSource(this);
		datasourceSerieEjercicios.open();
		//datasourceSerieEjercicios.dropTableSerieEjercicios();
		
		//serie = datasourceSerieEjercicios.createSerieEjercicios("Pelotas", 
		//		new ArrayList<Integer>(){{add(id1.getIdEjercicio());add(id2.getIdEjercicio());add(id3.getIdEjercicio());}}, 
		//		0, new Date());
		//datasourceSerieEjercicios.createSerieEjercicios("Bolígrafos", new ArrayList<Integer>(){{add(2);add(3);add(4);}});
		//datasourceSerieEjercicios.createSerieEjercicios("Pitos", new ArrayList<Integer>(){{add(0);add(1);add(2);}});
		
		
		List<SerieEjercicios> lista_series = datasourceSerieEjercicios.getAllSeriesEjercicios();
		serie=lista_series.get(0);
		ArrayAdapter<SerieEjercicios> adapterSeries = new ArrayAdapter<SerieEjercicios>(this,
				android.R.layout.simple_list_item_1, lista_series);
		
		lv = (ListView)findViewById(R.id.listaAlumnos);
		lv.setAdapter(adapterSeries);
		
		
		// ------
		// Alumno
		// ------
		/*datasourceAlumno = new AlumnoDataSource(this);
		datasourceAlumno.open();
		
		//Alumno a = datasourceAlumno.createAlumno(Utils.nombreRandom(),
		//		Utils.apellidoRandom() + " " + Utils.apellidoRandom(), Utils.fechaRandom(),
		//		Utils.sexoRandom(), "");*/

		// ---------
		// Resultado
		// ---------
		/*datasourceResultado = new ResultadoDataSource(this);
		datasourceResultado.open();
		//datasourceResultado.createResultado(1,1,10,5,new Date(),10,0,0);
		//datasourceResultado.createResultado(1,1,5,3,new Date(),10,0,0);
		//datasourceResultado.createResultado(1,1,1,1,new Date(),10,0,0);
		
		List<Resultado> lista_resultados = datasourceResultado.getAllResultados();
		
		ArrayAdapter<Resultado> adapterResultados = new ArrayAdapter<Resultado>(this,
				android.R.layout.simple_list_item_1, lista_resultados);
		
		lv = (ListView)findViewById(R.id.listaEjercicios);
		lv.setAdapter(adapterResultados);
		
		
		List<Resultado> lista_ejercicios = datasourceResultado.getResultadosAlumno(datasourceAlumno.getAlumnos(1));
		
		ArrayAdapter<Resultado> adapterResultado = new ArrayAdapter<Resultado>(this,
				android.R.layout.simple_list_item_1, lista_ejercicios);
		
		lv = (ListView)findViewById(R.id.listaAlumnos);
		lv.setAdapter(adapterResultado);
		 */


		comenzarRec = (Button) findViewById(R.id.btnComenzar);
		btn1 = (Button) findViewById(R.id.button1);
		
		/* Listener Captura Objeto Button */
		comenzarRec.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.i(TAG, "called ReconocimientoActivity");
				Intent myIntent = new Intent(MainActivity.this,
						ReconocimientoObjeto.class);
				finish();
				startActivity(myIntent);

			}
		});

		btn1.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (datasourceSerieEjercicios.actualizaDuracion(serie))
				toast(serie);
				//datasourceEjercicio.getDuracion(1);
				//datasourceObjeto.eliminaTodosObjetos();
				/*Intent myIntent = new Intent(MainActivity.this,
						Alumnos.class);
				finish();
				startActivity(myIntent);*/
			}
		});

	}

	@Override
	protected void onResume() {
		datasourceObjeto.open();
		//datasourceEjercicio.open();
		//datasourceSerieEjercicios.open();
		//datasourceResultado.open();
		super.onResume();
	}

	public void toast(Objeto obj) {
		Toast.makeText(this, "Id =" + obj.getId(), Toast.LENGTH_SHORT).show();
	}
	
	public void toast(SerieEjercicios serie) {
		Toast.makeText(this, "Duracion" + serie.getDuracion(), Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onPause() {
		datasourceObjeto.close();
		//datasourceEjercicio.close();
		//datasourceSerieEjercicios.close();
		//datasourceResultado.close();
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
