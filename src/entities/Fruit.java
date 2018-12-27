package entities;

import java.awt.Graphics;

import Geom.Point3D;
import gfx.Assets;

public class Fruit extends Entity{

	protected final int WIDTH = 40;
	protected final int HEIGHT = 40;
	
	public Fruit(int id, Point3D location, double weight) {
		super(id,location,weight);
	}

	public void render(Graphics g) {
		g.drawImage(Assets.fruit, location.ix() - WIDTH/2, location.iy() - HEIGHT/2, WIDTH, HEIGHT, null);
	}

	public double getWeight() {
		return speed_weight;
	}
}
