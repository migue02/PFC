package pfc.bd;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pfc.obj.Alumno;
import pfc.obj.TiposPropios.Sexo;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class AlumnoDataSource {

	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { MySQLiteHelper.COLUMN_ALUMNO_ID,
			MySQLiteHelper.COLUMN_ALUMNO_NOMBRE, MySQLiteHelper.COLUMN_ALUMNO_APELLIDOS,
			MySQLiteHelper.COLUMN_ALUMNO_FECHA_NAC, MySQLiteHelper.COLUMN_ALUMNO_SEXO, 
			MySQLiteHelper.COLUMN_ALUMNO_OBSERVACIONES };


	public AlumnoDataSource(Context context) {
		Log.w("Creando...", "Creando bd");
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
		database.execSQL(dbHelper.getSqlCreateAlumno());
	}

	public void close() {
		dbHelper.close();
	}
	
	public Alumno createAlumno(String nombre, String apellidos,
			Date fecha_nac, Sexo sexo, String observaciones) {

		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_ALUMNO_NOMBRE, nombre);
		values.put(MySQLiteHelper.COLUMN_ALUMNO_APELLIDOS, apellidos);
		values.put(MySQLiteHelper.COLUMN_ALUMNO_FECHA_NAC, fecha_nac.toString());
		values.put(MySQLiteHelper.COLUMN_ALUMNO_SEXO, sexo.toString());
		values.put(MySQLiteHelper.COLUMN_ALUMNO_OBSERVACIONES, observaciones);
		
		long insertId = database.insert(MySQLiteHelper.TABLE_ALUMNO, null,
				values); // Se inserta un alumno y se deuelve su id
		Log.w("Creando...", "Alumno " + nombre + " creado con id "+ insertId);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_ALUMNO,
		allColumns, MySQLiteHelper.COLUMN_ALUMNO_ID + " = " + insertId, null, null,
				null, null);// devuelve el alumno que se acaba de insertar
		

		cursor.moveToFirst();
		Alumno newAlumno = cursorToAlumno(cursor);
		cursor.close();
		return newAlumno;
	}
	
	public boolean modificaAlumno(int id, String nombre, String apellidos,
			Date fecha_nac, Sexo sexo, String observaciones) {

		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_ALUMNO_NOMBRE, nombre);
		values.put(MySQLiteHelper.COLUMN_ALUMNO_APELLIDOS, apellidos);
		values.put(MySQLiteHelper.COLUMN_ALUMNO_FECHA_NAC, fecha_nac.toString());
		values.put(MySQLiteHelper.COLUMN_ALUMNO_SEXO, sexo.toString());
		values.put(MySQLiteHelper.COLUMN_ALUMNO_OBSERVACIONES, observaciones);
		
		return database.update(MySQLiteHelper.TABLE_ALUMNO, values, MySQLiteHelper.COLUMN_ALUMNO_ID +" = "+id ,null)>0;
	}
	
	public boolean borraAlumno(int id){
		return database.delete(MySQLiteHelper.TABLE_ALUMNO, MySQLiteHelper.COLUMN_ALUMNO_ID +" = "+id , null)>0;
	}
	
	public List<Alumno> getAllAlumnos() {
		List<Alumno> alumnos = new ArrayList<Alumno>();
		Log.w("Obteniendo...", "Obteniendo todos los alumnos...");
		Cursor cursor = database.query(MySQLiteHelper.TABLE_ALUMNO, allColumns,
				null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Alumno alumno = cursorToAlumno(cursor);
			alumnos.add(alumno);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();			
		return alumnos;
	}
	
	private Alumno cursorToAlumno(Cursor cursor) {
		Alumno alumno = new Alumno();
		alumno.setIdAlumno(cursor.getInt(0));
		alumno.setNombre(cursor.getString(1));
		alumno.setApellidos(cursor.getString(2));
		try {
			alumno.setFecha_nac(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(cursor.getString(3)));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		alumno.setSexo(Sexo.valueOf(cursor.getString(4)));
		alumno.setObservaciones(cursor.getString(5));
		return alumno;
	}
	
}
