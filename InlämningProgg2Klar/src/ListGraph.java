/** Authors:
 @evbi5086 (Evelina Bisell),
 @tyhu6316 (Tyr Hullmann),
 @kiro5530 (Kim Rosenqvist)
 */

import java.util.*;

public class ListGraph <T> implements Graph<T>{

    private final Map<T, Set<Edge<T>>> nodes = new HashMap<>();

    @Override
    public void add(T node) {
        nodes.putIfAbsent(node, new HashSet<>());
    }

    @Override
    public void connect(T a, T b, String name, int weight) {

        if (!nodes.containsKey(a) || !nodes.containsKey(b)){
            throw new NoSuchElementException();
        } else if(weight < 0 ){
            throw new IllegalArgumentException();
        } else if (getEdgeBetween(a, b) != null){
            throw new IllegalStateException();
        }

        Set<Edge<T>> edgesA = nodes.get(a);
        Set<Edge<T>> edgesB = nodes.get(b);

        edgesA.add(new Edge<>(b, name, weight));
        edgesB.add(new Edge<>(a, name, weight));
    }

    @Override
    public void disconnect(T node1, T node2){

        checkNodesAndConnections(node1, node2);

        nodes.get(node1).removeIf(current -> current.getDestination().equals(node2));
        nodes.get(node2).removeIf(current -> current.getDestination().equals(node1));
    }

    @Override
    public Edge<T> getEdgeBetween(T next, T current) {

        if (!nodes.containsKey(next) || !nodes.containsKey(current)) {
            throw new NoSuchElementException();
        } else {
            for (Edge<T> edge : nodes.get(next)) {
                if (edge.getDestination().equals(current)) {
                    return edge;
                }
            }
        }
        return null;
    }

    @Override
    public Collection<Edge<T>> getEdgesFrom(T node){

        if (!nodes.containsKey(node)){
            throw new NoSuchElementException();
        }
        return new HashSet<Edge<T>>(nodes.get(node));
    }

    @Override
    public Set<T> getNodes(){

        return new HashSet<T>(nodes.keySet());
    }

    @Override
    public List<Edge<T>> getPath(T from, T to) {
        Map<T, T> connections = new HashMap<>();
        connections.put(from, null);

        LinkedList<T> queue = new LinkedList<>();
        queue.add(from);

        while (!queue.isEmpty()) {
            T city = queue.pollFirst();
            for (Edge<T> edge : nodes.get(city)) {
                T destination = edge.getDestination();
                if (!connections.containsKey(destination)) {
                    connections.put(destination, city);
                    queue.add(destination);
                }
            }
        }
        return gatherPath(from, to, connections);
    }

    private List<Edge<T>> gatherPath(T from, T to, Map<T, T> connection) {
        LinkedList<Edge<T>> path = new LinkedList<>();
        T current = to;

        while (!current.equals(from)) {
            T next = connection.get(current);
            if (next == null) return null;
            Edge<T> edge = getEdgeBetween(next, current);
            path.addFirst(edge);
            current = next;
        }
        return Collections.unmodifiableList(path);
    }

    @Override
    public boolean pathExists(T a, T b) {

        Set<T> visited = new HashSet<>();
        depthFirstVisitAll(a, visited);
        return visited.contains(b);
    }

    private void depthFirstVisitAll(T current, Set<T> visited) {
        visited.add(current);
        try {
            for (Edge<T> edge : nodes.get(current)) {
                if (!visited.contains(edge.getDestination())) {
                    depthFirstVisitAll(edge.getDestination(), visited);
                }
            }
        } catch (Exception e) {
            //Do nothing
        }
    }

    @Override
    public void remove(T node) {

        if (!nodes.containsKey(node)){
            throw new NoSuchElementException();
        }

        for (T n : nodes.keySet()){
            nodes.get(n).removeIf(current -> current.getDestination().equals(node));
        }
        nodes.remove(node);
    }

    @Override
    public void setConnectionWeight(T node1, T node2, int weight){
        if(weight < 0 ) {
            throw new IllegalArgumentException();
        }

        checkNodesAndConnections(node1, node2);

        getEdgeBetween(node1, node2).setWeight(weight);
        getEdgeBetween(node2, node1).setWeight(weight);
    }

    private void checkNodesAndConnections(T node1, T node2){
        if (!nodes.containsKey(node1) || !nodes.containsKey(node2)){
            throw new NoSuchElementException();
        } else if(getEdgeBetween(node1, node2) == null){
            throw new IllegalStateException();
        }
    }

    @Override
    public String toString(){
        String retur = "";
        for (T n : nodes.keySet()){
            retur += n.toString();
            for (Edge<T> edge : nodes.get(n)) {
                retur += " " + edge.toString();
            }
            retur += "\n";
        }
        return retur;
    }
}