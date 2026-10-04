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

    public static void unableToCan() {
        throw new IllegalStateException("A fatal exception occurred with no context");
    }

    public static void unableToCan(String message) {
        throw new IllegalStateException("A fatal exception occurred with the following message:\n" + message);
    }

}
