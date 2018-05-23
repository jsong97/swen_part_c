package mycontroller;

import java.util.HashMap;

import tiles.MapTile;
import utilities.Coordinate;
import world.WorldSpatial.Direction;

public class WallFollowingStrategy extends Strategy {

	public WallFollowingStrategy() {
		
	}
	
	public MoveDecision getNextMove(HashMap<Coordinate, MapTile> map, float carX, float carY, boolean isFollowingWall, Direction previousState) {
		HashMap<String, ?> test = new HashMap<String, ?> ();
		return new;
	}

}
