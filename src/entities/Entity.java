package entities;

import java.awt.Graphics;

import Geom.Point3D;

public abstract class Entity {

	private int id;
	protected Point3D location;
	protected double speed_weight;

	public Entity(int id, Point3D location, double speed_weight) {
		this.id = id;
		this.location = location;
		this.speed_weight = speed_weight;
	}
	
	public Point3D getLocation() {
		return location;
	};
	public int getId() {
		return id;
	}

}
