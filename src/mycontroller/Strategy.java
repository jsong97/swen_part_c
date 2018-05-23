package mycontroller;

import java.util.HashMap;

import tiles.MapTile;
import utilities.Coordinate;
import world.WorldSpatial;

public abstract class Strategy {
	
	public Strategy() {
		
	}
	
	public void mapDetails(HashMap<Coordinate,MapTile> currentView, Coordinate finalDestination) {
	}
	
//	abstract MoveDecision getNextMove(HashMap<Coordinate,MapTile> map, float carX, float carY, boolean isFollowingWall, WorldSpatial.Direction previousState);
	
	abstract MoveDecision getNextMove(HashMap<Coordinate,MapTile> map, boolean isFollowingWall, WorldSpatial.Direction previousState);
}