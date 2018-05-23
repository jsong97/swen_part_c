package mycontroller;

import world.WorldSpatial;
import utilities.Coordinate;

import java.util.HashMap;

import tiles.MapTile;

public class MoveDecision {
	boolean isFollowingWall;
	WorldSpatial.Direction previousState;
	int nextMove;
	HashMap<Coordinate, MapTile> map;
	HashMap<Coordinate, Boolean> visitedMap;
	
	public MoveDecision(boolean isFollowingWall, WorldSpatial.Direction previousState, int nextMove,
			HashMap<Coordinate, MapTile> map) {
		this.isFollowingWall = isFollowingWall;
		this.previousState = previousState;
		this.nextMove = nextMove;
		this.map = map;
		// this.visitedMap = visitedMap;
	}
}
