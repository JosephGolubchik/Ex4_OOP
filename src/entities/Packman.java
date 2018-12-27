package entities;

import java.awt.Graphics;

import Geom.Point3D;
import gfx.Assets;

public class Packman extends Robot{
	
	public final int WIDTH = 60;
	public final int HEIGHT = 60;
	
	public Packman(int id, Point3D location, double speed, double radius) {
		super(id,location,speed,radius);
	}

	public void render(Graphics g) {
		g.drawImage(Assets.packman, location.ix(), location.iy(), WIDTH, HEIGHT, null);
	}
}