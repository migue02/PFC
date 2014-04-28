package pfc.obj;

import java.util.ArrayList;

public class Ejercicio {
	
	private Integer idEjercicio;
	private String nombre;
	private ArrayList<Integer> objetos;
	
	public Ejercicio() {
		this.idEjercicio=-1;
		this.nombre="";
		this.objetos=new ArrayList<Integer>();
	}

	public Ejercicio(Integer idEjercicio, String nombre, ArrayList<Integer> objetos) {
		this.idEjercicio = idEjercicio;
		this.nombre = nombre;
		this.objetos = new ArrayList<Integer>(objetos);
	}
	
	public Ejercicio(Integer idEjercicio, String nombre, String objetos) {
		try {
			this.idEjercicio = idEjercicio;
			this.nombre = nombre;
			this.objetos = com.example.mipatternrecognition.Utils.ArrayListFromJson(objetos);	
		}catch (Exception e){
			this.idEjercicio=-1;
			this.nombre="";
			this.objetos=new ArrayList<Integer>();
		}
	}

	@Override
	public String toString() {
		return "Ejercicio [idEjercicio=" + idEjercicio + ", nombre=" + nombre
				+ ", objetos=" + objetos + "]";
	}

	public Integer getIdEjercicio() {
		return idEjercicio;
	}

	public void setIdEjercicio(Integer idEjercicio) {
		this.idEjercicio = idEjercicio;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public ArrayList<Integer> getObjetos() {
		return objetos;
	}

	public void setObjetos(ArrayList<Integer> objetos) {
		this.objetos = objetos;
	}	

}
