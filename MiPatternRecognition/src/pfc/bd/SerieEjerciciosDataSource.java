package pfc.bd;

import java.util.ArrayList;
import java.util.List;

import com.example.mipatternrecognition.Utils;

import pfc.obj.SerieEjercicios;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class SerieEjerciciosDataSource {

	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_ID,
			MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_NOMBRE,
			MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_IDEJERCICIOS };

	public SerieEjerciciosDataSource(Context context) {
		Log.w("Creando...", "Creando bd");
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
		database.execSQL(dbHelper.getSqlCreateSerieEjercicios());
		database.execSQL(dbHelper.sqlenableForeingKeys);
	}

	public void close() {
		dbHelper.close();
	}

	public SerieEjercicios createSerieEjercicios(SerieEjercicios serieEjercicios) {

		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_NOMBRE, serieEjercicios.getNombre());
		values.put(MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_IDEJERCICIOS,
				com.example.mipatternrecognition.Utils
						.ArrayListToJson(serieEjercicios.getEjercicios()));

		serieEjercicios.setIdSerie((int) database.insert(MySQLiteHelper.TABLE_SERIE_EJERCICIOS,
				null, values));
		return serieEjercicios;
	}
	
	public SerieEjercicios createSerieEjercicios(String nombre,
			ArrayList<Integer> ejercicios) {

		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_NOMBRE, nombre);
		values.put(MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_IDEJERCICIOS,
				com.example.mipatternrecognition.Utils
						.ArrayListToJson(ejercicios));

		long insertId = database.insert(MySQLiteHelper.TABLE_SERIE_EJERCICIOS,
				null, values); // Se inserta un ejercicio y se deuelve su id
		Log.w("Creando...", "Serie ejercicios " + nombre + " creada con id "
				+ insertId);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_SERIE_EJERCICIOS,

		allColumns, MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_ID + " = "
				+ insertId, null, null, null, null);// devuelve el ejercicio que
													// se acaba de
													// insertar

		cursor.moveToFirst();
		SerieEjercicios newSerieEjercicios = cursorToSerieEjercicios(cursor);
		cursor.close();
		return newSerieEjercicios;
	}
	
	public boolean modificaSerieEjercicios(SerieEjercicios serieEjercicios) {

		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_NOMBRE, serieEjercicios.getNombre());
		values.put(MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_IDEJERCICIOS,
				com.example.mipatternrecognition.Utils
						.ArrayListToJson(serieEjercicios.getEjercicios()));

		return database.update(MySQLiteHelper.TABLE_SERIE_EJERCICIOS, values,
				MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_ID + " = " + serieEjercicios.getIdSerie(), null) > 0;
	}
	

	public boolean modificaSerieEjercicios(int id, String nombre,
			ArrayList<Integer> ejercicios) {

		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_NOMBRE, nombre);
		values.put(MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_IDEJERCICIOS,
				com.example.mipatternrecognition.Utils
						.ArrayListToJson(ejercicios));

		return database.update(MySQLiteHelper.TABLE_SERIE_EJERCICIOS, values,
				MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_ID + " = " + id, null) > 0;
	}

	public boolean eliminarSerieEjercicios(int id) {
		return database.delete(MySQLiteHelper.TABLE_SERIE_EJERCICIOS,
				MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_ID + " = " + id, null) > 0;
	}

	public boolean eliminarTodasSeriesEjercicios() {
		return database.delete(MySQLiteHelper.TABLE_SERIE_EJERCICIOS, null,
				null) > 0;
	}
	
	public void dropTableSerieEjericios() {
		database.execSQL(dbHelper.getSqlDropSerieEjercicios());
		database.execSQL(dbHelper.getSqlCreateObjeto());
	}

	public List<SerieEjercicios> getAllSeriesEjercicios() {
		Log.w("Obteniendo...", "Obteniendo todas las series de ejercicios...");
		Cursor cursor = database.query(MySQLiteHelper.TABLE_SERIE_EJERCICIOS,
				allColumns, null, null, null, null, null);

		List<SerieEjercicios> series = new ArrayList<SerieEjercicios>();

		if (cursor != null && cursor.getCount() > 0){
			
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				SerieEjercicios serie = cursorToSerieEjercicios(cursor);
				series.add(serie);
				cursor.moveToNext();
			}
			cursor.close();
		}
		return series;
	}

	public SerieEjercicios getSerieEjercicios(int id) {
		Cursor cursor = database.query(MySQLiteHelper.TABLE_SERIE_EJERCICIOS,
				allColumns, MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_ID + " = "
						+ id, null, null, null, null);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			SerieEjercicios serie = cursorToSerieEjercicios(cursor);
			cursor.close();
			return serie;
		}
		return null;
	}

	private SerieEjercicios cursorToSerieEjercicios(Cursor cursor) {
		return new SerieEjercicios(cursor.getInt(0), cursor.getString(1),
				Utils.ArrayListFromJson(cursor.getString(2)));
	}

}
