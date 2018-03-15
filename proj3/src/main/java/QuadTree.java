import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class QuadTree {
    String fileName;
    double ullon, ullat; // Upper left corner (minimum longitude, maximum latitude)
    double lrlon, lrlat; // Lower right corner (maximum longitude, minimum latitude)

    QuadTree[] child;
    protected boolean hasChild;

    protected double LonDPP;
    int depth;


    public QuadTree(String fileName, double ullon, double ullat, double lrlon, double lrlat, HashSet<String> fileNames, int depth) {
        this.fileName = fileName;
        this.ullon = ullon;
        this.ullat = ullat;
        this.lrlon = lrlon;
        this.lrlat = lrlat;
        this.depth = depth;

        double midlat = (ullat + lrlat) / 2;
        double midlon = (ullon + lrlon) / 2;
        double halflat = (ullat - lrlat) / 2;
        double halflon = (lrlon - ullon) / 2;
        LonDPP = (lrlon - ullon) / MapServer.TILE_SIZE;

        // Positions of each quadrant
        double[] ullonChild = {ullon, midlon, ullon, midlon};
        double[] ullatChild = {ullat, ullat, midlat, midlat};

        child = new QuadTree[4];
        hasChild = false;
        for (int i = 1; i <= 4; i++) {
            String newFileName = fileName.replaceAll(".png", "") + i + ".png";
            if (fileName.equals("root.png"))
                newFileName = i + ".png";
            if (!fileNames.contains(newFileName))  // File does not exist
                continue;
            hasChild = true;

            double newUllon = ullonChild[i-1];
            double newUllat = ullatChild[i-1];
            double newLrlon = newUllon + halflon;
            double newLrlat = newUllat - halflat;
            child[i-1] = new QuadTree(newFileName, newUllon, newUllat, newLrlon, newLrlat, fileNames, depth+1);
        }
    }

    /**
     * Collect all images, to the appropriate LonDPP and in specified format, and return them in a QuadTree[][] array
     */
    public QuadTree[][] collect(double queryUllon, double queryUllat, double queryLrlon, double queryLrlat, double targetLonDPP) {
        //System.out.println(fileName);
        if (!overlap(queryUllon, queryUllat, queryLrlon, queryLrlat))
            return null;

        if (!hasChild || LonDPP <= targetLonDPP) {
            QuadTree[][] ret = new QuadTree[1][1];
            ret[0][0] = this;
            return ret;
        }

        QuadTree[][][] quads = new QuadTree[4][0][0];
        for (int i=0; i<=3; i++)
            quads[i] = child[i].collect(queryUllon, queryUllat, queryLrlon, queryLrlat, targetLonDPP);

        return mergeVertical(mergeHorizontal(quads[0], quads[1]), mergeHorizontal(quads[2], quads[3]));
    }

    /**
     * Helper method: Checks if the query box overlaps with this node
     */
    public boolean overlap(double queryUllon, double queryUllat, double queryLrlon, double queryLrlat) {
        if (queryLrlon <= ullon || queryUllon >= lrlon)
            return false;
        if (queryLrlat >= ullat || queryUllat <= lrlat)
            return false;
        return true;
    }

    /**
     * Helper method: Merge two arrays horizontally (i.e. arr2 is to the right of arr1)
     */
    public QuadTree[][] mergeHorizontal(QuadTree[][] arr1, QuadTree[][] arr2) {
        if (arr1 == null && arr2 == null) return null;
        if (arr1 == null) return arr2;
        if (arr2 == null) return arr1;

        if (arr1.length != arr2.length)
            throw new IllegalArgumentException();
        if (arr1.length == 0)
            return null;
        if (arr1[0].length == 0) return arr2;
        if (arr2[0].length == 0) return arr1;

        QuadTree[][] ret = new QuadTree[arr1.length][arr1[0].length + arr2[0].length];
        for (int i=0; i<arr1.length; i++) {
            System.arraycopy(arr1[i], 0, ret[i], 0, arr1[i].length);
            System.arraycopy(arr2[i], 0, ret[i], arr1[i].length, arr2[i].length);
        }
        return ret;
    }
    /**
     * Helper method: Merge two arrays verticlly (i.e. arr2 is below of arr1)
     */
    public QuadTree[][] mergeVertical(QuadTree[][] arr1, QuadTree[][] arr2) {
        if (arr1 == null && arr2 == null) return null;
        if (arr1 == null) return arr2;
        if (arr2 == null) return arr1;

        if (arr1.length == 0) return arr2;
        if (arr2.length == 0) return arr1;
        if (arr1[0].length != arr2[0].length)
            throw new IllegalArgumentException();
        if (arr1[0].length == 0)
            return null;

        QuadTree[][] ret = new QuadTree[arr1.length + arr2.length][arr1[0].length];
        System.arraycopy(arr1, 0, ret, 0, arr1.length);
        System.arraycopy(arr2, 0, ret, arr1.length, arr2.length);
        return ret;
    }
}
