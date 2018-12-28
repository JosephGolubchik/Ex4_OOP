package algo;

import java.util.ArrayList;
import java.util.Iterator;

import Geom.Point3D;
import entities.Box;

public class A_Star {
	
	public static boolean nodesCreated = false;
	public static int mapWidth, mapHeight;
	public static Node[] nodes;
	public static ArrayList<Box> boxes;
	public static Node start, end;
	
	public static ArrayList<Point3D> solve(int mapWidth, int mapHeight, ArrayList<Box> boxes, Point3D start, Point3D end){
		init(mapWidth, mapHeight, boxes, start, end);
		ArrayList<Point3D> path = new ArrayList<Point3D>();
		
		//Initialize nodes
		for (int x = 0; x < mapWidth; x++) {
			for (int y = 0; y < mapHeight; y++) {
				nodes[y * mapWidth + x].visited = false;
				nodes[y * mapWidth + x].parent = null;
				nodes[y * mapWidth + x].globalGoal = Double.POSITIVE_INFINITY;
				nodes[y * mapWidth + x].localGoal = Double.POSITIVE_INFINITY;
			}
		}
		
		Node currNode = A_Star.start;
		A_Star.start.localGoal = 0;
		A_Star.start.globalGoal = A_Star.start.distance(A_Star.end);
		
		ArrayList<Node> notTestedNodes = new ArrayList<Node>();
		notTestedNodes.add(A_Star.start);
		
		currNode = notTestedNodes.get(0);
		currNode.visited = true;
		
		
		while(!notTestedNodes.isEmpty() && !currNode.equals(A_Star.end)) {
			notTestedNodes.sort(null);
			
			while(!notTestedNodes.isEmpty() && notTestedNodes.get(0).visited) {
				notTestedNodes.remove(0);
			}
			
			if(notTestedNodes.isEmpty()) 
				break;
			
			Iterator<Node> node_it = currNode.neighbours.iterator();
			while(node_it.hasNext()) {
				Node neighbour = node_it.next();
				
				if(!neighbour.visited && !neighbour.obstacle) {
					notTestedNodes.add(neighbour);
				}
				
				double possiblyLowerGoal = currNode.localGoal + currNode.distance(neighbour);
				
				if (possiblyLowerGoal < neighbour.localGoal)
				{
					neighbour.parent = currNode;
					neighbour.localGoal = possiblyLowerGoal;

					neighbour.globalGoal = neighbour.localGoal + neighbour.distance(A_Star.end);
				}
			}
		}
		
		currNode = A_Star.end;
		while(currNode != null) {
			path.add(new Point3D(currNode.x, currNode.y, 0));
			currNode = currNode.parent;
		}
		
		return path;
	}
	
	public static void init(int mapWidth, int mapHeight, ArrayList<Box> boxes, Point3D start, Point3D end) {
		A_Star.boxes = boxes;
		A_Star.mapWidth = mapWidth;
		A_Star.mapHeight = mapHeight;
		
		A_Star.start = new Node(start.ix(), start.iy());
		A_Star.end = new Node(end.ix(), end.iy());
		
		createNodes(A_Star.nodes, A_Star.mapWidth, A_Star.mapHeight, A_Star.boxes);		
	}
	
	private static void createNodes(Node[] nodes, int mapWidth, int mapHeight, ArrayList<Box> boxes) {
		A_Star.nodes = new Node[mapWidth * mapHeight];
		nodesCreated = true;
		for (int x = 0; x < mapWidth; x++) {
			for (int y = 0; y < mapHeight; y++) {
				A_Star.nodes[(y * mapWidth) + x] = new Node();
				A_Star.nodes[(y * mapWidth) + x].x = x;
				A_Star.nodes[(y * mapWidth) + x].y = y;
				
				boolean searching = true;
				Iterator<Box> it = boxes.iterator();
				while(it.hasNext() && searching) {
					Box curr_box = it.next();
					if(curr_box.isInside(x, y)) {
						A_Star.nodes[(y * mapWidth) + x].obstacle = true;
						searching = false;
					}
				}
				
				if(y > 0) {
					A_Star.nodes[(y * mapWidth) + x].neighbours.add(A_Star.nodes[((y-1) * mapWidth) + x]);
				}
				if(y < mapHeight - 1) {
					A_Star.nodes[(y * mapWidth) + x].neighbours.add(A_Star.nodes[((y+1) * mapWidth) + x]);
				}
				if(x > 0) {
					A_Star.nodes[(y * mapWidth) + x].neighbours.add(A_Star.nodes[(y * mapWidth) + (x-1)]);
				}
				if(x < mapWidth - 1) {
					A_Star.nodes[(y * mapWidth) + x].neighbours.add(A_Star.nodes[(y * mapWidth) + (x+1)]);
				}
				
			}
		}
	}
	
	
	
}

class Node implements Comparable<Node>{
	public boolean obstacle;
	public boolean visited;
	public double globalGoal;
	public double localGoal;
	public int x;
	public int y;
	public ArrayList<Node> neighbours;
	public Node parent;
	
	public Node(int x, int y) {
		this.x = x;
		this.y = y;
		this.obstacle = false;
		this.visited = false;
		neighbours = new ArrayList<Node>();
	}
	
	public Node() {
		this.x = 0;
		this.y = 0;
		this.visited = false;
		this.obstacle = false;
		neighbours = new ArrayList<Node>();
	}
	
	public boolean equals(Node otherNode) {
		return x == otherNode.x && y == otherNode.y;
	}
	
	public double distance(Node otherNode) {
		double dx = Math.abs(x - otherNode.x);
		double dy = Math.abs(y - otherNode.y);
		return Math.sqrt(dx*dx + dy*dy);
	}

	@Override
	public int compareTo(Node otherNode) {
		return (int)(otherNode.globalGoal - globalGoal);
	}
}