//package mycontroller;
//
//import java.util.HashMap;
//import world.Car;
//import world.WorldSpatial;
//import tiles.MapTile;
//import utilities.Coordinate;
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
//	public WallFollowingStrategy getWallFollowingStrategy() {
//		return new WallFollowingStrategy();
//	}
//	
//	public DijkstraStrategy getDijkstraStrategy(Car car, Coordinate currentLoc,
//			HashMap<Coordinate,MapTile> currentView, Coordinate finalDestination,
//			WorldSpatial.Direction previousState) {
//		return new DijkstraStrategy(car, currentLoc, currentView, finalDestination, previousState);
//	}
//
//}
