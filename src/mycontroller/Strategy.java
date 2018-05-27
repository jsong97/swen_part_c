package mycontroller;

import java.util.HashMap;

import tiles.MapTile;
import utilities.Coordinate;
import world.Car;

public abstract class Strategy {
	protected Car car;
	
	public Strategy(Car car) {
		this.car = car;
	}

	abstract HashMap<Coordinate, Integer> getNextMove(HashMap<Coordinate, MapTile> map, float delta);
	
}