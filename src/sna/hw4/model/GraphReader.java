package sna.hw4.model;

import java.io.*;
import java.util.*;
import edu.uci.ics.jung.graph.*;

public class GraphReader {

	public static DirectedGraph<MyNode, MyEdge> getGraphFromFile(String filename) {
		
		try {
			
			DirectedGraph<MyNode, MyEdge> graph = new DirectedSparseGraph<MyNode, MyEdge>();			
			Scanner scanner = new Scanner(new BufferedReader(new FileReader(filename)));
			
			scanner.nextLine();
			String id = scanner.next();
			
			
			return graph; 
					
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	
	
}
