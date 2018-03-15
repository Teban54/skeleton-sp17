import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    // Recommended: QuadTree instance variable. You'll need to make
    //              your own QuadTree since there is no built-in quadtree in Java.
    HashSet<String> fileNames;
    QuadTree root;
    String imgRoot;

    /** imgRoot is the name of the directory containing the images.
     *  You may not actually need this for your class. */
    public Rasterer(String imgRoot) {
        // YOUR CODE HERE
        this.imgRoot = imgRoot;
        fileNames = readFileNames(imgRoot);
        root = new QuadTree("root.png", MapServer.ROOT_ULLON, MapServer.ROOT_ULLAT, MapServer.ROOT_LRLON, MapServer.ROOT_LRLAT, fileNames, 0);
    }

    /**
     * Helper method: Get list of file names
     * https://stackoverflow.com/questions/5694385/getting-the-filenames-of-all-files-in-a-folder
     */
    public HashSet<String> readFileNames(String imgRoot) {
        File folder = new File(imgRoot);
        File[] listOfFiles = folder.listFiles();
        HashSet<String> fileNames = new HashSet<>();
        for (File f : listOfFiles) {
            if (f.isFile())
                fileNames.add(f.getName());
        }
        return fileNames;
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     * <p>
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     * </p>
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @return A map of results for the front end as specified:
     * "render_grid"   -> String[][], the files to display
     * "raster_ul_lon" -> Number, the bounding upper left longitude of the rastered image <br>
     * "raster_ul_lat" -> Number, the bounding upper left latitude of the rastered image <br>
     * "raster_lr_lon" -> Number, the bounding lower right longitude of the rastered image <br>
     * "raster_lr_lat" -> Number, the bounding lower right latitude of the rastered image <br>
     * "depth"         -> Number, the 1-indexed quadtree depth of the nodes of the rastered image.
     *                    Can also be interpreted as the length of the numbers in the image
     *                    string. <br>
     * "query_success" -> Boolean, whether the query was able to successfully complete. Don't
     *                    forget to set this to true! <br>
     * @see #REQUIRED_RASTER_REQUEST_PARAMS
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        // System.out.println(params);
        Map<String, Object> results = new HashMap<>();
        //System.out.println("Since you haven't implemented getMapRaster, nothing is displayed in "
        //                   + "your browser.");
        //return results;

        double ullon = params.get("ullon");
        double ullat = params.get("ullat");
        double lrlon = params.get("lrlon");
        double lrlat = params.get("lrlat");
        double w = params.get("w");
        double h = params.get("h");
        double targetLonDPP = (lrlon - ullon) / w;

        // Check if the query box is out of range
        if (ullon >= lrlon || ullat <= lrlat
                || ullon >= root.lrlon || lrlon <= root.ullon
                || ullat <= root.lrlat || lrlat >= root.ullat) {
            results.put("query_success", false);
            return results;
        }

        QuadTree[][] gridArr = root.collect(ullon, ullat, lrlon, lrlat, targetLonDPP);

        if (gridArr == null) {  // Out of range or no result found
            results.put("query_success", false);
            return results;
        }

        results.put("render_grid", quadTreesToStrings(gridArr));
        results.put("raster_ul_lon", gridArr[0][0].ullon);
        results.put("raster_ul_lat", gridArr[0][0].ullat);
        results.put("raster_lr_lon", gridArr[gridArr.length-1][gridArr[gridArr.length-1].length-1].lrlon);
        results.put("raster_lr_lat", gridArr[gridArr.length-1][gridArr[gridArr.length-1].length-1].lrlat);
        results.put("depth", gridArr[0][0].depth);
        results.put("query_success", true);
        return results;
    }

    /**
     * Helper method: Convert QuadTree[][] to String[][]
     */
    public String[][] quadTreesToStrings(QuadTree[][] quadArr) {
        if (quadArr == null) return null;
        String[][] strArr = new String[quadArr.length][quadArr[0].length];
        for (int i=0; i<quadArr.length; i++)
            for (int j=0; j<quadArr[i].length; j++)
                strArr[i][j] = imgRoot + quadArr[i][j].fileName;
        return strArr;
    }


    public static void main(String[] args) {
        Map<String, Double> params = new HashMap<>();
        params.put("lrlon", -122.24053369025242);
        params.put("ullon", -122.24163047377972);
        params.put("w", 892.0);
        params.put("h", 875.0);
        params.put("ullat", 37.87655856892288);
        params.put("lrlat", 37.87548268822065);
        Map<String, Object> ret = new Rasterer("img/").getMapRaster(params);
        System.out.println(ret);
        String[][] tr = (String[][])ret.get("render_grid");
        for (int i=0; i<tr.length; i++) {
            for (int j=0; j<tr[i].length; j++)
                System.out.print(tr[i][j] + " ");
            System.out.println();
        }
    }
}
