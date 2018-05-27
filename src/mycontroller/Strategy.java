package mycontroller;

import java.util.HashMap;

import tiles.MapTile;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;
import world.WorldSpatial.Direction;
import world.WorldSpatial.RelativeDirection;

public abstract class Strategy {
	protected Car car;
	
	public Strategy(Car car) {
		this.car = car;
	}

	abstract boolean getNextMove(HashMap<Coordinate, MapTile> map, float delta);
	
}