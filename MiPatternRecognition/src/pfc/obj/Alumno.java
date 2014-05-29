package pfc.obj;

import java.text.SimpleDateFormat;
import java.util.Date;

import pfc.obj.TiposPropios.Sexo;

public class Alumno {
	
	private int idAlumno;
	private String nombre;
	private String apellidos;
	private Date fecha_nac;
	private String observaciones;	
	private Sexo sexo;
	
	public Alumno(){
		idAlumno = -1;
		nombre = "";
		apellidos = "";
		fecha_nac = new Date();//asigna la fecha actual
		observaciones = "";
		sexo = Sexo.NoDef;
	}
	
	public int getIdAlumno() {
		return idAlumno;
	}
	
	public void setIdAlumno(int idAlumno) {
		this.idAlumno = idAlumno;
	}

	public String getNombre() {
		return nombre;
	}
	
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	
	public String getApellidos() {
		return apellidos;
	}
	
	public void setApellidos(String apellidos) {
		this.apellidos = apellidos;
	}
	
	public Date getFecha_nac_AsDate() {
		return fecha_nac;
	}
	
	public String getFecha_nac_AsStrign() {
		SimpleDateFormat date = new SimpleDateFormat("dd/MM/yyyy");
		return date.format(fecha_nac);
	}
	
	public void setFecha_nac(Date fecha_nac) {
		this.fecha_nac = fecha_nac;
	}
	
	public String getObservaciones() {
		return observaciones;
	}
	
	public void setObservaciones(String observaciones) {
		this.observaciones = observaciones;
	}
	
	public Sexo getSexo() {
		return sexo;
	}
	
	public void setSexo(Sexo sexo) {
		this.sexo = sexo;
	}
	
	@Override
	public String toString() {
		return idAlumno + ".- " + nombre + ", " + apellidos + ", FechaNac " + getFecha_nac_AsStrign() + ", " + sexo;
	}	

}
