package sna.hw4.model;

public class MyNode {
	
	public enum Type{Paper, Author, Conference};
	
	private String sid;
	private int id;	

	public int getId() {
		return id;
	}

	public String getSid() {
		return sid;
	}

	public Type getType() {
		return type;
	}

	private Type type;
	
	MyNode(){}
	
	MyNode(String id){		
		this.sid = id;		
		if(id.startsWith("p")) this.type = Type.Paper;
		else if (id.startsWith("a")) this.type = Type.Author;
		else if (id.startsWith("c")) this.type = Type.Conference;
		this.id = Integer.parseInt(id.substring(1));
	}	
}