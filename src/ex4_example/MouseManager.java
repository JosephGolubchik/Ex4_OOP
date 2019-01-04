package ex4_example;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import Geom.Point3D;

public class MouseManager implements MouseListener{

	public int mouseX, mouseY;
	
	public MouseManager() {
		mouseX = mouseY = -1;
	}

	public Point3D mousePosPoint() {
		return new Point3D(mouseX, mouseY, 0);
	}
	
	public void mouseClicked(MouseEvent e) {
	}
	public void mouseEntered(MouseEvent e) {
	}
	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		this.mouseX = e.getX();
		this.mouseY = e.getY();
	}

	public void mouseReleased(MouseEvent e) {
	}

}
