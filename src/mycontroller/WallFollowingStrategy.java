package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tiles.HealthTrap;
import tiles.LavaTrap;
import tiles.MapTile;
import utilities.Coordinate;
import world.Car;
import world.World;
import world.WorldSpatial;
import world.WorldSpatial.Direction;
import world.WorldSpatial.RelativeDirection;

public class WallFollowingStrategy extends Strategy {
	
	private static WallFollowingStrategy instance = null;
	
	public static WallFollowingStrategy getInstance(Car car,int wallSensitivity, int EAST_THRESHOLD) {
		if (instance == null) {
			instance = new WallFollowingStrategy(car, wallSensitivity, EAST_THRESHOLD);
		}
		
		return instance;
	}
	
	private int wallSensitivity = 2;
	private boolean isFollowingWall = false; // This is initialized when the car sticks to a wall.
	private WorldSpatial.RelativeDirection lastTurnDirection = null; // Shows the last turn direction the car takes.
	private boolean isTurningLeft = false;
	private boolean isTurningRight = false; 
	private WorldSpatial.Direction previousState = null;
	private final float CAR_SPEED = 3;
	
	private HashMap<Coordinate, MapTile> map;
	public HashMap<Coordinate, Integer> keyMap;
	MoveDecision nextMove;
	List<Coordinate> visitedCoords;
	
	private int EAST_THRESHOLD;
	private int LAVA_ENTRY_THRESHOLD = 930;
	private int keysToFind;
	
	public WallFollowingStrategy(Car car, int wallSensitivity, int EAST_THRESHOLD) {
		super(car);
		this.wallSensitivity = wallSensitivity;
		this.EAST_THRESHOLD = EAST_THRESHOLD;
		map = World.getMap();
		keyMap = new HashMap<Coordinate, Integer>();
		visitedCoords = new ArrayList<Coordinate>();
		keysToFind = car.getKey();
	}
	
	public boolean getNextMove(HashMap<Coordinate, MapTile> map, float delta) {
		// Gets what the car can see
		HashMap<Coordinate, MapTile> currentView = car.getView();
		
		//Update map
		for (Coordinate coord : currentView.keySet()) {
			if (!visitedCoords.contains(coord)) {
				visitedCoords.add(coord);
			}
			
			MapTile tile = currentView.get(coord);
			if (tile.getType().equals(MapTile.Type.TRAP)) {
				this.map.put(coord, tile);
				if (tile instanceof LavaTrap && ((LavaTrap) tile).getKey() != 0) {
					int keyNum = ((LavaTrap)tile).getKey();
					keyMap.put(coord, keyNum);
				}
			}
		}
		
		System.out.println(keyMap.size());
		checkStateChange();
		detectBox(currentView, delta);
		
		// Check if finished finding keys
		if (keyMap.size() >= keysToFind - 1) {
			System.out.println("We donezo");
			return false;
		}
		
		// Check if stuck somewhere
		if (car.getSpeed() == 0 && visitedCoords.size() > 81) {
			return true;
		}

		// If you are not following a wall initially, find a wall to stick to!
		if(!isFollowingWall){
			if(car.getSpeed() < CAR_SPEED){
				car.applyForwardAcceleration();
			}
			// Turn towards the north
			if(!car.getOrientation().equals(WorldSpatial.Direction.NORTH)){
				lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
				if (car.getSpeed() == CAR_SPEED) {
					car.brake();
				}
				applyLeftTurn(car.getOrientation(),delta);
			}
			if(checkNorth(currentView, wallSensitivity+1)){
				// Turn right until we go back to east!
				if(!car.getOrientation().equals(WorldSpatial.Direction.EAST)){
					lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
					if (car.getSpeed() == CAR_SPEED) {
						car.brake();
					}
					applyRightTurn(car.getOrientation(),delta);
				}
				else{
					isFollowingWall = true;
				}
			}
		}
		// Once the car is already stuck to a wall, apply the following logic
		else{
			
			// Readjust the car if it is misaligned.
			readjust(lastTurnDirection,delta,isTurningLeft, isTurningRight);
			
			if(isTurningRight){
				applyRightTurn(car.getOrientation(),delta);
			}
			else if(isTurningLeft){
				// Apply the left turn if you are not currently near a wall.
				if(!checkFollowingWall(car.getOrientation(),currentView)){
					applyLeftTurn(car.getOrientation(),delta);
				}
				else{
					isTurningLeft = false;
				}
			}
			// Try to determine whether or not the car is next to a wall.
			else if(checkFollowingWall(car.getOrientation(),currentView)){
				// Maintain some velocity
				if(car.getSpeed() < CAR_SPEED){
					car.applyForwardAcceleration();
				}
				// If there is wall ahead, turn right!
				if(checkWallAhead(car.getOrientation(),currentView)){
					lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
					isTurningRight = true;				
					
				}

			}
			// This indicates that I can do a left turn if I am not turning right
			else{
				lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
				isTurningLeft = true;
			}
		}
		
		return false;
	}
	
	
	public boolean detectBox(HashMap<Coordinate, MapTile> currentView, float delta) {
		int count = 0;
		if (checkNorth(currentView, wallSensitivity)) {
			count++;
		}
		if (checkSouth(currentView, wallSensitivity)) {
			count++;
		}
		if (checkEast(currentView, wallSensitivity)) {
			count++;
		}
		if (checkWest(currentView, wallSensitivity)) {
			count++;
		}
		
		if (count == 3) {
			System.out.println("Box detected\n");
//			turnAround(delta);
			return true;
		}
		
		return false;
	}	
	

