package pfc.bd;

import java.sql.SQLClientInfoException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * This class implements the DataBase of the application and all of the possible
 * operations that you can do with the DataBase
 * 
 */
public class MySQLiteHelper extends SQLiteOpenHelper {

	private static final String DB_NAME = "reconocimiento.db";
	protected static String TABLE_OBJETO = "objeto";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NOMBRE = "nombre";
	public static final String COLUMN_KEYPOINTS = "keypoints";
	public static final String COLUMN_DESPCRIPTORES = "descriptores";

	private String sqlCreateObjeto = "create table " + TABLE_OBJETO + "("
			+ COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_NOMBRE + " varchar, " + COLUMN_KEYPOINTS + " text, "
			+ COLUMN_DESPCRIPTORES + " text)";


	public String getSqlCreateObjeto() {
		return sqlCreateObjeto;
	}

	private String sqlDropObjeto = "DROP TABLE IF EXISTS" +TABLE_OBJETO;
	/**
	 * Constructor that create a new DataBaseHelper to create, open, and/or
	 * manage a database.
	 * 
	 * @param context
	 *            to use to open or create the database
	 */
	public MySQLiteHelper(Context context) {
		super(context, DB_NAME, null, 1);
		Log.w("DATABASE", context.getDatabasePath(DB_NAME).toString());
		//context.deleteDatabase(context.getDatabasePath(DB_NAME).toString());
	}

	/**
	 * Called when the database is created for the first time. This is where the
	 * creation of tables and the initial population of the tables should
	 * happen.
	 * 
	 * @param db
	 *            The database.
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		Log.w("DATABASE", "Creando tabla objeto");
		db.execSQL(sqlCreateObjeto);
	}
	
	
	@Override
	  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    Log.w(MySQLiteHelper.class.getName(),
	        "Upgrading database from version " + oldVersion + " to "
	            + newVersion + ", which will destroy all old data");
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_OBJETO);
	    onCreate(db);
	  }


//	/**
//	 * It inserts in the PI table a new row with the values passed in the
//	 * parameters
//	 * 
//	 * @param keypoints
//	 *            The data of the object (keypoints)
//	 * 
//	 * @param descriptors
//	 *            The data of the object (descriptors)
//	 */
//	public void insertObjeto(String nombre, String keypoints, String descriptors) {
//		SQLiteDatabase db = getWritableDatabase();
//		if (db != null) {
//			String aux = "INSERT INTO " + TABLE_OBJETO
//					+ " (nombre, keypoints, descriptors) " + " VALUES( "
//					+ nombre + "," + keypoints + "," + descriptors + " ) ";
//			db.execSQL(aux);
//			db.close();
//		}
//	}
//
//	/**
//	 * It will read in the PI table and return a cursor with the query of a
//	 * select of the values "u" and "y" of every rows in the PI table
//	 * 
//	 * @return It return a cursor with the query of a select of the values "u"
//	 *         and "y" of every rows in the PI table
//	 */
//	public Cursor readObjeto(int id) {
//		SQLiteDatabase db = getReadableDatabase();
//
//		return db.rawQuery("SELECT nombre, keypoints, descriptors FROM "
//				+ TABLE_OBJETO + "where id = " + id, null);
//	}
//
//	/**
//	 * It will read in the PI table and return a cursor with the query of a
//	 * select of the values "u" and "y" of every rows in the PI table
//	 * 
//	 * @return It return a cursor with the query of a select of the values "u"
//	 *         and "y" of every rows in the PI table
//	 */
//	public Cursor readObjetos() {
//		SQLiteDatabase db = getReadableDatabase();
//
//		return db.rawQuery("SELECT * FROM " + TABLE_OBJETO, null);
//	}
//
//	/**
//	 * It will read in the PI table and return a cursor with the query of a
//	 * select of the values "u" and "y" of every rows in the PI table
//	 * 
//	 * @return It return a cursor with the query of a select of the values "u"
//	 *         and "y" of every rows in the PI table
//	 */
//	public int readCountObjetos() {
//		SQLiteDatabase db = getReadableDatabase();
//
//		return db.rawQuery("SELECT * FROM " + TABLE_OBJETO, null).getCount();
//	}
//
//	/**
//	 * It will drop the PI table if exists and will create a new one
//	 * 
//	 */
//	public void dropObjeto() {
//
//		SQLiteDatabase db = getReadableDatabase();
//		db.execSQL("DROP TABLE IF EXISTS " + TABLE_OBJETO);
//		db.execSQL(sqlCreateObjeto);
//		db.close();
//	}

}
