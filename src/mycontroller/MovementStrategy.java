package mycontroller;

import java.util.HashMap;

import tiles.MapTile;
import utilities.Coordinate;
import world.Car;

public abstract class MovementStrategy {
	protected Car car;
	
	public MovementStrategy(Car car) {
		this.car = car;
	}

	abstract HashMap<Coordinate, Integer> makeNextMove(HashMap<Coordinate, MapTile> map, float delta);
	
}