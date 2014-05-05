package pfc.bd;

import java.util.ArrayList;
import java.util.List;

import pfc.obj.Objeto;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * This class implements the DataBase of the application and all of the possible
 * operations that you can do with the DataBase
 * 
 */
public class ObjetoDataSource {

	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { MySQLiteHelper.COLUMN_OBJETO_ID,
			MySQLiteHelper.COLUMN_OBJETO_NOMBRE,
			MySQLiteHelper.COLUMN_OBJETO_KEYPOINTS,
			MySQLiteHelper.COLUMN_OBJETO_DESPCRIPTORES };

	public ObjetoDataSource(Context context) {
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
		database.execSQL(dbHelper.sqlenableForeingKeys);
		database.execSQL(dbHelper.getSqlCreateObjeto());
	}

	public void close() {
		dbHelper.close();
	}

	public Objeto createObjeto(Objeto objeto) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_OBJETO_NOMBRE, objeto.getNombre());
		values.put(MySQLiteHelper.COLUMN_OBJETO_KEYPOINTS, objeto.getKeypoints());
		values.put(MySQLiteHelper.COLUMN_OBJETO_DESPCRIPTORES, objeto.getDescriptores());

		objeto.setId((int) database.insert(MySQLiteHelper.TABLE_OBJETO, null, values));
		return objeto;
	}
	
	public Objeto createObjeto(String nombre, String keypoints,
			String descriptores) {

		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_OBJETO_NOMBRE, nombre);
		values.put(MySQLiteHelper.COLUMN_OBJETO_KEYPOINTS, keypoints);
		values.put(MySQLiteHelper.COLUMN_OBJETO_DESPCRIPTORES, descriptores);

		long insertId = database.insert(MySQLiteHelper.TABLE_OBJETO, null,
				values); // Se inserta un objeto y se deuelve su id
		Log.w("Creando...", "Objeto " + nombre + " creado con id " + insertId);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_OBJETO,

		allColumns, MySQLiteHelper.COLUMN_OBJETO_ID + " = " + insertId, null,
				null, null, null);// devuelve el objeto que se acaba de insertar

		cursor.moveToFirst();
		Objeto newObjeto = cursorToObjeto(cursor);
		cursor.close();
		return newObjeto;
	}
	
	public boolean modificaObjeto(int id, String nombre, String keypoints,
			String descriptores) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_OBJETO_NOMBRE, nombre);
		values.put(MySQLiteHelper.COLUMN_OBJETO_KEYPOINTS, keypoints);
		values.put(MySQLiteHelper.COLUMN_OBJETO_DESPCRIPTORES, descriptores);

		return database.update(MySQLiteHelper.TABLE_OBJETO, values,
				MySQLiteHelper.COLUMN_OBJETO_ID + " = " + id, null) > 0;
	}
	
	public boolean modificaObjeto(Objeto objeto) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_OBJETO_NOMBRE, objeto.getNombre());
		values.put(MySQLiteHelper.COLUMN_OBJETO_KEYPOINTS, objeto.getKeypoints());
		values.put(MySQLiteHelper.COLUMN_OBJETO_DESPCRIPTORES, objeto.getDescriptores());

		return database.update(MySQLiteHelper.TABLE_OBJETO, values,
				MySQLiteHelper.COLUMN_OBJETO_ID + " = " + objeto.getId(), null) > 0;
	}

	public boolean eliminaObjeto(int id) {
		return database.delete(MySQLiteHelper.TABLE_OBJETO,
				MySQLiteHelper.COLUMN_OBJETO_ID + " = " + id, null) > 0;
	}

	public boolean eliminaTodosObjetos() {
		return database.delete(MySQLiteHelper.TABLE_OBJETO, null, null) > 0;
	}

	public void dropTableObjeto() {
		Log.w("Deleting...", "Borrando tabla objetos");
		database.execSQL(dbHelper.getSqlDropObjeto());
		database.execSQL(dbHelper.getSqlCreateObjeto());
	}

	public List<Objeto> getAllObjetos() {
		List<Objeto> objetos = new ArrayList<Objeto>();
		Log.w("Obteniendo...", "Obteniendo todos los objetos...");
		Cursor cursor = database.query(MySQLiteHelper.TABLE_OBJETO, allColumns,
				null, null, null, null, null);

		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				Objeto objeto = cursorToObjeto(cursor);
				objetos.add(objeto);
				cursor.moveToNext();
			}
			cursor.close();
		}
		return objetos;
	}

	public Objeto getObjeto(long id) {
		Cursor cursor = database.query(MySQLiteHelper.TABLE_OBJETO, allColumns,
				MySQLiteHelper.COLUMN_OBJETO_ID + " = " + id, null, null, null,
				null);

		if (cursor != null && cursor.getCount() > 0) {
			Objeto objeto = new Objeto();
			cursor.moveToFirst();
			objeto = cursorToObjeto(cursor);
			cursor.close();
			return objeto;
		} else
			return null;
	}

	public Objeto getObjeto(String nombre) {

		Cursor cursor = database.query(MySQLiteHelper.TABLE_OBJETO, allColumns,
				MySQLiteHelper.COLUMN_OBJETO_NOMBRE + " = '" + nombre + "'",
				null, null, null, null);// devuelve el objeto que se pide

		if (cursor != null && cursor.getCount() > 0) {
			Objeto objeto = new Objeto();
			cursor.moveToFirst();
			objeto = cursorToObjeto(cursor);
			cursor.close();
			return objeto;
		} else
			return null;
	}

	private Objeto cursorToObjeto(Cursor cursor) {
		Objeto objeto = new Objeto();
		objeto.setId(cursor.getLong(0));
		objeto.setNombre(cursor.getString(1));
		objeto.setKeypoints(cursor.getString(2));
		objeto.setDescriptores(cursor.getString(3));
		return objeto;
	}

}
