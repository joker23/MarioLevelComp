package dk.itu.mario.level.generator;

import java.util.Random;
import java.util.*;
import java.io.*;

import dk.itu.mario.MarioInterface.Constraints;
import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.MarioInterface.LevelGenerator;
import dk.itu.mario.MarioInterface.LevelInterface;
import dk.itu.mario.level.CustomizedLevel;
import dk.itu.mario.level.MyLevel;

public class MyLevelGenerator extends CustomizedLevelGenerator implements LevelGenerator{

	public LevelInterface generateLevel(GamePlay playerMetrics) {
		try {
			DecisionTree tree = new DecisionTree("/home/steven/DecisionTreeLearning");
			int classification = tree.findClass(new double[]{10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10});
			System.out.println(classification);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//LevelInterface level = new MyLevel(320,15,new Random().nextLong(),1,LevelInterface.TYPE_OVERGROUND,playerMetrics);
		LevelInterface level = new MyLevel();
		return level;
	}

	@Override
	public LevelInterface generateLevel(String detailedInfo) {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * attribute list... (this is in array order)
	 *  0 completionTime		REAL
	 *  1 jumpsNumber		REAL
	 *  2 duckNumber		REAL
	 *  3 timeSpentDucking		REAL
	 *  4 timesPressedRun		REAL
	 *  5 timeSpentRunning		REAL
	 *  6 timeRunningRight		REAL
	 *  7 timeRunningLeft		REAL
	 *  8 kickedShells		REAL
	 *  9 totalTimeLittleMode		REAL
	 *  10 totalTimeLargeMode		REAL
	 *  11 totalTimeFireMode		REAL
	 *  12 timesSwichingPower		REAL
	 *  13 aimlessJumps		REAL
	 *  14 percentageBlocksDestroyed		REAL
	 *  15 percentageCoinBlocksDestroyed		REAL
	 *  16 percentageEmptyBlockesDestroyed		REAL
	 *  17 percentagePowerBlockDestroyed		REAL
	 *  18 timesOfDeathByFallingIntoGap		REAL
	 *  19 timesOfDeathByRedTurtle		REAL
	 *  20 timesOfDeathByGoomba		REAL
	 *  21timesOfDeathByGreenTurtle		REAL
	 *  22 timesOfDeathByArmoredTurtle		REAL
	 *  23 timesOfDeathByJumpFlower		REAL
	 *  24 timesOfDeathByCannonBall		REAL
	 *  25 timesOfDeathByChompFlower		REAL
	 *  26 percentageEnemiesKilled		REAL
	 *  27 percentageCoinsCollected		REAL
	 *	28 percentageSpentRunning		REAL
	 *	29 percentageSpentRunningRight		REAL
	 *	30 percentageTimeTaken		REAL
	 */
	private class DecisionTree {

		String[][] tree;
		HashMap<String, Integer> map;
		//make the decision tree :)
		public DecisionTree(String pathToDataFile) throws Exception {
			//results
			BufferedReader in = new BufferedReader(new FileReader(pathToDataFile));
			tree = new String[1000][1000];
			map = new HashMap<String, Integer>();
			String str;

			int readingDataMap = 0;
			boolean readingTree = false;
			int index = -1;
			int skip = 0;
			int counter = 0;

			while(true) {
				str = in.readLine().trim();
				if(skip --> 0) {
					continue;
				}
				if(str.contains("Attributes")){
					readingDataMap = Integer.parseInt(str.split(":")[1].trim());
				}

				if(readingDataMap --> 0) {
					map.put(str,index++);
				}

				if(readingTree) {
					String[] arr = str.split("[|]");
					tree[counter++][arr.length - 1] = arr[arr.length - 1].trim();
				}

				if(str.contains("=== Classifier model (full training set) ===")){
					readingTree = true;
					skip = 4;
				}

				if(readingTree && str.equals("")){
					break;
				}
			}

		}

		public int findClass(double[] attr){

			System.out.println(map);

			int ptrx = 0;
			int ptry = 0;

			boolean found = false;

			while(true) {
				String[] curr = tree[ptrx][ptry].split(" ");
				if(curr[2].contains(":")){
					return Integer.parseInt(curr[3]);
				}
				int currAttr = map.get(curr[0].trim());
				String curropt = curr[1].trim();
				double currval = Double.parseDouble(curr[2].trim());
				double compAttr = attr[currAttr];

				System.out.println(ptrx + " " + ptry);
				System.out.println(currAttr + " " + curropt + " " + currval + " " + compAttr);

				double comp = compAttr - currval;
				if((comp > 0 && curropt.contains(">"))
						|| (comp < 0 && curropt.contains("<"))
						|| (comp == 0 && curropt.contains("="))
				  ){
					ptrx ++;
					ptry ++;
				  }
				else {
					while(tree[++ptrx][ptry] == null );
				}
			}
		}
	}
}
