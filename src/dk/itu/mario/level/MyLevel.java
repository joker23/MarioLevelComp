package dk.itu.mario.level;

import java.util.Random;

import dk.itu.mario.MarioInterface.Constraints;
import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.MarioInterface.LevelInterface;
import dk.itu.mario.engine.sprites.SpriteTemplate;
import dk.itu.mario.engine.sprites.Enemy;


public class MyLevel extends Level{

	//Terrain Code
	private static final byte 	CANNON_PILLAR 	= 14 + 2 * 16;
	private static final byte 	CANNON_NECK		= 14 + 1 * 16;
	private static final byte	CANNON_HEAD		= 14 + 0 * 16;

	private static final byte	NOTHING			= 0;

	//Enemy Code
	private static final int	RED_TURTLE		= 0;
	private static final int	GREEN_TURTLE	= 1;
	private static final int	GOOMPA			= 2;
	private static final int	ARMORED_TURTLE	= 3;
	private static final int 	JUMP_FLOWER		= 4;
	private static final int	CANNON_BALL		= 5;
	private static final int	CHOMP_FLOWER	= 6;
	private static final boolean WINGED			= true;		//is the enemy winged?

	//Store information about the level
	public   int ENEMIES 		= 0; //the number of enemies the level contains
	public   int BLOCKS_EMPTY 	= 0; // the number of empty blocks
	public   int BLOCKS_COINS 	= 0; // the number of coin blocks
	public   int BLOCKS_POWER 	= 0; // the number of power blocks
	public   int COINS 			= 0; //These are the coins in boxes that Mario collect

	private static Random levelSeedRandom = new Random();
	public static long lastSeed;

	Random random;

	private int difficulty;
	private int type;
	private int gaps;


	/**
	 * this is a constructor used for testing
	 */
	public MyLevel(){
		this(100, 15);
		difficulty = 0;
		type = 0;
		gaps = 0;
		random = new Random();

		for(int i=0; i<width; i++){
			setBlock(i,height - 1,GROUND);
		}

		int floor = height - 1;

		xExit = width - 3;
		yExit = floor;

	//	placeHill(30, 3, 40);
	//	placeHill(40, 2, 10);
	//	placeGap(15,6);
		placeStairsRight(15, 1, 7, GROUND);

		fixWalls();
	}

	/**
	 * builds a cannon
	 *
	 * @param x : x coordinate of the cannon
	 * @param y : y coordinate of the cannon
	 * @param floor : y parameter of when the floor starts
	 *
	 * @return width of the cannon (1)
	 */
	private int buildCannon (int x, int yCannon, int floor) {

		for(int y = 0; y<height; y++) {
			if (y >= floor) {
				setBlock(x, y, GROUND);
			} else if (y >= yCannon){
				if (y == yCannon) {
					setBlock(x, y, CANNON_HEAD);
				} else if (y == yCannon + 1) {
					setBlock(x, y, CANNON_NECK);
				} else {
					setBlock(x, y, CANNON_PILLAR);
				}
			}
		}
		return 1;
	}

	/**
	 * places an Enemy at a specified area
	 *
	 * @param x : x coordinate
	 * @param y : y coordinate
	 * @param winged : does the enemy have wings? (does it jump?)
	 * @param type : type of enemy :
	 * 	RED_TURTLE		= 0
	 *	GREEN_TURTLE	= 1
	 *  GOOMPA			= 2
	 *  ARMORED_TURTLE	= 3
	 *  JUMP_FLOWER		= 4
	 *  CANNON_BALL		= 5
	 *  CHOMP_FLOWER	= 6
	 */

	private boolean placeEnemy (int x, int y, int type, boolean winged) {

		//don't place enemy if the block is occupied
		if (occupied(x, y)) {
			return false;
		}

		setSpriteTemplate(x, y, new SpriteTemplate(type, winged));

		return true;
	}

	// places an enemy on the floor
	private boolean placeEnemy(int x, int type, boolean winged) {
		return placeEnemy(x, findFloor(x), type, winged);
	}

	/**
	 * makes a gap for mario to jump across
	 * NOTES: in walking mode mario can clear gap length 4
	 * 		  in running mode mario can clear gap length 7
	 * @param xo : initial x coordinate
	 * @param len : length of the gap
	 */
	private boolean placeGap(int xo, int len){
		if(xo + len >= width){
			return false;
		}

		for(int x=xo; x < xo + len; x++ ) {
			for(int y=0; y<height; y++) {
				if(getBlock(x, y) == GROUND){
					setBlock(x, y, NOTHING);
				}
			}
		}
		return true;
	}

