package pfc.obj;

public class Objeto {
	private long id;
	private String nombre;
	private String keypoints;
	private String descriptores;

	public Objeto() {
		id=-1;
		nombre="";
		keypoints="";
		descriptores="";
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getKeypoints() {
		return keypoints;
	}

	public void setKeypoints(String keypoints) {
		this.keypoints = keypoints;
	}

	public String getDescriptores() {
		return descriptores;
	}

	public void setDescriptores(String descriptores) {
		this.descriptores = descriptores;
	}

	// Will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		return id + ".- " + nombre;
	}
}
