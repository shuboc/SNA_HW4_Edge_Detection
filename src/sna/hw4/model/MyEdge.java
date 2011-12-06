package sna.hw4.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MyEdge implements Comparable<MyEdge> {
	
	public enum Action{ Write, Publish, Coauthor }
	
	private int year;
	private Action action;
	
	//private int coauthorCount = 0;
	private int [] coauthorCount = null;
	private int totalCoauthorCount = 0;
	//private Map<Integer, Integer> coauthorCountMap = null;
	//TODO	
	
	/*public Map<Integer, Integer> getCoauthorCountMap() {
		return coauthorCountMap;
	}*/	

	public int getTotalCoauthorCount() {
		return totalCoauthorCount;
	}

	public int getCoauthorCount(int year) {		
		return this.coauthorCount[year-2000];
	}

	/*private void incrementTotalCoauthorCount(){
		this.coauthorCount++;
	}*/
	
	public void incrementCoauthorCount(int year){
		this.coauthorCount[year-2000]++;
		this.totalCoauthorCount++;
		//int count = this.coauthorCountMap.get(year);
		//this.coauthorCountMap.put(year, count+1);
		//this.incrementTotalCoauthorCount();
	}
	
	/*public void addPaper(MyNode paper){
		this.paperList.add(paper);
	}*/
	
	public int getYear() {
		return year;
	}

	public Action getAction() {
		return action;
	}
	
	MyEdge(){}
	
	MyEdge(int year, String action){		
		
		this.year = year;
		
		if(action.contains("WRITE")) 
			this.action = Action.Write;		
		else if(action.contains("PUBLISH")) 
			this.action = Action.Publish;		
		else if(action.contains("COAUTHOR")){ 
			this.action = Action.Coauthor;
			this.coauthorCount = new int [8];
			//Create coauthor count mapping
			/*this.coauthorCountMap = new TreeMap<Integer, Integer>();
			for(int y=2000; y<2010; y++){
				this.coauthorCountMap.put(y, 0);
			}*/
		}
		
	}

	@Override
	public int compareTo(MyEdge o) {
		// TODO Auto-generated method stub
		if ( this.totalCoauthorCount < o.totalCoauthorCount ) return 1;
		else if (this.totalCoauthorCount == o.totalCoauthorCount) return 0;
		else return -1;
	}
	
}
