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
	
	Strategy currentStrategy;
	// How many minimum units the wall is away from the player.
	private int wallSensitivity = 2;
	private boolean isFollowingWall = false; // This is initialized when the car sticks to a wall.
	private WorldSpatial.RelativeDirection lastTurnDirection = null; // Shows the last turn direction the car takes.
	private boolean isTurningLeft = false;
	private boolean isTurningRight = false; 
	private WorldSpatial.Direction previousState = null; // Keeps track of the previous state
	enum CAR_STATE { FINDING_KEYS, RETRIEVING_KEYS, REVERSING };
	CAR_STATE currentState;
	
	// Car Speed to move at
	private final float CAR_SPEED = 3;
	
	// Offset used to differentiate between 0 and 360 degrees
	private int EAST_THRESHOLD = 3;
	
	
	Coordinate initialGuess;
	boolean notSouth = true;
	
	public MyAIController(Car car) {
		super(car);
		currentState = CAR_STATE.FINDING_KEYS;
		Strategy keyFinding = WallFollowingStrategy.getInstance(car, wallSensitivity, EAST_THRESHOLD);
		currentStrategy = keyFinding;
		
	}

	@Override
	public void update(float delta) {
		if (currentState == CAR_STATE.FINDING_KEYS) {
			boolean stuck = this.currentStrategy.getNextMove(this.map, delta);
			if (stuck) {
				System.out.println("Time to reverse");
				currentState = CAR_STATE.REVERSING;
				currentStrategy = ReverseStrategy.getInstance(car);
			}
		} else if (currentState == CAR_STATE.REVERSING) {
			boolean gucci = this.currentStrategy.getNextMove(this.map, delta);
			if (gucci) {
				currentState = CAR_STATE.FINDING_KEYS;
				currentStrategy = WallFollowingStrategy.getInstance(car, wallSensitivity, EAST_THRESHOLD);
			}
		}
		
				
	}
	
	

}
