import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Stack;

import javax.swing.JTextArea;

/**
 * This represents the data structure storing all the roads, nodes, and
 * segments, as well as some information on which nodes and segments should be
 * highlighted.
 * 
 * @author tony
 */
public class Graph {
	// map node IDs to Nodes.
	Map<Integer, Node> nodes = new HashMap<>();
	// map road IDs to Roads.
	Map<Integer, Road> roads;
	// just some collection of Segments.
	Collection<Segment> segments;

	Collection<Node> articulate;
	Collection<Segment> path;
	PriorityQueue<Tuple> fringe;

	//for path printer
	DecimalFormat d = new DecimalFormat("#.##");

	Node highlightedNode;
	Node secondNode;

	Collection<Road> highlightedRoads = new HashSet<>();

	public Graph(File nodes, File roads, File segments, File polygons) {
		this.nodes = Parser.parseNodes(nodes, this);
		this.roads = Parser.parseRoads(roads, this);
		this.segments = Parser.parseSegments(segments, this);
		articulate();
	}

	public void articulate(){
		//new empty set
		articulate = new HashSet<>();
		for (Node n : nodes.values())
			n.depth = Integer.MAX_VALUE; //set to maximum val

		//entry set iterator
		//Iterator<Entry<Integer, Node>> it = nodes.values();
		//.entrySet().iterator();

		for (Node start : nodes.values()){
			//Node start = it.next().getValue();
			if (start.depth == Integer.MAX_VALUE){

				start.depth = 0;
				//number of subtrees
				int subTrees = 0;

				for (Segment neighbour : start.segments){
					Node neigh = neighbour.end.nodeID != start.nodeID ? neighbour.end : neighbour.start;
					if (neigh.depth == Integer.MAX_VALUE){
						//iterArtPts(neigh, start);
						recArtPts(neigh, 1, start);
						subTrees++;
					}
				}
				if (subTrees>1)
					articulate.add(start);
			}
		}
	}

	public void iterArtPts(Node first, Node sec){
		//returns 20,000 articulation points.
		Stack<Element> fringe = new Stack<>();
		fringe.push(new Element(first, 1, new Element(sec, 0 ,null)));
		while (!fringe.isEmpty()){
			Element e = fringe.peek();
			Node no = e.me;
			if (e.children == null){
				no.depth = e.depth;
				e.reach = e.depth;
				e.children = new LinkedList<Node>();
				for (Segment neighbour : no.segments){
					Node neigh = neighbour.end.nodeID != no.nodeID ? neighbour.end : neighbour.start;
					if (neigh.nodeID != e.parent.me.nodeID){
						e.children.add(neigh);
					}
				}
			}
			else if (!e.children.isEmpty()){
				while(!e.children.isEmpty()){
					Node c = e.children.poll();
					if (c.depth < Integer.MAX_VALUE)
						e.reach = Math.min(e.reach, c.depth);
					else
						fringe.push(new Element(c, no.depth+1, new Element(no, 0 ,null)));
				}
			}
			else{
				if (!no.equals(first)){
					if (e.reach >= e.parent.depth)
						articulate.add(e.parent.me);
					e.parent.reach = Math.min(e.parent.reach, e.reach);
				}
				fringe.pop();
			}
		}
	}

	public int recArtPts(Node node, int depth, Node from){
		node.depth=depth;
		int reachback = depth;
		for (Segment neighbour : node.segments){
			//make a temp variable for the appropriate end of the segment 
			Node neigh = neighbour.end.nodeID != node.nodeID ? neighbour.end : neighbour.start;
			if (from.nodeID != neigh.nodeID){
				if (neigh.depth < Integer.MAX_VALUE)
					reachback = Math.min(neigh.depth, reachback);
				else{
					int childReach = recArtPts(neigh, depth++, node);
					reachback = Math.min(childReach, reachback);
					if (childReach>=depth)
						articulate.add(node);
				}
			}
		}
		return reachback;
	}

