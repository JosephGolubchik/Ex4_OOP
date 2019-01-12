package ex4_example;

import java.awt.MouseInfo;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import Geom.Point3D;

public class MouseManager implements MouseListener{

	public int mouseX, mouseY;
	public GUI gui;
	
	public MouseManager(GUI gui) {
		this.gui = gui;
		mouseX = mouseY = 100;
	}

	public Point3D mousePosPoint() {
		return new Point3D(mouseX, mouseY, 0);
	}
	
	public void mouseClicked(MouseEvent e) {
		int mouseX=MouseInfo.getPointerInfo().getLocation().x-gui.display.getCanvas().getLocationOnScreen().x;
		int mouseY=MouseInfo.getPointerInfo().getLocation().y-gui.display.getCanvas().getLocationOnScreen().y;

	}
	public void mouseEntered(MouseEvent e) {
	}
	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

}
