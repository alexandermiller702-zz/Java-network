import java.util.Queue;


public class Element {

	public Queue<Node> children;
	public int reach;
	public Node me;
	public int depth;
	public Element parent;
	
	public Element(Node n, int d, Element e) {
		this.me = n; 
		this.depth = d; 
		this.parent = e;
		
	}

}
