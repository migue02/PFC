package pfc.obj;

import java.util.ArrayList;

public class SerieEjercicios {
	
	private Integer idSerie;
	private String nombre;
	private ArrayList<Integer> ejercicios;
	
	public SerieEjercicios() {
		this.idSerie=-1;
		this.nombre="";
		this.ejercicios=new ArrayList<Integer>();
	}

	public SerieEjercicios(Integer idSerie, String nombre, ArrayList<Integer> ejercicios) {
		this.idSerie = idSerie;
		this.nombre = nombre;
		this.ejercicios = new ArrayList<Integer>(ejercicios);
	}
	
	public SerieEjercicios(Integer idSerie, String nombre, String ejercicios) {
		try {
			this.idSerie = idSerie;
			this.nombre = nombre;
			this.ejercicios = com.example.mipatternrecognition.Utils.ArrayListFromJson(ejercicios);	
		}catch (Exception e){
			this.idSerie=-1;
			this.nombre="";
			this.ejercicios=new ArrayList<Integer>();
		}
	}

	@Override
	public String toString() {
		return "Serie [idSerie=" + idSerie + ", nombre=" + nombre
				+ ", ejercicios=" + ejercicios + "]";
	}

	public Integer getIdSerie() {
		return idSerie;
	}

	public void setIdSerie(Integer idSerie) {
		this.idSerie = idSerie;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public ArrayList<Integer> getEjercicios() {
		return ejercicios;
	}

	public void setEjercicios(ArrayList<Integer> ejercicios) {
		this.ejercicios = ejercicios;
	}
	
	
}
