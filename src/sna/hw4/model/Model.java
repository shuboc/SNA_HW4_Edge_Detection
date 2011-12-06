package sna.hw4.model;


import java.io.*;
import java.util.*;

import sna.hw4.model.MyEdge.Action;
import sna.hw4.model.MyNode.Type;

import edu.uci.ics.jung.graph.*;

import libsvm.*;

public class Model implements Cloneable{
	
	DirectedGraph<MyNode,MyEdge> graph = new DirectedSparseGraph<MyNode, MyEdge>();
	Map<String, MyNode> nodeMap = new HashMap<String, MyNode>();
	List<MyEdge> coauthorList = new ArrayList<MyEdge>();
	
	
	public int getPublishCount(MyNode author, int year){
		assert(author.getType()==Type.Author);		
		int [] count = new int [8];
		for(MyEdge edge : this.graph.getOutEdges(author)){
			if(edge.getAction()==Action.Write){
				count[edge.getYear()-2000]++;
			}
		}
		return count[year-2000];
	}
	
	public List<MyEdge> getCoauthorList() {
		return coauthorList;
	}
	
	public void sortCoauthorList(){
		Collections.sort(this.coauthorList);
	}

	public void loadFile(String filename) {	
		
		try {	
			
			Scanner scanner = new Scanner(new BufferedReader(new FileReader(filename)));						
			scanner.nextLine(); //Ignore the first line
						
			while(scanner.hasNext()){
				
				String sourceId = scanner.next();
				MyNode sourceNode = null;
				if( ! this.nodeMap.containsKey(sourceId) ){
					sourceNode = new MyNode(sourceId);
					this.nodeMap.put(sourceId, sourceNode);
					this.graph.addVertex(sourceNode);
				}
				else sourceNode = this.nodeMap.get(sourceId);
				
				String destinationId = scanner.next();
				MyNode destinationNode = null;
				if( ! this.nodeMap.containsKey(destinationId) ){
					destinationNode = new MyNode(destinationId);
					this.nodeMap.put(destinationId, destinationNode);
					this.graph.addVertex(destinationNode);
				}
				else destinationNode = this.nodeMap.get(destinationId);
				
				String action = scanner.next();
				int year = scanner.nextInt();				
				MyEdge edge = new MyEdge(year, action);
				if(!this.graph.addEdge(edge, sourceNode, destinationNode)) /*System.out.println("error?")*/;			
			}	
			
			scanner.close();
			
			//Create co-author relationship
			for(MyNode paper : this.graph.getVertices()){
				if( paper.getType() == Type.Paper ){
					for(MyNode author1 : this.graph.getPredecessors(paper)){
						if(author1.getType() == Type.Author ){
							for(MyNode author2 : this.graph.getPredecessors(paper)){
								if(author2.getType() == Type.Author ){
									if(author1.getSid() != author2.getSid()){
										
										int year = this.graph.findEdge(author1, paper).getYear();
										MyEdge coauthorEdge = null;
										if(author1.getId()<author2.getId()) coauthorEdge = this.graph.findEdge(author1, author2);
										else coauthorEdge = this.graph.findEdge(author2, author1);
										
										if( coauthorEdge == null ){
											
											coauthorEdge = new MyEdge(year, "COAUTHOR");
											this.coauthorList.add(coauthorEdge);
											
											if(author1.getId()<author2.getId()) this.graph.addEdge(coauthorEdge, author1, author2);
											else this.graph.addEdge(coauthorEdge, author2, author1);
										}
										coauthorEdge.incrementCoauthorCount(year);
										//coauthorEdge
										//coauthorLink.addPaper(paper);
										//this.graph.addEdge(coauthorLink, author2, author1);
									}
								} 
							}
						} 
					}
				}
			}						
			
		} catch (FileNotFoundException e) {			
			e.printStackTrace();			
		}
	}
	
