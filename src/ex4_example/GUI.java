package ex4_example;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.filechooser.FileSystemView;

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
import entities.Robot;
import gfx.Assets;

public class GUI implements Runnable {

	//Display
	private Display display;
	private int width, height;
	private BufferStrategy bs;
	private Graphics g;

	//Game
	private A_Star_2 star;
	private Thread thread;
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
	public Point3D playerStart;
	
	private double total_time;
	private double time_left;
	private double killed_by_ghost;
	private double score;
	private double out_of_box;

	//Input
	private KeyManager keyManager;
	private MouseManager mouseManager;

	//Flags
	private boolean running = false;
	private boolean playing = false;
	private boolean firstLoaded = false;
	private boolean didFirstPath = false;


	/**
	 * Constructor
	 * @param play Object that is responsible for advancing the game and returning information about it.
	 * @param start Top left GPS coordinate of the map.
	 * @param end Bottom right GPS coordinate of the map.
	 */
	public GUI(){
		keyManager = new KeyManager();
		mouseManager = new MouseManager();
		player = new Player(0, new Point3D(0,0,0), 0, 0);
		dest = new Point3D(0,0);

	}

	/**
	 * Initialize the GUI after the thread has been started:
	 * - Load the needed images.
	 * - Set the pixel width and height of the display to those of the map image.
	 * - Create a display object which will create our GUI window.
	 * - Add mouse and keyboard listeners to our window.
	 * - Load the initial game board, we get the board info as a string from our play object.
	 */
	private void init(){
		Assets.loadImages();
		width = Assets.map.getWidth();
		height =  Assets.map.getHeight();
		display = new Display("Packman", width, height);
		display.getFrame().addKeyListener(keyManager);
		display.getFrame().addMouseListener(mouseManager);
		
		JMenuBar menubar = new JMenuBar();
		JButton openBtn = new JButton("Open");
		JButton runBtn = new JButton("Run");
		
		openBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				playing = false;
				JFileChooser jfc = new JFileChooser("data");
				int returnValue = jfc.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File selectedFile = jfc.getSelectedFile();
					play = new Play(selectedFile.getAbsolutePath());
					String map_data = play.getBoundingBox();
					String[] words = map_data.split(",");
					start = new Point3D(Double.parseDouble(words[2]), Double.parseDouble(words[3]));
					end = new Point3D(Double.parseDouble(words[5]), Double.parseDouble(words[6]));
					loadBoard(play);
					play.setIDs(209195353,2222,3333);
					Point3D initLocation = pixelsToPoint(bestStartPoint());
					play.setInitLocation(initLocation.x(), initLocation.y());
					loadBoard(play);
					play.start();
					firstLoaded = true;
					didFirstPath = false;
				}
			}         
		});  
		
		runBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				playing = true;
			}         
		});  
		
		menubar.add(openBtn);
		menubar.add(runBtn);
		display.getFrame().setJMenuBar(menubar);


	}

	/**
	 * Called every time the window is refreshed:
	 * - Calls the tick function of the keyboard listener.
	 * - Loads the updated game board.
	 * - Calls a functin to recalculate the players path.
	 * - Move function checks which keys are pressed and acts accordingly.
	 * - Calls the game to continue if the game has been started.
	 */
	private void tick(){
		keyManager.tick();
		move();
		if(playing) {
			loadBoard(play);
			playAlgo();
			updateStats();
		}
	}

	/**
	 * checks which keys are pressed and acts accordingly:
	 * - Moves the player up, right, down or left while the corresponding 'aswd' key is pressed.
	 * - Recalculates players optimal path while 'e' is pressed.
	 * - Moves the player according to the latest path without recalculating while 'r' is pressed.
	 * - Moves the player according to the optimal apth and recalculates when needed after 't' is pressed once, stops if 't' is pressed again.
	 */
	private void move() {
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
		if(keyManager.t) {
//			calcPath();
			playing = false;
		}
	}

	/**
	 * Call next move in the game:
	 * - If all fruits have been eaten, do nothing.
	 * - If player is very close to his destination fruit, stop recalculating the path until it has been eaten, to prevent going in circles around the fruit.
	 * - If the player is close to a box corner, recalculate the path to prevent the player from getting stuck in a wall.
	 * - Calculate the angle the player needs to move in and move him using calcAngle() function.
	 */
	public void playAlgo() {
		if(!fruits.isEmpty()) {
			if(!didFirstPath) {
				calcPath();
				didFirstPath = true;
			}
			if(pixelDistance(player.getLocation(), closestGhost()) < 50) {
				calcPath();
				escape();
			}
			if(pixelDistance(player.getLocation(), closestFruit()) > 10 && !radiusInsideBox(player.getLocation(), 10)) {
				calcPath();
			}
			calcAngle();
		}
	}

	public void updateStats() {
		String info = play.getStatistics();
		String[] infos = info.split(" ");
		total_time = Double.parseDouble(infos[8].substring(5));
		score = Double.parseDouble(infos[9].substring(6));
		time_left = Double.parseDouble(infos[12].substring(5));
		killed_by_ghost = Double.parseDouble(infos[16].substring(7));
		out_of_box = Double.parseDouble(infos[20].substring(4));
//		System.out.println(Arrays.toString(infos));
		System.out.println(total_time);
	}
	
	/**
	 * Distance in pixels between two points using the pythagorean theorem.
	 * @param p0 First point.
	 * @param p1 Second point.
	 * @return distance in pixels between the two points.
	 */
	public double pixelDistance(Point3D p0, Point3D p1) {
		int dx = Math.abs(p0.ix() - p1.ix());
		int dy = Math.abs(p0.iy() - p1.iy());
		return Math.sqrt((dx*dx) + (dy*dy));
	}

	/**
	 * Calculates the angle the player needs to move in next:
	 * - If the player reached the last point of his current path, calculate a new path and reset dest_id.
	 * - Destination is decided using dest_id as an index.
	 * - If the player is very close to the next point in his path, increment dest_id.
	 * - The angle the player needs to go in is the azimuth between the players location and the next point's location.
	 */
	private void calcAngle() {
		ArrayList<Point3D> path = star.getPath();
		if(path.size() - dest_id - 1 > 0) {
			dest = path.get(path.size() - dest_id - 1);
			Point3D dest_gis = pixelsToPoint(dest);
			Point3D player_gis = pixelsToPoint(player.getLocation());
			int radius = 5;
			if((player.getLocation().ix() >= dest.ix()-radius && player.getLocation().ix() <= dest.ix()+radius) &&
					(player.getLocation().iy() >= dest.iy()-radius && player.getLocation().iy() <= dest.iy()+radius)) {
				dest_id++;
			}
			player.angle = azimuth(player_gis, dest_gis);
			play.rotate(player.angle);
		}
		else {
			dest_id = 0;
			calcPath();	
		}
	}

	/**
	 * Returns the fruit that the player can get to the fastest.
	 * @return The location of the closest fruit.
	 */
	public Point3D closestFruit() {
		Fruit closest = fruits.get(0);
//		A_Star_2 s0 = new A_Star_2(player.getLocation(), closest.getLocation(),boxes,this,6);
//		s0.algo();
//		double minTime = s0.pathDistance()/player.getSpeed();
//		Iterator<Fruit> it = fruits.iterator();
//		while(it.hasNext()) {
//			Fruit f = it.next();
//			double minTimeFromPac = Double.POSITIVE_INFINITY;
//			Iterator<Packman> pack_it = packmans.iterator();
//			while(pack_it.hasNext()) {
//				Packman pack = pack_it.next();
//				s0 = new A_Star_2(player.getLocation(), f.getLocation(),boxes,this,6);
//				s0.algo();
//				if(s0.pathDistance()/player.getSpeed() < minTime && s0.pathDistance()/player.getSpeed() < timePackmanToFruit(pack, f)) {
//					minTime = s0.pathDistance()/player.getSpeed();
//					closest = f;
//				}
//			}
//		}
		return closest.getLocation();
	}

	/**
	 * Returns the ghost that is closest to the player.
	 * @return The location of the closest ghost.
	 */
	public Point3D closestGhost() {
		if(ghosts.size() == 0) return new Point3D(0,0);
		Ghost closest = ghosts.get(0);
		double minDistance = Double.POSITIVE_INFINITY;
		Iterator<Ghost> it = ghosts.iterator();
		while(it.hasNext()) {
			Ghost g = it.next();
			if(pixelDistance(player.getLocation(),g.getLocation()) < minDistance) {
				minDistance = pixelDistance(player.getLocation(),g.getLocation());
				closest = g;
			}
		}
		return closest.getLocation();
	}

	/**
	 * Azimuth between two points.
	 * @param gps0 First point.
	 * @param gps1 Second point.
	 * @return The azimuth between the two points in degrees.
	 */
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

	/**
	 * Main drawing function:
	 * - Sets up 3 buffers to be used for drawing on the canvas.
	 * - Gets the canvas's graphics object which we will use to draw on the canvas.
	 * - Clears the screen each time before drawing.
	 * - Draws the map image.
	 * - Draws the game entities.
	 * - Draws a straight line from the player to it's destination fruit in red.
	 * - Draws the players current path in white.
	 */
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
		if(firstLoaded && player != null && packmans != null && ghosts != null && fruits != null && boxes != null) {
			drawBoard(player, packmans, ghosts, fruits, boxes);

			g.setColor(Color.red);
			if(!fruits.isEmpty())
				g.drawLine(player.getLocation().ix(), player.getLocation().iy(), closestFruit().ix(), closestFruit().iy());

			if(star != null) {
				ArrayList<Point3D> path = star.getPath();
				drawPath(path);
			}
		}
		
		//End Drawing!
		bs.show();
		g.dispose();
	}

	/**
	 * Called when the GUI thread is started.
	 * Makes sure the graphics are drawn at 60 frames per second.
	 */
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

	/**
	 * Calls the A* algorithm to calculate the shortest path from the player to his destination:
	 * - If there are no fruits left do nothing.
	 * - Sends the player to the fruit it is closest to.
	 * - If the player is very close to the fruit, calculate a very accurate path to the fruit in order to not miss the specific pixel.
	 * - Also do the accurate calculation if the player is close to a ghost and if the player is close to a box corner.
	 * - Otherwise calculate a less accurate but good enough path, this takes significantly less time to calculate.
	 */
	public void calcPath() {
		if(fruits.size() > 0) {
			Point3D player_loc = player.getLocation();
			Point3D dest_loc = closestFruit();
			if(pixelDistance(player_loc, dest_loc) < 30 || radiusNearBoxCorner(player.getLocation(), 10)) {
				star = new A_Star_2(player_loc, dest_loc, boxes, this, 2);
			}
			else {
				star = new A_Star_2(player_loc, dest_loc, boxes, this, 7);
			}

			star.algo();
		}

	}

	public void escape() {
		Point3D closest = closestGhost();
		double ghostAngle = azimuth(closest, player.getLocation());
		play.rotate(ghostAngle+100);

	}

	public double timePlayerToFruit(Fruit fruit) {
		A_Star_2 st = new A_Star_2(player.getLocation(), fruit.getLocation(), boxes, this, 6);
		st.algo();
		double distance = st.pathDistance();
		return distance/player.getSpeed();
	}

	public double timePackmanToFruit(Packman pac, Fruit fruit) {
		return pixelDistance(pac.getLocation(), fruit.getLocation())/pac.getSpeed();
	}

	/**
	 * Finds the best starting point for the player: the fruit which as the most close fruits to it.
	 * @return Location of the best starting point.
	 */
	public Point3D bestStartPoint() {
		int minAvgDist = Integer.MAX_VALUE;
		Point3D bestStart = fruits.get(0).getLocation();
		Iterator<Fruit> it = fruits.iterator();
		while(it.hasNext()) {
			int distSum = 0;
			Fruit f = it.next();
			Iterator<Fruit> it2 = fruits.iterator();
			while(it2.hasNext()) {
				Fruit f2 = it2.next();
				distSum += pixelDistance(f.getLocation(), f2.getLocation());
			}
			int avgDist = distSum/fruits.size();
			if(avgDist < minAvgDist) {
				minAvgDist = avgDist;
				bestStart = f.getLocation();
			}
		}
		return bestStart;
	}

	/**
	 * Checks if the player is within a square radius of a box corner.
	 * @param position Player's position.
	 * @param radius The radius.
	 * @return True or false.
	 */
	public boolean radiusNearBoxCorner(Point3D position, int radius) {
		Iterator<Box> box_it = boxes.iterator();
		while(box_it.hasNext()) {
			Box box = box_it.next();
			Point3D top_left = box.getTop_left_pix_point();
			Point3D bottom_right = box.getBottom_right_pix_point();
			Point3D top_right = new Point3D(bottom_right.ix(), top_left.iy());
			Point3D bottom_left = new Point3D(top_left.ix(), bottom_right.iy());
			Point3D[] corners = {top_left, bottom_right, top_right, bottom_left};
			for (int x = 0; x < 2*radius+1; x++) {
				for (int y = 0; y < 2*radius+1; y++) {
					for (int i = 0; i < corners.length; i++) {
						if((position.ix()-radius+x >= corners[i].ix()-radius && position.ix()-radius+x <= corners[i].ix()+radius) &&
								(position.iy()-radius+y >= corners[i].iy()-radius && position.iy()-radius+y <= corners[i].iy()+radius)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Checks if the player is within a square radius of any point of a box.
	 * @param position Players position.
	 * @param radius The radius.
	 * @return True or false.
	 */
	public boolean radiusInsideBox(Point3D position, int radius) {
		Iterator<Box> box_it = boxes.iterator();
		while(box_it.hasNext()) {
			Box box = box_it.next();
			for (int i = 0; i < 2*radius+1; i++) {
				for (int j = 0; j < 2*radius+1; j++) {
					if(box.isInside(position.ix() - radius + i, position.iy() - radius + j)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Draws the current path.
	 * @param path
	 */
	public void drawPath(ArrayList<Point3D> path) {
		Iterator<Point3D> it = path.iterator();
		g.setColor(Color.white);
		while(it.hasNext()) {
			Point3D point = it.next();
			g.fillRect(point.ix(), point.iy(), 2, 2);
		}
	}

	/**
	 * Reads the game board information string we get from the play object and creates the packmans, ghosts, fruits, boxes and the player.
	 * @param play
	 */
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
		firstLoaded = true;
	}

	/**
	 * Draws the packmans, ghosts, fruits, boxes and the player.
	 * @param player
	 * @param packmans
	 * @param ghosts
	 * @param fruits
	 * @param boxes
	 */
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

	/**
	 * Function that starts the thread.
	 */
	public synchronized void start(){
		if(running)
			return;
		running = true;
		thread = new Thread(this);
		thread.start();
	}

	/**
	 * Function that stops the thread.
	 */
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
	 * Returns a point some amount of meters off in a given azimuth from a given point.
	 * @param pix_point Given point in pixels.
	 * @param meters Amount of meters to move.
	 * @param azimuth The azimuth.
	 * @return A new point in pixels some amount of meters away from the given pixel point.
	 */
	public Point3D addMetersAzimuth(Point3D pix_point, double meters, double azimuth) {
		Point3D gps = pixelsToPoint(pix_point);
		double R = 6371;
		azimuth = Math.toRadians(azimuth);
		double km = meters/1000;

		double lat1 = Math.toRadians(gps.x());
		double lon1 = Math.toRadians(gps.y());

		double lat2 = Math.asin( Math.sin(lat1)*Math.cos(km/R) +
				Math.cos(lat1)*Math.sin(km/R)*Math.cos(azimuth));

		double lon2 = lon1 + Math.atan2(Math.sin(azimuth)*Math.sin(km/R)*Math.cos(lat1),
				Math.cos(km/R)-Math.sin(lat1)*Math.sin(lat2));

		lat2 = Math.toDegrees(lat2);
		lon2 = Math.toDegrees(lon2);
		Point3D new_gis = new Point3D(lat2, lon2);

		return pointToPixels(new_gis);
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

	//Getters

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

	public Player getPlayer() {
		return player;
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

}

