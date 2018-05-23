package mycontroller;

import java.util.HashMap;

import tiles.MapTile;
import utilities.Coordinate;
import world.WorldSpatial.Direction;

public class StrategyFactory {
	private static StrategyFactory instance = null;
	
	private StrategyFactory() {
		
	}
	
	public static StrategyFactory getInstance() {
		if (instance == null) {
			instance = new StrategyFactory();
		}
		
		return instance;
	}
	
	public WallFollowingStrategy getWallFollowingStrategy() {
		return new WallFollowingStrategy();
	}
	
	public DijkstraStrategy getDijkstraStrategy() {
		return new DijkstraStrategy();
	}

}
