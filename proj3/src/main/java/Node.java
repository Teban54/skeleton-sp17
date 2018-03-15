import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.jar.Attributes;

public class Node {
    public long id;
    double lon;
    double lat;
    private ArrayList<Long> dests;
    //private TreeSet<Long> dests;
    //private TreeSet<Double> dists;
    //private HashMap<Long, Double> dests2dists;

    public Node(long id, double lon, double lat) {
        this.id = id;
        this.lon = lon;
        this.lat = lat;
        /*dests = new TreeSet<>((o1, o2) -> {
            double d1 = dests2dists.get(o1);
            double d2 = dests2dists.get(o2);
            if (d1 == d2)
                return o1.compareTo(o2);
            else
                return Double.compare(d1, d2);
        });
        dists = new TreeSet<>();
        dests2dists = new HashMap<>();*/
        dests = new ArrayList<>();
    }

    //public void addEdge(long dest, double dist) {
    public void addEdge(long dest) {
        //dests2dists.put(dest, dist);
        dests.add(dest);
        //dists.add(dist);
    }

    //public TreeSet<Long> getDestinations() {
    public ArrayList<Long> getDestinations() {
        return dests;
    }
    /*public TreeSet<Double> getDistances() {
        return dists;
    }

    public double getDistanceToDestination(Long dest) {
        if (!dests2dists.containsKey(dest))
            throw new IllegalArgumentException();
        return dests2dists.get(dest);
    }*/

    /** For API */
    public void removeEdge(long dest){
        if (!dests.contains(dest))
            return;  // Exception not considered yet (probably won't)
        dests.remove(dest);
    }
}
