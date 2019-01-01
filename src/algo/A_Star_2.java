package algo;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;

import Geom.Point3D;
import entities.Box;
import entities.Ghost;
import ex4_example.GUI;

public class A_Star_2 {

	public static int WIDTH;
	public static int HEIGHT;
	public static int COLS;
	public static int ROWS;
	public final static int DELAY_BETWEEN_MOVE = 0;
	public final static int DELAY_AFTER_FINISH = 0;

	public Cell[][] grid;

	public GUI gui;

	public ArrayList<Cell> openSet;
	public ArrayList<Cell> closedSet;

	public ArrayList<Cell> path;
	public ArrayList<Box> boxes;

	public Point3D start_point, end_point;

	public boolean done;

	public boolean gridCreated;
	public boolean didFirstCalc;
	
	public int cell_size;

	public A_Star_2(Point3D start_point, Point3D end_point, ArrayList<Box> boxes, GUI gui, int cell_size) {
		this.cell_size = cell_size;
		this.COLS = gui.getWidth()/cell_size;
		this.ROWS = gui.getHeight()/cell_size;
		this.boxes = boxes;
		this.gui = gui;
		initGrid();
		done = false;
		path = new ArrayList<Cell>();
		this.start_point = new Point3D(start_point.ix()/cell_size, start_point.iy()/cell_size);
		this.end_point = new Point3D(end_point.ix()/cell_size, end_point.iy()/cell_size);
		gridCreated = false;
		didFirstCalc = false;
	}

	public double heuristic(Cell c0, Cell c1) {
		double dx = Math.abs(c0.x - c1.x);
		double dy = Math.abs(c0.y - c1.y);
		double d = Math.sqrt(dx*dx + dy*dy);
		if(c1.ghost) d = 1000;
		return d;
	}

