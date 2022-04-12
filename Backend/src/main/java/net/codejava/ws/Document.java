package net.codejava.ws;

public class Document {
	private int group;
	private double[] vector = new double[]{};
	private String title;
	private String context;

	public Document(int group, double[] vector, String title, String context) {
		super();
		this.group = group;
		this.vector = vector;
		this.title = title;
		this.context = context;
	}
	
	public int getGroup() {
		return group;
	}
	public void setGroup(int group) {
		this.group = group;
	}
	public double[] getVector() {
		return vector;
	}
	public void setVector(double[] vector) {
		this.vector = vector;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContext() {
		return context;
	}
	public void setContext(String context) {
		this.context = context;
	}
	
	
	
	
	
	

}
