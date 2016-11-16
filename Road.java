import java.util.Collection;
import java.util.HashSet;


public class Road {
	public final int roadID;
	public final String name, city;
	public final Collection<Segment> components;
	//roads have no links to any intersections or segments.
	
	public final int type, speed, clss;
	public final Boolean oneway, nCars, nPeds, nBike;
	
	//roadID, type, label, city, oneway, speed, class, not for car, no peds, no bikes.
	public Road(int ID, int type, String label, String city, int oneway, int speed, int clss, int nCars, int nPeds, int nBike) {
		this.roadID=ID;
		this.type=type;
		this.name=label;
		this.city=city;
		this.oneway=oneway==1?true:false;
		this.speed=speed;
		this.clss=clss;
		this.nCars=nCars==1?true:false;
		this.nPeds=nPeds==1?true:false;
		this.nBike=nBike==1?true:false;
		this.components = new HashSet<Segment>();
	}
	
	public void addSegment(Segment seg) {
		components.add(seg);
	}


}