	public void algo() {

		openSet = new ArrayList();
		closedSet = new ArrayList();

		if(!gridCreated) {
			for (int i = 0; i < grid.length; i++) {
				for (int j = 0; j < grid[0].length; j++) {
					grid[i][j] = new Cell(i,j);
				}
			}

			for (int i = 0; i < grid.length; i++) {
				for (int j = 0; j < grid[0].length; j++) {
					grid[i][j].addNeighbours(grid);
				}
			}
			
			gridCreated = true;
		}
		Cell start = grid[start_point.ix()][start_point.iy()];
		Cell end = grid[end_point.ix()][end_point.iy()];
		start.wall = false;
		end.wall = false;

		addOpen(start);

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

			if(openSet.get(lowest_index).equals(end)) {

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
				if(!closedSet.contains(neigbour) && !neigbour.wall) {
					if( (neigbour.x == curr_cell.x+1 && neigbour.y == curr_cell.y+1) ||
							(neigbour.x == curr_cell.x-1 && neigbour.y == curr_cell.y+1) ||
							(neigbour.x == curr_cell.x+1 && neigbour.y == curr_cell.y-1) ||
							(neigbour.x == curr_cell.x-1 && neigbour.y == curr_cell.y-1) ) {
						tempG = curr_cell.gCost + Math.sqrt(2);
					}
					else {
						tempG = curr_cell.gCost + 1;
					}

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

					neigbour.hCost = heuristic(neigbour, end);
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

		System.out.println("No solution"); 
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
//			if(curr != path.get(path.size()-2)) {
				points.add(new Point3D(curr.x*cell_size, curr.y*cell_size, 0));
//			}
//			else {
//				int temp_x = curr.x;
//				int temp_y = curr.y;
//				curr = it.next();
//				while(!(temp_x == curr.x && temp_y == curr.y)) {
//					Point3D next_point = gui.addMetersAzimuth(new Point3D(temp_x, temp_y), 300, gui.getPlayer().angle);
//					points.add(new Point3D(temp_x, temp_y, 0));
//					temp_x = next_point.ix();
//					temp_y = next_point.iy();
//				}
//			}
			
		}

		return points;
	}
	
	public double pathDistance() {
		if(didFirstCalc) {
			double distance = 0;
			Iterator<Cell> it0 = path.iterator();
			Iterator<Cell> it1 = path.iterator();
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

	public void initGrid() {
		grid = new Cell[COLS][ROWS]; 
		for (int x = 0; x < COLS; x++) {
			for (int y = 0; y < ROWS; y++) {
				grid[x][y] = new Cell(x,y);
			}
		}
		
		
		
	}

	class Cell {
		int x, y;
		double fCost, gCost, hCost;
		boolean open;
		boolean closed;
		boolean inPath;
		boolean wall;
		boolean ghost;
		ArrayList<Cell> neighbours;
		Cell prev;

		public Cell(int x, int y) {
			this.x = x;
			this.y = y;
			fCost = gCost = hCost = 0;
			open = false;
			closed = false;
			inPath = false;
			neighbours = new ArrayList<Cell>();
			prev = null;
			wall = false;
			ghost = false;

			if(boxes != null) {
				Iterator<Box> box_it = boxes.iterator();
				while(box_it.hasNext()) {
					Box box = box_it.next();
					if(box.isInside(x*cell_size, y*cell_size)) {
						this.wall = true;
					}
				}
			}	
			
			if(gui.getGhosts() != null) {
				Iterator<Ghost> ghost_it = gui.getGhosts().iterator();
				while(ghost_it.hasNext()) {
					Ghost ghost = ghost_it.next();
					int margin = 30;
					if( (x*cell_size >= ghost.getLocation().ix() - margin && x*cell_size <= ghost.getLocation().ix() + margin) &
						(y*cell_size >= ghost.getLocation().iy() - margin && y*cell_size <= ghost.getLocation().iy() + margin) ) {
						this.ghost = true;
					}
				}
			}


		}

		public void addNeighbours(Cell[][] grid) {
			if(y > 0) {
				neighbours.add(grid[x][y-1]);
			}
			if(y < grid[0].length - 1) {
				neighbours.add(grid[x][y+1]);
			}
			if(x > 0) {
				neighbours.add(grid[x-1][y]);
			}
			if(x < grid.length - 1) {
				neighbours.add(grid[x+1][y]);
			}
			if(x > 0 && y > 0) {
				neighbours.add(grid[x-1][y-1]);
			}
			if(x > 0 && y < grid[0].length - 1) {
				neighbours.add(grid[x-1][y+1]);
			}
			if(x < grid.length - 1 && y > 0) {
				neighbours.add(grid[x+1][y-1]);
			}
			if(x < grid.length - 1 && y < grid[0].length - 1) {
				neighbours.add(grid[x+1][y+1]);
			}
		}

		public boolean equals(Object obj) {
			if (obj != null && getClass() == obj.getClass()) {
				Cell cell = (Cell)obj;
				return x == cell.x && y == cell.y;
			}
			return false;
		}

		public void render(Graphics g, boolean margins) {
			int x_margin, y_margin;
			int cell_width, cell_height;
			int top_left_x, top_left_y;
			int mid_x, mid_y;
			if(margins) {
				x_margin = (int)(2.0/27.0 * WIDTH);
				y_margin = (int)(2.0/27.0 * HEIGHT);
				cell_width = (int)(3.0/27.0 * WIDTH);
				cell_height = (int)(3.0/27.0 * HEIGHT);
				top_left_x = x_margin + x*(cell_width+x_margin);
				top_left_y = y_margin + y*(cell_height+y_margin);
				mid_x = top_left_x + cell_width/2;
				mid_y = top_left_y + cell_height/2;
			}
			else {
				x_margin = 0;
				y_margin = 0;
				cell_width = WIDTH/COLS;
				cell_height = HEIGHT/ROWS;
				top_left_x = x*cell_width;
				top_left_y = y*cell_height;
				mid_x = top_left_x + cell_width/2;
				mid_y = top_left_y + cell_height/2;
			}
			if(wall)
				g.setColor(Color.decode("#000000")); //Black
			else if(inPath)
				g.setColor(Color.decode("#88c7f7")); //Blue
			else if(open) 
				g.setColor(Color.decode("#81bc3a")); //Green
			else if(closed)
				g.setColor(Color.decode("#bc4f39")); //Red
			else
				g.setColor(Color.decode("#dddddd"));
			g.fillRect(x_margin + x*(cell_width+x_margin), y_margin + y*(cell_height+y_margin), cell_width, cell_height);

			if(!margins) {
				g.setColor(Color.decode("#999999"));
				g.drawRect(x_margin + x*(cell_width+x_margin), y_margin + y*(cell_height+y_margin), cell_width-1, cell_height-1);
			}

			//Draw f,g,h costs on screen inside cells
			//			gui.drawStringCentered("f: "+fCost, Color.white, mid_x, top_left_y + (int)(cell_width/2.2), (int)(cell_width/3.3));
			//			gui.drawStringCentered("g: "+gCost, Color.white, mid_x, top_left_y + (int)(cell_width/1.4), (int)(cell_width/3.8));
			//			gui.drawStringCentered("h: "+hCost, Color.white, mid_x, top_left_y + (int)(cell_width*1.02), (int)(cell_width/3.8));
		}
	}


}
