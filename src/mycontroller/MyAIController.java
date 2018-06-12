package mycontroller;

import java.util.HashMap;

import controller.CarController;
import tiles.MapTile;
import utilities.Coordinate;
import world.Car;
import world.World;

public class MyAIController extends CarController{

	HashMap<Coordinate,MapTile> map;
	HashMap<Coordinate, Boolean> visitedMap;
	
	// we need to find the number of keys there are from the beginning
	int numKeys;
	
	MovementStrategy currentStrategy;
	DijkstraStrategy dijkstraStrategy;
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
		MovementStrategy keyFinding = WallFollowingStrategy.getInstance(car, wallSensitivity, EAST_THRESHOLD);
		currentStrategy = keyFinding;
		dijkstraStrategy = DijkstraStrategy.getInstance(car);
		this.map = World.getMap();
		
		// instantiate this at the beginning
		this.numKeys = car.getKey();
	}

	@Override
	public void update(float delta) {
		if (currentState == CAR_STATE.FINDING_KEYS) {
			this.keyMap = this.currentStrategy.makeNextMove(this.map, delta);
			if ((keyMap != null) && (keyMap.size() == numKeys)){
				currentState = CAR_STATE.RETRIEVING_KEYS;
			}
		} 
		else if (currentState == CAR_STATE.RETRIEVING_KEYS) {
			dijkstraStrategy.setKeyMap(keyMap);
			currentStrategy = dijkstraStrategy;
			this.keyMap = this.currentStrategy.makeNextMove(this.map, delta);		
		}			
	}
}