package net.codejava.ws;


import java.util.List;

public class RequestWrapper {
	 Query query;
	 List<Document> result;

	public RequestWrapper(Query query, List<Document> result) {
		this.query = query;
		this.result = result;
	}
	
	public Query getQuery() {
		return query;
	}
	public void setQuery(Query query) {
		this.query = query;
	}
	
	public List<Document> getResult() {
		return result;
	}
	public void setResult(List<Document> result) {
		this.result = result;
	}
	

	 
	 
	 
}
