package gfx;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * The art assets of the game.
 */

public class Assets {

	public static BufferedImage map;
	public static BufferedImage player;
	public static BufferedImage packman;
	public static BufferedImage ghost;
	public static BufferedImage fruit;

	public static void loadImages() {
		try {
			map = ImageIO.read(new File("data/Ariel1.png"));
			packman = ImageIO.read(new File("data/packman.png"));
			fruit = ImageIO.read(new File("data/fruit.png"));
			ghost = ImageIO.read(new File("data/ghost.png"));
			player = ImageIO.read(new File("data/player.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
