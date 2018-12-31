package ex4_example;
import java.util.ArrayList;

import Geom.Point3D;
import Robot.Play;
import algo.A_Star;
/**
 * This is the basic example of how to work with the Ex4 "server" like system:
 * 1. Create a "play" with one of the 9 attached files 
 * 2. Set your ID's - of all the group members (numbers only);
 * 3. Get the GPS coordinates of the "arena" - as in Ex3.
 * 4. Get the game-board data
 * 5. Set the "player" init location - should be a valid location
 * 6. Start the Server
 * 7. while (the game is running):
 * 7.1 "Play" as long as there are "fruits" and time
 * 7.2 get the current score of the game
 * 7.3 get the game-board current state
 * 7.4 goto 7
 * 8. done - report the results to the DB.
 * @author ben-moshe
 *
 */
public class Ex4_Main_Example {
	public static void main(String[] args) {

		/**
		 * SCORE:
		 * 	# Eaten by ghost: -20
		 *  # Eat fruit: +fruit.getWeight()
		 *  # Eat packman: +1
		 *  # Out of box: -1
		 *  # + TimeLeft/1000
		 *  
		 */
		
		

		// 1) Create a "play" from a file (attached to Ex4)
		String file_name = "data/Ex4_OOP_example5.csv";
		Play play1 = new Play(file_name);

		// 2) Set your ID's - of all the group members
		play1.setIDs(666,2222,3333);

		// 3)Get the GPS coordinates of the "arena"s
		String map_data = play1.getBoundingBox();
//		System.out.println("Bounding Box info: "+map_data);

		String[] words = map_data.split(",");
		Point3D start = new Point3D(Double.parseDouble(words[2]), Double.parseDouble(words[3]));
		Point3D end = new Point3D(Double.parseDouble(words[5]), Double.parseDouble(words[6]));
		
		GUI gui = new GUI(play1, start, end);
		gui.start();
		
		
		// 4) get the game-board data
		ArrayList<String> board_data = play1.getBoard();
//		for(int i=0;i<board_data.size();i++) {
//			System.out.println(board_data.get(i));
//		}
//		System.out.println();
//		System.out.println("Init Player Location should be set using the bounding box info");

		// 5) Set the "player" init location - should be a valid location
		play1.setInitLocation(32.1040,35.2061);

		// 6) Start the "server"
		play1.start(); // default max time is 100 seconds (1000*100 ms).

		// 7) "Play" as long as there are "fruits" and time
		
		long delay = 30; //in milliseconds
		long time_passed = 0;
		
		long last_time = System.currentTimeMillis();
		while(play1.isRuning()) {
			time_passed += System.currentTimeMillis() - last_time;
			last_time = System.currentTimeMillis();
			if(time_passed > delay) {
				time_passed = 0;
				
				double turn = last_time/10;
//				play1.rotate(turn);

				// 7.2) get the current score of the game
				String info = play1.getStatistics();
//				System.out.println(info);
				
				// 7.3) get the game-board current state
				board_data = play1.getBoard();
//				for(int a=0;a<board_data.size();a++) {
//					System.out.println(board_data.get(a));
//				}
//				System.out.println();
			}
		}
		// 8) stop the server - not needed in the real implementation.
		//play1.stop();
		System.out.println("**** Done Game (user stop) ****");

		// 9) print the data & save to the course DB
		String info = play1.getStatistics();
		System.out.println(info);
	}
}
