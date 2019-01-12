package entities;

import java.awt.Color;
import java.awt.Graphics;

import Geom.Point3D;

public class Box{

	public Point3D getTop_left_pix_point() {
		return top_left_pix_point;}

	public void setTop_left_pix_point(Point3D top_left_pix_point) {
		this.top_left_pix_point = top_left_pix_point;}

	public Point3D getBottom_right_pix_point() {
		return bottom_right_pix_point;}

	public void setBottom_right_pix_point(Point3D bottom_right_pix_point) {
		this.bottom_right_pix_point = bottom_right_pix_point;}

	public int getWidth() {
		return width;}

	public void setWidth(int width) {
		this.width = width;}

	public int getHeight() {
		return height;}

	public void setHeight(int height) {
		this.height = height;}

	private Point3D top_left_pix_point;
	private Point3D bottom_right_pix_point;
	private Point3D top_right_pix_point;
	private Point3D bottom_left_pix_point;
	
	private int width, height;

	public Box(Point3D top_left_pix_point, Point3D bottom_right_pix_point, int width, int height) {
		this.top_left_pix_point = top_left_pix_point;
		this.bottom_right_pix_point = bottom_right_pix_point;
		this.top_right_pix_point = new Point3D(bottom_right_pix_point.ix(), top_left_pix_point.iy());
		this.bottom_left_pix_point = new Point3D(top_left_pix_point.ix(), bottom_right_pix_point.iy());
		this.width = width;
		this.height = height;
	}

	public Point3D getTop_right_pix_point() {
		return top_right_pix_point;
	}

	public Point3D getBottom_left_pix_point() {
		return bottom_left_pix_point;
	}

	public void render(Graphics g) {
		g.setColor(new Color(50,50,50));
		g.fillRect(top_left_pix_point.ix(), top_left_pix_point.iy(), width, height);
//		g.setColor(Color.orange);
//		g.fillOval(top_left_pix_point.ix() - 4, top_left_pix_point.iy() - 4, 8, 8);
//		g.setColor(Color.blue);
//		g.fillOval(top_right_pix_point.ix() - 4, top_right_pix_point.iy() - 4, 8, 8);
//		g.setColor(Color.green);
//		g.fillOval(bottom_left_pix_point.ix() - 4, bottom_left_pix_point.iy() - 4, 8, 8);
//		g.setColor(Color.MAGENTA);
//		g.fillOval(bottom_right_pix_point.ix() - 4, bottom_right_pix_point.iy() - 4, 8, 8);
//		g.setColor(new Color(50,50,50));
	}

	public boolean isInside(Point3D point) {
		int margin = 0;
		if( (point.ix() >= top_left_pix_point.ix() - margin && point.ix() <= bottom_right_pix_point.ix() + margin) &
				(point.iy() >= top_left_pix_point.iy() - margin && point.iy() <= bottom_right_pix_point.iy() + margin) ) {
			return true;
		}
		return false;
	}

}
