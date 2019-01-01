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

	public boolean isInside(int x, int y) {
		int margin = 3;
		if( (x >= top_left_pix_point.ix() - margin && x <= bottom_right_pix_point.ix() + margin) &
				(y >= top_left_pix_point.iy() - margin && y <= bottom_right_pix_point.iy() + margin) ) {
			return true;
		}
		return false;
	}

}
