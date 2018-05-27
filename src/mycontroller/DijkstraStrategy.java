package mycontroller;

import java.util.ArrayList;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


import mycontroller.Edge;
import mycontroller.Vertex;
import mycontroller.Graph;

import tiles.MapTile;
import utilities.Coordinate;
import world.World;
import world.WorldSpatial;
import world.WorldSpatial.Direction;
import world.Car;

import tiles.HealthTrap;
import tiles.LavaTrap;
import tiles.MapTile;
import world.WorldSpatial.RelativeDirection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



// the Dijkstra class needs to:
// 1. get the hashmap of the map
// 2. add the destination
// 3. get the current location
// 4. calculate the route
// 5. return the next step (just has to move to the next node)

public class DijkstraStrategy extends Strategy {
	// this is the full map
	private HashMap<Coordinate, MapTile> worldMap;
	
    // this is the current view and current location
    private HashMap<Coordinate, MapTile> currentView;
    private Coordinate currentLoc;
    
    // this is the destination
    private HashMap<Coordinate, Integer> keyMap;
    private Coordinate finalDestination;
    
    // these are needed to build a map of nodes
    private ArrayList<Vertex> nodes;
    private ArrayList<Edge> edges;
    private Set<Vertex> settledNodes;
    private Set<Vertex> unSettledNodes;
    private Map<Vertex, Vertex> predecessors;
    private Map<Vertex, Integer> distance;
    
	HashMap<Coordinate, MapTile> map;
	HashMap<Coordinate, Boolean> visitedMap;
	
	Strategy currentStrategy;
	// How many minimum units the wall is away from the player.
	private int wallSensitivity = 2;
	private boolean isFollowingWall = false; // This is initialized when the car sticks to a wall.
	private WorldSpatial.RelativeDirection lastTurnDirection = null; // Shows the last turn direction the car takes.
	private boolean isTurningLeft = false;
	private boolean isTurningRight = false; 
	
	// Car Speed to move at
	private final float CAR_SPEED = 3;
	
	// Offset used to differentiate between 0 and 360 degrees
	private int EAST_THRESHOLD = 3;
	
	
	Coordinate initialGuess;
	boolean notSouth = true;
    
    // this is the graph we use to perform dijkstra
    private Graph graph;
    
    // this is the path we take to the destination
    private LinkedList<Vertex> path;
    
    private Car car;

    private static DijkstraStrategy instance = null;
    
    public static DijkstraStrategy getInstance(Car car) {
		if (instance == null) {
			instance = new DijkstraStrategy(car);
		}
		return instance;
	}
  
    public DijkstraStrategy(Car car) {
    	// get the overall map from the game
    	super(car);
    	this.worldMap = World.getMap();
    	this.car = car;
    	this.currentLoc = new Coordinate(car.getPosition());
    	
    	// add the destination and the current view
    	// this is all in mapDetails()
    	// add the destination
    	
    	// get the current view from MyAIController
    	this.currentView = car.getView();
    	
        // use the buildRoute() function to get the route and graph
        // use getNextMove() to return the next step   
    }
    
    public void setDestination(HashMap<Coordinate, Integer> keyMap) {
    	this.keyMap = keyMap;
    	int keyToFind = car.getKey();
    	for (Coordinate keyLoc : keyMap.keySet()) {
    		if (keyToFind == keyMap.get(keyLoc)) {
    			this.finalDestination = keyLoc;
    		}
    	}
    }
  
    
    // this will build a graph using vertexes and nodes from scratch
    public void buildRoute() {
    	// create a copy of the array so that we can operate on this array
    	this.nodes = new ArrayList<Vertex>();
        this.edges = new ArrayList<Edge>();
        
        // converting HashMap into something useable
        // just an iterator to give ids to the nodes
        int id_count = 0;
        
        // also find the destination id of the coordinate
        int destination_id = 0;
        
        
	    for (Coordinate key : worldMap.keySet()) {
	    	Vertex location = new Vertex(id_count, key);
	    	nodes.add(location);
	    	id_count += 1;
	    	if (key.equals(finalDestination)) {
	    		destination_id = id_count;
	    	}
	    }
	    
	    // we need to convert the HashMap<Coordinate, MapTile>
	    // right now just adding random coordinates
	    
	    // now we need to build all possible edges that we can...
	    // also need a nested array...
	    for (Vertex spot : nodes) {
	    	int spot_x_loc = spot.getLocation().getX();
	    	int spot_y_loc = spot.getLocation().getY();
	    	for (Vertex inner_spot : nodes) {
	    		int inner_spot_x_loc = inner_spot.getLocation().getX();
	    		int inner_spot_y_loc = inner_spot.getLocation().getY();
		    	if (spot_x_loc == inner_spot_x_loc) {
		    		// 'inner spot' is either right below or right above 'spot'
		    		if (spot_y_loc - inner_spot_y_loc == 1){
		    			addLane(spot.getId(), inner_spot.getId());
		    		}
		    		else if ((spot_y_loc - inner_spot_y_loc) == -1) {
		    			addLane(spot.getId(), inner_spot.getId());
		    		}
		    	} 
		    	else if (spot_y_loc == inner_spot_y_loc) {
		    		// 'inner spot' is either left or right of 'spot'
		    		if (spot_x_loc - inner_spot_x_loc == 1){
		    			addLane(spot.getId(), inner_spot.getId());
		    		}
		    		else if ((spot_x_loc - inner_spot_x_loc) == -1) {
		    			addLane(spot.getId(), inner_spot.getId());
		    		}
		    	}
		    }
	    }
	
	    // Lets check from location Loc_1 to Loc_10
	    this.graph = new Graph(nodes, edges);
	    
	    
	    // the location we'll start from (vertex 0)
	    execute(nodes.get(0));
	    
	    // the target location and the path to it (destination vertex in this case)
	    path = getPath(nodes.get(destination_id));
	
	    assert path != null;
	    assert path.size() > 0;
	
	    for (Vertex vertex : path) {
	        System.out.println(vertex.getId());
	    }
	}

