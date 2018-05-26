//package mycontroller;
//
//import java.util.HashMap;
//
//import tiles.MapTile;
//import utilities.Coordinate;
//import world.Car;
//import world.WorldSpatial.Direction;
//
//public class StrategyFactory {
//	private static StrategyFactory instance = null;
//	
//	private StrategyFactory() {
//		
//	}
//	
//	public static StrategyFactory getInstance() {
//		if (instance == null) {
//			instance = new StrategyFactory();
//		}
//		
//		return instance;
//	}
//	
//	public WallFollowingStrategy getWallFollowingStrategy(Car car, int wallSensitivity, int EAST_THRESHOLD) {
//		return new WallFollowingStrategy(car, wallSensitivity, EAST_THRESHOLD);
//	}
//	
//	public DjikstraStrategy getDjikstraStrategy() {
//		return new DjikstraStrategy();
//	}
//
//	public ReverseStrategy getReverseStrategy(Car car) {
//		return new ReverseStrategy(car);
//	}
//}
