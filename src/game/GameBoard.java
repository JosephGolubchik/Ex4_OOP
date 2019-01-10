package game;

import java.util.ArrayList;
import java.util.Iterator;

import Coords.LatLonAlt;
import Geom.Point3D;
import Robot.Play;
import algo.A_Star_2;
import coords.MyCoords;
import entities.Box;
import entities.Fruit;
import entities.Ghost;
import entities.Packman;
import entities.Player;

public class GameBoard {

	// IDS
	private final long ID_1 = 308019306; // Eli Haimov
	private final long ID_2 = 209195353; // Yosef Golubchik
	private final long ID_3 = 307993030; // Elad Cohen
	
	// Objects
	private Player player;
	private ArrayList<Packman> packmans;
	private ArrayList<Ghost> ghosts;
	private ArrayList<Fruit> fruits;
	private ArrayList<Box> boxes;
	private A_Star_2 star;
	private Play play;
	
	// Map
	private int height;
	private int width;
	private Point3D start;
	private Point3D end;
	public Point3D playerStart;
	
	// Flags
	private boolean firstLoaded = false;
	private boolean didFirstPath = false;
	private boolean escaping = false;
	
	// Board Stats
	private String game_file_name = "";
	private double total_time = 0;
	private double time_left = 100000;
	private double killed_by_ghost = 0;
	private double score = 0;
	private double out_of_box = 0;
	
	
//	private Point3D dest;
//	private int dest_id = 0;
	
	/**
	 * Creates a play object from a given map file path, then calls initBoard to initialize the gameBoard.
	 * @param play contains information about the state of the game.
	 */
	public GameBoard(String map_file_path) {
		play = new Play(map_file_path);
		game_file_name = map_file_path.substring(map_file_path.lastIndexOf('\\')+9, map_file_path.length()-4);
		initBoard();
	}
	
	/**
	 * Initializes the GameBoard:
	 * Sets the maps bounding box, the ID's, find's best starting point and sets it and calls loadBoard to do the rest.
	 */
	private void initBoard() {
		height = gfx.Assets.map.getHeight();
		width = gfx.Assets.map.getWidth();
		
		String map_data = play.getBoundingBox();
		String[] words = map_data.split(",");
		start = new Point3D(Double.parseDouble(words[2]), Double.parseDouble(words[3]));
		end = new Point3D(Double.parseDouble(words[5]), Double.parseDouble(words[6]));
		
		play.setIDs(ID_1, ID_2, ID_3);
		
		loadBoard();
		Point3D initLocation = pixelsToPoint(bestStartPoint());
		play.setInitLocation(initLocation.x()+0.0005, initLocation.y()+0.0005);
		
		loadBoard();
	}

	public void nextMove() {
		loadBoard();
		playAlgo();
		updateStats();
	}
	
	/**
	 * Starts the game.
	 */
	public void startGame() {
		play.start();
		firstLoaded = true;
		didFirstPath = false;
	}
	
	/**
	 * Loads the gameBoard objects: packmans, fruits, ghosts and boxes using information from the play object.
	 */
	public void loadBoard() {
		packmans = new ArrayList<Packman>();
		ghosts = new ArrayList<Ghost>();
		fruits = new ArrayList<Fruit>();
		boxes = new ArrayList<Box>();
		
		ArrayList<String> board = play.getBoard();
		Iterator<String> it = board.iterator();
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
					if(player == null) {
						double radius = Double.parseDouble(words[6]);
						player = new Player(id, pix_point, speed_weight, radius);
					}
					else {
						player.setLocation(pix_point);
					}
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
		firstLoaded  = true;
	}
	
	/**
	 * Finds the best starting point for the player: the fruit which has the most close fruits to it.
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
				distSum += MyCoords.pixelDistance(f.getLocation(), f2.getLocation());
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
	 * Call next move in the game:
	 * - If all fruits have been eaten, do nothing.
	 * - If player is very close to his destination fruit, stop recalculating the path until it has been eaten, to prevent going in circles around the fruit.
	 * - If the player is close to a box corner, recalculate the path to prevent the player from getting stuck in a wall.
	 * - Calculate the angle the player needs to move in and move him using calcAngle() function.
	 */
	public void playAlgo() {
		if(!fruits.isEmpty()) {
			calcPath();
			calcAngle();
			didFirstPath = true;
		}
	}
	
	/**
	 * Constructs an array containing the game statistics.
	 * @return An array of the game statistics.
	 */
	public double[] getStats() {
		double[] stats = new double[5];
		stats[0] = total_time;
		stats[1] = score;
		stats[2] = time_left;
		stats[3] = killed_by_ghost;
		stats[4] = out_of_box;
		return stats;
	}
	
	/**
	 * Gets game statistics from play object and updates the board fields.
	 */
	private void updateStats() {
		String info = play.getStatistics();
		String[] infos = info.split(" ");
		total_time = Double.parseDouble(infos[8].substring(5));
		score = Double.parseDouble(infos[9].substring(7, infos[9].length()-1));
		time_left = Double.parseDouble(infos[11].substring(5, infos[11].length()-1));
		killed_by_ghost = Double.parseDouble(infos[14].substring(7, infos[14].length()-1));
		out_of_box = Double.parseDouble(infos[17].substring(4));
	}
	
