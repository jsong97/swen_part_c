package mycontroller;

import tiles.MapTile;

public class Edge  {
    private MapTile.Type tileType;
	private final Vertex source;
    private final Vertex destination;
    private final int weight;
    

    public Edge(MapTile.Type tileType, Vertex source, Vertex destination) {
        this.tileType = tileType;
    	this.source = source;
        this.destination = destination;
        switch (this.tileType) {
		case WALL:
			this.weight = 100000;
			break;
		case ROAD:
			this.weight = 1;
			break;
		case START:
			this.weight = 1;
			break;
		case FINISH:
			this.weight = 1;
			break;
		default:
			this.weight = 1;
			break;
        }
    }

    public Vertex getDestination() {
        return destination;
    }

    public Vertex getSource() {
        return source;
    }
    public int getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return source + " " + destination;
    }
}