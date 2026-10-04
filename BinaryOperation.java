import java.awt.*;

public class BinaryOperation extends BlockExpression {

    String expressionSymbol;

    BlockExpression child0;
    BlockExpression child1;

    @Override
    public Expression[] getChildExpressions() {
        return new Expression[] { child0, child1 };
    }

    @Override 
    public String compile() {
        return "(" + child0.compile() + " " + expressionSymbol + " " + child1.compile() + ")";
    }

    @Override
    public void paint(Graphics g) {

    }

    @Override
    public double getCascadingHeight() {
        return Math.max(child0.getCascadingHeight(), child1.getCascadingHeight()) + 10;
    }

    @Override
    public double getCascadingWidth() {
        return child0.getCascadingWidth() + child1.getCascadingWidth() + 10;
    }
}
