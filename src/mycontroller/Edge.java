package mycontroller;

import tiles.MapTile;

public class Edge  {
    private final MapTile.Type tileType;
    private final Vertex source;
    private final Vertex destination;
    private final int weight;
    

    public Edge(MapTile.Type tileType, Vertex source, Vertex destination, int weight) {
        this.tileType = tileType;
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    public MapTile.Type getTileType() {
        return tileType;
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