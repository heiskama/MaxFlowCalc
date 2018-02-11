import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Calculate maximum flow in a flow network in O(VE^2) time. Implementation of the Edmonds-Karp algorithm.
 * 
 * @author Matti Heiskanen
 * @version 1.0, 02/01/18
 * @see https://en.wikipedia.org/wiki/Edmonds%E2%80%93Karp_algorithm
 */
public class MaxFlowCalc {
	
	private static final int WHITE = 0, GREY = 1, BLACK = 2;
	
	double[][] capacity, residualCapacity, flow;
	int[] parent, color, distance;
	int nodeCount, source, sink;
	double[] minCapacity;
	double maxFlow;
	List<Integer> queue;

	public static void main(String[] args) throws IOException {
		MaxFlowCalc network;
		
		// Process input parameters
		if (args.length < 2) {
			printHelp();
			return;
		}
		else if (args[0].equalsIgnoreCase("--file") || args[0].equalsIgnoreCase("-f")) {
			File file = new File(args[1]);
			if (!file.exists()) {
				System.err.println("File doesn't exists.");
				return;
			}
			else {
				network = new MaxFlowCalc(file);
			}
		}
		else if (args[0].equalsIgnoreCase("--random") || args[0].equalsIgnoreCase("-r")) {
			int nodes = Integer.parseInt(args[1]);
			if (nodes < 2) { 
				System.err.println("There must be at least 2 nodes");
				return;
			}
			else {
				double[][] random = getRandomNetwork(nodes);
				network = new MaxFlowCalc(random);
			}
		}
		else {
			printHelp();
			return;
		}
		
		/* Handle printing parameters and print the selected
		 * (a) Capacity matrix
		 * (b) Flow matrix
		 * (c) Capacity representation
		 * (d) Flow representation
		 */
		if (args.length == 4 && (args[2].equalsIgnoreCase("--print") || args[2].equalsIgnoreCase("-p"))) {
			if (args[3].toLowerCase().contains("a")) { // If parameters contain "a", print the capacity matrix
				System.out.println("Capacity matrix");
				network.printCapacityMatrix();
				System.out.println();
			}
			if (args[3].toLowerCase().contains("b")) { // If parameters contain "b", print the flow matrix
				System.out.println("Flow matrix");
				network.printFlowMatrix();
				System.out.println();
			}
			if (args[3].toLowerCase().contains("c")) { // If parameters contain "c", print the capacity representation
				System.out.println("Initial flow");
				network.printCapacity();
				System.out.println();
			}
			if (args[3].toLowerCase().contains("d")) { // If parameters contain "d", print the flow representation
				System.out.println("Final flow");
				network.printFlow();
				System.out.println();
			}
		}
		else if (args.length == 2) {
			// No print parameters specified
		}
		else {
			System.err.println("Unknown parameters.");
			return;
		}
		
		System.out.println("Maximum flow is " + network.maxFlow);
	}

	public MaxFlowCalc(double[][] array) {
		this.capacity = array;
		this.nodeCount = capacity.length;
		this.maximumFlow();
	}

	public MaxFlowCalc(File file) {
		this(readMatrix(file));
	}
	
	public double getMaximumFlow() {
		return this.maxFlow;
	}
	
	public double[][] getCapacity() {
		return this.capacity;
	}
	
	public double[][] getResidualCapacity() {
		return this.residualCapacity;
	}
	
	public double[][] getFlow() {
		return this.flow;
	}

	/**
	 * Calculate maximum flow for the network. Leverage findPath() method, which helps to trace
	 * the newly found path and increase it's flow. Repeat finding paths and increasing the flow until
	 * no paths remain. Maximum flow has been attained at that point.
	 */
	private void maximumFlow() {
		this.source = 0;
		this.sink = capacity.length - 1;
	
		// Residual capacity is initialized to be the maximum capacity of the network
		this.residualCapacity = new double[this.nodeCount][this.nodeCount];
		for (int i = 0; i < this.residualCapacity.length; i++)
			for (int j = 0; j < this.residualCapacity.length; j++)
				this.residualCapacity[i][j] = capacity[i][j];
	
		// No flow in the beginning
		this.flow = new double[this.nodeCount][this.nodeCount];
		
		this.color = new int[this.nodeCount];
		this.parent = new int[this.nodeCount];
		this.distance = new int[this.nodeCount];
		this.minCapacity = new double[this.nodeCount];
	
		// Color the nodes white and set the parent as -1 meaning no parent.
		for (int i = 0; i < this.nodeCount; i++) {
			this.color[i] = WHITE;
			this.parent[i] = -1;
			this.distance[i] = Integer.MAX_VALUE;
			this.minCapacity[i] = Double.MAX_VALUE;
		}
	
		this.color[0] = GREY;
		this.parent[0] = -1;
		this.distance[0] = 0;
	
		while (findPath()) {
			this.maxFlow = this.maxFlow + this.minCapacity[this.sink];
			int i = this.sink;
			int u;
			while (i != this.source) { // Trace the new path and increase flow
				u = this.parent[i];
				this.flow[u][i] = this.flow[u][i] + this.minCapacity[sink];
				this.residualCapacity[u][i] = this.residualCapacity[u][i] - this.minCapacity[sink];
				i = this.parent[i];
			}
		}
		
	}

