package camp.xit.jacod.impl;

import java.util.*;
import static java.util.Collections.emptySet;

class DirectedGraph<T> implements Iterable<T> {

    // key is a Node, value is a set of Nodes connected by outgoing edges from the key
    private final Map<T, Set<T>> graph = new HashMap<>();


    public boolean addNode(T node) {
        if (graph.containsKey(node)) {
            return false;
        }

        graph.put(node, new HashSet<>());
        return true;
    }


    public void addNodes(Collection<T> nodes) {
        nodes.forEach(node -> addNode(node));
    }


    public void addEdge(T src, T dest) {
        if (!graph.containsKey(src)) addNode(src);
        if (!graph.containsKey(dest)) addNode(dest);

        validateSourceAndDestinationNodes(src, dest);

        // Add the edge by adding the dest node into the outgoing edges
        graph.get(src).add(dest);
    }


    public void removeEdge(T src, T dest) {
        validateSourceAndDestinationNodes(src, dest);

        graph.get(src).remove(dest);
    }


    public boolean edgeExists(T src, T dest) {
        validateSourceAndDestinationNodes(src, dest);

        return graph.get(src).contains(dest);
    }


    public Set<T> edgesFrom(T node) {
        // Check that the node exists.
        Set<T> edges = graph.get(node);
        if (edges == null)
            throw new NoSuchElementException("Source node does not exist.");

        return Collections.unmodifiableSet(edges);
    }


    @Override
    public Iterator<T> iterator() {
        return graph.keySet().iterator();
    }


    public int size() {
        return graph.size();
    }


    public boolean isEmpty() {
        return graph.isEmpty();
    }


    private void validateSourceAndDestinationNodes(T src, T dest) {
        // Confirm both endpoints exist
        if (!graph.containsKey(src) || !graph.containsKey(dest))
            throw new NoSuchElementException("Both nodes must be in the graph.");
    }


    public List<T> sort() {
        return TopologicalSort.sort(this, emptySet());
    }


    public List<T> sort(Set<T> excluded) {
        return TopologicalSort.sort(this, excluded);
    }

}
