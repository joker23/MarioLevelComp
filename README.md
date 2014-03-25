changelog
=========
- changed the datarecorder and other things so that they take on myLevel
- MarioComponents is changed

compiling
=========
- use ant build
	$ant

running
========
java -cp bin dk.itu.mario.engine.PlayCustomized

NOTE: first run will not have any player data so it will generate a default map (similar to random)

running different types of Maps
========
- go into src/dk/itu/mario/level/generator/MyLevelGenerator.java
- uncomment the type of map you wish to see
- then re-compile the game and run it!
