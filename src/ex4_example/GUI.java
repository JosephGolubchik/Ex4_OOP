package ex4_example;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import Coords.Cords;
import Coords.GeoBox;
import Coords.Map;
import Geom.Point3D;
import Robot.Play;

public class GUI implements Runnable {

	private Display display;
	private int width, height;

	private boolean running = false;
	private Thread thread;

	private BufferStrategy bs;
	private Graphics g;
	
	private Play play;
	
	private BufferedImage mapImg, pacImg, fruitImg;
	
	private Point3D start;
	private Point3D end;

	//Input
	private KeyManager keyManager;

	public GUI(Play play, Point3D start, Point3D end){
		keyManager = new KeyManager();
		this.play = play;
		this.start = start;
		this.end = end;

	}

	private void init(){
		
		try {
			mapImg = ImageIO.read(new File("data/Ariel1.png"));
			pacImg = ImageIO.read(new File("data/packman.png"));
			fruitImg = ImageIO.read(new File("data/fruit.png"));
			
			width = mapImg.getWidth();
			height = mapImg.getHeight();
			display = new Display("Packman", width, height);
			display.getFrame().addKeyListener(keyManager);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public void setWidth(int width) {
		this.width = width;}

	public void setHeight(int height) {
		this.height = height;}

	private void tick(){
		keyManager.tick();
	}

	private void render(){
		bs = display.getCanvas().getBufferStrategy();
		if(bs == null){
			display.getCanvas().createBufferStrategy(3);
			return;
		}
		g = bs.getDrawGraphics();
		//Clear Screen
		g.clearRect(0, 0, width, height);
		//Draw Here!
		
		g.drawImage(mapImg, 0, 0, null);
		drawBoard(play);

		//End Drawing!
		bs.show();
		g.dispose();
	}

	public void run(){

		init();

		int fps = 60;
		double timePerTick = 1000000000 / fps;
		double delta = 0;
		long now;
		long lastTime = System.nanoTime();
		long timer = 0;
		int ticks = 0;

		while(running){
			now = System.nanoTime();
			delta += (now - lastTime) / timePerTick;
			timer += now - lastTime;
			lastTime = now;

			if(delta >= 1){
				tick();
				render();
				ticks++;
				delta--;
			}

			if(timer >= 1000000000){
//				System.out.println("FPS: " + ticks);
				ticks = 0;
				timer = 0;
			}
		}

		stop();

	}

	private void drawBoard(Play play) {
		ArrayList<String> board = play.getBoard();
		Iterator<String> it = board.iterator();
		while(it.hasNext()) {
			String line = it.next();
			String[] words = line.split(",");
			String type = words[0];
			Point3D pix_point = pointToPixels(new Point3D(Double.parseDouble(words[2]), Double.parseDouble(words[3]), Double.parseDouble(words[4])));
			if(type.equals("M")) {
				g.drawImage(pacImg, pix_point.ix(), pix_point.iy(), 70, 70, null);
			}
			else if(type.equals("P")) {
				g.drawImage(pacImg, pix_point.ix(), pix_point.iy(), 50, 50, null);
			}
			else if(type.equals("F")) {
				g.drawImage(fruitImg, pix_point.ix(), pix_point.iy(), 20, 20, null);
			}
		}
	}
	
	public KeyManager getKeyManager() {
		return keyManager;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public synchronized void start(){
		if(running)
			return;
		running = true;
		thread = new Thread(this);
		thread.start();
	}

	public synchronized void stop(){
		if(!running)
			return;
		running = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets a point in latitude and longitude and returns a point in pixels on the image
	 * We used stackoverflow.com/questions/38748832/convert-longitude-and-latitude-coordinates-to-image-of-a-map-pixels-x-and-y-coor
	 * @param coordinate
	 * @return
	 */
	public Point3D pointToPixels(Point3D latLonPoint) {
		double mapLatDiff = start.x() - end.x();
		double mapLongDiff = end.y() - start.y();
		
		double latDiff = start.x() - latLonPoint.x();
	    double longDiff = latLonPoint.y() - start.y();
	 
	    int x = (int) (width*(longDiff/mapLongDiff));
	    int y = (int) (height*(latDiff/mapLatDiff));

	    return new Point3D(x, y);
	}
	
	/**
	 * Gets a point in pixels and returns a point in latitude and longitude
	 * We used stackoverflow.com/questions/38748832/convert-longitude-and-latitude-coordinates-to-image-of-a-map-pixels-x-and-y-coor
	 * @param coordinate
	 * @return
	 */
	public Point3D pixelsToPoint(Point3D pixelsPoint) {
		double mapLatDiff = start.x() - end.x();
		double mapLongDiff = end.y() - start.y();

	    double latDiff = pixelsPoint.y() * mapLatDiff/height;
	    double longDiff = pixelsPoint.x() * mapLongDiff/width;
	    
	    double newLat = start.x() - latDiff;
	    double newLong = start.y() + longDiff;
	    
	    return new Point3D(newLat, newLong);
	}

}
