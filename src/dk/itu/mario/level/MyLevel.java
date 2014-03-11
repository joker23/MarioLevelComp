package dk.itu.mario.level;

import java.util.*;

import dk.itu.mario.MarioInterface.Constraints;
import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.MarioInterface.LevelInterface;
import dk.itu.mario.engine.sprites.SpriteTemplate;
import dk.itu.mario.engine.sprites.Enemy;

//TODO think about changing the boolean to int return
//TODO think about separating things into layers...such as layer 0 is ground layer 1 is 3-4 (where mario can jump to)
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
		placeStairs(13, 1, 7, GROUND, true);

		placeTube(23, 7, new SpriteTemplate(JUMP_FLOWER, !WINGED));

		placeStairs(30, 1, 7, GROUND, false);

		placeBlockArray(new int[]{1,1,1,1}, 60, floor - 4);

		boolean[][] canReach = bfs(height-1, 0);

		for(int i=0; i<height; i++){
			for(int j=0; j<width; j++){
				if(canReach[i][j]) {
					setBlock(j, i, COIN);
				}
			}
		}

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

	/**place stairs
	 * places stairs on the map
	 *	NOTE: 	mario could not clear dy > 3
	 *			type should only be GROUND or ROCK...if it is something else...then it might be hard
	 * @param xo : initial x
	 * @param dy : change in y at every step
	 * @param len: length of stairs (dy * len is the max height)
	 * @param type : type of material that builds the stairs
	 * @param right : is this stair facing the right?
	 */
	private boolean placeStairs(int xo, int dy, int len, byte type, boolean right) {
		if(right) {
			return placeStairsRight(xo, dy, len, type);
		} else {
			return placeStairsLeft(xo, dy, len, type);
		}
	}

	private boolean placeStairsRight (int xo, int dy, int len, byte type) {

		if(xo < 2 || xo + len > width-2){
			return false;
		}

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

	private boolean placeStairsLeft(int xo, int dy, int len, byte type){

		if(xo < 2 || xo+len > width-2){
			return false;
		}

		int yo = len * dy;
		int floor = findFloor(xo);

		if(type == GROUND){
			for(int y=floor; y>=yo; y --) {
				setBlock(xo-1, y, type);
			}
		}

		int counter = 0;
		for(int x=xo; x<xo+len; x++){
			int maxy = yo + (dy * (counter ++));
			for(int y=floor; y>=maxy; y--){
				setBlock(x, y, type);
			}
		}

		return true;
	}

	/**
	 * generates a array of continuous blocks
	 *
	 * @param blockArray : contents of the blocks in array form
	 * 		0 : empty block
	 * 		1 : block with coin
	 * 		2 : block with powerup
	 */
	private boolean placeBlockArray(int[] blockArray, int xo, int y) {
		if(xo < 10 || blockArray.length + xo > width - 10) {
			return false;
		}

		for(int x=xo; x<xo + blockArray.length; x++) {
			switch (blockArray[x - xo]) {
				case 0 :
					setBlock(x, y, BLOCK_EMPTY);
					break;
				case 1 :
					setBlock(x, y, BLOCK_COIN);
					break;
				case 2 :
					setBlock(x, y, BLOCK_POWERUP);
					break;
				default :
					//do nothing
			}
		}

		return true;
	}

	//TODO place pipes
	//TODO bottom up
	private int placeTube(int xo, int h, SpriteTemplate enemy) {

		int length = 2;
		int floor = findFloor(xo);

		if (enemy != null) {
			setSpriteTemplate(xo, h, enemy);
		}

		for (int x = xo; x < xo + length; x++) {

			for (int y = 0; y < height; y++) {
				if (y > floor) {
					setBlock(x, y,GROUND);

				}
				else if (y >= h){
					int xPic = 10 + x - xo;

					if (y == h) {
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
		return length;
	}

	private boolean[][] bfs(int x, int y) {
		int[] dx = {0, 0, -1, 1};
		int[] dy = {1, -1, 0, 0};

		boolean[][] res = new boolean[height][width];
		boolean[][] vis = new boolean[height][width];

		vis[x][y] = true;
		res[x][y] = true;

		Queue<Integer> xq = new LinkedList<Integer>();
		Queue<Integer> yq = new LinkedList<Integer>();
		xq.add(x);
		yq.add(y);

		while(!xq.isEmpty()){
			int currx = xq.poll();
			int curry = yq.poll();

			vis[currx][curry] = true;
			for(int i=0; i<dx.length; i++){
				int newx = dx[i] + currx;
				int newy = dy[i] + curry;
				if(canReach(newx, newy, newx>=currx) && !vis[newx][newy]) {
					res[newx][newy] = true;
					vis[newx][newy] = true;
					xq.add(newx);
					yq.add(newy);
				}
			}
		}

		return res;
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

	private boolean canReach(int x, int y, boolean lower) {
		if(x<0 || x>=height || y<0 || y>=width) {
			return false;
		}

		if(occupied(y, x)) {
			return false;
		}
		if(!lower){
			int dy = findPlatform(y, x);
			if(dy >=5){
				return false;
			}
		}

		return true;
	}

	/**
	 * @return closest platform given x and y
	 */
	private int findPlatform(int x, int y) {
		if(getBlock(x, y) != 0) {
			return 0;
		}
		int plat = y;
		while(getBlock(x, plat + 1) == 0){
			plat ++;
		}

		return plat - y;
	}

	/**
	 * @return closest platform given x
	 */
	private int findPlatform(int x) {

		int plat = 0;
		while(getBlock(x, plat + 1) == 0){
			plat ++;
		}

		return plat;
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public MyLevel(int width, int height) {
		super(width, height);
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