	public void addLane(int sourceLocNo, int destLocNo) {
		Coordinate sourceLoc = nodes.get(sourceLocNo).getLocation();
		Coordinate destLoc = nodes.get(sourceLocNo).getLocation();
		int firstWeight = 0;
		int secondWeight = 0;
		int finalWeight = 0;
		for (Coordinate key : worldMap.keySet()){
			if (sourceLoc.equals(key)) {
				switch(worldMap.get(key).getType()) {
					case WALL:
						firstWeight = 10000;
						break;
					case TRAP:
						firstWeight = 10;
						break;
					default:
						firstWeight = 1;
						break;
				}
			}
		};
		for (Coordinate key : worldMap.keySet()){
			if (destLoc.equals(key)) {
				switch(worldMap.get(key).getType()) {
					case WALL:
						secondWeight = 10000;
						break;
					case TRAP:
						secondWeight = 10;
						break;
					default:
						secondWeight = 1;
						break;
				}
			}
		};
		finalWeight = firstWeight + secondWeight;
	    Edge lane = new Edge(nodes.get(sourceLocNo), nodes.get(destLocNo), finalWeight);
	    edges.add(lane);
	}
	

    public void execute(Vertex source) {
        settledNodes = new HashSet<Vertex>();
        unSettledNodes = new HashSet<Vertex>();
        distance = new HashMap<Vertex, Integer>();
        predecessors = new HashMap<Vertex, Vertex>();
        distance.put(source, 0);
        unSettledNodes.add(source);
        while (unSettledNodes.size() > 0) {
            Vertex node = getMinimum(unSettledNodes);
            settledNodes.add(node);
            unSettledNodes.remove(node);
            findMinimalDistances(node);
        }
    }

    private void findMinimalDistances(Vertex node) {
        List<Vertex> adjacentNodes = getNeighbors(node);
        for (Vertex target : adjacentNodes) {
            if (getShortestDistance(target) > getShortestDistance(node)
                    + getDistance(node, target)) {
                distance.put(target, getShortestDistance(node)
                        + getDistance(node, target));
                predecessors.put(target, node);
                unSettledNodes.add(target);
            }
        }

    }

    private int getDistance(Vertex node, Vertex target) {
        for (Edge edge : edges) {
            if (edge.getSource().equals(node)
                    && edge.getDestination().equals(target)) {
                return edge.getWeight();
            }
        }
        throw new RuntimeException("Should not happen");
    }

    private List<Vertex> getNeighbors(Vertex node) {
        List<Vertex> neighbors = new ArrayList<Vertex>();
        for (Edge edge : edges) {
            if (edge.getSource().equals(node)
                    && !isSettled(edge.getDestination())) {
                neighbors.add(edge.getDestination());
            }
        }
        return neighbors;
    }

    private Vertex getMinimum(Set<Vertex> vertexes) {
        Vertex minimum = null;
        for (Vertex vertex : vertexes) {
            if (minimum == null) {
                minimum = vertex;
            } else {
                if (getShortestDistance(vertex) < getShortestDistance(minimum)) {
                    minimum = vertex;
                }
            }
        }
        return minimum;
    }

    private boolean isSettled(Vertex vertex) {
        return settledNodes.contains(vertex);
    }

    private int getShortestDistance(Vertex destination) {
        Integer d = distance.get(destination);
        if (d == null) {
            return Integer.MAX_VALUE;
        } else {
            return d;
        }
    }