	/**
	 * places a hill
	 * NOTES : Mario could only clear a height 3 hill
	 *
	 * @param xo : starting x coordinate
	 * @param h : height of the hill
	 * @param len : length of the hill
	 */
	private boolean placeHill (int xo, int h, int len) {

		int yo = findFloor(xo) - h;

		for(int x = xo; x < xo + len; x ++ ) {
			for(int y=yo; y<height; y++) {
				setBlock(x, y, GROUND);
			}
		}

		return true;
	}

	//TODO place stairs wrapper with place stairs left
	//TODO place pipes
	public boolean placeStairsRight (int xo, int dy, int len, byte type) {

		int yo = findFloor(xo);
		int counter = 0;
		for(int x = xo; x<xo+len; x ++ ){
			int y = yo - (dy * (counter ++));
			for(int k= yo; k>=y; k--){
				setBlock(x, k, type);
			}
		}

		if(type == GROUND){
			int x = xo + len;
			int maxy = yo - (dy * (counter - 1));
			for(int y = yo; y>=maxy; y--){
				setBlock(x, y, type);
			}
		}

		return true;
	}


	/***********************************utility functions******************************************/
	/**
	 * @return if the block at x and y is occupied already
	 */
	private boolean occupied(int x, int y) {
		return getBlock(x,y) != 0;
	}

