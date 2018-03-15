import java.util.*;

/**
 * This class provides a shortestPath method for finding routes between two points
 * on the map. Start by using Dijkstra's, and if your code isn't fast enough for your
 * satisfaction (or the autograder), upgrade your implementation by switching it to A*.
 * Your code will probably not be fast enough to pass the autograder unless you use A*.
 * The difference between A* and Dijkstra's is only a couple of lines of code, and boils
 * down to the priority you use to order your vertices.
 */
public class Router {
    /**
     * Return a LinkedList of <code>Long</code>s representing the shortest path from st to dest, 
     * where the longs are node IDs.
     */
    public static LinkedList<Long> shortestPath(GraphDB g, double stlon, double stlat, double destlon, double destlat) {
        //return new LinkedList<Long>();
        long s = g.closest(stlon, stlat);
        long t = g.closest(destlon, destlat);
        return new Router(g).AStar(s, t);
    }

    /**
     * Start of A* implementation.
     */

    private HashMap<Long, Double> dist;
    private HashMap<Long, Long> from;
    private PriorityQueue<Long> queue;
    private HashSet<Long> visited;
    private GraphDB g;

    public Router(GraphDB g) {
        this.g = g;
    }

    protected LinkedList<Long> AStar(long s, long t) {
        dist = new HashMap<>();
        dist.put(s, 0.0);
        from = new HashMap<>();
        from.put(s, (long) -1);
        visited = new HashSet<>();
        // queue = new Queue<>();  // Brute-force BFS
        queue = new PriorityQueue<>(Comparator.comparingDouble(o -> getPriority(o, t)));
        queue.add(s);

        while (!queue.isEmpty()) {
            long x = queue.poll();
            if (x == t)
                return reconstruct(x);
            visited.add(x);
            for (long y : g.adjacent(x)) {
                if (visited.contains(y))
                    continue;
                if (!dist.containsKey(y) || dist.get(x) + g.distance(x, y) < dist.get(y)) {
                    dist.put(y, dist.get(x) + g.distance(x, y));
                    from.put(y, x);
                    queue.remove(y);  // Ensure its position is updated
                    queue.add(y);
                }
            }
        }

        return null;
    }

    /**
     * Calculates priority of a node with respect to the target.
     * Different in Dijkstra and A*.
     */
    protected double getPriority(long id, long dest) {
        if (dist.containsKey(id))
            //return dist.get(id);  // Dijkstra
            return dist.get(id) + g.distance(id, dest);
        return Double.MAX_VALUE;
    }

    /**
     * Recursively produces a LinkedList of nodes from s to t
     */
    private LinkedList<Long> reconstruct(long id) {
        if (!from.containsKey(id))
            throw new RuntimeException("Faulty route");
        LinkedList<Long> list;
        if (from.get(id) == -1) {
            list = new LinkedList<>();
        } else {
            list = reconstruct(from.get(id));
        }
        list.add(id);
        return list;
    }
}
