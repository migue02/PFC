package pfc.bd;

import java.util.ArrayList;
import java.util.List;

import com.example.mipatternrecognition.Utils;

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
			MySQLiteHelper.COLUMN_EJERCICIO_OBJETOS,
			MySQLiteHelper.COLUMN_EJERCICIO_DESCRIPCION,
			MySQLiteHelper.COLUMN_EJERCICIO_DURACION};

	public EjercicioDataSource(Context context) {
		Log.w("Creando...", "Creando bd");
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
		database.execSQL(dbHelper.getSqlCreateEjercicio());
		database.execSQL(dbHelper.sqlenableForeingKeys);
	}

	public void close() {
		dbHelper.close();
	}
	
	public void dropTableEjercicios() {
		Log.w("Deleting...", "Borrando tabla ejercicio");
		database.execSQL(dbHelper.getSqlDropEjercicio());
		database.execSQL(dbHelper.getSqlCreateEjercicio());
	}

	public Ejercicio createEjercicio(Ejercicio ejercicio) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_EJERCICIO_NOMBRE, ejercicio.getNombre());
		values.put(MySQLiteHelper.COLUMN_EJERCICIO_OBJETOS,
				com.example.mipatternrecognition.Utils.ArrayListToJson(ejercicio.getObjetos()));
		values.put(MySQLiteHelper.COLUMN_EJERCICIO_DESCRIPCION,ejercicio.getDescripcion());
		values.put(MySQLiteHelper.COLUMN_EJERCICIO_DURACION,ejercicio.getDuracion());
		
		ejercicio.setIdEjercicio((int) database.insert(MySQLiteHelper.TABLE_EJERCICIO, null, values));
		return ejercicio;
	}
	
	public Ejercicio createEjercicio(String nombre, ArrayList<Integer> objetos, String descripcion, double duracion) {

		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_EJERCICIO_NOMBRE, nombre);
		values.put(MySQLiteHelper.COLUMN_EJERCICIO_OBJETOS,
				com.example.mipatternrecognition.Utils.ArrayListToJson(objetos));
		values.put(MySQLiteHelper.COLUMN_EJERCICIO_DESCRIPCION, descripcion);
		values.put(MySQLiteHelper.COLUMN_EJERCICIO_DURACION, duracion);
		
		long insertId = database.insert(MySQLiteHelper.TABLE_EJERCICIO, null,
				values); // Se inserta un ejercicio y se deuelve su id
		Cursor cursor = database.query(MySQLiteHelper.TABLE_EJERCICIO,

		allColumns, MySQLiteHelper.COLUMN_EJERCICIO_ID + " = " + insertId,
				null, null, null, null);// devuelve el ejercicio que se acaba de
										// insertar

		cursor.moveToFirst();
		Ejercicio newEjercicio = cursorToEjercicio(cursor);
		cursor.close();
		return newEjercicio;
	}
	
	public boolean modificaEjercicio(Ejercicio ejercicio) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_EJERCICIO_NOMBRE, ejercicio.getNombre());
		values.put(MySQLiteHelper.COLUMN_EJERCICIO_OBJETOS,
				com.example.mipatternrecognition.Utils.ArrayListToJson(ejercicio.getObjetos()));
		values.put(MySQLiteHelper.COLUMN_EJERCICIO_DESCRIPCION,ejercicio.getDescripcion());
		values.put(MySQLiteHelper.COLUMN_EJERCICIO_DURACION,ejercicio.getDuracion());

		return database.update(MySQLiteHelper.TABLE_EJERCICIO, values,
				MySQLiteHelper.COLUMN_EJERCICIO_ID + " = " + ejercicio.getIdEjercicio(), null) > 0;
	}

	public boolean modificaEjercicio(int id, String nombre,
			ArrayList<Integer> objetos, String descripcion, double duracion) {

		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_EJERCICIO_NOMBRE, nombre);
		values.put(MySQLiteHelper.COLUMN_EJERCICIO_OBJETOS,
				com.example.mipatternrecognition.Utils.ArrayListToJson(objetos));
		values.put(MySQLiteHelper.COLUMN_EJERCICIO_DESCRIPCION,descripcion);
		values.put(MySQLiteHelper.COLUMN_EJERCICIO_DURACION,duracion);

		return database.update(MySQLiteHelper.TABLE_EJERCICIO, values,
				MySQLiteHelper.COLUMN_EJERCICIO_ID + " = " + id, null) > 0;
	}

	public boolean eliminaEjercicio(int id) {
		return database.delete(MySQLiteHelper.TABLE_EJERCICIO,
				MySQLiteHelper.COLUMN_EJERCICIO_ID + " = " + id, null) > 0;
	}

	public boolean eliminaTodosEjercicios() {
		return database.delete(MySQLiteHelper.TABLE_EJERCICIO, null, null) > 0;
	}
	
	public void dropTableEjericio() {
		database.execSQL(dbHelper.getSqlDropEjercicio());
		database.execSQL(dbHelper.getSqlCreateEjercicio());
	}

	public List<Ejercicio> getAllEjercicios() {
		Cursor cursor = database.query(MySQLiteHelper.TABLE_EJERCICIO,
				allColumns, null, null, null, null, null);

		List<Ejercicio> ejercicios = new ArrayList<Ejercicio>();
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				Ejercicio ejercicio = cursorToEjercicio(cursor);
				ejercicios.add(ejercicio);
				cursor.moveToNext();
			}
			cursor.close();
		}
		return ejercicios;
	}

	public Ejercicio getEjercicios(int id) {
		Cursor cursor = database.query(MySQLiteHelper.TABLE_EJERCICIO,
				allColumns, MySQLiteHelper.COLUMN_EJERCICIO_ID + " = " + id,
				null, null, null, null);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			Ejercicio ejercicio = cursorToEjercicio(cursor);
			cursor.close();
			return ejercicio;
		}
		return null;
	}
	
	public double getDuracion(int id) {
		Cursor cursor = database.query(MySQLiteHelper.TABLE_EJERCICIO, 
				new String [] {MySQLiteHelper.COLUMN_EJERCICIO_DURACION}, 
				MySQLiteHelper.COLUMN_EJERCICIO_ID + " = " + id, null, null, null, null);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			return cursor.getDouble(0);
		}
		return 0;
	}

	private Ejercicio cursorToEjercicio(Cursor cursor) {
		return new Ejercicio(cursor.getInt(0), cursor.getString(1),
				Utils.ArrayListFromJson(cursor.getString(2)),cursor.getString(3), cursor.getDouble(4));
	}

}
