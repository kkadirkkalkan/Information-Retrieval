package net.codejava.ws;

public class Query {
	private String context;
	private double[] vector = new double[]{};

	public Query(String context, double[] vector ) {
		super();
		this.context = context;
		this.vector = vector;
	}
	
	public String getContext() {
		return context;
	}
	public void setContext(String context) {
		this.context = context;
	}
	public double[] getVector() {
		return vector;
	}
	public void setVector(double[] vector) {
		this.vector = vector;
	}
	
	
	
	
	

}
