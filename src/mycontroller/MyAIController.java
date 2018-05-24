package mycontroller;

import java.util.HashMap;

import controller.CarController;
import tiles.MapTile;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;

public class MyAIController extends CarController{

	HashMap<Coordinate, MapTile> map;
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
		// currentStrategy = StrategyFactory.getInstance().getWallFollowingStrategy();
		
		// get the current view and location that we need to pass into Dijkstra
		HashMap<Coordinate, MapTile> currentView = getView();
		Coordinate currentLoc = new Coordinate(getPosition());
		
		// in the final edition, this would need to be a function leading to the key we can
		// pick up at this time.
		
		Coordinate finalDestination = new Coordinate(5, 5);
		currentStrategy = StrategyFactory.getInstance().getDijkstraStrategy(car, currentLoc, 
				currentView, finalDestination, previousState);
	}

	@Override
	public void update(float delta) {
		// going to assume that we know the coordinate we're getting to:
		Coordinate firstKey = new Coordinate(5, 8);
		currentStrategy.mapDetails(map, firstKey);
		// MoveDecision decisionMade = currentStrategy.getNextMove(this.map, getX(), getY(), isFollowingWall, previousState);
		MoveDecision decisionMade = currentStrategy.getNextMove(this.map, isFollowingWall, previousState);
		switch (decisionMade.nextMove) {
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
