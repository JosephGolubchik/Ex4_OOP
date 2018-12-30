package ex4_example;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Iterator;

import Coords.Cords;
import Coords.LatLonAlt;
import Geom.Point3D;
import Robot.Play;
import algo.A_Star_2;
import entities.Box;
import entities.Fruit;
import entities.Ghost;
import entities.Packman;
import entities.Player;
import gfx.Assets;

public class GUI implements Runnable {

	private Display display;
	private int width, height;

	private boolean running = false;
	private Thread thread;

	private BufferStrategy bs;
	private Graphics g;

	private Play play;

	private Point3D start;
	private Point3D end;

	private Player player;
	private ArrayList<Packman> packmans;
	private ArrayList<Ghost> ghosts;

	private ArrayList<Fruit> fruits;
	private ArrayList<Box> boxes;
	
	private Point3D dest;
	private int dest_id = 0;

	//Input
	private KeyManager keyManager;
	private MouseManager mouseManager;
	
	private A_Star_2 star;

	public GUI(Play play, Point3D start, Point3D end){
		keyManager = new KeyManager();
		mouseManager = new MouseManager();
		this.play = play;
		this.start = start;
		this.end = end;
		player = new Player(0, new Point3D(0,0,0), 0, 0);
		dest = new Point3D(0,0);

	}

	private void init(){

		Assets.loadImages();
		width = Assets.map.getWidth();
		height =  Assets.map.getHeight();
		display = new Display("Packman", width, height);
		display.getFrame().addKeyListener(keyManager);
		display.getFrame().addMouseListener(mouseManager);


	}

	public void setWidth(int width) {
		this.width = width;}

	public void setHeight(int height) {
		this.height = height;}

	private void tick(){
		keyManager.tick();
		loadBoard(play);
		move();
	}

	private void move() {
		//		if(mouseManager.mousePosPoint().x() != -1) {
		//			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" ); 
		//			Point3D player_gis_pos = pixelsToPoint(player.getLocation());
		//			Point3D mouse_gis_pos = pixelsToPoint(mouseManager.mousePosPoint());
		//			double[] player_loc = { player_gis_pos.x(), player_gis_pos.y(), 0};
		//			double[] mouse_loc = { mouse_gis_pos.x(), mouse_gis_pos.y(), 0 };
		//			double angle = Cords.azmDist(player_loc, mouse_loc)[0];
		//			play.rotate(angle);
		//		}

//		if(keyManager.down) {
//			g.setColor(Color.green);
//			if(player.getLocation().ix() == player.dest.ix() && player.getLocation().iy() == player.dest.iy()) {
//				if(player.dest_id <= path.size())
//					player.dest_id++;
//				else
//					return;
//			}
//			Point3D dest = path.get(player.dest_id);
//			Point3D dest_gis = pixelsToPoint(dest);
//			
//			g.drawRect(dest_gis.ix(), dest_gis.iy(), 100, 100);
//			System.out.println(dest_gis); 
//			
//			Point3D player_gis = pixelsToPoint(player.getLocation());
//			double[] dest_coords = { dest_gis.x(), dest_gis.y(), 0 };
//			double[] player_coords = { player_gis.x(), player_gis.y(), 0 };
//			player.angle = Cords.azmDist(dest_coords, player_coords)[0];
//			System.out.println(player.angle); 
//			play.rotate(player.angle);
//		}
		if(keyManager.down)
			play.rotate(0);
		if(keyManager.right)
			play.rotate(90);
		if(keyManager.up)
			play.rotate(180);
		if(keyManager.left)
			play.rotate(270);
		if(keyManager.e)
			calcPath();
		if(keyManager.r)
			calcAngle();
	}

	private void calcAngle() {
		ArrayList<Point3D> path = star.getPath();
		if(path.size() - dest_id - 1 > 0) {
			dest = path.get(path.size() - dest_id - 1);
			Point3D dest_gis = pixelsToPoint(dest);
			Point3D player_gis = pixelsToPoint(player.getLocation());
			dest_id++;
			player.angle = azimuth(player_gis, dest_gis);
			play.rotate(player.angle);
		}
		else {
			calcPath();
			dest_id = 0;
		}
	}
	