    /*
     * This method returns the path from the source to the selected target and
     * NULL if no path exists
     */
    
    public LinkedList<Vertex> getPath(Vertex target) {
        LinkedList<Vertex> path = new LinkedList<Vertex>();
        Vertex step = target;
        // check if a path exists
        if (predecessors.get(step) == null) {
            return null;
        }
        path.add(step);
        while (predecessors.get(step) != null) {
            step = predecessors.get(step);
            path.add(step);
        }
        // Put it into the correct order
        Collections.reverse(path);
        return path;
    }

	int findDirectionToMove(HashMap<Coordinate, MapTile> map, boolean isFollowingWall) {
		// we need to first take the next tile on the route
		buildRoute();
		
		// get the next stop on the path
		Vertex nextVertex = path.get(1);
		int xDirection = nextVertex.getLocation().getX() - currentLoc.getX();
		int yDirection = nextVertex.getLocation().getY() - currentLoc.getY();
		
		int directionMoving;
		
		// we need to go up
		if (yDirection > 0) {
			directionMoving = 4;
		} 
		
		// we need to go down
		else if (yDirection < 0 ){
			directionMoving = 3;
		}
		
		// we need to go right
		else if (xDirection > 0) {
			directionMoving = 2;
		}
		
		// we need to go left
		else  {
			directionMoving = 1;
		}
		return directionMoving;
	}
	
	public boolean getNextMove(HashMap<Coordinate, MapTile> map, float delta) {
		// going to assume that we know the coordinate we're getting to:
		setDestination(WallFollowingStrategy.keyMap);
		
		// MoveDecision decisionMade = currentStrategy.getNextMove(this.map, getX(), getY(), isFollowingWall, previousState);
		int decisionMade = findDirectionToMove(this.map, isFollowingWall);
		
		
		// Gets what the car can see
		HashMap<Coordinate, MapTile> currentView = car.getView();
		
		
		// first, get the car up to speed
		if(car.getSpeed() < CAR_SPEED){
			car.applyForwardAcceleration();
		}
		switch (decisionMade) {
		
			// need to turn north
			case 1:
				if(!car.getOrientation().equals(WorldSpatial.Direction.NORTH)){
					lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
					applyLeftTurn(car.getOrientation(),delta);
				}
				break;
				
			// need to turn east
			case 2:
				if(checkNorth(currentView)){
					// Turn right until we go back to east!
					if(!car.getOrientation().equals(WorldSpatial.Direction.EAST)){
						lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
						applyRightTurn(car.getOrientation(),delta);
					}
				}
				break;
				
			// need to turn south
			case 3:
				if(checkNorth(currentView)){
					// Turn right until we go back to east!
					if(!car.getOrientation().equals(WorldSpatial.Direction.SOUTH)){
						lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
						applyRightTurn(car.getOrientation(),delta);
					}
				}
				break;
				
			// need to turn west
			case 4:
				if(checkNorth(currentView)){
					// Turn right until we go back to east!
					if(!car.getOrientation().equals(WorldSpatial.Direction.WEST)){
						lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
						applyRightTurn(car.getOrientation(),delta);
					}
				}
				break;
		}
		return true;			
	}
	
	/**
	 * Readjust the car to the orientation we are in.
	 * @param lastTurnDirection
	 * @param delta
	 */
	private void readjust(WorldSpatial.RelativeDirection lastTurnDirection, float delta) {
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
			return checkEast(currentView);
		case NORTH:
			return checkNorth(currentView);
		case SOUTH:
			return checkSouth(currentView);
		case WEST:
			return checkWest(currentView);
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
			return checkNorth(currentView);
		case NORTH:
			return checkWest(currentView);
		case SOUTH:
			return checkEast(currentView);
		case WEST:
			return checkSouth(currentView);
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
	public boolean checkEast(HashMap<Coordinate, MapTile> currentView){
		// Check tiles to my right
		Coordinate currentPosition = new Coordinate(car.getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x+i, currentPosition.y));
			if(tile.isType(MapTile.Type.WALL)){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkWest(HashMap<Coordinate,MapTile> currentView){
		// Check tiles to my left
		Coordinate currentPosition = new Coordinate(car.getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x-i, currentPosition.y));
			if(tile.isType(MapTile.Type.WALL)){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkNorth(HashMap<Coordinate,MapTile> currentView){
		// Check tiles to towards the top
		Coordinate currentPosition = new Coordinate(car.getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y+i));
			if(tile.isType(MapTile.Type.WALL)){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkSouth(HashMap<Coordinate,MapTile> currentView){
		// Check tiles towards the bottom
		Coordinate currentPosition = new Coordinate(car.getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y-i));
			if(tile.isType(MapTile.Type.WALL)){
				return true;
			}
		}
		return false;
	}
}