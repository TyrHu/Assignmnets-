// PROG2 VT2021, Inlämningsuppgift, del 1
// Grupp 002
// Tyr Hullmann tyhu6316
// Evelina Bisell evbi5086
// Kim Rosenqvist kiro5530

import java.util.Objects;

public class Edge<T> {

    private final T destination;
    private final String name;
    private int weight;

    public Edge(T node, String name, int weight) {
        this.destination = Objects.requireNonNull(node);
        this.name = Objects.requireNonNull(name);
        this.weight = weight;
    }

    public T getDestination(){
        return destination;
    }

    public int getWeight(){
        return weight;
    }

    public void setWeight(int weight){
        this.weight = weight;
    }

    public String getName(){
        return name;
    }

    @Override
    public String toString() {
        return String.format("till %s med %s tar %d", destination, name, weight);
    }
}
