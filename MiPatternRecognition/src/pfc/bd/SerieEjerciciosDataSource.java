package pfc.bd;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.mipatternrecognition.Utils;

import pfc.obj.Ejercicio;
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
			MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_IDEJERCICIOS,
			MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_DURACION,
			MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_FECHA_MODIFICACION};
	private Context context;

	public SerieEjerciciosDataSource(Context context) {
		Log.w("Creando...", "Creando bd");
		dbHelper = new MySQLiteHelper(context);
		this.context = context;
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
		database.execSQL(dbHelper.getSqlCreateSerieEjercicios());
		database.execSQL(dbHelper.sqlenableForeingKeys);
	}

	public void close() {
		dbHelper.close();
	}

	public void dropTableSerieEjercicios() {
		Log.w("Deleting...", "Borrando tabla serieEjercicios");
		database.execSQL(dbHelper.getSqlDropSerieEjercicios());
		database.execSQL(dbHelper.getSqlCreateSerieEjercicios());
	}

	
	public SerieEjercicios createSerieEjercicios(SerieEjercicios serieEjercicios) {

		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_NOMBRE, serieEjercicios.getNombre());
		values.put(MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_IDEJERCICIOS,
				com.example.mipatternrecognition.Utils
						.ArrayListToJson(serieEjercicios.getEjercicios()));
		values.put(MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_DURACION,serieEjercicios.getDuracion());
		values.put(MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_FECHA_MODIFICACION,
				new SimpleDateFormat("yyyy-MM-dd").format(serieEjercicios.getFecha_modificacion_AsDate()));
		serieEjercicios.setIdSerie((int) database.insert(MySQLiteHelper.TABLE_SERIE_EJERCICIOS,
				null, values));
		return serieEjercicios;
	}
	
	public SerieEjercicios createSerieEjercicios(String nombre,
			ArrayList<Integer> ejercicios, double duracion, Date fecha_modificacion) {

		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_NOMBRE, nombre);
		values.put(MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_IDEJERCICIOS,
				com.example.mipatternrecognition.Utils
						.ArrayListToJson(ejercicios));
		values.put(MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_DURACION,duracion);
		values.put(MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_FECHA_MODIFICACION,
				new SimpleDateFormat("yyyy-MM-dd").format(fecha_modificacion));

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
		values.put(MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_DURACION,serieEjercicios.getDuracion());
		values.put(MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_FECHA_MODIFICACION,
				new SimpleDateFormat("yyyy-MM-dd").format(serieEjercicios.getFecha_modificacion_AsDate()));

		return database.update(MySQLiteHelper.TABLE_SERIE_EJERCICIOS, values,
				MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_ID + " = " + serieEjercicios.getIdSerie(), null) > 0;
	}
	

	public boolean modificaSerieEjercicios(int id, String nombre,
			ArrayList<Integer> ejercicios, double duracion, Date fecha_modificacion) {

		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_NOMBRE, nombre);
		values.put(MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_IDEJERCICIOS,
				com.example.mipatternrecognition.Utils
						.ArrayListToJson(ejercicios));
		values.put(MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_DURACION,duracion);
		values.put(MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_FECHA_MODIFICACION,
				new SimpleDateFormat("yyyy-MM-dd").format(fecha_modificacion));

		return database.update(MySQLiteHelper.TABLE_SERIE_EJERCICIOS, values,
				MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_ID + " = " + id, null) > 0;
	}
	
	public boolean actualizaDuracion(SerieEjercicios serie){
		ContentValues values = new ContentValues();
		
		double duracionAnterior = serie.getDuracion();
		EjercicioDataSource ejercicioDS = new EjercicioDataSource(context);
		ejercicioDS.open();
		
		double aux = ejercicioDS.getDuracion(1);

		serie.setDuracion(0);
		
		for(int i=0;i<serie.getEjercicios().size(); i++)
			serie.setDuracion(serie.getDuracion() + ejercicioDS.getDuracion(serie.getEjercicios().get(i)));
		
		values.put(MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_DURACION, serie.getDuracion());
		
		boolean modificado = modificaSerieEjercicios(serie);
		
		ejercicioDS.close();
		
		if (!modificado)
			serie.setDuracion(duracionAnterior);
		return modificado;
	}

	public boolean eliminarSerieEjercicios(int id) {
		return database.delete(MySQLiteHelper.TABLE_SERIE_EJERCICIOS,
				MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_ID + " = " + id, null) > 0;
	}

	public boolean eliminarTodasSeriesEjercicios() {
		return database.delete(MySQLiteHelper.TABLE_SERIE_EJERCICIOS, null,
				null) > 0;
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
		SerieEjercicios serie = new SerieEjercicios(cursor.getInt(0), cursor.getString(1),
				Utils.ArrayListFromJson(cursor.getString(2)),cursor.getDouble(3),new Date());
		try {
			serie.setFecha_modificacion(new SimpleDateFormat("yyyy-MM-dd").parse(cursor
					.getString(3)));
		} catch (ParseException e) {
			Log.e("ERROR_FECHA", "Error al obtener la fecha");
			e.printStackTrace();
		}
		return serie;
	}

}
