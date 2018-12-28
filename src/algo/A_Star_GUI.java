package algo;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;

import ex4_example.Display;
import ex4_example.KeyManager;
import ex4_example.MouseManager;

public class A_Star_GUI implements Runnable {

	private Display display;
	private int width, height;

	private boolean running = false;
	private Thread thread;

	private BufferStrategy bs;
	private Graphics g;

	//Input
	private KeyManager keyManager;
	private MouseManager mouseManager;
	
	private A_Star_2 star;
	private boolean margins;

	public A_Star_GUI(A_Star_2 star, int width, int height){
		keyManager = new KeyManager();
		mouseManager = new MouseManager();
		this.width = width;
		this.height = height;
		this.star = star;
		margins = true;

	}

	private void init(){

		display = new Display("A*", width, height);
		display.getFrame().addKeyListener(keyManager);
		display.getFrame().addMouseListener(mouseManager);


	}

	public void setWidth(int width) {
		this.width = width;}

	public void setHeight(int height) {
		this.height = height;}

	private void tick(){
		keyManager.tick();
	}

	private void move() {
		if(keyManager.down) {}
		if(keyManager.right) {}

		if(keyManager.up) {}

		if(keyManager.left) {}

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

		g.fillRect(0, 0, width, height);
		
		for (int x = 0; x < star.ROWS; x++) {
			for (int y = 0; y < star.COLS; y++) {
				star.grid[x][y].render(g, margins);
			}
		}

		//End Drawing!
		bs.show();
		g.dispose();
	}

	public void setMargins(boolean flag) {
		margins = flag;
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
				if(!star.done)
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
	
	public void drawStringCentered(String s, Color color, int x, int y, int fontSize) {
		g.setFont(new Font(g.getFont().getFontName(), Font.PLAIN, fontSize)); 
		g.setColor(color);
		
		int text_width = (int) g.getFontMetrics().getStringBounds(s, g).getWidth();
		int text_height = (int) g.getFontMetrics().getStringBounds(s, g).getHeight();
		
		g.drawString(s, x - text_width/2, y - text_height/2);
	}



}

