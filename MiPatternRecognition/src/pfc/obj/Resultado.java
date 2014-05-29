package pfc.obj;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Resultado {

	private int idResultado;
	private int idAlumno;
	private int idEjercicio;
	private Date fechaRealizacion;
	private double duracion;
	private int numeroObjetosReconocer;
	private int aciertos;
	private int fallos;
	private double puntuacion;
	
	public Resultado() {
		this.idResultado = -1;
		this.idAlumno = -1;
		this.idEjercicio = -1;
		this.fechaRealizacion = new Date();
		this.duracion = -1;
		this.numeroObjetosReconocer = -1;
		this.aciertos = -1;
		this.fallos = -1;
		this.puntuacion = -1.0;
	}
	
	public Resultado(int idResultado, int idAlumno, int idEjercicio,
			Date fechaRealizacion, double duracion, int numeroObjetosReconocer,
			int aciertos, int fallos, double puntuacion) {
		this.idResultado = idResultado;
		this.idAlumno = idAlumno;
		this.idEjercicio = idEjercicio;
		this.fechaRealizacion = fechaRealizacion;
		this.duracion = duracion;
		this.numeroObjetosReconocer = numeroObjetosReconocer;
		this.aciertos = aciertos;
		this.fallos = fallos;
		this.puntuacion = puntuacion;
	}
	public int getIdResultado() {
		return idResultado;
	}
	public void setIdResultado(int idResultado) {
		this.idResultado = idResultado;
	}
	public int getIdAlumno() {
		return idAlumno;
	}
	public void setIdAlumno(int idAlumno) {
		this.idAlumno = idAlumno;
	}
	public int getIdEjercicio() {
		return idEjercicio;
	}
	public void setIdEjercicio(int idEjercicio) {
		this.idEjercicio = idEjercicio;
	}
	public Date getFechaRealizacion() {
		return fechaRealizacion;
	}
	public void setFechaRealizacion(Date fechaRealizacion) {
		this.fechaRealizacion = fechaRealizacion;
	}
	public double getDuracion() {
		return duracion;
	}
	public void setDuracion(double duracion) {
		this.duracion = duracion;
	}
	public int getNumeroObjetosReconocer() {
		return numeroObjetosReconocer;
	}
	public void setNumeroObjetosReconocer(int numeroObjetosReconocer) {
		this.numeroObjetosReconocer = numeroObjetosReconocer;
	}
	public int getAciertos() {
		return aciertos;
	}
	public void setAciertos(int aciertos) {
		this.aciertos = aciertos;
	}
	public int getFallos() {
		return fallos;
	}
	public void setFallos(int fallos) {
		this.fallos = fallos;
	}
	public double getPuntuacion() {
		return puntuacion;
	}
	public void setPuntuacion(double puntuacion) {
		this.puntuacion = puntuacion;
	}
	@Override
	public String toString() {
		return "Resultado [idResultado=" + idResultado + ", idAlumno="
				+ idAlumno + ", idEjercicio=" + idEjercicio
				+ ", fechaRealizacion=" + fechaRealizacion + ", duracion="
				+ duracion + ", numeroObjetosReconocer="
				+ numeroObjetosReconocer + ", aciertos=" + aciertos
				+ ", fallos=" + fallos + ", puntuacion=" + puntuacion + "]";
	}

	public String getFechaRealizacion_AsStrign() {
		return new SimpleDateFormat("dd/MM/yyyy").format(fechaRealizacion);
	}
	
}