	/**
	 * Find a path, with free capacity, from source node to sink node. Return true if such a path exists, otherwise return false.
	 * Search through nodes in breadth-first order. After method execution, the path can be traced by following parent[] array 
	 * references backwards. The minimum capacity among the arcs of the path is saved in minCapacity[sink]. 
	 */
	private boolean findPath() {
		this.queue = new ArrayList<>();
		this.queue.add(0);
		
		/* Nodes are first discovered and later examined, which means discovering it's neighbor nodes.
		 * 
		 * White node = a node which has not yet been discovered
		 * Grey node = a node which has been discovered, but has not yet been examined
		 * Black node = a node which has been examined
		 */
		
		// In the beginning all nodes are white
		for (int i = 0; i < this.nodeCount; i++) {
			this.color[i] = WHITE;
			this.minCapacity[i] = Double.MAX_VALUE;
		}
		
		// First the source node is discovered
		this.color[source] = GREY;
		
		/*
		 * Discovered nodes are added to a queue from which they are picked for examination one by one.
		 * Examination means checking if there are connections to undiscovered nodes. Newly discovered nodes receive
		 * the examined node as their parent node, and minCapacity[] is updated to contain the minimum capacity on the
		 * path to the node.
		 */
		while (!this.queue.isEmpty()) {
			int u = this.queue.remove(0);
			for (int i = u, j = u; j < this.nodeCount; j++) {
				if (this.residualCapacity[i][j] > 0 && this.color[j] == WHITE) { // Search for undiscovered neighbor nodes
					this.color[j] = GREY;
					this.parent[j] = u;
					this.distance[j] = this.distance[u] + 1; // Distance from source node to debug breadth-first search.
					this.minCapacity[j] = Math.min(this.minCapacity[i], this.residualCapacity[i][j]);
					this.queue.add(j);
				}
			}
			this.color[u] = BLACK;
			if (u == sink)
				return true;
		}
		return false;
	}
	

	
	// Helper methods
	
	/**
	 * Get random integer in a range 
	 * @param min (inclusive)
	 * @param max (inclusive)
	 */
	private static int randomInteger(int min, int max) {
	   int range = (max - min) + 1;     
	   return (int)(Math.random() * range) + min;
	}
	
	/**
	 * Get random double from 1..10
	 */
	private static double randomDouble() {
		return (double)randomInteger(1,10);
	}

	/**
	 * Read the network from a file. Assume the first row contains the network size followed by network definition.
	 * Return an array defined in the file.
	*/
	private static double[][] readMatrix(File file) {
		BufferedReader input = null;
		double[][] output = null;
		
		try {
			input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String row = input.readLine().trim();
			int size = 0;
		
			// Read network size from the first row
			try {
				size = Integer.parseInt(row);
				if (size < 2) {
					System.err.println("Network size must be at least 2.");
					return null;
				}
			} catch (NumberFormatException nfe) {
				System.err.println("Number format error in network size definition: " + row);
				return null;
			}
		
			output = new double[size][size];
		
			// Read node information from the rest of the rows
			int i = 0;
			while ((row = input.readLine()) != null) {
				String str[] = row.split(" ");
				double array[] = new double[str.length];
				for(int j = 0; j < array.length; j++) {
					try {
						array[j] = Double.parseDouble(str[j]);
					}
					catch (NumberFormatException nfe) {
						System.err.println("Error in node definition: " + str[j] + ", row " + (i+2));
						return null;
					}
				}
				for (int k = 0; k < array.length; k++) {
					output[k][i] = array[k];
				}
				i = i + 1;
			}
		} catch (IOException ioex) {
			System.err.println("Error reading matrix from file.");
			return null;
		} finally {
			try {
				input.close();
			}
			catch (IOException ioex) {
				System.err.println("Error closing the file reader.");
			}
		}
		return output;
	}

