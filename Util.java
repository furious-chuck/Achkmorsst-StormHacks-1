import java.awt.*;

public class Util {
    private Util() {}

    public static Polygon createPolygon(Vector[] vertexes) {
        Polygon poly = new Polygon();
        for (Vector vert : vertexes) {
            poly.addPoint((int) vert.getX(), (int) vert.getY());
        }
        return poly;
    }

}
