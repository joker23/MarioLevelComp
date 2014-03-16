changelog
=========
- changed the datarecorder and other things so that they take on myLevel
- everything works now and we have output
- now we need someone good at design to design a game :D
- Made Deterministic ways of generating a map

Logic
=====
- Use a BFS to see where we can go
- Generate randomly from that at first...
- Generation order:
	1) ground
	2) blocks (part of the terrains)
	3) obstacles (stairs, gaps, cannons, tubes)
	4) enemies
	5) coins
- use line sweep to tell where how mario can move...

Enemy And Objects
=====
- Terrain elements
	- Pipes 	: prevent continuous runnning
	- Stairs 	: prevent continuous running
	- Cannon	: just makes things hard
	- blocks	: can be stacked on the top
- enemies