	/**
	 * Readjust the car to the orientation we are in.
	 * @param lastTurnDirection
	 * @param delta
	 */
	private void readjust(WorldSpatial.RelativeDirection lastTurnDirection, float delta, boolean isTurningLeft, boolean isTurningRight) {
		if(lastTurnDirection != null){
			if(!isTurningRight && lastTurnDirection.equals(WorldSpatial.RelativeDirection.RIGHT)){
				adjustRight(car.getOrientation(),delta);
			}
			else if(!isTurningLeft && lastTurnDirection.equals(WorldSpatial.RelativeDirection.LEFT)){
				adjustLeft(car.getOrientation(),delta);
			}
		}
		
	}
	
	/**
	 * Try to orient myself to a degree that I was supposed to be at if I am
	 * misaligned.
	 */
	private void adjustLeft(WorldSpatial.Direction orientation, float delta) {
		
		switch(orientation){
		case EAST:
			if(car.getAngle() > WorldSpatial.EAST_DEGREE_MIN+EAST_THRESHOLD){
				car.turnRight(delta);
			}
			break;
		case NORTH:
			if(car.getAngle() > WorldSpatial.NORTH_DEGREE){
				car.turnRight(delta);
			}
			break;
		case SOUTH:
			if(car.getAngle() > WorldSpatial.SOUTH_DEGREE){
				car.turnRight(delta);
			}
			break;
		case WEST:
			if(car.getAngle() > WorldSpatial.WEST_DEGREE){
				car.turnRight(delta);
			}
			break;
			
		default:
			break;
		}
		
	}

	private void adjustRight(WorldSpatial.Direction orientation, float delta) {
		switch(orientation){
		case EAST:
			if(car.getAngle() > WorldSpatial.SOUTH_DEGREE && car.getAngle() < WorldSpatial.EAST_DEGREE_MAX){
				car.turnLeft(delta);
			}
			break;
		case NORTH:
			if(car.getAngle() < WorldSpatial.NORTH_DEGREE){
				car.turnLeft(delta);
			}
			break;
		case SOUTH:
			if(car.getAngle() < WorldSpatial.SOUTH_DEGREE){
				car.turnLeft(delta);
			}
			break;
		case WEST:
			if(car.getAngle() < WorldSpatial.WEST_DEGREE){
				car.turnLeft(delta);
			}
			break;
			
		default:
			break;
		}
		
	}
	
	/**
	 * Checks whether the car's state has changed or not, stops turning if it
	 *  already has.
	 */
	private void checkStateChange() {
		if(previousState == null){
			previousState = car.getOrientation();
		}
		else{
			if(previousState != car.getOrientation()){
				if(isTurningLeft){
					isTurningLeft = false;
				}
				if(isTurningRight){
					isTurningRight = false;
				}
				previousState = car.getOrientation();
			}
		}
	}
	
	/**
	 * Turn the car counter clock wise (think of a compass going counter clock-wise)
	 */
	private void applyLeftTurn(WorldSpatial.Direction orientation, float delta) {
		switch(orientation){
		case EAST:
			if(!car.getOrientation().equals(WorldSpatial.Direction.NORTH)){
				car.turnLeft(delta);
			}
			break;
		case NORTH:
			if(!car.getOrientation().equals(WorldSpatial.Direction.WEST)){
				car.turnLeft(delta);
			}
			break;
		case SOUTH:
			if(!car.getOrientation().equals(WorldSpatial.Direction.EAST)){
				car.turnLeft(delta);
			}
			break;
		case WEST:
			if(!car.getOrientation().equals(WorldSpatial.Direction.SOUTH)){
				car.turnLeft(delta);
			}
			break;
		default:
			break;
		
		}
		
	}
	
