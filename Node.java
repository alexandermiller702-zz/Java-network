import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class Node {

	public final int nodeID;
	public final Location loc;
	public final Collection<Segment> segments;
	
	public boolean visited;
	public Segment pathFrom;
	public int depth;
	public double cost;

	public Node(int ID, double lat, double lon) {
		this.nodeID = ID;
		this.loc= Location.newFromLatLon(lat,lon);
		this.segments = new HashSet<Segment>();
	}
	

	public void addSegment (Segment s){
		segments.add(s);
	}

	public void draw(Graphics g, Dimension area, Location origin, double scale){
		Point p = loc.asPoint(origin, scale);

		// for efficiency, don't render nodes that are off-screen.
		if (p.x < 0 || p.x > area.width || p.y < 0 || p.y > area.height)
			return;
		
		int size = (int) (roadMap.NODE_GRADIENT*Math.log(scale) + roadMap.NODE_INTERCEPT);
		g.fillRect(p.x - size / 2, p.y - size / 2, size, size);
	}
	
	public String toString() {
		Set<String> edges = new HashSet<String>();
		for (Segment s : segments) {
			if (!edges.contains(s.road.name))
				edges.add(s.road.name);
		}

		String str = "ID: " + nodeID + "  loc: " + loc + "\nroads: ";
		for (String e : edges) {
			str += e + ", ";
		}
		return str.substring(0, str.length() - 2);
	}
}
