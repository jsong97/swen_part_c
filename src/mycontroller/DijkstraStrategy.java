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
    private Coordinate finalDestination;
    
    // these are needed to build a map of nodes
    private ArrayList<Vertex> nodes;
    private ArrayList<Edge> edges;
    private Set<Vertex> settledNodes;
    private Set<Vertex> unSettledNodes;
    private Map<Vertex, Vertex> predecessors;
    private Map<Vertex, Integer> distance;
    
    // this is the graph we use to perform dijkstra
    private Graph graph;
    
    // this is the path we take to the destination
    private LinkedList<Vertex> path;
    
    private Car car;
    private WorldSpatial.Direction previousState;

    public DijkstraStrategy(Car car, Coordinate currentLoc, 
    		HashMap<Coordinate,MapTile> currentView, Coordinate finalDestination,
    		WorldSpatial.Direction previousState) {
    	// get the overall map from the game
    	this.worldMap = World.getMap();
    	this.car = car;
    	this.previousState = previousState;
    	this.currentLoc = currentLoc;
    	
    	// add the destination and the current view
    	// this is all in mapDetails()
    	// add the destination
    	this.finalDestination = finalDestination;
    	
    	// get the current view from MyAIController
    	this.currentView = currentView;
    	
        // use the buildRoute() function to get the route and graph
        // use getNextMove() to return the next step   
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
	    
	    // now we need to build all possible edges that we can
	    // need a nested array...
	    for (Vertex spot : nodes) {
	    	int spot_x_loc = spot.getLocation().getX();
	    	int spot_y_loc = spot.getLocation().getY();
	    	for (Vertex inner_spot : nodes) {
	    		int inner_spot_x_loc = inner_spot.getLocation().getX();
	    		int inner_spot_y_loc = inner_spot.getLocation().getY();
		    	if (spot_x_loc == inner_spot_x_loc) {
		    		// 'inner spot' is either right below or right above 'spot'
		    		if (((spot_y_loc - inner_spot_y_loc) == 1) || 
		    				((spot_y_loc - inner_spot_y_loc) == -1)) {
		    			addLane(spot.getId(), inner_spot.getId());
		    		}
		    	} 
		    	else if (spot_y_loc == inner_spot_y_loc) {
		    		// 'inner spot' is either left or right of 'spot'
		    		if (((spot_x_loc - inner_spot_x_loc) == 1) || 
		    				((spot_x_loc - inner_spot_x_loc) == -1)) {
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

	MoveDecision getNextMove(HashMap<Coordinate, MapTile> map, boolean isFollowingWall,
			Direction previousState) {
		// we need to first take the next tile on the route
		buildRoute();
		
		// get the next stop on the path
		Vertex nextVertex = path.get(1);
		int xDirection = nextVertex.getLocation().getX() - currentLoc.getX();
		int yDirection = nextVertex.getLocation().getY() - currentLoc.getY();
		
		int directionMoving;
		MoveDecision nextMove;
		
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
		System.out.println("direction moving is: " + directionMoving);
		nextMove = new MoveDecision(isFollowingWall, previousState, directionMoving, worldMap);
		return nextMove;
	}
}