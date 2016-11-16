import java.awt.Point;

/**
 * A Location is a point in a 2D coordinate system, with increasing x from west
 * to east and increasing y from south to north (ordinary mathematical
 * coordinates). Locations are represented with two doubles, (with an
 * unspecified length unit - could be kilometers, for example), and have a fixed
 * origin in the middle of Auckland.
 * 
 * Points, on the other hand, represent pixel positions on the screen. A Point
 * is described by two integers: x pixels across and y pixels down. Note the y
 * coordinate has its direction flipped from Location objects.
 * 
 * Methods are provided to convert between these two coordinate systems, but
 * this conversion requires an origin Location (a Location at the origin will be
 * converted to the point (0,0), which is probably the top-left of the screen),
 * and a scale specifying how many pixels per length unit. Typically the scale
 * will be ( windowSize /(maxLocation - minLocation) ).
 * 
 * Finally, a method is provided to convert out of the latitude-longitude
 * coordinate system used in the input files and into the Location coordinate
 * system.
 */

public class Location {

	// the center of Auckland City according to Google Maps
	private static final double CENTRE_LAT = -36.847622;
	private static final double CENTRE_LON = 174.763444;

	// how many kilometers per degree.
	private static final double SCALE_LAT = 111.0;
	private static final double DEG_TO_RAD = Math.PI / 180;

	// fields are public for easy access, but they are final so that the
	// location is immutable.
	public final double x;
	public final double y;

	public Location(double x, double y) {
		this.x = x;
		this.y = y;
	}

	// -------------------------------------------
	// conversion methods. you want to use these.
	// -------------------------------------------

	/**
	 * Makes a new Point object from this Location object and returns it. To
	 * create this Point, an origin location and the scale of the window are
	 * required. Note the vertical direction is inverted
	 */
	public Point asPoint(Location origin, double scale) {
		int u = (int) ((x - origin.x) * scale);
		int v = (int) ((origin.y - y) * scale);
		return new Point(u, v);
	}

	/**
	 * Create a new Location object from a given Point object, as well as the
	 * origin and scale. This is effectively the opposite of the asPoint method.
	 */
	public static Location newFromPoint(Point point, Location origin, double scale) {
		return new Location(point.x / scale + origin.x, origin.y - point.y
				/ scale);
	}

	/**
	 * Create a new Location object from the given latitude and longitude, which
	 * is the format used in the data files.
	 */
	public static Location newFromLatLon(double lat, double lon) {
		double y = (lat - CENTRE_LAT) * SCALE_LAT;
		double x = (lon - CENTRE_LON)
				* (SCALE_LAT * Math.cos((lat - CENTRE_LAT) * DEG_TO_RAD));
		return new Location(x, y);
	}

	// ------------------------------------------
	// some utility methods for Location objects
	// ------------------------------------------

	/**
	 * Returns a new Location object that is this Location object moved by the
	 * given dx and dy, ie. this returns a Location representing (x + dx, y +
	 * dy).
	 */
	public Location moveBy(double dx, double dy) {
		return new Location(x + dx, y + dy);
	}

	/**
	 * Return distance between this location and another
	 */
	public double distance(Location other) {
		return Math.hypot(this.x - other.x, this.y - other.y);
	}

	/**
	 * Return true if this location is within dist of other Uses manhattan
	 * distance for greater speed. Equivalent to whether other is within a
	 * diamond shape around this location.
	 */
	public boolean isClose(Location other, double dist) {
		return Math.abs(this.x - other.x) + Math.abs(this.y - other.y) <= dist;
	}

	public String toString() {
		return String.format("(%.3f, %.3f)", x, y);
	}
}

// code for COMP261 assignments
