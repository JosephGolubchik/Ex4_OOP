package algo;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;

import Geom.Point3D;
import coords.MyCoords;
import entities.Box;
import entities.Ghost;
import ex4_example.GUI;
import game.GameBoard;

public class A_Star_3 {

	public final static int DELAY_BETWEEN_MOVE = 0;
	public final static int DELAY_AFTER_FINISH = 0;

	public ArrayList<Cell> cells;

	public GameBoard board;

	public ArrayList<Cell> openSet;
	public ArrayList<Cell> closedSet;

	public ArrayList<Cell> path;
	public ArrayList<Box> boxes;

	public Cell start_point, end_point;

	public boolean done;

	public boolean gridCreated;
	public boolean didFirstCalc;
	

	public A_Star_3(Point3D start_point, Point3D end_point, ArrayList<Box> boxes, GameBoard board) {
		this.boxes = boxes;
		this.board = board;
		done = false;
		path = new ArrayList<Cell>();
		cells = board.getGraph().cells;
		
		for (Iterator<Cell> iterator = cells.iterator(); iterator.hasNext();) {
			Cell cell = iterator.next();
			if(cell.x == start_point.ix() && cell.y == start_point.iy()){
				this.start_point = cell;
			}
			else if(cell.x == end_point.ix() && cell.y == end_point.iy()) {
				this.end_point = cell;
			}
		}
	}

	public double heuristic(Cell c0, Cell c1) {
		double dx = Math.abs(c0.x - c1.x);
		double dy = Math.abs(c0.y - c1.y);
		double d = Math.sqrt(dx*dx + dy*dy);
		return d;
	}

	public void algo() {

		openSet = new ArrayList();
		closedSet = new ArrayList();

		addOpen(start_point);

		while(!openSet.isEmpty()) {
			int a = 0;
			int lowest_index = 0;
			int i = 0;
			Iterator<Cell> open_it = openSet.iterator();
			while(open_it.hasNext()) {
				Cell curr = open_it.next();
				if(curr.fCost < openSet.get(lowest_index).fCost) {
					lowest_index = i;
				}
				i++;
			}

			Cell curr_cell = openSet.get(lowest_index);

			if(openSet.get(lowest_index).equals(end_point)) {

				//Find the path
				path = new ArrayList<Cell>();
				Cell temp = curr_cell;
				addPath(temp);
				temp.inPath = true;
				while(temp != null) {
					addPath(temp.prev);
					temp = temp.prev;
				}

//				System.out.println("Done!"); 
				didFirstCalc = true;
				done = true;
				return;
			}

			removeOpen(curr_cell);
			addClosed(curr_cell);

			ArrayList<Cell> neighbours = curr_cell.neighbours;
			Iterator<Cell> it = neighbours.iterator();
			while(it.hasNext()) {
				Cell neigbour = it.next();
				double tempG;
				if(!closedSet.contains(neigbour)) {
			
					tempG = curr_cell.gCost + MyCoords.pixelDistance(new Point3D(curr_cell.x, curr_cell.y), new Point3D(neigbour.x, neigbour.y));

					if(openSet.contains(neigbour)) {
						if(tempG < neigbour.gCost) {
							neigbour.gCost = tempG;
							neigbour.prev = curr_cell;
						}
					}
					else {
						neigbour.gCost = tempG;
						neigbour.prev = curr_cell;
						addOpen(neigbour);
					}

					neigbour.hCost = heuristic(neigbour, end_point);
					neigbour.fCost = neigbour.gCost + neigbour.hCost;

					//Find the path
					path = new ArrayList<Cell>();
					Cell temp = curr_cell;
					addPath(temp);
					temp.inPath = true;
					while(temp != null) {
						addPath(temp.prev);
						temp = temp.prev;
					}
					
					Iterator<Cell> path_it = path.iterator();
					while(path_it.hasNext()) {
						Cell cell = path_it.next();
						cell.inPath = false;
					}
				}
			}

		}

//		System.out.println("No solution"); 
		didFirstCalc = true;
		done = true;
	}

	
	public Point3D getPointBeforeFruit() {
		return new Point3D(path.get(path.size()-2).x, path.get(path.size()-2).y);
	}
	
	public ArrayList<Point3D> getPath(){
		ArrayList<Point3D> points = new ArrayList<Point3D>();

		Iterator<Cell> it = path.iterator();
		while(it.hasNext()) {
			Cell curr = it.next();
			points.add(new Point3D(curr.x, curr.y));
			
		}

		return points;
	}
	
	public double pathDistance() {
		if(didFirstCalc) {
			double distance = 0;
			Iterator<Cell> it0 = path.iterator();
			Iterator<Cell> it1 = path.iterator();
			if(!it1.hasNext())
				return distance;
			it1.next();
			while(it1.hasNext()) {
				Cell curr = it0.next();
				Cell next = it1.next();
				distance += Math.sqrt(Math.abs(curr.x-next.x)*Math.abs(curr.x-next.x) + Math.abs(curr.y-next.y)*Math.abs(curr.y-next.y));
			}
			return distance;
		}
		return 0;
	}
	
	private void addPath(Cell cell) {
		if(cell != null) {
			path.add(cell);
			cell.inPath = true;
		}
	}

	private void addOpen(Cell cell) {
		openSet.add(cell);
		cell.open = true;
	}

	private void addClosed(Cell cell) {
		closedSet.add(cell);
		cell.closed = true;
	}

	private void removeOpen(Cell cell) {
		openSet.remove(cell);
		cell.open = false;
	}

	private void removeClosed(Cell cell) {
		closedSet.remove(cell);
		cell.closed = false;
	}


}
