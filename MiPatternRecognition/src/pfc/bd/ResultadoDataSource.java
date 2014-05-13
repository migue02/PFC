package pfc.bd;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pfc.obj.Alumno;
import pfc.obj.Resultado;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ResultadoDataSource {
	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { MySQLiteHelper.COLUMN_RESULTADO_ID,
			MySQLiteHelper.COLUMN_RESULTADO_ID_ALUMNO,
			MySQLiteHelper.COLUMN_RESULTADO_ID_EJERCICIO,
			MySQLiteHelper.COLUMN_RESULTADO_ACIERTOS,
			MySQLiteHelper.COLUMN_RESULTADO_FALLOS,
			MySQLiteHelper.COLUMN_RESULTADO_FECHA,
			MySQLiteHelper.COLUMN_RESULTADO_PUNTUACION,
			MySQLiteHelper.COLUMN_RESULTADO_DURACION,
			MySQLiteHelper.COLUMN_RESULTADO_NUM_OBJETOS};

	public ResultadoDataSource(Context context) {
		Log.w("Creando...", "Creando bd");
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
		database.execSQL(dbHelper.sqlenableForeingKeys);
		database.execSQL(dbHelper.getSqlCreateResultado());		
	}

	public void close() {
		dbHelper.close();
	}
	
	public Resultado createResultado(Resultado resultado) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_RESULTADO_ID_ALUMNO, resultado.getIdAlumno());
		values.put(MySQLiteHelper.COLUMN_RESULTADO_ID_EJERCICIO, resultado.getIdEjercicio());
		values.put(MySQLiteHelper.COLUMN_RESULTADO_FECHA,
				new SimpleDateFormat("dd/MM/yyyy").format(resultado.getFechaRealizacion()));
		values.put(MySQLiteHelper.COLUMN_RESULTADO_ACIERTOS, resultado.getAciertos());
		values.put(MySQLiteHelper.COLUMN_RESULTADO_FALLOS, resultado.getFallos());
		values.put(MySQLiteHelper.COLUMN_RESULTADO_DURACION, resultado.getDuracion());
		values.put(MySQLiteHelper.COLUMN_RESULTADO_NUM_OBJETOS, resultado.getNumeroObjetosReconocer());
		values.put(MySQLiteHelper.COLUMN_RESULTADO_PUNTUACION, resultado.getPuntuacion());
		
		long id= database.insert(MySQLiteHelper.TABLE_RESULTADO, null, values);
		resultado.setIdResultado((int)id);
		return resultado;
	}
	
	public Resultado createResultado(int idAlumno, int idEjercicio, int aciertos, int fallos,
			Date fecha, int duracion, int numObjetos, double puntuacion) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_RESULTADO_ID_ALUMNO, idAlumno);
		values.put(MySQLiteHelper.COLUMN_RESULTADO_ID_EJERCICIO, idEjercicio);
		values.put(MySQLiteHelper.COLUMN_RESULTADO_FECHA,
				new SimpleDateFormat("dd/MM/yyyy").format(fecha));
		values.put(MySQLiteHelper.COLUMN_RESULTADO_ACIERTOS, aciertos);
		values.put(MySQLiteHelper.COLUMN_RESULTADO_FALLOS, fallos);
		values.put(MySQLiteHelper.COLUMN_RESULTADO_DURACION, duracion);
		values.put(MySQLiteHelper.COLUMN_RESULTADO_NUM_OBJETOS, numObjetos);
		values.put(MySQLiteHelper.COLUMN_RESULTADO_PUNTUACION, puntuacion);
		
		long insertId = database.insert(MySQLiteHelper.TABLE_RESULTADO, null, values);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_RESULTADO, allColumns,
				MySQLiteHelper.COLUMN_RESULTADO_ID + " = " + insertId, null, null,
				null, null);// devuelve el alumno que se acaba de insertar

		cursor.moveToFirst();
		Resultado newResultado = cursorToResultado(cursor);
		cursor.close();
		return newResultado;
	}
	
	public boolean modificaResultado(Resultado resultado) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_RESULTADO_ID_ALUMNO, resultado.getIdAlumno());
		values.put(MySQLiteHelper.COLUMN_RESULTADO_ID_EJERCICIO, resultado.getIdEjercicio());
		values.put(MySQLiteHelper.COLUMN_RESULTADO_FECHA,
				new SimpleDateFormat("dd/MM/yyyy").format(resultado.getFechaRealizacion()));
		values.put(MySQLiteHelper.COLUMN_RESULTADO_ACIERTOS, resultado.getAciertos());
		values.put(MySQLiteHelper.COLUMN_RESULTADO_FALLOS, resultado.getFallos());
		values.put(MySQLiteHelper.COLUMN_RESULTADO_DURACION, resultado.getDuracion());
		values.put(MySQLiteHelper.COLUMN_RESULTADO_NUM_OBJETOS, resultado.getNumeroObjetosReconocer());
		values.put(MySQLiteHelper.COLUMN_RESULTADO_PUNTUACION, resultado.getPuntuacion());
		
		return database.update(MySQLiteHelper.TABLE_RESULTADO, values,
				MySQLiteHelper.COLUMN_RESULTADO_ID + " = " + resultado.getIdResultado(), null) > 0;
	}

	public boolean modificaResultado(int id, int idAlumno, int idEjercicio, int aciertos, int fallos,
			Date fecha, int duracion, int numObjetos, double puntuacion) {

		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_RESULTADO_ID_ALUMNO, idAlumno);
		values.put(MySQLiteHelper.COLUMN_RESULTADO_ID_EJERCICIO, idEjercicio);
		values.put(MySQLiteHelper.COLUMN_RESULTADO_FECHA,
				new SimpleDateFormat("dd/MM/yyyy").format(fecha));
		values.put(MySQLiteHelper.COLUMN_RESULTADO_ACIERTOS, aciertos);
		values.put(MySQLiteHelper.COLUMN_RESULTADO_FALLOS, fallos);
		values.put(MySQLiteHelper.COLUMN_RESULTADO_DURACION, duracion);
		values.put(MySQLiteHelper.COLUMN_RESULTADO_NUM_OBJETOS, numObjetos);
		values.put(MySQLiteHelper.COLUMN_RESULTADO_PUNTUACION, puntuacion);

		return database.update(MySQLiteHelper.TABLE_RESULTADO, values,
				MySQLiteHelper.COLUMN_RESULTADO_ID + " = " + id, null) > 0;
	}

	public boolean borraResultado(int id) {
		return database.delete(MySQLiteHelper.TABLE_RESULTADO,
				MySQLiteHelper.COLUMN_RESULTADO_ID + " = " + id, null) > 0;
	}

	public boolean borraTodosResultados() {
		return database.delete(MySQLiteHelper.TABLE_RESULTADO, null, null) > 0;
	}
	
	public void dropTableResultado() {
		database.execSQL(dbHelper.getSqlDropResultado());
		database.execSQL(dbHelper.getSqlCreateResultado());
	}

	public List<Resultado> getAllResultados() {
		Cursor cursor = database.query(MySQLiteHelper.TABLE_RESULTADO, allColumns,
				null, null, null, null, null);

		List<Resultado> resultados = new ArrayList<Resultado>();
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				Resultado resultado = cursorToResultado(cursor);
				resultados.add(resultado);
				cursor.moveToNext();
			}
			cursor.close();
		}
		return resultados;
	}

	public Resultado getResultado(int id) {
		Cursor cursor = database.query(MySQLiteHelper.TABLE_RESULTADO, allColumns,
				MySQLiteHelper.COLUMN_RESULTADO_ID + " = " + id, null, null,
				null, null);

		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			Resultado resultado = cursorToResultado(cursor);
			cursor.close();
			return resultado;
		}
		return null;
	}


	public List<Resultado> getResultadosAlumno(Alumno alumno){
		
		List<Resultado> resultados = new ArrayList<Resultado>();
		
		
		String query="SELECT R."+MySQLiteHelper.COLUMN_RESULTADO_ID+", "+MySQLiteHelper.COLUMN_RESULTADO_ID_ALUMNO+", "+MySQLiteHelper.COLUMN_RESULTADO_ID_EJERCICIO+
				", sum("+MySQLiteHelper.COLUMN_RESULTADO_ACIERTOS+"), sum("+MySQLiteHelper.COLUMN_RESULTADO_FALLOS+"),"+
				" "+MySQLiteHelper.COLUMN_RESULTADO_FECHA+", sum("+MySQLiteHelper.COLUMN_RESULTADO_PUNTUACION+"), sum("+MySQLiteHelper.COLUMN_RESULTADO_DURACION+"),"+
				" sum("+MySQLiteHelper.COLUMN_RESULTADO_NUM_OBJETOS+")"+
				" FROM "+MySQLiteHelper.TABLE_RESULTADO+" R, "+MySQLiteHelper.TABLE_ALUMNO+" A"+
				" WHERE R."+MySQLiteHelper.COLUMN_RESULTADO_ID_ALUMNO+" = A."+MySQLiteHelper.COLUMN_ALUMNO_ID+" AND "+MySQLiteHelper.COLUMN_RESULTADO_ID_EJERCICIO+" = "+alumno.getIdAlumno()+
				" AND "+MySQLiteHelper.COLUMN_RESULTADO_FECHA+" > (DATETIME('NOW') - 7)"+
				" GROUP BY FECHAREALIZACION, R.IDALUMNO";
		Cursor cursor = database.rawQuery(query, null);
		
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				Resultado resultado = cursorToResultado(cursor);
				resultados.add(resultado);
				cursor.moveToNext();
			}
			cursor.close();
		}
		
		return resultados;
		
	}
	
	private Resultado cursorToResultado(Cursor cursor) {
		Resultado resultado = new Resultado();
		resultado.setIdResultado(cursor.getInt(0));
		resultado.setIdAlumno(cursor.getInt(1));
		resultado.setIdEjercicio(cursor.getInt(2));
		resultado.setAciertos(cursor.getInt(3));
		resultado.setFallos(cursor.getInt(4));
		try {
			resultado.setFechaRealizacion(new SimpleDateFormat("dd/MM/yyyy").parse(cursor
					.getString(5)));
		} catch (ParseException e) {
			Log.e("ERROR_FECHA", "Error al obtener la fecha");
			e.printStackTrace();
		}
		resultado.setPuntuacion(cursor.getDouble(6));
		resultado.setDuracion(cursor.getInt(7));
		resultado.setNumeroObjetosReconocer(cursor.getInt(8));
		return resultado;
	}

}
