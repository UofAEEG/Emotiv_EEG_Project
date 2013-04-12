package game.sokoban;
import game.helper.*;

import java.util.ArrayList;
import java.util.Iterator;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

/*
 * @author tfung
 */
public class Sokoban extends BasicGame{

	final static int windowWidth = 800;
	final static int windowHeight = 600;
	
	Image background = null;
	Image wall = null;
	Image box = null;
	
	int imageLen = 256;
	float backScale = 0.5f;
	float wallScale = 0.25f;
	
	float wallLen = imageLen*wallScale;
	
	ArrayList<Pair> walls;
	ArrayList<Pair> boxes;
	
	/* temporary hard code - level 1 */
	public void level() {
		//left wall
		walls.add(new Pair(wallLen*1,wallLen*1));
		walls.add(new Pair(wallLen*1,wallLen*2));
		walls.add(new Pair(wallLen*1,wallLen*3));
		walls.add(new Pair(wallLen*1,wallLen*4));
		walls.add(new Pair(wallLen*1,wallLen*5));
		walls.add(new Pair(wallLen*1,wallLen*6));
		walls.add(new Pair(wallLen*1,wallLen*7));
		walls.add(new Pair(wallLen*1,wallLen*8));
		
		//top
		walls.add(new Pair(wallLen*2,wallLen*1));
		walls.add(new Pair(wallLen*3,wallLen*1));
		walls.add(new Pair(wallLen*3,wallLen*0));
		walls.add(new Pair(wallLen*4,wallLen*0));
		walls.add(new Pair(wallLen*5,wallLen*0));
		walls.add(new Pair(wallLen*6,wallLen*0));
		walls.add(new Pair(wallLen*7,wallLen*0));
		
		//right
		walls.add(new Pair(wallLen*7,wallLen*0));
		walls.add(new Pair(wallLen*7,wallLen*1));
		walls.add(new Pair(wallLen*7,wallLen*2));
		walls.add(new Pair(wallLen*7,wallLen*3));
		walls.add(new Pair(wallLen*7,wallLen*4));
		walls.add(new Pair(wallLen*7,wallLen*5));
		walls.add(new Pair(wallLen*8,wallLen*5));
		walls.add(new Pair(wallLen*8,wallLen*6));
		walls.add(new Pair(wallLen*8,wallLen*7));
		walls.add(new Pair(wallLen*8,wallLen*8));
		
		//bottom
		walls.add(new Pair(wallLen*2,wallLen*8));
		walls.add(new Pair(wallLen*3,wallLen*8));
		walls.add(new Pair(wallLen*4,wallLen*8));
		walls.add(new Pair(wallLen*5,wallLen*8));
		walls.add(new Pair(wallLen*6,wallLen*8));
		walls.add(new Pair(wallLen*7,wallLen*8));
		walls.add(new Pair(wallLen*8,wallLen*8));
		
		//internal wall
		walls.add(new Pair(wallLen*2,wallLen*3));
		walls.add(new Pair(wallLen*3,wallLen*3));
		walls.add(new Pair(wallLen*3,wallLen*4));
		walls.add(new Pair(wallLen*3,wallLen*5));
		walls.add(new Pair(wallLen*4,wallLen*4));
		
		//boxes
		boxes.add(new Pair(wallLen*4,wallLen*2));
		boxes.add(new Pair(wallLen*5,wallLen*3));
		boxes.add(new Pair(wallLen*5,wallLen*4));
		boxes.add(new Pair(wallLen*5,wallLen*6));
		boxes.add(new Pair(wallLen*4,wallLen*6));
		boxes.add(new Pair(wallLen*2,wallLen*6));
		boxes.add(new Pair(wallLen*6,wallLen*6));
	}
	
	public Sokoban() {
		super("Sokoban");
	}

	public static void main(String[] args) {
		Sokoban sokoban = new Sokoban();
		
		try {
			AppGameContainer app = new AppGameContainer(sokoban);
			app.setDisplayMode(windowWidth,windowHeight,false);
			
			app.start();
		} catch (SlickException e) {
	    	e.printStackTrace();
		}
		
	}

	@Override
	public void render(GameContainer container, Graphics g) throws SlickException {
		for (int i=0; i<windowWidth; i+=(imageLen*backScale)) {
			for (int j=0; j<windowHeight; j+=(imageLen*backScale)) {
				background.draw(i,j,backScale);	
			}
		}
		
		
		Iterator<Pair> wallIterator = walls.iterator();
		Iterator<Pair> boxesIterator = boxes.iterator();
		
		while(wallIterator.hasNext()) {
			Pair pair = wallIterator.next();
			wall.draw(pair.getX(), pair.getY(), wallScale);
		}
		
		while(boxesIterator.hasNext()) {
			Pair pair = boxesIterator.next();
			box.draw(pair.getX(), pair.getY(), wallScale);
		}
		
	}

	@Override
	public void init(GameContainer container) throws SlickException {
		background = new Image("images/Floor_WoodAlternating_256_d1.tga");
		wall = new Image("images/brick_guiGen_256_d.tga");
		box = new Image("images/Metal_WallPanel_256_d.tga");
		
		// temp
		walls = new ArrayList<Pair>();
		boxes = new ArrayList<Pair>();
		level();
	}

	@Override
	public void update(GameContainer container, int delta) throws SlickException {
		
	}

}