package pfc.bd;

import java.util.ArrayList;
import java.util.List;

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
	private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
			MySQLiteHelper.COLUMN_NOMBRE, MySQLiteHelper.COLUMN_KEYPOINTS,
			MySQLiteHelper.COLUMN_DESPCRIPTORES };

	public ObjetoDataSource(Context context) {
		Log.w("Creando...", "Creando bd");
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public Objeto createObjeto(String nombre, String keypoints,
			String descriptores) {

		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_NOMBRE, nombre);
		values.put(MySQLiteHelper.COLUMN_KEYPOINTS, keypoints);
		values.put(MySQLiteHelper.COLUMN_DESPCRIPTORES, descriptores);

		long insertId = database.insert(MySQLiteHelper.TABLE_OBJETO, null,
				values); // Se inserta un objeto y se deuelve su id
		Log.w("Creando...", "Objeto " + nombre + " creado con id "+ insertId);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_OBJETO,

		allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null, null,
				null, null);// devuelve el objeto que se acaba de insertar

		cursor.moveToFirst();
		Objeto newObjeto = cursorToObjeto(cursor);
		cursor.close();
		return newObjeto;
	}

	public void deleteObjeto(Objeto objeto) {
		long id = objeto.getId();
		Log.w("Deleting...", "Objeto deleted with id: " + id);
		database.delete(MySQLiteHelper.TABLE_OBJETO, MySQLiteHelper.COLUMN_ID
				+ " = " + id, null);
	}

	public List<Objeto> getAllObjetos() {
		List<Objeto> objetos = new ArrayList<Objeto>();
		Log.w("Obteniendo...", "Obteniendo todos los objetos...");
		Cursor cursor = database.query(MySQLiteHelper.TABLE_OBJETO, allColumns,
				null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Objeto objeto = cursorToObjeto(cursor);
			objetos.add(objeto);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return objetos;
	}

	public Objeto getObjeto(long id) {

		Cursor cursor = database.query(MySQLiteHelper.TABLE_OBJETO, allColumns,
				MySQLiteHelper.COLUMN_ID + " = '" + id + "'", null,
				null, null, null);// devuelve el objeto que se pide

		Objeto objeto = new Objeto();
		if (cursor != null) {
			cursor.moveToFirst();
			objeto = cursorToObjeto(cursor);
			// make sure to close the cursor
			cursor.close();
		}
		return objeto;
	}
	
	public Objeto getObjeto(String nombre) {

		Cursor cursor = database.query(MySQLiteHelper.TABLE_OBJETO, allColumns,
				MySQLiteHelper.COLUMN_NOMBRE + " = '" + nombre+ "'", null,
				null, null, null);// devuelve el objeto que se pide

		Objeto objeto = new Objeto();
		if (cursor != null) {
			cursor.moveToFirst();
			objeto = cursorToObjeto(cursor);
			// make sure to close the cursor
			cursor.close();
		}
		return objeto;
	}

	private Objeto cursorToObjeto(Cursor cursor) {
		Objeto objeto = new Objeto();
		objeto.setId(cursor.getLong(0));
		objeto.setNombre(cursor.getString(1));
		objeto.setKeypoints(cursor.getString(2));
		objeto.setDescriptores(cursor.getString(3));
		return objeto;
	}

	// /**
	// * It inserts in the PI table a new row with the values passed in the
	// * parameters
	// *
	// * @param keypoints
	// * The data of the object (keypoints)
	// *
	// * @param descriptors
	// * The data of the object (descriptors)
	// */
	// public void insertObjeto(String nombre, String keypoints, String
	// descriptors) {
	// SQLiteDatabase db = getWritableDatabase();
	// if (db != null) {
	// String aux = "INSERT INTO " + TableObjeto
	// + " (nombre, keypoints, descriptors) " + " VALUES( "
	// + nombre + "," + keypoints + "," + descriptors + " ) ";
	// db.execSQL(aux);
	// db.close();
	// }
	// }
	//
	// /**
	// * It will read in the PI table and return a cursor with the query of a
	// * select of the values "u" and "y" of every rows in the PI table
	// *
	// * @return It return a cursor with the query of a select of the values "u"
	// * and "y" of every rows in the PI table
	// */
	// public Cursor readObjeto(int id) {
	// SQLiteDatabase db = getReadableDatabase();
	//
	// return db.rawQuery("SELECT nombre, keypoints, descriptors FROM "
	// + TableObjeto + "where id = " + id, null);
	// }
	//
	// /**
	// * It will read in the PI table and return a cursor with the query of a
	// * select of the values "u" and "y" of every rows in the PI table
	// *
	// * @return It return a cursor with the query of a select of the values "u"
	// * and "y" of every rows in the PI table
	// */
	// public Cursor readObjetos() {
	// SQLiteDatabase db = getReadableDatabase();
	//
	// return db.rawQuery("SELECT * FROM " + TableObjeto, null);
	// }
	//
	// /**
	// * It will read in the PI table and return a cursor with the query of a
	// * select of the values "u" and "y" of every rows in the PI table
	// *
	// * @return It return a cursor with the query of a select of the values "u"
	// * and "y" of every rows in the PI table
	// */
	// public int readCountObjetos() {
	// SQLiteDatabase db = getReadableDatabase();
	//
	// return db.rawQuery("SELECT * FROM " + TableObjeto, null).getCount();
	// }
	//
	// /**
	// * It will drop the PI table if exists and will create a new one
	// *
	// */
	// public void dropObjeto() {
	//
	// SQLiteDatabase db = getReadableDatabase();
	// db.execSQL("DROP TABLE IF EXISTS " + TableObjeto);
	// db.execSQL(sqlCreateObjeto);
	// db.close();
	// }
	//
	// @Override
	// public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
	// // TODO Auto-generated method stub
	//
	// }

}
