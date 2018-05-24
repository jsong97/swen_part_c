package mycontroller;

import utilities.Coordinate;
public class Vertex {
    final private String id;
    final private String name;
    final private Coordinate location;


    public Vertex(String id, String name, Coordinate location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    
    public Coordinate getLocation() {
    	return location;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
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
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return name;
    }

}