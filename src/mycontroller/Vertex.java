package mycontroller;

import utilities.Coordinate;
public class Vertex {
    final private int id;
    final private Coordinate location;

    public Vertex(int id, Coordinate location) {
        this.id = id;
        this.location = location;
    }
    
    public int getId() {
        return id;
    }
    
    public Coordinate getLocation() {
    	return location;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Vertex other = (Vertex) obj;
        if (id == 0) {
            if (other.id != 0)
                return false;
        } 
//        else if (!id.equals(other.id)){
//            return false;
//    	}
        return true;
    }
}