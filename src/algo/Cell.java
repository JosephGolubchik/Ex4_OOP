package algo;

import java.util.ArrayList;

public class Cell {
	int x, y;
	double fCost, gCost, hCost;
	boolean open;
	boolean closed;
	boolean inPath;
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
	}

	public boolean equals(Object obj) {
		if (obj != null && getClass() == obj.getClass()) {
			Cell cell = (Cell)obj;
			return x == cell.x && y == cell.y;
		}
		return false;
	}
	
	public void addNeighbour(Cell cell) {
		neighbours.add(cell);
	}

}