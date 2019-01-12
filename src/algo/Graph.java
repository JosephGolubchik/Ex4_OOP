package algo;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import Geom.Point3D;
import entities.Box;
import entities.Fruit;
import game.GameBoard;

public class Graph {

	GameBoard board;
	ArrayList<Cell> cells;

	public Graph(GameBoard board) {
		this.board = board;
		
		cells = new ArrayList<Cell>();
		cells.add(new Cell(board.getPlayer().getLocation().ix(), board.getPlayer().getLocation().iy()));

		int margin = 10;
		
		Iterator<Box> box_it = board.getBoxes().iterator();
		while(box_it.hasNext()) {
			Box b = box_it.next();
			Collections.addAll(cells, new Cell(b.getTop_left_pix_point().ix() - margin, b.getTop_left_pix_point().iy() - margin), 
									  new Cell(b.getTop_right_pix_point().ix() + margin, b.getTop_right_pix_point().iy() - margin),
									  new Cell(b.getBottom_left_pix_point().ix() - margin, b.getBottom_left_pix_point().iy() + margin),
									  new Cell(b.getBottom_right_pix_point().ix() + margin, b.getBottom_right_pix_point().iy() + margin));
		}

		Iterator<Fruit> fruit_it = board.getFruits().iterator();
		while(fruit_it.hasNext()) {
			Fruit f = fruit_it.next();
			cells.add(new Cell(f.getLocation().ix(), f.getLocation().iy()));
		}


		Iterator<Cell> first_point_it = cells.iterator();
		while(first_point_it.hasNext()) {
			Cell first_point = first_point_it.next();
			Iterator<Cell> sec_point_it = cells.iterator();
			while(sec_point_it.hasNext()) {
				Cell sec_point = sec_point_it.next();
				
				if (!isCross(new Point3D(first_point.x, first_point.y), new Point3D(sec_point.x, sec_point.y), board.getBoxes())) {
					first_point.addNeighbour(sec_point);
					sec_point.addNeighbour(first_point);
				}
			}
		}
	}

	public void drawGraph(Graphics g) {
		Iterator<Cell> cell_it = cells.iterator();
		while(cell_it.hasNext()) {
			Cell cell = cell_it.next();
			Iterator<Cell> neigh_it = cell.neighbours.iterator();
			while(neigh_it.hasNext()) {
				Cell neigh = neigh_it.next();
				g.setColor(new Color(255,255,255,50));
				g.drawLine(cell.x, cell.y, neigh.x, neigh.y);
			}
		}
	}
	
	public boolean doesLineIntersectAnyBox(Point3D p0, Point3D p1) {
		Iterator<Box> box_it = board.getBoxes().iterator();
		while(box_it.hasNext()) {
			Box curr_box = box_it.next();
			if(doLineBoxIntersect(p0, p1, curr_box)) return true;
		}
		return false;
	}
	
	public boolean doLineBoxIntersect(Point3D p0, Point3D p1, Box box) {
		if( (doLinesIntersect(p0, p1, box.getTop_left_pix_point(), box.getTop_right_pix_point())) ||
			(doLinesIntersect(p0, p1, box.getTop_right_pix_point(), box.getBottom_right_pix_point())) ||
			(doLinesIntersect(p0, p1, box.getBottom_right_pix_point(), box.getBottom_left_pix_point())) ||
			(doLinesIntersect(p0, p1, box.getBottom_left_pix_point(), box.getTop_left_pix_point())) ||
			p0.equals(p1)) {
			return true;
		}
		return false;
	}
	
	public boolean doLinesIntersect(Point3D p0, Point3D p1, Point3D p2, Point3D p3) {
		Box box_line_0 = lineToBox(p0, p1);
		Box box_line_1 = lineToBox(p2, p3);
		if((box_line_0.getTop_left_pix_point().ix() < box_line_1.getBottom_right_pix_point().ix()) &&
		   (box_line_0.getBottom_right_pix_point().ix() > box_line_1.getTop_left_pix_point().ix()) &&
		   (box_line_0.getTop_left_pix_point().iy() < box_line_1.getBottom_right_pix_point().iy()) &&
		   (box_line_0.getBottom_right_pix_point().iy() > box_line_1.getTop_left_pix_point().iy())) {
			return true;
		}
		return false;
		   
	}
	
