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


// the Dijkstra class needs to:
// 1. get the hashmap of the map
// 2. add the destination
// 3. get the current location
// 4. calculate the route
// 5. return the next step (just has to move to the next node)

public class DijkstraStrategy extends Strategy {
	// this is the full map
	private HashMap<Coordinate, MapTile> worldMap;
	
    // this is the current view
    private HashMap<Coordinate, MapTile> currentView;
    
    // this is the destination
    private Coordinate finalDestination;
    
    // these are needed to build a map of nodes
    private List<Vertex> nodes;
    private List<Edge> edges;
    private Set<Vertex> settledNodes;
    private Set<Vertex> unSettledNodes;
    private Map<Vertex, Vertex> predecessors;
    private Map<Vertex, Integer> distance;
    
    private Graph graph;

    public DijkstraStrategy() {
    	// get the overall map from the game
    	this.worldMap = World.getMap();
    	
    	// add the destination and the current view
    	// this is all in mapDetails()
    	
        // use the buildRoute() function to get the route and graph
        // use getNextMove() to return the next step   
    }
    
    public void mapDetails(HashMap<Coordinate,MapTile> currentView, Coordinate finalDestination) {
    	// add the destination
    	this.finalDestination = finalDestination;
    	
    	// get the current view from MyAIController
    	this.currentView = currentView;
    	buildRoute();
    }
    
    // this will build a graph using vertexes and nodes from scratch
    public void buildRoute() {
    	// create a copy of the array so that we can operate on this array
    	this.nodes = new ArrayList<Vertex>();
        this.edges = new ArrayList<Edge>();
        
	    for (int i = 0; i < 11; i++) {
	        Vertex location = new Vertex("Node_" + i, "Node_" + i);
	        nodes.add(location);
	    }
	    
	    // in the final version, this will take in the HashMap and build
	    // the node graph out of it. For now, we just create random locations
	    
	    // in the final version, the lanes are added from the current location
	    // as the first row
	    // addLane(Trap Type, Coordinate.x, Coordinate.y, Weight (based on trap))
	    addLane(MapTile.Type.START, 0, 1, 1);
	    addLane(MapTile.Type.ROAD, 0, 2, 310000);
	    addLane(MapTile.Type.ROAD, 1, 3, 3);
	    addLane(MapTile.Type.ROAD, 2, 6, 5);
	    addLane(MapTile.Type.ROAD, 2, 7, 6);
	    addLane(MapTile.Type.ROAD, 3, 7, 7);
	    addLane(MapTile.Type.ROAD, 7, 10, 250);
	    // weight for wall should be extremely large
	    addLane(MapTile.Type.WALL, 8, 9, 100000);
	    addLane(MapTile.Type.ROAD, 7, 9, 2);
	    addLane(MapTile.Type.ROAD, 4, 9, 3);
	    addLane(MapTile.Type.ROAD, 9, 10, 4);
	    addLane(MapTile.Type.ROAD, 1, 10, 10000);
	
	    // Lets check from location Loc_1 to Loc_10
	    this.graph = new Graph(nodes, edges);
	    
	    //DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);
	    //dijkstra.execute(nodes.get(0));
	    
	    // this should get the start location instead of 0
	    
	    execute(nodes.get(0));
	    // LinkedList<Vertex> path = dijkstra.getPath(nodes.get(10));
	    LinkedList<Vertex> path = getPath(nodes.get(10));
	
	    assert path != null;
	    assert path.size() > 0;
	
	    for (Vertex vertex : path) {
	        System.out.println(vertex.getId());
	    }
	}

	private void addLane(MapTile.Type tileType, int sourceLocNo, int destLocNo,
	        int weight) {
	    Edge lane = new Edge(tileType, nodes.get(sourceLocNo), nodes.get(destLocNo), weight);
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
		// TODO Auto-generated method stub
		int nextMove = 1;
		MoveDecision thisMove = new MoveDecision(isFollowingWall, previousState, nextMove, map);
		return thisMove;
	}
}