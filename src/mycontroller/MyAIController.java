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
	
	// Car Speed to move at
	private final float CAR_SPEED = 3;
	
	// Offset used to differentiate between 0 and 360 degrees
	private int EAST_THRESHOLD = 3;
	
	
	Coordinate initialGuess;
	boolean notSouth = true;
	
	public MyAIController(Car car) {
		super(car);
		currentStrategy = StrategyFactory.getInstance().getWallFollowingStrategy();
	}

	@Override
	public void update(float delta) {
		switch (this.currentStrategy.getNextMove(this.map, getX(), getY(), isFollowingWall, previousState)) {
			case 1:
				applyForwardAcceleration();
				break;
			case 2:
				applyReverseAcceleration();
				break;
			case 3:
				turnLeft(delta);
				break;
			case 4:
				turnRight(delta);
				break;
		}
				
	}
	
	

}