	public Box lineToBox(Point3D c0, Point3D c1) {
		int minX = Math.min(c0.ix(), c1.ix());
		int maxX = Math.max(c0.ix(), c1.ix());
		int maxY = Math.max(c0.iy(), c1.iy());
		int minY = Math.max(c0.iy(), c1.iy());
		int width = Math.abs(c0.ix() - c1.ix());
		int height = Math.abs(c0.iy() - c1.iy());
		return new Box(new Point3D(minX, minY), new Point3D(maxX, maxY), width, height);
	}
	
	public boolean isCross(Point3D src,Point3D dst,ArrayList<Box> boxes) {
		if ((src.ix()==dst.ix())&&(src.iy()==dst.iy())) return true;		
		double minX = Math.min(dst.ix(), src.ix());
		double maxX = Math.max(dst.ix(), src.ix());
		double minY = Math.min(dst.iy(), src.iy());
		double maxY = Math.max(dst.iy(), src.iy());
		boolean findCross = false;
		Iterator<Box> IterBox = boxes.iterator();
		while (IterBox.hasNext() && !findCross) {
			Box curr_box = IterBox.next();
			if(curr_box.isInside(src) && curr_box.isInside(dst)) findCross = true;
			Point3D p1 = curr_box.getTop_left_pix_point();
			Point3D p2 = curr_box.getTop_right_pix_point();
			Point3D p3 = curr_box.getBottom_left_pix_point();
			Point3D p4 = curr_box.getBottom_right_pix_point();

			if ((p1.ix()>minX)&&(p1.ix()<maxX)) {
//				double result_p1p3 = function(p1.ix(),src,dst,'Y');
				double result_p1p3 = lineValueAtX(src,dst,p1.ix());
				if ((result_p1p3>=p1.iy())&&(result_p1p3<=p3.iy())) findCross = true;
			}
			if ((p2.ix()>minX)&&(p2.ix()<maxX)) {
//				double result_p4p2  = function(p2.ix(),src,dst,'Y');
				double result_p4p2 = lineValueAtX(src,dst,p2.ix());
				if ((result_p4p2>=p1.iy())&&(result_p4p2<=p4.iy())) findCross =  true;
			}

			if ((p3.iy()>minY)&&(p3.iy()<maxY)) {
//				double result_p3p2  = function(p3.iy(),src,dst,'X');
				double result_p3p2  = lineXAtValue(src,dst,p3.iy());
				if ((result_p3p2>=p3.ix())&&(result_p3p2<=p4.ix())) findCross =  true;
			}
			if ((p1.iy()>minY)&&(p1.iy()<maxY)) {
//				double result_p1p4  = function(p1.iy(),src,dst,'X');
				double result_p1p4  = lineXAtValue(src,dst,p1.iy());
				if ((result_p1p4>=p1.ix())&&(result_p1p4<=p2.ix())) findCross =  true;
			}
		}
		return findCross;
	}
	
	private double lineValueAtX(Point3D p0, Point3D p1, int x) {
		double dy = p1.y() - p0.y();
		double dx = p1.x() - p0.x();
		double m = dy/dx;
		return m*x + p0.y() - m*p0.x();
	}
	
	private double lineXAtValue(Point3D p0, Point3D p1, int y) {
		double dy = p1.y() - p0.y();
		double dx = p1.x() - p0.x();
		if(dx == 0) dx = 0.0001;
		double m = dy/dx;
		return (y - p0.y() + m*p0.x())/m;
	}
	
	private static double function(double XorY,Point3D src,Point3D dst,char type) {
		double minX = Math.min(dst.ix(), src.ix());
		double maxX = Math.max(dst.ix(), src.ix());
		double mehane = (maxX-minX);
		if (mehane==0) mehane = 0.00001;
		double m;
		if (dst.ix()> src.ix())
			m = (dst.iy()-src.iy())/mehane;
		else m = (src.iy()-dst.iy())/mehane;
		if (type == 'Y')
			return (m*(XorY-src.ix()))+src.iy();
		else {
			if (m==0) m=0.00001;
			return ((XorY-src.iy())/m)+src.iy();
		}
	}
}