	/**
	 * @return floor at a given x
	 */
	private int findFloor (int x) {

		int floor = 0;
		while(getBlock(x, floor+1) != GROUND) {
			floor ++;
		}

		return floor;
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public MyLevel(int width, int height) {
		super(width, height);
	}

	private int buildJump(int xo, int maxLength) {
		gaps++;
		//jl: jump length
		//js: the number of blocks that are available at either side for free
		int js = random.nextInt(4) + 2;
		int jl = random.nextInt(2) + 2;
		int length = js * 2 + jl;

		boolean hasStairs = random.nextInt(3) == 0;

		int floor = height - 1 - random.nextInt(4);
		//run from the start x position, for the whole length
		for (int x = xo; x < xo + length; x++) {
			if (x < xo + js || x > xo + length - js - 1) {
				//run for all y's since we need to paint blocks upward
				for (int y = 0; y < height; y++) {	//paint ground up until the floor
					if (y >= floor) {
						setBlock(x, y, GROUND);
					}
					//if it is above ground, start making stairs of rocks
					else if (hasStairs) {	//LEFT SIDE
						if (x < xo + js) { //we need to max it out and level because it wont
							//paint ground correctly unless two bricks are side by side
							if (y >= floor - (x - xo) + 1) {
								setBlock(x, y, ROCK);
							}
						}
						else { //RIGHT SIDE
							if (y >= floor - ((xo + length) - x) + 2) {
								setBlock(x, y, ROCK);
							}
						}
					}
				}
			}
		}

		return length;
	}


	private int buildTubes(int xo, int maxLength) {
		int length = random.nextInt(10) + 5;
		if (length > maxLength) length = maxLength;

		int floor = height - 1 - random.nextInt(4);
		int xTube = xo + 1 + random.nextInt(4);
		int tubeHeight = floor - random.nextInt(2) - 2;
		for (int x = xo; x < xo + length; x++) {
			if (x > xTube + 1) {
				xTube += 3 + random.nextInt(4);
				tubeHeight = floor - random.nextInt(2) - 2;
			}
			if (xTube >= xo + length - 2) xTube += 10;

			if (x == xTube && random.nextInt(11) < difficulty + 1) {
				setSpriteTemplate(x, tubeHeight, new SpriteTemplate(Enemy.ENEMY_FLOWER, false));
				ENEMIES++;
			}

			for (int y = 0; y < height; y++) {
				if (y >= floor) {
					setBlock(x, y,GROUND);

				}
				else {
					if ((x == xTube || x == xTube + 1) && y >= tubeHeight) {
						int xPic = 10 + x - xTube;

						if (y == tubeHeight) {
							//tube top
							setBlock(x, y, (byte) (xPic + 0 * 16));
						}
						else {
							//tube side
							setBlock(x, y, (byte) (xPic + 1 * 16));
						}
					}
				}
			}
		}

		return length;
	}


	private void fixWalls() {
		boolean[][] blockMap = new boolean[width + 1][height + 1];

		for (int x = 0; x < width + 1; x++) {
			for (int y = 0; y < height + 1; y++) {
				int blocks = 0;
				for (int xx = x - 1; xx < x + 1; xx++) {
					for (int yy = y - 1; yy < y + 1; yy++) {
						if (getBlockCapped(xx, yy) == GROUND){
							blocks++;
						}
					}
				}
				blockMap[x][y] = blocks == 4;
			}
		}
		blockify(this, blockMap, width + 1, height + 1);
	}

	private void blockify(Level level, boolean[][] blocks, int width, int height){
		int to = 0;
		if (type == LevelInterface.TYPE_CASTLE) {
			to = 4 * 2;
		}
		else if (type == LevelInterface.TYPE_UNDERGROUND) {
			to = 4 * 3;
		}

		boolean[][] b = new boolean[2][2];

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				for (int xx = x; xx <= x + 1; xx++) {
					for (int yy = y; yy <= y + 1; yy++) {
						int _xx = xx;
						int _yy = yy;
						if (_xx < 0) _xx = 0;
						if (_yy < 0) _yy = 0;
						if (_xx > width - 1) _xx = width - 1;
						if (_yy > height - 1) _yy = height - 1;
						b[xx - x][yy - y] = blocks[_xx][_yy];
					}
				}

				if (b[0][0] == b[1][0] && b[0][1] == b[1][1]) {
					if (b[0][0] == b[0][1]) {
						if (b[0][0]) {
							level.setBlock(x, y, (byte) (1 + 9 * 16 + to));
						}
						else {
							// KEEP OLD BLOCK!
						}
					}
					else {
						if (b[0][0]) {
							//down grass top?
							level.setBlock(x, y, (byte) (1 + 10 * 16 + to));
						}
						else {
							//up grass top
							level.setBlock(x, y, (byte) (1 + 8 * 16 + to));
						}
					}
				}
				else if (b[0][0] == b[0][1] && b[1][0] == b[1][1]) {
					if (b[0][0]) {
						//right grass top
						level.setBlock(x, y, (byte) (2 + 9 * 16 + to));
					}
					else {
						//left grass top
						level.setBlock(x, y, (byte) (0 + 9 * 16 + to));
					}
				}
				else if (b[0][0] == b[1][1] && b[0][1] == b[1][0]) {
					level.setBlock(x, y, (byte) (1 + 9 * 16 + to));
				}
				else if (b[0][0] == b[1][0]) {
					if (b[0][0]) {
						if (b[0][1]) {
							level.setBlock(x, y, (byte) (3 + 10 * 16 + to));
						}
						else {
							level.setBlock(x, y, (byte) (3 + 11 * 16 + to));
						}
					}
					else {
						if (b[0][1]) {
							//right up grass top
							level.setBlock(x, y, (byte) (2 + 8 * 16 + to));
						}
						else {
							//left up grass top
							level.setBlock(x, y, (byte) (0 + 8 * 16 + to));
						}
					}
				}
				else if (b[0][1] == b[1][1]) {
					if (b[0][1]) {
						if (b[0][0]) {
							//left pocket grass
							level.setBlock(x, y, (byte) (3 + 9 * 16 + to));
						}
						else {
							//right pocket grass
							level.setBlock(x, y, (byte) (3 + 8 * 16 + to));
						}
					}
					else {
						if (b[0][0]) {
							level.setBlock(x, y, (byte) (2 + 10 * 16 + to));
						}
						else {
							level.setBlock(x, y, (byte) (0 + 10 * 16 + to));
						}
					}
				}
				else {
					level.setBlock(x, y, (byte) (0 + 1 * 16 + to));
				}
			}
		}
	}

	public MyLevel clone() throws CloneNotSupportedException {

		MyLevel clone=new MyLevel(width, height);

		clone.xExit = xExit;
		clone.yExit = yExit;
		byte[][] map = getMap();
		SpriteTemplate[][] st = getSpriteTemplate();

		for (int i = 0; i < map.length; i++)
			for (int j = 0; j < map[i].length; j++) {
				clone.setBlock(i, j, map[i][j]);
				clone.setSpriteTemplate(i, j, st[i][j]);
			}
		clone.BLOCKS_COINS = BLOCKS_COINS;
		clone.BLOCKS_EMPTY = BLOCKS_EMPTY;
		clone.BLOCKS_POWER = BLOCKS_POWER;
		clone.ENEMIES = ENEMIES;
		clone.COINS = COINS;

		return clone;

	}


}
