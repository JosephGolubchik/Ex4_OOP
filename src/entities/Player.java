package entities;

import java.awt.Graphics;

import Geom.Point3D;
import gfx.Assets;

public class Player extends Robot{

	public final int WIDTH = 60;
	public final int HEIGHT = 60;
	
	public Player(int id, Point3D location, double speed, double radius) {
		super(id,location,speed,radius);
	}

	public void render(Graphics g) {
		g.drawImage(Assets.player, location.ix() - WIDTH/2, location.iy() - HEIGHT/2, WIDTH, HEIGHT, null);
	}
	

}
