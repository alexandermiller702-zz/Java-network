import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;


public class roadMap extends GUI {
	public static final Color NODE_COLOUR = Color.BLUE;
	public static final Color SEGMENT_COLOUR = Color.black;
	public static final Color HIGHLIGHT_COLOUR = Color.RED;

	public static final Color ART_COLOUR = Color.GREEN;
	public static final Color PATH_COLOUR = Color.YELLOW;
	public static final Color SECOND_COLOUR = Color.MAGENTA;

	// these two constants define the size of the node squares at different zoom
	// levels; the equation used is node size = NODE_INTERCEPT + NODE_GRADIENT *
	// log(scale)
	public static final int NODE_INTERCEPT = 1;
	public static final double NODE_GRADIENT = 0.8;

	// defines how much you move per button press, and is dependent on scale.
	public static final double MOVE_AMOUNT = 100;
	// defines how much you zoom in/out per button press, and the maximum and
	// minimum zoom levels.
	public static final double ZOOM_FACTOR = 1.3;
	public static final double MIN_ZOOM = 1, MAX_ZOOM = 200;

	// how far away from a node you can click before it isn't counted.
	public static final double MAX_CLICKED_DISTANCE = 0.15;

	// these two define the 'view' of the program, ie. where you're looking and
	// how zoomed in you are.
	private Location origin;
	private double scale;

	// our data structures.
	private Graph graph;
	private Trie trie;

	//also need a trie for suburbs, road labels.

	@Override
	protected void redraw(Graphics g) {
		if (graph != null)
			graph.draw(g, getDrawingAreaDimension(), origin, scale);
	}

	@Override
	protected void onClick(MouseEvent e) {
		Location clicked = Location.newFromPoint(e.getPoint(), origin, scale);
		// find the closest node.
		double bestDist = Double.MAX_VALUE;
		Node closest = null;
		for (Node node : graph.nodes.values()) {
			double distance = clicked.distance(node.loc);
			if (distance < bestDist) {
				bestDist = distance;
				closest = node;
			}
		}

		if(e.getButton() == 1){
			// if it's close enough, highlight it and show some information.
			if (clicked.distance(closest.loc) < MAX_CLICKED_DISTANCE) {
				graph.setHighlight(closest);
			}

			if (graph.secondNode != null)
				graph.astar(getTextOutputArea());
			else 
				getTextOutputArea().setText(closest.toString());
		}
		else{
			if (clicked.distance(closest.loc) < MAX_CLICKED_DISTANCE){
				graph.setSecondNode(closest);
			}
			
			if (graph.highlightedNode != null)
				graph.astar(getTextOutputArea());
			else
				getTextOutputArea().setText(closest.toString());
		}
		
	}



	@Override
	protected void onSearch() {
		if (trie == null)
			return;

		// get the search query and run it through the trie.
		String query = getSearchBox().getText();
		Collection<Road> selected = trie.get(query);

		// figure out if any of our selected roads exactly matches the search
		// query. if so, as per the specification, we should only highlight
		// exact matches. there may be (and are) many exact matches, however, so
		// we have to do this carefully.
		boolean exactMatch = false;
		for (Road road : selected)
			if (road.name.equals(query))
				exactMatch = true;

		// make a set of all the roads that match exactly, and make this our new
		// selected set.
		if (exactMatch) {
			Collection<Road> exactMatches = new HashSet<>();
			for (Road road : selected)
				if (road.name.equals(query))
					exactMatches.add(road);
			selected = exactMatches;
		}

		// set the highlighted roads.
		graph.setHighlight(selected);

		// now build the string for display. we filter out duplicates by putting
		// it through a set first, and then combine it.
		Collection<String> names = new HashSet<>();
		for (Road road : selected)
			names.add(road.name);
		String str = "";
		for (String name : names)
			str += name + "; ";

		if (str.length() != 0)
			str = str.substring(0, str.length() - 2);
		getTextOutputArea().setText(str);
	}

	@Override
	protected void onMove(Move m) {
		switch(m){
		case NORTH:
			origin = origin.moveBy(0, MOVE_AMOUNT / scale);
			break;

		case SOUTH:
			origin = origin.moveBy(0, -MOVE_AMOUNT / scale);
			break;

		case EAST:
			origin = origin.moveBy(MOVE_AMOUNT / scale, 0);
			break;

		case WEST:
			origin = origin.moveBy(-MOVE_AMOUNT / scale, 0);
			break;

		case ZOOM_IN:
			if (scale < MAX_ZOOM) {
				scaleOrigin(true);
				scale *= ZOOM_FACTOR;
			}
			break;

		case  ZOOM_OUT:
			if (scale > MIN_ZOOM) {
				scaleOrigin(false);
				scale /= ZOOM_FACTOR;
			}
			break;
		}
		return;
	}

	private void scaleOrigin(boolean zoomIn) {
		Dimension area = getDrawingAreaDimension();
		double zoom = zoomIn ? 1 / ZOOM_FACTOR : ZOOM_FACTOR;

		int dx = (int) ((area.width - (area.width * zoom)) / 2);
		int dy = (int) ((area.height - (area.height * zoom)) / 2);

		origin = Location.newFromPoint(new Point(dx, dy), origin, scale);
	}

	public void printer(ArrayList<String> s){
		getTextOutputArea().setText(s.get(s.size()-1));
		for (int i = s.size()-2; i >= 0; i--){
			getTextOutputArea().append(s.get(i));
		}

	}

	@Override
	protected void onLoad(File node, File road, File segment, File polygon) {
		graph = new Graph(node, road, segment, polygon);
		trie = new Trie(graph.roads.values());
		origin = new Location(-250, 250); // close enough
		scale = 1;
		getTextOutputArea().setText(graph.articulate.size()+" Articulation points (in green)");
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new roadMap();
	}

}
