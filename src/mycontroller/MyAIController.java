package mycontroller;

import java.util.HashMap;

import controller.CarController;
import tiles.MapTile;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;

public class MyAIController extends CarController{

	HashMap<Coordinate,MapTile> map;
	HashMap<Coordinate, Boolean> visitedMap;
	
	// we need to find the number of keys there are from the beginning
	int numKeys;
	
	Strategy currentStrategy;
	// How many minimum units the wall is away from the player.
	private int wallSensitivity = 2;

	enum CAR_STATE { FINDING_KEYS, RETRIEVING_KEYS };
	CAR_STATE currentState;
	
	private HashMap<Coordinate, Integer> keyMap;
	
	// Offset used to differentiate between 0 and 360 degrees
	private int EAST_THRESHOLD = 3;
	
	
	Coordinate initialGuess;
	boolean notSouth = true;
	
	public MyAIController(Car car) {
		super(car);
		currentState = CAR_STATE.FINDING_KEYS;
		Strategy keyFinding = WallFollowingStrategy.getInstance(car, wallSensitivity, EAST_THRESHOLD);
		currentStrategy = keyFinding;	
		
		// instantiate this at the beginning
		this.numKeys = car.getKey();
	}

	@Override
	public void update(float delta) {
		if (currentState == CAR_STATE.FINDING_KEYS) {
			this.keyMap = this.currentStrategy.getNextMove(this.map, delta);
			if (keyMap.size() == numKeys){
				System.out.println("SWAPPING TO DIJKSTRA");
				currentState = CAR_STATE.RETRIEVING_KEYS;
			}
		} else {
			currentStrategy = DijkstraStrategy.getInstance(car, keyMap);
			this.keyMap = this.currentStrategy.getNextMove(this.map, delta);
			
		}
//		else if (currentState == CAR_STATE.REVERSING) {
//			boolean gucci = this.currentStrategy.getNextMove(this.map, delta);
//			if (gucci) {
//				currentState = CAR_STATE.FINDING_KEYS;
//				currentStrategy = WallFollowingStrategy.getInstance(car, wallSensitivity, EAST_THRESHOLD);
//			}
//		} else if (currentState == CAR_STATE.DIJKSTRA) {
//			// we still need to set the final destination
//			currentStrategy = DijkstraStrategy.getInstance(car);
//			
//		}
		
				
	}
	
	

}