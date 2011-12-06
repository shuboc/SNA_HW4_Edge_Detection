package sna.hw4.coauthor;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import sna.hw4.model.Model;
import sna.hw4.model.MyEdge;
import sna.hw4.model.MyNode;
import sna.hw4.model.MyNode.Type;

public class CoauthorPrediction {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Model model = new Model();
		model.loadFile("train_2000-2007_all-edgelist.csv");
		
		int pCount = 0;
		int aCount = 0;
		int cCount = 0;
		
		int coCount = 0;
		int wCount = 0;
		int pubCount = 0;
		
		for(MyNode node : model.getGraph().getVertices()){
			switch(node.getType()){
				case Paper:
					pCount++;
					break;
				case Author:
					aCount++;
					break;
				case Conference:
					cCount++;
					break;					
			}
		}
		
		for(MyEdge edge : model.getGraph().getEdges()){
			switch(edge.getAction()){
				case Coauthor:
					coCount++;
					break;
				case Write:
					wCount++;
					break;
				case Publish:
					pubCount++;
					break;
			}
		}
			
		System.out.println("total vertex count: " + model.getGraph().getVertexCount());
		System.out.println("author count:       " + aCount);
		System.out.println("paper count:        " + pCount);		
		System.out.println("conference count:   " + cCount);
		
		System.out.println("total edge count: " + model.getGraph().getEdgeCount());
		System.out.println("coauthor count: " + coCount);
		System.out.println("write count: " + wCount);
		System.out.println("publish count: " + pubCount);
		
		//model.sortCoauthorList();
		
		/*int count = 0;
		for(MyEdge coauthor : model.getCoauthorList()){
			for(int y=2000; y<=2007; y++){
				System.out.println(String.valueOf(y) + ": " + coauthor.getCoauthorCount(y));
			}
			System.out.println();
			count++;
			if(count>20) break;
		}*/
		
		//model.genHeuristicOutput("heuristic_30000.txt", 30000);
		//model.getSVMTrainingData(1000000, "database.train", "database.test");
		//model.getSVMTrainingData2(5000, "database.train", "database.test");
		
		model.generateSVMSubmitData("hw4.test", "hw4.author", 4000000);
		
		System.out.println("Done!");
				
	}

}