	public double azimuth(Point3D gps0, Point3D gps1) {
		int radius = 6371000;
		double lat0 = Math.toRadians(gps0.x()); double lon0 = Math.toRadians(gps0.y());
		double lat1 = Math.toRadians(gps1.x()); double lon1 = Math.toRadians(gps1.y());
		double dlat = Math.toRadians(gps1.x() - gps0.x());
		double dlon = Math.toRadians(gps1.y() - gps0.y());
		double azimuth = Math.atan2(Math.sin(dlon) * Math.cos(lat1), Math.cos(lat0) * Math.sin(lat1) - Math.sin(lat0)
						 * Math.cos(lat1) * Math.cos(dlon));
		return Math.toDegrees(azimuth);
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

		g.drawImage(Assets.map, 0, 0, null);
		drawBoard(player, packmans, ghosts, fruits, boxes);
		
		g.fillOval(dest.ix(), dest.iy(), 20, 20);
		
		g.setColor(Color.red);
		if(!fruits.isEmpty())
			g.drawLine(player.getLocation().ix(), player.getLocation().iy(), fruits.get(0).getLocation().ix(), fruits.get(0).getLocation().iy());
		
		
		if(star != null) {
			ArrayList<Point3D> path = star.getPath();
			drawPath(path);
		}
		
//		drawPath();

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
	
	public void calcPath() {
		Point3D player_loc = player.getLocation();
		Point3D dest_loc = fruits.get(0).getLocation();
		star = new A_Star_2(player_loc, dest_loc, boxes, this);
		
		star.algo();
	}
	
	public void drawPath(ArrayList<Point3D> path) {
		Iterator<Point3D> it = path.iterator();
		g.setColor(Color.white);
		while(it.hasNext()) {
			Point3D point = it.next();
			g.fillRect(point.ix(), point.iy(), 2, 2);
		}
		
	}

	private void loadBoard(Play play) {
		ArrayList<String> board = play.getBoard();
		Iterator<String> it = board.iterator();
		packmans = new ArrayList<Packman>();
		ghosts = new ArrayList<Ghost>();
		fruits = new ArrayList<Fruit>();
		boxes = new ArrayList<Box>();
		while(it.hasNext()) {
			String line = it.next();
			String[] words = line.split(",");

			String type = words[0];
			int id = Integer.parseInt(words[1]);

			if(!type.equals("B")) {
				LatLonAlt gis_point = new LatLonAlt(Double.parseDouble(words[2]), Double.parseDouble(words[3]), Double.parseDouble(words[4]));
				Point3D pix_point = pointToPixels(gis_point);
				double speed_weight = Double.parseDouble(words[5]);
				if(type.equals("M")) {
					double radius = Double.parseDouble(words[6]);
					player = new Player(id, pix_point, speed_weight, radius);
				}
				else if(type.equals("P")) {
					double radius = Double.parseDouble(words[6]);
					Packman packman = new Packman(id, pix_point, speed_weight, radius);
					packmans.add(packman);
				}
				else if(type.equals("G")) {
					double radius = Double.parseDouble(words[6]);
					Ghost ghost = new Ghost(id, pix_point, speed_weight, radius);
					ghosts.add(ghost);
				}
				else if(type.equals("F")) {
					Fruit fruit = new Fruit(id, pix_point, speed_weight);
					fruits.add(fruit);
				}
			}
			else if(type.equals("B")) {
				LatLonAlt top_left_gis_point = new LatLonAlt(Double.parseDouble(words[2]), Double.parseDouble(words[3]), Double.parseDouble(words[4]));
				LatLonAlt bottom_right_gis_point = new LatLonAlt(Double.parseDouble(words[5]), Double.parseDouble(words[6]), Double.parseDouble(words[7]));
				Point3D top_left_pix_point = pointToPixels(top_left_gis_point);
				Point3D bottom_right_pix_point = pointToPixels(bottom_right_gis_point);
				int box_width = bottom_right_pix_point.ix() - top_left_pix_point.ix();
				int box_height = bottom_right_pix_point.iy() - top_left_pix_point.iy();
				Box box = new Box(top_left_pix_point, bottom_right_pix_point, box_width, box_height);
				this.boxes.add(box);
			}
		}
	}
	private void drawBoard(Player player, ArrayList<Packman> packmans, ArrayList<Ghost> ghosts, ArrayList<Fruit> fruits, ArrayList<Box> boxes) {
		player.render(g);
		Iterator<Packman> pack_it = packmans.iterator();
		while(pack_it.hasNext())
			pack_it.next().render(g);
		Iterator<Ghost> ghost_it = ghosts.iterator();
		while(ghost_it.hasNext())
			ghost_it.next().render(g);
		Iterator<Fruit> fruit_it = fruits.iterator();
		while(fruit_it.hasNext())
			fruit_it.next().render(g);
		Iterator<Box> box_it = boxes.iterator();
		while(box_it.hasNext())
			box_it.next().render(g);
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
	
	public ArrayList<Packman> getPackmans() {
		return packmans;
	}

	public ArrayList<Ghost> getGhosts() {
		return ghosts;
	}

	public ArrayList<Fruit> getFruits() {
		return fruits;
	}

	public ArrayList<Box> getBoxes() {
		return boxes;
	}

}

