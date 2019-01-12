package entities;

import java.awt.Graphics;
import java.util.ArrayList;

import Geom.Point3D;
import gfx.Assets;

/**
 * This class represents the player which we control during the game.
 */

public class Player extends Robot{

	public final int WIDTH = 60;
	public final int HEIGHT = 60;
	
	ArrayList<Point3D> path;
	public Point3D dest;
	public int dest_id;
	public double angle;
	
	
	public Player(int id, Point3D location, double speed, double radius) {
		super(id,location,speed,radius);
		angle = 0;
		dest_id = 0;
		dest = this.location;
	}

	public void render(Graphics g) {
		g.drawImage(Assets.player, location.ix() - WIDTH/2, location.iy() - HEIGHT/2, WIDTH, HEIGHT, null);
	}

	// Getters and Setters
	public ArrayList<Point3D> getPath() {
		return path;
	}

	public void setPath(ArrayList<Point3D> path) {
		this.path = path;
	}

	public Point3D getDest() {
		return dest;
	}

	public void setDest(Point3D dest) {
		this.dest = dest;
	}

	public int getDest_id() {
		return dest_id;
	}

	public void setDest_id(int dest_id) {
		this.dest_id = dest_id;
	}

	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}
	
	

}