	/**
	 * Turn the car clock wise (think of a compass going clock-wise)
	 */
	private void applyRightTurn(WorldSpatial.Direction orientation, float delta) {
		switch(orientation){
		case EAST:
			if(!car.getOrientation().equals(WorldSpatial.Direction.SOUTH)){
				car.turnRight(delta);
			}
			break;
		case NORTH:
			if(!car.getOrientation().equals(WorldSpatial.Direction.EAST)){
				car.turnRight(delta);
			}
			break;
		case SOUTH:
			if(!car.getOrientation().equals(WorldSpatial.Direction.WEST)){
				car.turnRight(delta);
			}
			break;
		case WEST:
			if(!car.getOrientation().equals(WorldSpatial.Direction.NORTH)){
				car.turnRight(delta);
			}
			break;
		default:
			break;
		
		}
		
	}

	/**
	 * Check if you have a wall in front of you!
	 * @param orientation the orientation we are in based on WorldSpatial
	 * @param currentView what the car can currently see
	 * @return
	 */
	private boolean checkWallAhead(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView){
		switch(orientation){
		case EAST:
			return checkEast(currentView, wallSensitivity);
		case NORTH:
			return checkNorth(currentView, wallSensitivity);
		case SOUTH:
			return checkSouth(currentView, wallSensitivity);
		case WEST:
			return checkWest(currentView, wallSensitivity);
		default:
			return false;
		
		}
	}
	
	/**
	 * Check if the wall is on your left hand side given your orientation
	 * @param orientation
	 * @param currentView
	 * @return
	 */
	private boolean checkFollowingWall(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView) {
		
		switch(orientation){
		case EAST:
			return checkNorth(currentView, wallSensitivity);
		case NORTH:
			return checkWest(currentView, wallSensitivity);
		case SOUTH:
			return checkEast(currentView, wallSensitivity);
		case WEST:
			return checkSouth(currentView, wallSensitivity);
		default:
			return false;
		}
		
	}
	
	
	/**
	 * Method below just iterates through the list and check in the correct coordinates.
	 * i.e. Given your current position is 10,10
	 * checkEast will check up to wallSensitivity amount of tiles to the right.
	 * checkWest will check up to wallSensitivity amount of tiles to the left.
	 * checkNorth will check up to wallSensitivity amount of tiles to the top.
	 * checkSouth will check up to wallSensitivity amount of tiles below.
	 */
	public boolean checkEast(HashMap<Coordinate, MapTile> currentView, int sensitivity){
		// Check tiles to my right
		Coordinate currentPosition = new Coordinate(car.getPosition());
		for(int i = 0; i <= sensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x+i, currentPosition.y));
			if(tile.isType(MapTile.Type.WALL) || tile.isType(MapTile.Type.FINISH) || 
					(car.getHealth() < LAVA_ENTRY_THRESHOLD && tile instanceof LavaTrap && !(map.get(currentPosition) instanceof LavaTrap))){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkWest(HashMap<Coordinate,MapTile> currentView, int sensitivity){
		// Check tiles to my left
		Coordinate currentPosition = new Coordinate(car.getPosition());
		for(int i = 0; i <= sensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x-i, currentPosition.y));
			if(tile.isType(MapTile.Type.WALL) || tile.isType(MapTile.Type.FINISH) || 
					(car.getHealth() < LAVA_ENTRY_THRESHOLD && tile instanceof LavaTrap && !(map.get(currentPosition) instanceof LavaTrap))){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkNorth(HashMap<Coordinate,MapTile> currentView, int sensitivity){
		// Check tiles to towards the top
		Coordinate currentPosition = new Coordinate(car.getPosition());
		for(int i = 0; i <= sensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y+i));
			if(tile.isType(MapTile.Type.WALL) || tile.isType(MapTile.Type.FINISH) || 
					(car.getHealth() < LAVA_ENTRY_THRESHOLD && tile instanceof LavaTrap && !(map.get(currentPosition) instanceof LavaTrap))){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkSouth(HashMap<Coordinate,MapTile> currentView, int sensitivity){
		// Check tiles towards the bottom
		Coordinate currentPosition = new Coordinate(car.getPosition());
		for(int i = 0; i <= sensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y-i));
			if(tile.isType(MapTile.Type.WALL) || tile.isType(MapTile.Type.FINISH) || 
					(car.getHealth() < LAVA_ENTRY_THRESHOLD && tile instanceof LavaTrap && !(map.get(currentPosition) instanceof LavaTrap))){
				return true;
			}
		}
		return false;
	}
	

}
