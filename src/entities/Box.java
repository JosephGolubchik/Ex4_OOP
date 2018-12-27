package entities;

import java.awt.Graphics;

import Geom.Point3D;

public class Box{
	
	private Point3D top_left_pix_point;
	private Point3D bottom_right_pix_point;
	private int width, height;
	
	public Box(Point3D top_left_pix_point, Point3D bottom_right_pix_point, int width, int height) {
		this.top_left_pix_point = top_left_pix_point;
		this.bottom_right_pix_point = bottom_right_pix_point;
		this.width = width;
		this.height = height;
	}
	
	public void render(Graphics g) {
		g.fillRect(top_left_pix_point.ix(), top_left_pix_point.iy(), width, height);
	}
	
}
