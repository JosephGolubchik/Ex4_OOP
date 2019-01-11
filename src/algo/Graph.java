package algo;

import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import Geom.Point3D;
import entities.Box;
import entities.Fruit;
import game.GameBoard;

public class Graph {

	ArrayList<Cell> cells;

	public Graph(GameBoard board) {
		cells.add(new Cell(board.getPlayer().getLocation().ix(), board.getPlayer().getLocation().iy()));

		Iterator<Box> box_it = board.getBoxes().iterator();
		while(box_it.hasNext()) {
			Box b = box_it.next();
			Collections.addAll(cells, new Cell(b.getTop_left_pix_point().ix(), b.getTop_left_pix_point().iy()), 
									  new Cell(b.getTop_right_pix_point().ix(), b.getTop_right_pix_point().iy()),
									  new Cell(b.getBottom_left_pix_point().ix(), b.getBottom_left_pix_point().iy()),
									  new Cell(b.getBottom_right_pix_point().ix(), b.getBottom_right_pix_point().iy()));
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

	public boolean isCross(Point3D src,Point3D dst,ArrayList<Box> boxes) {
		if ((src.ix()==dst.ix())&&(src.iy()==dst.iy())) return true;		
		double minX = Math.min(dst.ix(), src.ix());
		double maxX = Math.max(dst.ix(), src.ix());
		double minY = Math.min(dst.iy(), src.iy());
		double maxY = Math.max(dst.iy(), src.iy());
		boolean findCross = false;
		Iterator<Box> IterBox = boxes.iterator();
		while (IterBox.hasNext()) {
			Box curr_box = IterBox.next();
			if(curr_box.isInside(src) && curr_box.isInside(dst)) findCross = true;
			int P1_x = curr_box.getTop_left_pix_point().ix(), P1_y = curr_box.getTop_left_pix_point().iy();
			int P2_x = curr_box.getTop_right_pix_point().ix(), P2_y = curr_box.getTop_right_pix_point().iy();
			int P3_x = curr_box.getBottom_left_pix_point().ix(), P3_y = curr_box.getBottom_left_pix_point().iy();
			int P4_x = curr_box.getBottom_right_pix_point().ix(), P4_y = curr_box.getBottom_right_pix_point().iy();

			if ((P1_x>minX)&&(P1_x<maxX)) {
				double result_p1p3 = function(P1_x,src,dst,'Y');
				if ((result_p1p3>=P1_y)&&(result_p1p3<=P3_y)) findCross = true;
			}
			if ((P2_x>minX)&&(P2_x<maxX)) {
				double result_p4p2  = function(P2_x,src,dst,'Y');
				if ((result_p4p2>=P1_y)&&(result_p4p2<=P4_y)) findCross =  true;
			}

			if ((P3_y>minY)&&(P3_y<maxY)) {
				double result_p3p2  = function(P3_y,src,dst,'X');
				if ((result_p3p2>=P3_x)&&(result_p3p2<=P4_x)) findCross =  true;
			}
			if ((P1_y>minY)&&(P1_y<maxY)) {
				double result_p1p4  = function(P1_y,src,dst,'X');
				if ((result_p1p4>=P1_x)&&(result_p1p4<=P2_x)) findCross =  true;
			}
		}
		return findCross;
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


