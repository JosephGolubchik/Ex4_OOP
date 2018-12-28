package algo;

import java.util.ArrayList;
import java.util.Iterator;

import Geom.Point3D;
import entities.Box;

public class A_Star {
	
	public static boolean nodesCreated = false;
	public static int mapWidth, mapHeight;
	public static Node[] nodes = new Node[mapWidth * mapHeight];
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
		A_Star.start.globalGoal = Node.distance(A_Star.start, A_Star.end);
		
		ArrayList<Node> notTestedNodes = new ArrayList<Node>();
		notTestedNodes.add(A_Star.start);
		
		while(!notTestedNodes.isEmpty()) {
			notTestedNodes.sort(c);
		}
		
		return path;
	}
	
	public static void init(int mapWidth, int mapHeight, ArrayList<Box> boxes, Point3D start, Point3D end) {
		A_Star.boxes = boxes;
		A_Star.mapWidth = mapWidth;
		A_Star.mapHeight = mapHeight;
		
		A_Star.start = new Node(start.ix(), start.iy());
		A_Star.end = new Node(end.ix(), end.iy());
		
		if(!nodesCreated)
			createNodes(A_Star.nodes, A_Star.mapWidth, A_Star.mapHeight, A_Star.boxes);		
	}
	
	private static void createNodes(Node[] nodes, int mapWidth, int mapHeight, ArrayList<Box> boxes) {
		nodesCreated = true;
		for (int x = 0; x < mapWidth; x++) {
			for (int y = 0; y < mapHeight; y++) {
				nodes[y * mapWidth + x].x = x;
				nodes[y * mapWidth + x].y = y;
				
				boolean searching = true;
				Iterator<Box> it = boxes.iterator();
				while(it.hasNext() && searching) {
					Box curr_box = it.next();
					if(curr_box.isInside(x, y)) {
						nodes[y * mapWidth + x].obstacle = true;
						searching = false;
					}
				}
				
				if(y > 0) {
					nodes[y * mapWidth + x].neighbours.add(nodes[(y-1) * mapWidth + x]);
				}
				if(y < mapHeight - 1) {
					nodes[y * mapWidth + x].neighbours.add(nodes[(y+1) * mapWidth + x]);
				}
				if(x > 0) {
					nodes[y * mapWidth + x].neighbours.add(nodes[y * mapWidth + (x-1)]);
				}
				if(x < mapWidth - 1) {
					nodes[y * mapWidth + x].neighbours.add(nodes[y * mapWidth + (x+1)]);
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
		return (int)(globalGoal - otherNode.globalGoal);
	}
}