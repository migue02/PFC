package pfc.bd;

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
	
	// ------------
	// TABLA OBJETO
	// ------------
	protected static String TABLE_OBJETO = "Objeto";
	public static final String COLUMN_OBJETO_ID = "_id";
	public static final String COLUMN_OBJETO_NOMBRE = "nombre";
	public static final String COLUMN_OBJETO_KEYPOINTS = "keypoints";
	public static final String COLUMN_OBJETO_DESPCRIPTORES = "descriptores";
	public static final String COLUMN_OBJETO_COLS = "cols";
	public static final String COLUMN_OBJETO_ROWS = "rows";

	private String sqlCreateObjeto = "create table if not exists " + TABLE_OBJETO + "(" 
			+ COLUMN_OBJETO_ID + " integer primary key autoincrement, " 
			+ COLUMN_OBJETO_NOMBRE + " varchar, " 
			+ COLUMN_OBJETO_KEYPOINTS + " text, "
			+ COLUMN_OBJETO_DESPCRIPTORES + " text, "
			+ COLUMN_OBJETO_COLS + " integer, "
			+ COLUMN_OBJETO_ROWS + " integer)";

	// ------------
	// TABLA ALUMNO
	// ------------
	protected static String TABLE_ALUMNO = "Alumno";
	public static final String COLUMN_ALUMNO_ID = "_id";
	public static final String COLUMN_ALUMNO_NOMBRE = "nombre";
	public static final String COLUMN_ALUMNO_APELLIDOS = "apellidos";
	public static final String COLUMN_ALUMNO_FECHA_NAC = "fecha_nac";
	public static final String COLUMN_ALUMNO_SEXO = "sexo";
	public static final String COLUMN_ALUMNO_OBSERVACIONES = "observaciones";

	private String sqlCreateAlumno = "create table if not exists " + TABLE_ALUMNO + "(" 
			+ COLUMN_ALUMNO_ID + " integer primary key autoincrement, " 
			+ COLUMN_ALUMNO_NOMBRE	+ " varchar, " 
			+ COLUMN_ALUMNO_APELLIDOS + " varchar, "
			+ COLUMN_ALUMNO_FECHA_NAC + " date, " 
			+ COLUMN_ALUMNO_SEXO + " varchar, " 
			+ COLUMN_ALUMNO_OBSERVACIONES + " varchar)";

	// ---------------
	// TABLA EJERCICIO
	// ---------------
	protected static String TABLE_EJERCICIO = "Ejercicio";
	public static final String COLUMN_EJERCICIO_ID = "_id";
	public static final String COLUMN_EJERCICIO_NOMBRE = "nombre";
	public static final String COLUMN_EJERCICIO_OBJETOS = "objetos";
	public static final String COLUMN_EJERCICIO_DURACION = "duracion";
	// public static final String COLUMN_EJERCICIO_SONIDO_ACIERTO = "acierto";
	// public static final String COLUMN_EJERCICIO_SONIDO_FALLO = "fallo";

	private String sqlCreateEjercicio = "create table if not exists " + TABLE_EJERCICIO + "(" 
			+ COLUMN_EJERCICIO_ID + " integer primary key autoincrement, " 
			+ COLUMN_EJERCICIO_NOMBRE + " varchar, " 
			+ COLUMN_EJERCICIO_OBJETOS + " varchar, "
			+ COLUMN_EJERCICIO_DURACION + " real)";
	
	// -------------------------
	// TABLA SERIE DE EJERCICIOS
	// -------------------------
	protected static String TABLE_SERIE_EJERCICIOS = "SerieEjercicios";
	public static final String COLUMN_SERIE_EJERCICIOS_ID = "_id";
	public static final String COLUMN_SERIE_EJERCICIOS_NOMBRE = "nombre";
	public static final String COLUMN_SERIE_EJERCICIOS_IDEJERCICIOS = "ejercicios";
	public static final String COLUMN_SERIE_EJERCICIOS_DURACION = "duracion";
	public static final String COLUMN_SERIE_EJERCICIOS_FECHA_MODIFICACION = "fecha_modificacion";
	
	private String sqlCreateSerieEjercicios = "create table if not exists "	+ TABLE_SERIE_EJERCICIOS + "(" 
			+ COLUMN_SERIE_EJERCICIOS_ID + " integer primary key autoincrement, "
			+ COLUMN_SERIE_EJERCICIOS_NOMBRE + " varchar, "
			+ COLUMN_SERIE_EJERCICIOS_IDEJERCICIOS + " varchar, "
			+ COLUMN_SERIE_EJERCICIOS_DURACION + " real, "
			+ COLUMN_SERIE_EJERCICIOS_FECHA_MODIFICACION + " date)";
	
	// ---------------
	// TABLA RESULTADO
	// ---------------
	protected static String TABLE_RESULTADO = "Resultado";
	public static final String COLUMN_RESULTADO_ID = "_id";
	public static final String COLUMN_RESULTADO_ID_ALUMNO = "idAlumno";
	public static final String COLUMN_RESULTADO_ID_EJERCICIO = "idEjercicio";
	public static final String COLUMN_RESULTADO_FECHA = "fechaRealizacion";
	public static final String COLUMN_RESULTADO_DURACION = "duracion";
	public static final String COLUMN_RESULTADO_NUM_OBJETOS = "numeroObjetosReconocer";
	public static final String COLUMN_RESULTADO_ACIERTOS = "aciertos";
	public static final String COLUMN_RESULTADO_FALLOS = "fallos";
	public static final String COLUMN_RESULTADO_PUNTUACION = "puntuacion";

	private String sqlCreateResultado = "create table if not exists " + TABLE_RESULTADO 
			+ "(" + COLUMN_RESULTADO_ID + " integer primary key autoincrement, "
			+ COLUMN_RESULTADO_ID_ALUMNO + " integer "
			+ "REFERENCES "+TABLE_ALUMNO+"("+COLUMN_ALUMNO_ID+") ON DELETE CASCADE, "//ON UPDATE CASCADE no necesario ya que el id no se va a modificar nunca
			+ COLUMN_RESULTADO_ID_EJERCICIO + " integer "
			+ "REFERENCES "+TABLE_SERIE_EJERCICIOS+"("+COLUMN_EJERCICIO_ID+") ON DELETE CASCADE, "//ON UPDATE CASCADE
			+ COLUMN_RESULTADO_FECHA + " date, " 
			+ COLUMN_RESULTADO_DURACION + " real, " 
			+ COLUMN_RESULTADO_NUM_OBJETOS + " integer, "
			+ COLUMN_RESULTADO_ACIERTOS + " integer, "
			+ COLUMN_RESULTADO_FALLOS + " integer, "
			+ COLUMN_RESULTADO_PUNTUACION + " real)";
	
	// -----------------
	// BORRADO DE TABLAS
	// -----------------
	private String sqlDropObjeto = "DROP TABLE IF EXISTS " + TABLE_OBJETO;
	private String sqlDropAlumno = "DROP TABLE IF EXISTS " + TABLE_ALUMNO;
	private String sqlDropEjercicio = "DROP TABLE IF EXISTS " + TABLE_EJERCICIO;
	private String sqlDropSerieEjercicios = "DROP TABLE IF EXISTS "
			+ TABLE_SERIE_EJERCICIOS;
	
	private String sqlDropResultado = "DROP TABLE IF EXISTS "
			+ TABLE_RESULTADO;
	
	public String getSqlCreateObjeto() {
		return sqlCreateObjeto;
	}

	public String getSqlDropObjeto() {
		return sqlDropObjeto;
	}

	public String getSqlCreateAlumno() {
		return sqlCreateAlumno;
	}

	public String getSqlDropAlumno() {
		return sqlDropAlumno;
	}

	public String getSqlCreateEjercicio() {
		return sqlCreateEjercicio;
	}

	public String getSqlDropEjercicio() {
		return sqlDropEjercicio;
	}

	public String getSqlCreateSerieEjercicios() {
		return sqlCreateSerieEjercicios;
	}

	public String getSqlDropSerieEjercicios() {
		return sqlDropSerieEjercicios;
	}

	public String getSqlCreateResultado() {
		return sqlCreateResultado;
	}

	public String getSqlDropResultado() {
		return sqlDropResultado;
	}

	// ----------------------
	// HABILITAR FOREIGN KEYS
	// ----------------------
	public String sqlenableForeingKeys = "PRAGMA foreign_keys = ON";

	
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
		// context.deleteDatabase(context.getDatabasePath(DB_NAME).toString());
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
		Log.w("DATABASE", "Creando tablas");
		db.execSQL(sqlenableForeingKeys);
		db.execSQL(sqlCreateObjeto);
		db.execSQL(sqlCreateAlumno);
		db.execSQL(sqlCreateEjercicio);
		db.execSQL(sqlCreateSerieEjercicios);
		db.execSQL(sqlCreateResultado);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(MySQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL(sqlDropObjeto);
		db.execSQL(sqlDropAlumno);
		onCreate(db);
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
	// String aux = "INSERT INTO " + TABLE_OBJETO
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
	// + TABLE_OBJETO + "where id = " + id, null);
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
	// return db.rawQuery("SELECT * FROM " + TABLE_OBJETO, null);
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
	// return db.rawQuery("SELECT * FROM " + TABLE_OBJETO, null).getCount();
	// }
	//
	// /**
	// * It will drop the PI table if exists and will create a new one
	// *
	// */
	// public void dropObjeto() {
	//
	// SQLiteDatabase db = getReadableDatabase();
	// db.execSQL("DROP TABLE IF EXISTS " + TABLE_OBJETO);
	// db.execSQL(sqlCreateObjeto);
	// db.close();
	// }

}
