import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


public class Trie {
	private TrieNode root = new TrieNode();

	public Trie (Collection<Road> roads) {
		for (Road r : roads)
			add(r);
	}

	public void add(Road road){
		if (road.name.equals("-"))
			return;

		TrieNode node = this.root;
		char[] name = road.name.toCharArray();
		for (int i = 0 ; i < name.length ; i++) {
			if (node.children.containsKey(name[i])) 
				node = node.children.get(name[i]);
			else{
				TrieNode next = new TrieNode();
				node.children.put(name[i],next);
				node = next;
			}
		}
	}


	public Collection<Road> get(String street){
		TrieNode node = root;
		for (char c: street.toCharArray()){
			node= node.children.get(c);

			if (node == null)
				return new HashSet<>();
		}

		Collection<Road> names = new HashSet<>();
		traverse(node, names);
		return names;
	}

	private static void traverse (TrieNode root, Collection<Road> elements){
		elements.addAll(root.data);

		for (Character c : root.children.keySet()){
			traverse(root.children.get(c), elements);
		}
	}

	private class TrieNode{
		Map<Character, TrieNode> children = new HashMap<>();
		Collection<Road> data = new HashSet<>();
	}
}
