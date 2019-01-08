package ex4_example;
import java.awt.Color;
import java.awt.Font;
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
import game.GameBoard;
import gfx.Assets;

public class GUI implements Runnable {

	//Display
	private Display display;
	private int width, height;
	private BufferStrategy bs;
	private Graphics g;
	private Thread thread;
	
	//Game
	private GameBoard board;
	private String game_file_name;
	
//	private int escape_count = 0;

	//Input
	private KeyManager keyManager;
	private MouseManager mouseManager;

	//Flags
	private boolean running = false;
	private boolean playing = false;
	private boolean escaping = false;


	/**
	 * Constructor
	 * @param play Object that is responsible for advancing the game and returning information about it.
	 * @param start Top left GPS coordinate of the map.
	 * @param end Bottom right GPS coordinate of the map.
	 */
	public GUI(){
		keyManager = new KeyManager();
		mouseManager = new MouseManager();

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
					game_file_name = selectedFile.getAbsolutePath();
					board = new GameBoard(game_file_name);
				}
			}         
		});  
		
		runBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				board.startGame();
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
	 * - Calls a function to recalculate the players path.
	 * - Move function checks which keys are pressed and acts accordingly.
	 * - Calls the game to continue if the game has been started.
	 */
	private void tick(){
		keyManager.tick();
		move();
		if(playing) {
			board.nextMove();
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
//		if(keyManager.down)
//			play.rotate(0);
//		if(keyManager.right)
//			play.rotate(90);
//		if(keyManager.up)
//			play.rotate(180);
//		if(keyManager.left)
//			play.rotate(270);
//		if(keyManager.e)
//			calcPath();
//		if(keyManager.r)
//			calcAngle();
//		if(keyManager.t) {
////			calcPath();
//			playing = false;
//		}
	}
	
	private void drawString(String str, int x, Color c) {
		g.setFont(new Font("Assistant", Font.BOLD, 18));
		g.setColor(Color.black);
		g.drawString(str, x, g.getFontMetrics().getHeight()-3);
		g.setColor(c);
		g.drawString(str, x+2, g.getFontMetrics().getHeight()-5);
	}
	
	private void drawString(String str, int x, int y, Color c) {
		g.setFont(new Font("Assistant", Font.BOLD, 18));
		g.setColor(Color.black);
		g.drawString(str, x, y-3);
		g.setColor(c);
		g.drawString(str, x+2, y-5);
	}
	
	private void drawStats(Graphics g) {
		int shift = 500;
		String file_name = game_file_name.substring(game_file_name.lastIndexOf('\\')+9, game_file_name.length()-4);
		double[] stats = board.getStats();
		drawString(file_name, 5, Color.white);
		drawString("Total Time: "+stats[0], 20 + shift, Color.white);
		drawString("Time Left: "+stats[2], 220 + shift, Color.white);
		drawString("Score: "+stats[1], 420 + shift, Color.white);
		drawString("Killed by Ghosts: "+stats[3], 570 + shift, Color.white);
		drawString("Out of Box: "+stats[4], 790 + shift, Color.white);
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
		
		// Draw ariel map image
		g.drawImage(Assets.map, 0, 0, null);
		
//		if(board.isFirstLoaded() && board.getPlayer() != null && board.getPackmans() != null && board.getGhosts() != null && board.getFruits() != null && board.getBoxes() != null) {
		if(board != null && board.isFirstLoaded()) {
			drawBoard(board);

			// Draw straight line from player to closest fruit
			g.setColor(Color.red);
			if(!board.getFruits().isEmpty()) {
				g.drawLine(board.getPlayer().getLocation().ix(), board.getPlayer().getLocation().iy(), board.closestFruit().ix(), board.closestFruit().iy());
			}
			
			//Draw player path
			if(board.isDidFirstPath())
				drawPath(board.getPlayer().getPath());
			
			// Draw stats
			g.setColor(new Color(0,0,0,100));
			g.fillRect(0, 0, width, 25);
			drawStats(g);
		}
		else { // If board hasn't loaded yet, just draw stats.
			g.setColor(new Color(0,0,0,100));
			g.fillRect(0, 0, width, 25);
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
	 * Draws the packmans, ghosts, fruits, boxes and the player.
	 * @param player
	 * @param packmans
	 * @param ghosts
	 * @param fruits
	 * @param boxes
	 */
	private void drawBoard(GameBoard board) {
		board.getPlayer().render(g);
		Iterator<Packman> pack_it = board.getPackmans().iterator();
		while(pack_it.hasNext())
			pack_it.next().render(g);
		Iterator<Ghost> ghost_it = board.getGhosts().iterator();
		while(ghost_it.hasNext())
			ghost_it.next().render(g);
		Iterator<Fruit> fruit_it = board.getFruits().iterator();
		while(fruit_it.hasNext())
			fruit_it.next().render(g);
		Iterator<Box> box_it = board.getBoxes().iterator();
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

	

	//Getters
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

