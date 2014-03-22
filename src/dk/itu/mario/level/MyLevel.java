package dk.itu.mario.level;

import java.util.*;

import dk.itu.mario.MarioInterface.Constraints;
import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.MarioInterface.LevelInterface;
import dk.itu.mario.engine.sprites.SpriteTemplate;
import dk.itu.mario.engine.sprites.Enemy;

//TODO think about changing the boolean to int return
//TODO think about separating things into layers...such as layer 0 is ground layer 1 is 3-4 (where mario can jump to)
//TODO look for stray ground blocks...
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

	private Random random;

	private int difficulty; //how difficult we want the round
	private int type;
	private int gaps;	//number of gaps in the level
	private int hills;	//number of hills in the level
	private int stairs;	//number of stairs in the level
	private int pipes;	//number of pipes in the level
	private int cannons;	//number of cannons in the level
	private int enemies; //this might be separated
	private int coins; //number of coins in the level
	private int coinCluster;	//chaos coefficient of the coins
	private double first_level;
	private double second_level;
	private int max_level_length;

	/**
	 * this is a constructor used for testing
	 */
	public MyLevel(){
		this(320, 15);
		this.first_level = .1;
		this.second_level = .1;
		this.difficulty = 0;
		this.type = 0;
		this.gaps = 8;
		this.stairs = 1;
		this.hills = 15;
		this.pipes = 5;
		this.cannons = 2;
		this.enemies = 30;
		this.coins = 100;
		this.coinCluster = 3;
		this.random = new Random();

		//initialize the ground
		for(int i=0; i<width; i++){
			setBlock(i,height - 1,GROUND);
		}

		int[][] momentum = sweep();
		boolean[] vis = new boolean[width]; // make sure our hiils are not stacking

		while(hills --> 0) {
			int xo = random.nextInt(width-30) + 10;
			int len = random.nextInt(width/5);
			int h = random.nextInt(2) + 1;
			if (vis[xo] || !placeHill(xo, h, len)) {
				hills ++;
			} else {
				vis[xo] = true;
			}
		}

		momentum = sweep();

		//places gaps
		while(gaps --> 0) {
			int x1, len;
			while(true){
				x1 = random.nextInt(width - 30) + 10;
				int floor = findFloor(x1);
				if(floor < 0) continue; //don't stack gaps...
				len = random.nextInt((int)(momentum[floor][x1] * 1.5)) + 2;
				if(findFloor(x1 + len) < 0 ) continue;
				break;
			}

			placeGap(x1, len);
		}

		while(pipes --> 0) {
			int xo = random.nextInt(width - 20) + 10;
			int y = findFloor(xo);
			int height = random.nextInt(2) + 2;
			y -= height;

			if(y < 4 || getBlock(xo, y) != 0) {
				pipes ++;
				continue;
			}

			placeTube(xo, y, (random.nextInt(3) == 2) ? new SpriteTemplate(JUMP_FLOWER, !WINGED) : null);
		}

		while(cannons --> 0) {
			int xo = random.nextInt(width - 20) + 10;
			int y = findFloor(xo);
			int height = random.nextInt(3) + 1;
			y -= height;

			if(y < 4) {
				cannons ++;
				continue;
			}

			if(!placeCannon(xo, y)) {
				cannons ++;
			}
		}

		momentum = sweep();
		int numBlocks = (int) Math.ceil(width * first_level); //number of blocks we want to cover the first floor with
		int[] nextFloor = getNextFloor(momentum);

		while(numBlocks > 0) {
			int xo = random.nextInt(width - 20) + 10;
			int y = nextFloor[xo];
			if(y == height) {
				continue;
			}
			int maxdisplacement = 0;
			for(int i=0; i<width - xo; i++){
				if(momentum[y][i + xo] != momentum[y][xo]){
					break;
				} maxdisplacement ++;
			}

			if(maxdisplacement < 4) {
				continue;
			}

			int len = random.nextInt(maxdisplacement - 3) + 3;

			int[] blocks = new int[len];
			placeBlockArray(blocks, xo, y);
			numBlocks -= len;
		}

		momentum = sweep();
		numBlocks = (int) Math.ceil(width * second_level); //number of blocks we want to cover the first floor with
		nextFloor = getNextFloor(momentum);

		for(int i: nextFloor){
			System.out.print(i);
		}
		while(numBlocks > 0) {
			int xo = random.nextInt(width - 20) + 10;
			int y = nextFloor[xo];
			if(y < 3) {
				continue;
			}
			int maxdisplacement = 0;
			for(int i=0; i<width - xo; i++){
				if(momentum[y][i + xo] != momentum[y][xo]){
					break;
				} maxdisplacement ++;
			}

			if(maxdisplacement < 4) {
				continue;
			}

			int len = random.nextInt(maxdisplacement - 3) + 3;

			int[] blocks = new int[len];
			placeBlockArray(blocks, xo, y);
			numBlocks -= len;
		}

		momentum = sweep();

		while(enemies --> 0) {
			int xo = random.nextInt(width - 20) + 10;
			int y = findFloor(xo);
			if(occupied(xo, y)) {
				enemies ++;
				continue;
			}
			placeEnemy(xo, y, GOOMPA, !WINGED);
		}

		while(coins --> 0) {
			int x = random.nextInt(width - 20) + 10;
			int y = random.nextInt(height - 5) + 2;

			if(occupied(x, y) || momentum[y][x] < coinCluster) {
				coins ++;
				continue;
			}

			setBlock(x, y, COIN);
		}




		// placeHill(30, 3, 40);
		// placeHill(40, 2, 10);
		// placeGap(15,6);
		// placeStairs(25, 1, 7, GROUND, true);

		// placeTube(23, 7, new SpriteTemplate(JUMP_FLOWER, !WINGED));

		// placeStairs(30, 1, 7, GROUND, false);

		// placeBlockArray(new int[]{1,1,1,1}, 60, findFloor(60) - 4);

		// int[][] sweeprez = sweep();
		// int[] nextFloor = getNextFloor(sweeprez);

		//	for(int i=0; i<nextFloor.length; i++) {
		//		if(nextFloor[i] > 0) {
		//			setBlock(i, nextFloor[i], BLOCK_EMPTY);
		//		}
		//	}

		//	sweeprez = sweep();
		//	for(int y=0; y<height; y++){
		//		for(int x=0; x<width; x++){
		//			if(sweeprez[y][x] > 0)
		//				setBlock(x,y, COIN);
		//		}
		//	}

		xExit = width - 3;
		yExit = findFloor(xExit) + 1;

		clean();
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
	private boolean placeCannon (int x, int yCannon) {
		int floor = findFloor(x);

		if(getBlock(x, yCannon) != 0) {
			return false;
		}
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
		return true;
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

		if(yo < Math.ceil(height/2)) {
			return false;
		}

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

	private int[] getNextFloor(int[][] board){
		int[] ret = new int[width];
		for(int x = 8; x < width - 8; x ++ ){
			for(int y = 1; y < height - 1; y ++) {
				if(board[y+1][x-1] == 2) {
					ret[x] = y;
					break;
				}
			}
		}
		return ret;
	}

	private int[][] sweep() {
		int maxjump = 4;
		int[][] ret = new int[height][width];
		for(int i=0; i<height - 1; i++){
			if(occupied(0, i + 1)){
				ret[i][0] = maxjump;
			}
		}
		for(int y=height - 2; y > 0; y--) {
			for(int x=1; x<width; x++) {
				if(occupied(x, y)) { //blocked
					ret[y][x] = 0;
				}
				else if(y+1 == height) { //if we are at a gap
					ret[y][x] = 0;
				}
				else if(occupied(x, y + 1)) { //is the floor
					ret[y][x] = maxjump;
				}
				else { //in the air somewhere
					ret[y][x] = Math.max(0,Math.max( //don't go under negative
								ret[y+1][x-1] - 1,	//jump from ground or continue jump
								ret[y+1][x] -1));	//jump from standstill
				}
			}
		}

		for(int y=height - 2; y > 0; y--) {
			for(int x=width-2; x>0; x--) {
				if(occupied(x, y)) { //blocked
					ret[y][x] = 0;
				}
				else if(y+1 == height) {
					ret[y][x] = 0;
				}
				else if(occupied(x, y + 1)) { //is the floor
					ret[y][x] = Math.max(maxjump, ret[y][x]);
				}
				else { //in the air somewhere
					ret[y][x] = Math.max(ret[y][x],Math.max( //don't go under negative
								ret[y+1][x+1] - 1,	//jump from ground or continue jump
								ret[y+1][x]-1));	//jump from standstill
				}
			}
		}

		return ret;
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
		while(floor < height && getBlock(x, floor+1) != GROUND) {
			floor ++;
		}

		return floor == height ? -1 : floor;
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

	//this method will take out all lone ground blocks
	private void clean(){
		for(int x = 1; x<width-1; x++){
			for(int y=0; y<height; y++){
				if(getBlock(x, y) == GROUND){
					if(getBlock(x-1, y) != GROUND && getBlock(x+1, y) != GROUND){ //LONE BLOCK
						setBlock(x, y, ROCK); //replace it with a rock so that it will still be functionally the same
					}

					if(y<height - 1 && getBlock(x, y + 1) != GROUND){ //floating block...remove it
						setBlock(x, y, (byte)0);
					}
				}
			}
		}
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
