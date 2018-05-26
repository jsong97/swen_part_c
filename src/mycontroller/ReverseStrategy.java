package mycontroller;

import java.util.HashMap;

import tiles.MapTile;
import utilities.Coordinate;
import world.Car;

public class ReverseStrategy extends Strategy {

	private static ReverseStrategy instance = null;
	float delta = 0;
	
	public ReverseStrategy(Car car) {
		super(car);
	}
	
	public static ReverseStrategy getInstance(Car car) {
		if (instance == null) {
			instance = new ReverseStrategy(car);
		}
		return instance;
	}
	
	public boolean getNextMove(HashMap<Coordinate, MapTile> map, float delta) {
		this.delta += delta;
		if (this.delta < 0.4) {
			car.applyReverseAcceleration();
			car.turnLeft(delta);
		}
		else if (this.delta < 1.5){
			car.applyForwardAcceleration();
		}
		else {
			this.delta = 0;
			return true;
		}
//		car.applyForwardAcceleration();
		return false;
	}
}