	/**
	 * Calculates the angle the player needs to move in next:
	 * - If the player reached the last point of his current path, calculate a new path and reset dest_id.
	 * - Destination is decided using dest_id as an index.
	 * - If the player is very close to the next point in his path, increment dest_id.
	 * - The angle the player needs to go in is the azimuth between the players location and the next point's location.
	 */
	private void calcAngle() {
//		if(!escaping) {
			ArrayList<Point3D> path = star.getPath();
			path.add(closestFruit());
			player.setPath(path);
			if(player.getPath().size() - player.getDest_id() - 1 > 0) {
				player.setDest(player.getPath().get(player.getPath().size() - player.getDest_id() - 1));
				Point3D dest_gis = pixelsToPoint(player.getDest());
				Point3D player_gis = pixelsToPoint(player.getLocation());
				int radius = 5;
				// If close enough to current destination, start moving to next destination.
				if((player.getLocation().ix() >= player.getDest().ix()-radius && player.getLocation().ix() <= player.getDest().ix()+radius) &&
						(player.getLocation().iy() >= player.getDest().iy()-radius && player.getLocation().iy() <= player.getDest().iy()+radius)) {
					player.setDest_id(player.getDest_id()+1);
				}
				player.angle = MyCoords.azimuth(player_gis, dest_gis);
				play.rotate(player.angle);
			}
			else {
				player.setDest_id(0);
				calcPath();	
			}
//		}
//		else {
//			play.rotate(player.angle);
//		}
	}
	
	/**
	 * Calls the A-Star algorithm to calculate the shortest path from the player to his destination:
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
//			if(MyCoords.pixelDistance(player_loc, dest_loc) < 30 || radiusNearBoxCorner(player.getLocation(), 10)) {
			if(radiusNearBoxCorner(player.getLocation(), 10)) {
				star = new A_Star_2(player_loc, dest_loc, boxes, this, 2);
			}
			else {
				star = new A_Star_2(player_loc, dest_loc, boxes, this, 15);
			}
			star.algo();
		}

	}

	/**
	 * Returns the fruit that the player can get to the fastest.
	 * @return The location of the closest fruit.
	 */
	public Point3D closestFruit() {
		Fruit closest = null;
		double minDistance = Double.POSITIVE_INFINITY;
		Iterator<Fruit> it = fruits.iterator();
		while(it.hasNext()) {
			Fruit f = it.next();
			A_Star_2 s0 = new A_Star_2(player.getLocation(), f.getLocation(),boxes,this,20);
			s0.algo();
			if(s0.pathDistance() < minDistance) {
				minDistance = s0.pathDistance();
				closest = f;
			}
		}
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
			if(MyCoords.pixelDistance(player.getLocation(),g.getLocation()) < minDistance) {
				minDistance = MyCoords.pixelDistance(player.getLocation(),g.getLocation());
				closest = g;
			}
		}
		return closest.getLocation();
	}
	
	/**
	 * Time it takes for player to get to given fruit.
	 * @param fruit
	 * @return
	 */
	public double timePlayerToFruit(Fruit fruit) {
		A_Star_2 st = new A_Star_2(player.getLocation(), fruit.getLocation(), boxes, this, 6);
		st.algo();
		double distance = st.pathDistance();
		return distance/player.getSpeed();
	}

	/**
	 * Time it takes for given packman to get to a given fruit.
	 * @param pac
	 * @param fruit
	 * @return
	 */
	public double timePackmanToFruit(Packman pac, Fruit fruit) {
		return MyCoords.pixelDistance(pac.getLocation(), fruit.getLocation())/pac.getSpeed();
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
			for (int i = 0; i < corners.length; i++) {
				if(MyCoords.pixelDistance(position, corners[i]) < radius) return true;
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
	
	//////// Ghost functions ////////
//	public boolean isOppositeAngle(Ghost ghost) {
//		double ghost_angle = azimuth(player.getLocation(), ghost.getLocation());
//		double angle_diff = Math.min(player.angle, ghost_angle)+180-Math.max(player.angle, ghost_angle);
//		if(Math.abs(angle_diff) <= 30)
//			return true;
//		return false;
//	}
	
//	public void escape() {
//		double distance = 100;
//		Point3D closest_ghost = closestGhost();
//		double player_to_ghost_angle = azimuth(player.getLocation(), closest_ghost);
//
//		while(new_angle < player_to_ghost_angle - 50) {
//			new_angle += 50;
//			if(!goesIntoWall(new_angle, distance) )
//		}
//		
//		
//		
//		Iterator<Ghost> ghost_it = ghosts.iterator();
//		while(ghost_it.hasNext() && !escaping) {
//			Ghost curr = ghost_it.next();
//			escape_count = 0;
//			if(isOppositeAngle(curr)) {
//				double new_angle = player.angle - 70;
//				if (new_angle >=360) new_angle = player.angle + 70;
//				System.out.println("Current player angle: "+player.angle);
//				player.angle = new_angle;
//				System.out.println("changing angle, new angle: "+player.angle);
//				escaping = true;
//			}
//		}
//		escape_count++;
//		System.out.println(escape_count);
//		if(escape_count >= 60 || escape_count == 0)
//			escaping = false;
//	}

	// Getters and Setters
	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}
	
	public Player getPlayer() {
		return player;
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
	
	public boolean isFirstLoaded() {
		return firstLoaded;
	}
	
	public boolean isDidFirstPath() {
		return didFirstPath;
	}
}
