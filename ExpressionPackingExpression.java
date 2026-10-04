import java.awt.*;

public abstract class ExpressionPackingExpression extends BlockExpression {

    BlockExpression[] expressions;

    double leftSpace;
    double expressionSpacing;
    double rightSpace;

    double verSpace;

    final Vector defaultSize = new RectVector(50, 30);

    @Override
    public double getCascadingHeight() {
        if (expressions.length == 0) {
            return defaultSize.getY() + verSpace * 2;
        }
        double maxSize = 0;
        for (BlockExpression exp : expressions) {
            double height = exp == null ? defaultSize.getY() : exp.getCascadingHeight();
            if (height > maxSize) {
                maxSize = height;
            }
        }
        return maxSize + verSpace * 2;
    }

    @Override
    public double getCascadingWidth() {
        if (expressions.length == 0) {
            return defaultSize.getX() + leftSpace + rightSpace;
        }
        double total = leftSpace + rightSpace;
        for (BlockExpression exp : expressions) {
            total += exp == null ? defaultSize.getX() : exp.getCascadingWidth();
        }
        total += expressionSpacing * (expressions.length - 1);
        return total;
    }

    public double getWidthUpToExpressionAt(int index) {
        if (expressions.length <= index) {
            Util.unableToCan();
        }
        double total = leftSpace;
        for (int i = 0; i < index; i++) {
            total += expressions[i] == null ? defaultSize.getX() : expressions[i].getCascadingWidth();
        }
        total += expressionSpacing * (index - 1);
        return total;
    }

    @Override
    public Expression[] getChildExpressions() {
        return expressions;
    }

    @Override
    public void setChildElement(int childID, Expression newExpression) {
        expressions[childID] = (BlockExpression) newExpression;
    }

    @Override
    public void paint(Graphics g) {
        getCascadingHeight();
    }
}
