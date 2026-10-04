import java.awt.*;

public class BinaryOperation extends BlockExpression {

    String expressionSymbol;

    BlockExpression child0;
    BlockExpression child1;

    public BinaryOperation(BlockExpression child0, BlockExpression child1, String expressionSymbol) {
        this.child0 = child0;
        this.child1 = child1;
        this.expressionSymbol = expressionSymbol;
    }

    @Override
    public void setChildElement(int childID, Expression newExpression) {
        if (!(newExpression instanceof BlockExpression)) {
            Util.unableToCan();
        }
        BlockExpression be = (BlockExpression) newExpression;
        if (childID == 0) {
            child0 = be;
            child0.parentExpression = this;
        } else if (childID == 1) {
            child1 = be;
            child1.parentExpression = this;
        } else {
            Util.unableToCan();
        }
    }

    @Override
    public Expression[] getChildExpressions() {
        return new Expression[] { child0, child1 };
    }

    @Override 
    public String compile() {
        return "(" + child0.compile() + " " + expressionSymbol + " " + child1.compile() + ")";
    }

    @Override
    public double getCascadingHeight() {
        return Math.max(child0.getCascadingHeight(), child1.getCascadingHeight()) + 10;
    }

    @Override
    public double getCascadingWidth() {
        return child0.getCascadingWidth() + child1.getCascadingWidth() + 10;
    }

    @Override
    public void paint(Graphics g) {

        double c0Width = child0 == null ? 50 : child0.getCascadingWidth();
        double c0Height = child0 == null ? 30 :child0.getCascadingHeight();
        double c1Width = child1 == null ? 50 : child1.getCascadingWidth();
        double c1Height = child1 == null ? 50 : child1.getCascadingHeight();

        double width = 30 + c0Width + c1Width;
        double height = 20 + Math.max(c0Height, c1Height);

        g.setColor(mainColor);

        Polygon poly = Util.createPolygon(
                new Vector[] {
                        (new RectVector(2, 0)).add(position).subtract(Global.cameraPos),
                        (new RectVector(width - 2, 0)).add(position).subtract(Global.cameraPos),
                        (new RectVector(width, 2)).add(position).subtract(Global.cameraPos),
                        (new RectVector(width, height - 2)).add(position).subtract(Global.cameraPos),
                        (new RectVector(width - 2, height)).add(position).subtract(Global.cameraPos),
                        (new RectVector(2, height)).add(position).subtract(Global.cameraPos),
                        (new RectVector(0, height - 2)).add(position).subtract(Global.cameraPos),
                        (new RectVector(0, height / 2.0 + 5)).add(position).subtract(Global.cameraPos),
                        (new RectVector(-5, height / 2.0)).add(position).subtract(Global.cameraPos),
                        (new RectVector(0, height / 2.0 - 5)).add(position).subtract(Global.cameraPos),
                        (new RectVector(0, 2)).add(position).subtract(Global.cameraPos)
                }
        );

        g.fillPolygon(poly);

        if (child0 == null) {
            Polygon emptySadChildSlot = Util.createPolygon(
                    new Vector[] {
                            (new RectVector(10 + 2, 10)).add(position).subtract(Global.cameraPos),
                            (new RectVector(c0Width - 2, 10)).add(position).subtract(Global.cameraPos),
                            (new RectVector(c0Width, 10 + 2)).add(position).subtract(Global.cameraPos),
                            (new RectVector(c0Width, c0Height - 2)).add(position).subtract(Global.cameraPos),
                            (new RectVector(c0Width - 2, c0Height)).add(position).subtract(Global.cameraPos),
                            (new RectVector(2, c0Height)).add(position).subtract(Global.cameraPos),
                            (new RectVector(0, c0Height - 2)).add(position).subtract(Global.cameraPos),
                            (new RectVector(0, c0Height / 2.0 + 5)).add(position).subtract(Global.cameraPos),
                            (new RectVector(-5, c0Height / 2.0)).add(position).subtract(Global.cameraPos),
                            (new RectVector(0, c0Height / 2.0 - 5)).add(position).subtract(Global.cameraPos),
                            (new RectVector(0, 2)).add(position).subtract(Global.cameraPos)
                    }
            );
            g.setColor(Color.WHITE);
            g.fillPolygon(emptySadChildSlot);
            g.setColor(outlineColor);
            g.drawPolygon(emptySadChildSlot);
        } else {
            child0.paint(g);
        }

        if (child1 == null) {
            Polygon emptySadChildSlot = Util.createPolygon(
                    new Vector[] {
                            (new RectVector(c0Width + 10 + 10 + 2, 10)).add(position).subtract(Global.cameraPos),
                            (new RectVector(c0Width + 10 + c1Width - 2, 10)).add(position).subtract(Global.cameraPos),
                            (new RectVector(c0Width + 10 + c1Width, 10 + 2)).add(position).subtract(Global.cameraPos),
                            (new RectVector(c0Width + 10 + c1Width, c0Height - 2)).add(position).subtract(Global.cameraPos),
                            (new RectVector(c0Width + 10 + c1Width - 2, c0Height)).add(position).subtract(Global.cameraPos),
                            (new RectVector(c0Width + 10 + 2, c0Height)).add(position).subtract(Global.cameraPos),
                            (new RectVector(c0Width + 10 + 0, c0Height - 2)).add(position).subtract(Global.cameraPos),
                            (new RectVector(c0Width + 10 + 0, c0Height / 2.0 + 5)).add(position).subtract(Global.cameraPos),
                            (new RectVector(c0Width + 10 + -5, c0Height / 2.0)).add(position).subtract(Global.cameraPos),
                            (new RectVector(c0Width + 10 + 0, c0Height / 2.0 - 5)).add(position).subtract(Global.cameraPos),
                            (new RectVector(c0Width + 10 + 0, 2)).add(position).subtract(Global.cameraPos)
                    }
            );
            g.setColor(Color.WHITE);
            g.fillPolygon(emptySadChildSlot);
            g.setColor(outlineColor);
            g.drawPolygon(emptySadChildSlot);
        } else {
            child1.paint(g);
        }

        g.setColor(outlineColor);
        g.drawPolygon(poly);

        // todo: make this draw the operator
    }
}
