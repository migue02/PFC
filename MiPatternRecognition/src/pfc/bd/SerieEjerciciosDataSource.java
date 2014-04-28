package pfc.bd;

import java.util.ArrayList;
import java.util.List;

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
	}

	public void close() {
		dbHelper.close();
	}

	public SerieEjercicios createSerieEjercicios(String nombre,
			ArrayList<Integer> ejercicios) {

		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_NOMBRE, nombre);
		values.put(MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_IDEJERCICIOS,
				com.example.mipatternrecognition.Utils
						.ArrayListToJson(ejercicios));

		long insertId = database.insert(MySQLiteHelper.TABLE_SERIE_EJERCICIOS, null,
				values); // Se inserta un ejercicio y se deuelve su id
		Log.w("Creando...", "Serie ejercicios " + nombre + " creada con id "
				+ insertId);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_SERIE_EJERCICIOS,

		allColumns, MySQLiteHelper.COLUMN_SERIE_EJERCICIOS_ID + " = " + insertId,
				null, null, null, null);// devuelve el ejercicio que se acaba de
										// insertar

		cursor.moveToFirst();
		SerieEjercicios newSerieEjercicios = cursorToSerieEjercicios(cursor);
		cursor.close();
		return newSerieEjercicios;
	}

	public List<SerieEjercicios> getAllSeriesEjercicios() {
		List<SerieEjercicios> series = new ArrayList<SerieEjercicios>();
		Log.w("Obteniendo...", "Obteniendo todas las series de ejercicios...");
		Cursor cursor = database.query(MySQLiteHelper.TABLE_SERIE_EJERCICIOS,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			SerieEjercicios serie = cursorToSerieEjercicios(cursor);
			series.add(serie);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return series;
	}

	private SerieEjercicios cursorToSerieEjercicios(Cursor cursor) {
		return new SerieEjercicios(cursor.getInt(0), cursor.getString(1),
				cursor.getString(2));
	}

}
