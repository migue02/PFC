package com.example.mipatternrecognition;

import java.lang.reflect.Modifier;
import java.util.List;

import pfc.bd.AlumnoDataSource;
import pfc.obj.Alumno;
import pfc.obj.SerieEjercicios;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.mipatternrecognition.Utils;

public class Alumnos extends Activity {

	public AlumnoDataSource datasourceAlumno;
	public Button btnAniadir;
	public Button btnModificar;
	public Button btnBorrar;
	
	public ListView lv;
	public ArrayAdapter<Alumno> adapterSeries;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alumnos);

		datasourceAlumno = new AlumnoDataSource(this);
		datasourceAlumno.open();

		btnAniadir = (Button) findViewById(R.id.buttonAniadir);
		btnModificar = (Button) findViewById(R.id.buttonModificar);
		btnBorrar = (Button) findViewById(R.id.buttonBorrar);
		
		List<Alumno> listaAlumnos = datasourceAlumno.getAllAlumnos();
		
		adapterSeries = new ArrayAdapter<Alumno>(this,
				android.R.layout.simple_list_item_activated_1, listaAlumnos);
		
		lv = (ListView)findViewById(R.id.listViewAlumnos);
		lv.setAdapter(adapterSeries);
		
		btnAniadir.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Alumno a = datasourceAlumno.createAlumno(Utils.nombreRandom(),
						Utils.apellidoRandom() + " " + Utils.apellidoRandom(), Utils.fechaRandom(),
						Utils.sexoRandom(), "");
				adapterSeries.clear();
				adapterSeries.addAll(datasourceAlumno.getAllAlumnos());
				lv.setAdapter(adapterSeries);
			}
		});

		btnBorrar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Alumno a = (Alumno) lv.getSelectedItem();
				datasourceAlumno.modificaAlumno(a.getIdAlumno(),Utils.nombreRandom(),
						Utils.apellidoRandom() + " " + Utils.apellidoRandom(), Utils.fechaRandom(),
						Utils.sexoRandom(), "");
				adapterSeries.clear();
				adapterSeries.addAll(datasourceAlumno.getAllAlumnos());
				lv.setAdapter(adapterSeries);				
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.alumnos, menu);
		return true;
	}
	
	@Override
	protected void onResume() {
		datasourceAlumno.open();
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		datasourceAlumno.close();
		super.onPause();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			Intent myIntent = new Intent(Alumnos.this,
					MainActivity.class);
			finish();
			startActivity(myIntent);
			return true;

		}

		return super.onKeyDown(keyCode, event);
	}
	
}