	public void genHeuristicOutput(String filename, int size){
		
		this.sortCoauthorList();		
		try {
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			int count = 0;
			for(MyEdge coauthorEdge : this.getCoauthorList()){
				int x2 = coauthorEdge.getCoauthorCount(2007);
				int x1 = coauthorEdge.getCoauthorCount(2006);
				int x0 = coauthorEdge.getCoauthorCount(2005);
				int v2 = x2 - x1;
				int v1 = x1 - x0;
				int a1 = v2 - v1;
				//System.out.println(coauthorEdge.getTotalCoauthorCount());
				String author1 = this.getGraph().getSource(coauthorEdge).getSid();
				String author2 = this.getGraph().getDest(coauthorEdge).getSid();				
				writer.write(author1 + " " + author2);
				writer.newLine();
				count++;
				if(count > size) break;
			}
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/*
	public void getSVMTrainingData(){
		
		int count = 0; 
		//int trainingDataSize = 3000; //Train data size
		int attrNum = 5; //Use pass attrNum years coauthor counts as attributes and the last one as the answer		
		BufferedWriter writer;
		
		try {
			writer = new BufferedWriter(new FileWriter("train.txt"));

			for(MyEdge coauthorEdge : this.getCoauthorList()){
				
				for(int y=2000; y<2008-attrNum; y++){
					
					if( coauthorEdge.getCoauthorCount(y+attrNum) > 0 ) writer.write("1: ");
					else writer.write("2: ");	
					
					for(int attr = 0; attr<attrNum; attr++){						
						writer.write(String.valueOf(attr+1) + ":" + String.valueOf(coauthorEdge.getCoauthorCount(y+attr)) + " ");
					}
					writer.newLine();
				}
				
			
				count++;
				//if(count > trainingDataSize) break;
			}
			writer.close();
		
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}*/
	
public void getSVMTrainingData2(int trainingDataSize, String trainFile, String testFile){
		
		//Collections.shuffle(coauthorList);
		Collections.sort(coauthorList);
	
		int count = 0; 		 
				
		BufferedWriter trainWriter;
		BufferedWriter testWriter;
		try {
			trainWriter = new BufferedWriter(new FileWriter(trainFile));
			testWriter = new BufferedWriter(new FileWriter(testFile));

			for(MyEdge coauthorEdge : this.getCoauthorList()){
				
				//Use 1/10 data as testing data
				BufferedWriter writer = null;
				if(Math.random() < 0.1) writer = testWriter;
				else writer = trainWriter;
												
				//Use pass 6 years co-author counts as attributes
				int totalAttrNum = 5;
				Random rand = new Random();
				int startYear = 2000 + rand.nextInt(8-totalAttrNum);
				//int totalAttrNum = 6;				
				//int startYear = 2000;
				
				//Class label; use the last year's data as the answer
				if( coauthorEdge.getCoauthorCount(startYear+totalAttrNum) > 0 
						/*|| coauthorEdge.getCoauthorCount(startYear+totalAttrNum+1) > 0 */) writer.write("1 ");
				else writer.write("2 ");
				
				int attrNum = 1;
				
				MyNode author1 = this.graph.getSource(coauthorEdge);
				MyNode author2 = this.graph.getDest(coauthorEdge);				
				
				//Publish count for each single author
				/*for(int y = startYear; y<startYear+totalAttrNum; y++){					
					int publishCount = this.getPublishCount(author1, y);
					writer.write(String.valueOf(attrNum) + ":" + String.valueOf(publishCount) + " ");
					attrNum++;
				}*/				
				
				/*for(int y = startYear; y<startYear+totalAttrNum; y++){					
					int publishCount = this.getPublishCount(author2, y);
					writer.write(String.valueOf(attrNum) + ":" + String.valueOf(publishCount) + " ");
					attrNum++;
				}*/
				
				//Co-author count for the two authors
				for(int y = startYear; y<startYear+totalAttrNum; y++){														
					writer.write(String.valueOf(attrNum) + ":" + String.valueOf(coauthorEdge.getCoauthorCount(y)) + " ");
					attrNum++;
				}
				
				writer.newLine();

				count++;
				if(trainingDataSize > 0){
					if(count > trainingDataSize) break;
				}
			}
			trainWriter.close();
			testWriter.close();
		
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

public void getSVMTrainingData(int trainingDataSize, String trainFile, String testFile){

	Collections.sort(coauthorList);

	int count = 0; 		 
			
	BufferedWriter trainWriter;
	BufferedWriter testWriter;
	try {
		trainWriter = new BufferedWriter(new FileWriter(trainFile));
		testWriter = new BufferedWriter(new FileWriter(testFile));

		for(MyEdge coauthorEdge : this.getCoauthorList()){
						
			//Use 1/10 data as testing data
			BufferedWriter writer = null;
			if(Math.random() < 0.1) writer = testWriter;
			else writer = trainWriter;
											
			//Use pass 6 years co-author counts as attributes
			int totalAttrNum = 3;
			Random rand = new Random();
			//int startYear = 2000 + rand.nextInt(8-totalAttrNum);
			//int totalAttrNum = 6;				
			int startYear = 2004;
			
			//Class label; use the last year's data as the answer
			if( coauthorEdge.getCoauthorCount(startYear+totalAttrNum) > 0 
					/*|| coauthorEdge.getCoauthorCount(startYear+totalAttrNum+1) > 0 */) writer.write("1 ");
			else writer.write("2 ");
			
			int attrNum = 1;
			
			MyNode author1 = this.graph.getSource(coauthorEdge);
			MyNode author2 = this.graph.getDest(coauthorEdge);				
			
			//Publish count for each single author
			/*for(int y = startYear; y<startYear+totalAttrNum; y++){					
				int publishCount = this.getPublishCount(author1, y);
				writer.write(String.valueOf(attrNum) + ":" + String.valueOf(publishCount) + " ");
				attrNum++;
			}*/				
			
			/*for(int y = startYear; y<startYear+totalAttrNum; y++){					
				int publishCount = this.getPublishCount(author2, y);
				writer.write(String.valueOf(attrNum) + ":" + String.valueOf(publishCount) + " ");
				attrNum++;
			}*/			
			
			//Co-author count for the two authors
			/*for(int y = startYear; y<startYear+totalAttrNum; y++){														
				writer.write(String.valueOf(attrNum) + ":" + String.valueOf(coauthorEdge.getCoauthorCount(y)) + " ");
				attrNum++;
			}*/
			writer.write(String.valueOf(attrNum) + ":" + String.valueOf(coauthorEdge.getCoauthorCount(startYear+totalAttrNum-1)) + " ");
			attrNum++;
			
			//Difference
			int [] diff = new int [2];
			for(int y = startYear, i = 0; y<startYear+totalAttrNum-1; y++, i++){
				diff[i] = coauthorEdge.getCoauthorCount(y+1) - coauthorEdge.getCoauthorCount(y);				
				//writer.write(String.valueOf(attrNum) + ":" + String.valueOf(diff[i]) + " ");
				//attrNum++;
			}
			writer.write(String.valueOf(attrNum) + ":" + String.valueOf(diff[1]) + " ");
			attrNum++;
			
			//2nd-order difference
			int diff_2 = diff[1] - diff[0];
			writer.write(String.valueOf(attrNum) + ":" + String.valueOf(diff_2) + " ");
			attrNum++;
			
			//Total coauthor count
			//int coauthorCount = coauthorEdge.getTotalCoauthorCount();
			//writer.write(String.valueOf(attrNum) + ":" + String.valueOf(coauthorCount) + " ");
			//attrNum++;
			
			writer.newLine();

			count++;
			if(trainingDataSize > 0){
				if(count > trainingDataSize) break;
			}
		}
		trainWriter.close();
		testWriter.close();
	
	} catch (IOException e1) {
		e1.printStackTrace();
	}
}	


	public void generateSVMSubmitData(String dataList, String authorList, int maxSize){
				
		BufferedWriter dataWriter;
		BufferedWriter authorWriter;
		
		this.sortCoauthorList();
		
		try {
			dataWriter = new BufferedWriter(new FileWriter(dataList));
			authorWriter = new BufferedWriter(new FileWriter(authorList));
			
			this.sortCoauthorList();
			int count = 0;
			//int maxSize = 50000;

			for(MyEdge coauthorEdge : this.getCoauthorList()){	
								
				Random rand = new Random();
				int totalAttrNum = 3;
				int startYear = 2005;
								
				MyNode author1 = this.graph.getSource(coauthorEdge);
				MyNode author2 = this.graph.getDest(coauthorEdge);	
				authorWriter.write(author1.getSid() + " " + author2.getSid());	
				authorWriter.newLine();
				
				dataWriter.write("2 ");
				
				//3 feature
				int attrNum = 1;
				
				dataWriter.write(String.valueOf(attrNum) + ":" + String.valueOf(coauthorEdge.getCoauthorCount(startYear+totalAttrNum-1)) + " ");
				attrNum++;
				
				//Difference
				int [] diff = new int [2];
				for(int y = startYear, i = 0; y<startYear+totalAttrNum-1; y++, i++){
					diff[i] = coauthorEdge.getCoauthorCount(y+1) - coauthorEdge.getCoauthorCount(y);				
					//writer.write(String.valueOf(attrNum) + ":" + String.valueOf(diff[i]) + " ");
					//attrNum++;
				}
				dataWriter.write(String.valueOf(attrNum) + ":" + String.valueOf(diff[1]) + " ");
				attrNum++;
				
				//2nd-order difference
				int diff_2 = diff[1] - diff[0];
				dataWriter.write(String.valueOf(attrNum) + ":" + String.valueOf(diff_2) + " ");
				attrNum++;
				
				//Total coauthor count
				//int coauthorCount = coauthorEdge.getTotalCoauthorCount();
				//writer.write(String.valueOf(attrNum) + ":" + String.valueOf(coauthorCount) + " ");
				//attrNum++;
				
				dataWriter.newLine();
				count++;
				if(count > maxSize) break;

				
				/*int attrNum = 1;
				
				//Publish count for each single author
				for(int y = startYear; y<startYear+totalAttrNum; y++){					
					int publishCount = this.getPublishCount(author1, y);
					dataWriter.write(String.valueOf(attrNum) + ":" + String.valueOf(publishCount) + " ");
					attrNum++;
				}				
				for(int y = startYear; y<startYear+totalAttrNum; y++){					
					int publishCount = this.getPublishCount(author2, y);
					dataWriter.write(String.valueOf(attrNum) + ":" + String.valueOf(publishCount) + " ");
					attrNum++;
				}
				
				//Co-author count for the two authors
				for(int y = startYear; y<startYear+totalAttrNum; y++){														
					dataWriter.write(String.valueOf(attrNum) + ":" + String.valueOf(coauthorEdge.getCoauthorCount(y)) + " ");
					attrNum++;
				}
				
				dataWriter.newLine();	*/	
				
				
			}			
			authorWriter.close();
			dataWriter.close();
		
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
	}
		
	public DirectedGraph<MyNode, MyEdge> getGraph() {
		return graph;
	}

	public Map<String, MyNode> getNodeMap() {
		return nodeMap;
	}

	public static void main(String[] args)throws IOException {		

	}
}
