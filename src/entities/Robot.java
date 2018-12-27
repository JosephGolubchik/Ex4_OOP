package entities;

import java.awt.Graphics;

import Geom.Point3D;
import gfx.Assets;

public class Robot extends Entity{

	public final int WIDTH = 50;
	public final int HEIGHT = 50;
	
	protected double radius;
	
	public Robot(int id, Point3D location, double speed, double radius) {
		super(id,location,speed);
		this.radius = radius;
	}

	public void render(Graphics g) {
		g.drawImage(Assets.packman, location.ix() - WIDTH/2, location.iy() - HEIGHT/2, WIDTH, HEIGHT, null);
	}

	public double getSpeed() {
		return speed_weight;
	}
	
	public double getRadius() {
		return radius;
	}
}
