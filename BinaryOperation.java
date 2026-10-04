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
            child0.moveSelfAndAllChildrenTo(this.position.add(new RectVector(10, 10)));
            if (child1 != null) child1.moveSelfAndAllChildrenTo(this.position.add(new RectVector(10 + child0.getCascadingWidth(), 10)));
        } else if (childID == 1) {
            child1 = be;
            child1.parentExpression = this;
            child1.moveSelfAndAllChildrenTo(this.position.add(new RectVector(10 + child0.getCascadingWidth(), 10)));
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
        return Math.max(child0.getCascadingHeight(), child1.getCascadingHeight()) + 20;
    }

    @Override
    public double getCascadingWidth() {
        return child0.getCascadingWidth() + child1.getCascadingWidth() + 30;
    }

    @Override
    public void paint(Graphics g) {

        double c0Width = child0 == null ? 50 : child0.getCascadingWidth();
        double c0Height = child0 == null ? 30 : child0.getCascadingHeight();
        double c1Width = child1 == null ? 50 : child1.getCascadingWidth();
        double c1Height = child1 == null ? 30 : child1.getCascadingHeight();

        double width = 30 + c0Width + c1Width;
        double height = 20 + Math.max(c0Height, c1Height);

        GraphicsUtils.drawThatGoofyExpressionShape(g, Color.MAGENTA, Color.BLACK, position.subtract(Global.cameraPos), new RectVector(width, height));

        if (child0 == null) {
            GraphicsUtils.drawThatGoofyExpressionShape(g, Color.WHITE, Color.BLACK, position.add(new RectVector(10, 10)).subtract(Global.cameraPos), new RectVector(c0Width, c0Height));
        } else {
            child0.paint(g);
        }

        if (child1 == null) {
            GraphicsUtils.drawThatGoofyExpressionShape(g, Color.WHITE, Color.BLACK, position.add(new RectVector(20 + c0Width, 10)).subtract(Global.cameraPos), new RectVector(c1Width, c1Height));
        } else {
            child1.paint(g);
        }


        // todo: make this draw the operator
    }
}