	public void astar(JTextArea jArea){
		Node start = highlightedNode;
		Node goal = secondNode;
		fringe = new PriorityQueue<Tuple>();
		for (Node n : nodes.values()){
			n.visited = false; 
			n.pathFrom = null;
		}
		fringe.add(new Tuple(start,null,0,start.loc.distance(goal.loc)));
		while (!fringe.isEmpty()){
			Tuple t = fringe.poll();
			Node n = t.node;
			Segment p = t.path;
			double toHere = t.costToHere;

			if (!n.visited){
				n.visited = true;
				n.pathFrom = p;
				n.cost = toHere;
				if (n.nodeID == goal.nodeID){
					pathString(secondNode, highlightedNode, jArea);
					return;
				}
				for (Segment s : n.segments){
					//check segment for oneway
					//if oneway neigh can only be end.
					if (!s.road.oneway || s.road.oneway && s.start.nodeID == n.nodeID){
						Node neigh = s.end.nodeID != n.nodeID ? s.end : s.start;
						if (!neigh.visited){
							double costToNeigh = toHere + s.length;
							double estTotal = costToNeigh + neigh.loc.distance(goal.loc);
							fringe.add(new Tuple(neigh, s, costToNeigh, estTotal));
						}
					}
				}
			}
		}
		jArea.append(start.toString()+"\n"+goal.toString()+"\n"+"irreconcilable path");
	}



	public void pathString(Node e, Node start, JTextArea jArea){
		path = new ArrayList<>();
		Stack<String> details = new Stack<>();

		String lastRoad = "";
		double lastLength = 0;
		double totallength= 0;

		//taking end node e initially
		recString(e, start, details, lastRoad, lastLength, totallength);

		jArea.setText("");
		for (int i = 0; i < details.size() ; i++){
			jArea.append(details.pop());
		}

	}

	public void recString(Node e, Node start, Stack<String> details, String lastRoad, double lastLength, double totallength){
		//e is current position in the linkedlist 
		if (e.equals(start))
			return;

		path.add(e.pathFrom);
		Node neigh = e.pathFrom.end.nodeID != e.nodeID ? e.pathFrom.end : e.pathFrom.start;
		totallength += e.pathFrom.length;
		//if this road is part of the last road
		if (e.pathFrom.road.name.equals(lastRoad)){
			lastLength = lastLength+ e.pathFrom.length;
			//modify the existing road record to reflect this
			details.pop();
			details.push(lastRoad+ ": "+d.format(lastLength)+"\n");
		}
		else{
			//add the details to the string list
			details.push(e.pathFrom.road.name+ ": "+d.format(e.pathFrom.length)+"\n");
			lastRoad = e.pathFrom.road.name;
			lastLength = e.pathFrom.length;
		}
		//pass the call along the path
		recString(neigh, start, details, lastRoad, lastLength, totallength);
	}


	public void draw(Graphics g, Dimension screen, Location origin, double scale) {
		// a compatibility wart on swing is that it has to give out Graphics
		// objects, but Graphics2D objects are nicer to work with. Luckily
		// they're a subclass, and swing always gives them out anyway, so we can
		// just do this.
		Graphics2D g2 = (Graphics2D) g;

		// draw all the segments.
		g2.setColor(roadMap.SEGMENT_COLOUR);
		for (Segment s : segments)
			s.draw(g2, origin, scale);

		// draw the segments of all highlighted roads.
		g2.setColor(roadMap.HIGHLIGHT_COLOUR);
		g2.setStroke(new BasicStroke(3));
		for (Road road : highlightedRoads) {
			for (Segment seg : road.components) {
				seg.draw(g2, origin, scale);
			}
		}

		// draw all the nodes.
		g2.setColor(roadMap.NODE_COLOUR);
		for (Node n : nodes.values())
			n.draw(g2, screen, origin, scale);

		// draw the articulation points
		if (!articulate.isEmpty()) {
			g2.setColor(roadMap.ART_COLOUR);
			for (Node n : articulate)
				n.draw(g2, screen, origin, scale);
		}

		//draw a path
		if (highlightedNode != null && secondNode != null){
			g2.setColor(roadMap.PATH_COLOUR);
			for (Segment s : path)
				s.draw(g2, origin, scale);
		}

		// draw the highlighted node, if it exists.
		if (highlightedNode != null) {
			g2.setColor(roadMap.HIGHLIGHT_COLOUR);
			highlightedNode.draw(g2, screen, origin, scale);
		}

		//draw second node and draw path.
		if (secondNode != null) {
			g2.setColor(roadMap.SECOND_COLOUR);
			secondNode.draw(g2, screen, origin, scale);
			g2.setColor(roadMap.PATH_COLOUR);
			//draw path
		}

	}

	public void setHighlight(Node node) {
		this.highlightedNode = node;
	}

	public void setSecondNode(Node n){
		this.secondNode = n;
	}

	public void setHighlight(Collection<Road> roads) {
		this.highlightedRoads = roads;
	}
}

// code for COMP261 assignments