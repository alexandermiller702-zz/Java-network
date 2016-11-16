
public class Tuple implements Comparable<Tuple> {

	public final Node node;
	public final Segment path;
	public final double costToHere;
	public final double estTotal;

	public Tuple(Node node, Segment path, double cost, double est) {
		this.node = node;
		this.path = path;
		this.costToHere = cost;
		this.estTotal = est;
	}

	@Override
	public int compareTo(Tuple t) {
		return (int) (this.estTotal - t.estTotal);
	}

}
