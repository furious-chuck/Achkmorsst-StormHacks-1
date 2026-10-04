import java.awt.*;

public class GraphicsUtils {
    private GraphicsUtils() {}

    public static void drawThatGoofyExpressionShape(Graphics g, Color mainColor, Color outlineColor, Vector positionOnScreen, Vector size) {
        Polygon poly = Util.createPolygon(
                new Vector[] {
                        (new RectVector(2, 0)).add(positionOnScreen),
                        (new RectVector(size.getX() - 2, 0)).add(positionOnScreen),
                        (new RectVector(size.getX(), 2)).add(positionOnScreen),
                        (new RectVector(size.getX(), size.getY() - 2)).add(positionOnScreen),
                        (new RectVector(size.getX() - 2, size.getY())).add(positionOnScreen),
                        (new RectVector(2, size.getY())).add(positionOnScreen),
                        (new RectVector(0, size.getY() - 2)).add(positionOnScreen),
                        (new RectVector(0, size.getY() / 2.0 + 5)).add(positionOnScreen),
                        (new RectVector(-5, size.getY() / 2.0)).add(positionOnScreen),
                        (new RectVector(0, size.getY() / 2.0 - 5)).add(positionOnScreen),
                        (new RectVector(0, 2)).add(positionOnScreen)
                }
        );
        g.setColor(mainColor);

        g.fillPolygon(poly);
        g.setColor(outlineColor);
        g.drawPolygon(poly);
    }

    public static void drawStatementShape(Graphics g, Color mainColor, Color outlineColor, Vector positionOnScreen, Vector size) {
        Polygon poly = Util.createPolygon(
                new Vector[] {
                        (new RectVector(2, 0)).add(positionOnScreen),
                        (new RectVector(10, 0)).add(positionOnScreen),
                        (new RectVector(10, 5)).add(positionOnScreen),
                        (new RectVector(20, 5)).add(positionOnScreen),
                        (new RectVector(20, 0)).add(positionOnScreen),
                        (new RectVector(size.getX() - 2, 0)).add(positionOnScreen),
                        (new RectVector(size.getX(), 2)).add(positionOnScreen),
                        (new RectVector(size.getX(), size.getY() - 2)).add(positionOnScreen),
                        (new RectVector(size.getX() - 2, size.getY())).add(positionOnScreen),

                        (new RectVector(20, size.getY())).add(positionOnScreen),
                        (new RectVector(20, size.getY() + 5)).add(positionOnScreen),
                        (new RectVector(10, size.getY() + 5)).add(positionOnScreen),
                        (new RectVector(10, size.getY())).add(positionOnScreen),

                        (new RectVector(2, size.getY())).add(positionOnScreen),
                        (new RectVector(0, size.getY() - 2)).add(positionOnScreen),
                        (new RectVector(0, 2)).add(positionOnScreen)
                }
        );
        g.setColor(mainColor);

        g.fillPolygon(poly);
        g.setColor(outlineColor);
        g.drawPolygon(poly);
    }
    /*
    public static void drawStatementShapeWithGap(Graphics g, Color mainColor, Color outlineColor, Vector positionOnScreen, double width, double headerHeight, double gapSize, double footerHeight) {
        Polygon poly = Util.createPolygon(
                new Vector[] {
                        (new RectVector(2, 0)).add(positionOnScreen),
                        (new RectVector(10, 0)).add(positionOnScreen),
                        (new RectVector(10, 5)).add(positionOnScreen),
                        (new RectVector(20, 5)).add(positionOnScreen),
                        (new RectVector(20, 0)).add(positionOnScreen),
                        (new RectVector(size.getX() - 2, 0)).add(positionOnScreen),
                        (new RectVector(size.getX(), 2)).add(positionOnScreen),
                        (new RectVector(size.getX(), size.getY() - 2)).add(positionOnScreen),
                        (new RectVector(size.getX() - 2, size.getY())).add(positionOnScreen),

                        (new RectVector(20, size.getY())).add(positionOnScreen),
                        (new RectVector(20, size.getY() + 5)).add(positionOnScreen),
                        (new RectVector(10, size.getY() + 5)).add(positionOnScreen),
                        (new RectVector(10, size.getY())).add(positionOnScreen),

                        (new RectVector(2, size.getY())).add(positionOnScreen),
                        (new RectVector(0, size.getY() - 2)).add(positionOnScreen),
                        (new RectVector(0, 2)).add(positionOnScreen)
                }
        );
        g.setColor(mainColor);

        g.fillPolygon(poly);
        g.setColor(outlineColor);
        g.drawPolygon(poly);
    }
    */

}