	/**
	 *  Generate a random network with the following properties:
	 *   - There exists a path to sink node from every node
	 *   - There exists a path from source node to every node
	 *   - No cycles (a path or a connection from a node to itself) exist in the network
	 *   
	 *  Node count must be 2 or more.
	 */
	public static double[][] getRandomNetwork(int nodeCount) {
		if (nodeCount < 2)
			return null;
		
		double[][] array = new double[nodeCount][nodeCount];
	
		/* Imagine the nodes to be in a row. Create 1-2 connections from every node to a node further
		 * ahead in the row. After that create 1-2 connections to every node from a previous node.
		 */
		for (int i = 0; i < nodeCount-1; i++) {
			for (int j = 0; j < randomInteger(1,2); j++) {
				array[i][randomInteger(i+1, nodeCount-1)] = randomDouble();
			}
		}
		for (int i = 1; i < nodeCount; i++) {
			for (int j = 0; j < randomInteger(1,2); j++) {
				array[randomInteger(0, i-1)][i] = randomDouble();
			}
		}
		return array;
	}

	// Methods for printing and debugging

	/** 
	 * Print help message of this program
	 */
	private static void printHelp() {
		System.out.println("Usage: MaxFlowCalc --file|-f|--random|-r filename|numberofnodes (--print|-p print parameters)");
		System.out.println("");
		System.out.println("Print parameters a,b,c and d control the output.");
		System.out.println("If the parameter string contains the char, print (in this order):");
		System.out.println("(a) Capacity matrix");
		System.out.println("(b) Flow matrix");
		System.out.println("(c) Capacity representation");
		System.out.println("(d) Flow representation");
		System.out.println("");
		System.out.println("For example: java MaxFlowCalc -file nodes.txt");
		System.out.println("   Read the network from file \"nodes.txt\" and print only the maximum flow");
		System.out.println("For example: java MaxFlowCalc --random 15 --print abcd");
		System.out.println("   Generate a random network with 15 nodes and print information with all options: a,b,c and d");
	}

	/**
	 * Print an array. Used to print capacity and flow matrices.
	 */
	private static void printMatrix(double[][] array) {
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[i].length; j++) {
				if (array[j][i] == 0)
				System.out.print("***" + " ");
				else
				System.out.print(array[j][i] + " ");
			}
			System.out.println("");
		}
	}
	
	/**
	 * Print the capacity matrix
	 */
	public void printCapacityMatrix() {
		printMatrix(this.capacity);
	}

	/**
	 * Print the flow matrix
	 */
	public void printFlowMatrix() {
		printMatrix(this.flow);
	}

	/**
	 * Print a representation of the network's flow. Show usage and max capacity of every arc.
	 * Arcs with no flow are omitted. Nodes are numbered 1, 2, ... etc.
	 * 
	 * The output has the format start node->end node (usage/max capacity)
	 * For example, nodes 1 has a connection to node 2. Max capacity is 9 with current usage of 5.
	 * The output would be: 1->2 (5/9)
	 */
	public void printFlow() {
		for (int i = 0; i < this.capacity.length; i++) {
			String row = "";
			boolean emptyrow = true;
			
			for (int j = 0; j < this.capacity[i].length; j++) {
				try {
					if (this.flow[i][j] != 0) {
						emptyrow = false;
						row = row + (i+1) + "->" + (j+1) + " (" +  this.flow[i][j] + "/" + this.capacity[i][j] + ") ";
					}
				} catch (ArrayIndexOutOfBoundsException oobe) {
					System.err.println("Array index out of bounds. i: " + i + " j: " + j);
					return;
				}
			}
			
			if (emptyrow == false)
				System.out.println(row);
		}
	}

	/**
	 * Print a compact representation of the capacity of the network. Nodes are numbered 1, 2, ...
	 */
	public void printCapacity() {
		for (int i = 0; i < this.capacity.length; i++) {
			String row = "";
			boolean emptyrow = true;
			
			for (int j = 0; j < this.capacity[i].length; j++) {
				if (this.capacity[i][j] != 0) {
					emptyrow = false;
					row = row + (i+1) + "->" + (j+1) + " (" + 0 + "/" + this.capacity[i][j] + ") ";
				}
			}
			
			if (emptyrow == false)
				System.out.println(row);
		}
	}

	/**
	 * Print node distances from the source node. This can be used to debug breadth-first search.
	 */
	private void printMatrixDistances(double[][] array) {
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[i].length; j++) {
				if (array[j][i] == 0)
					System.out.print("***" + " ");
				else
					System.out.print(" " + this.distance[i] + "  ");
			}
			System.out.println("");
		}
	}

}