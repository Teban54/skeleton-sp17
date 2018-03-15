import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.*;

/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */
public class GraphDB {
    /** Your instance variables for storing the graph. You should consider
     * creating helper classes, e.g. Node, Edge, etc. */
    //ArrayList<Node> nodes;
    HashMap<Long, Node> nodes;

    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     * @param dbPath Path to the XML file to be parsed.
     */
    public GraphDB(String dbPath) {
        nodes = new HashMap<>();
        try {
            File inputFile = new File(dbPath);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputFile, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     *  Remove nodes with no connections from the graph.
     *  While this does not guarantee that any two nodes in the remaining graph are connected,
     *  we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        ArrayList<Long> itr = new ArrayList<>(nodes.keySet());
        for (long id : itr) {
            Node x = nodes.get(id);
            if (x.getDestinations().isEmpty())
                nodes.remove(id);
                //removeNode(id);
        }
    }

    /** Returns an iterable of all vertex IDs in the graph. */
    Iterable<Long> vertices() {
        //YOUR CODE HERE, this currently returns only an empty list.
        //return new ArrayList<Long>();
        ArrayList<Long> ret = new ArrayList<>();
        for (Node x : nodes.values())
            ret.add(x.id);
        return ret;
    }

    /** Returns ids of all vertices adjacent to v. */
    Iterable<Long> adjacent(long v) {
        return nodes.get(v).getDestinations();
    }

    /** Returns the Euclidean distance between vertices v and w, where Euclidean distance
     *  is defined as sqrt( (lonV - lonV)^2 + (latV - latV)^2 ). */
    double distance(long v, long w) {
        Node x = nodes.get(v);
        Node y = nodes.get(w);
        return actualDistance(x.lon, x.lat, y.lon, y.lat);
    }

    /**
     * Helper method: calculate Euclidean distance
     */
    private double actualDistance(double lon1, double lat1, double lon2, double lat2) {
        return Math.sqrt(Math.pow(lon1 - lon2, 2) + Math.pow(lat1 - lat2, 2));
    }

    /** Returns the vertex id closest to the given longitude and latitude. */
    long closest(double lon, double lat) {
        ArrayList<Node> nodelist = new ArrayList<>(nodes.values());
        Collections.sort(nodelist, (o1, o2) -> {
            double d1 = actualDistance(o1.lon, o1.lat, lon, lat);
            double d2 = actualDistance(o2.lon, o2.lat, lon, lat);
            return (d1 == d2)? Long.compare(o1.id, o2.id): Double.compare(d1, d2);
        });
        return nodelist.get(0).id;
    }

    /** Longitude of vertex v. */
    double lon(long v) {
        return nodes.get(v).lon;
    }

    /** Latitude of vertex v. */
    double lat(long v) {
        return nodes.get(v).lat;
    }

    /**
     * Helper method: Add a node
     */
    void addNode(long id, double lon, double lat) {
        nodes.put(id, new Node(id, lon, lat));
    }

    /**
     * Helper method: Add an edge (distance not supported)
     */
    void addEdge(long s, long t) {
        if (!nodes.containsKey(s) || !nodes.containsKey(t))
            throw new IllegalArgumentException();
        nodes.get(s).addEdge(t);
        nodes.get(t).addEdge(s);
    }

    /**
     * For API: remove a node
     */
    void removeNode(long id) {
        if (!nodes.containsKey(id))
            throw new IllegalArgumentException();
        for (Node x : nodes.values())
            x.removeEdge(id);  // Exceptions will not be thrown if there's not an edge (due to Node.removeEdge method)
        nodes.remove(id);
    }

    /**
     * For API: remove an edge
     * (Exceptions will not be thrown if the edge does not exist)
     */
    void removeEdge(long s, long t) {
        nodes.get(s).removeEdge(t);
        nodes.get(t).removeEdge(s);
    }
}
