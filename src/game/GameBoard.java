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
		String map_data = play.getBoundingBox();
		String[] words = map_data.split(",");
		start = new Point3D(Double.parseDouble(words[2]), Double.parseDouble(words[3]));
		end = new Point3D(Double.parseDouble(words[5]), Double.parseDouble(words[6]));
		
		play.setIDs(ID_1, ID_2, ID_3);
		
		Point3D initLocation = pixelsToPoint(bestStartPoint());
		play.setInitLocation(initLocation.x()+0.0005, initLocation.y()+0.0005);
		
		loadBoard();
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
	private void loadBoard() {
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
			if(!didFirstPath) {
				calcPath();
				didFirstPath = true;
			}
			if(pixelDistance(player.getLocation(), closestGhost()) < 70 || escaping) {
//				calcPath();
				escape();
			}
			if(pixelDistance(player.getLocation(), closestFruit()) > 5 && !radiusInsideBox(player.getLocation(), 10) && !escaping) {
				calcPath();
			}
			calcAngle();
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
