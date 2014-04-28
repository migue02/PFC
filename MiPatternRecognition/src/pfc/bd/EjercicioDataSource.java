package pfc.bd;

import java.util.ArrayList;
import java.util.List;

import pfc.obj.Ejercicio;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class EjercicioDataSource {
	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { MySQLiteHelper.COLUMN_EJERCICIO_ID,
			MySQLiteHelper.COLUMN_EJERCICIO_NOMBRE,
			MySQLiteHelper.COLUMN_EJERCICIO_OBJETOS};

	public EjercicioDataSource(Context context) {
		Log.w("Creando...", "Creando bd");
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
		database.execSQL(dbHelper.getSqlCreateEjercicio());
	}

	public void close() {
		dbHelper.close();
	}

	public Ejercicio createEjercicio(String nombre, ArrayList<Integer> objetos) {

		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_EJERCICIO_NOMBRE, nombre);
		values.put(MySQLiteHelper.COLUMN_EJERCICIO_OBJETOS, com.example.mipatternrecognition.Utils.ArrayListToJson(objetos));

		long insertId = database.insert(MySQLiteHelper.TABLE_EJERCICIO, null,
				values); // Se inserta un ejercicio y se deuelve su id
		Log.w("Creando...", "Ejercicio " + nombre + " creado con id " + insertId);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_EJERCICIO,

		allColumns, MySQLiteHelper.COLUMN_EJERCICIO_ID + " = " + insertId, null,
				null, null, null);// devuelve el ejercicio que se acaba de insertar

		cursor.moveToFirst();
		Ejercicio newEjercicio = cursorToEjercicio(cursor);
		cursor.close();
		return newEjercicio;
	}

	public List<Ejercicio> getAllEjercicios() {
		List<Ejercicio> ejercicios = new ArrayList<Ejercicio>();
		Log.w("Obteniendo...", "Obteniendo todos los ejercicios...");
		Cursor cursor = database.query(MySQLiteHelper.TABLE_EJERCICIO, allColumns,
				null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Ejercicio ejercicio = cursorToEjercicio(cursor);
			ejercicios.add(ejercicio);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return ejercicios;
	}

	private Ejercicio cursorToEjercicio(Cursor cursor) {
		return new Ejercicio(cursor.getInt(0),cursor.getString(1),cursor.getString(2));
	}

}
