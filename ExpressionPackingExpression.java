import java.awt.*;

public abstract class ExpressionPackingExpression extends BlockExpression {

    BlockExpression[] expressions;

    double leftSpace = 10;
    double expressionSpacing = 10;
    double rightSpace = 10;

    double verSpace = 10;

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
        total += expressionSpacing * index;
        return total;
    }

    @Override
    public Expression[] getChildExpressions() {
        return expressions;
    }

    @Override
    public void setChildElement(int childID, Expression newExpression) {
        if (newExpression == null) {
            expressions[childID] = null;
            return;
        }
        BlockExpression be = (BlockExpression) newExpression;
        expressions[childID] = be;
        be.parentExpression = this;
        be.position = position.add(new RectVector(getWidthUpToExpressionAt(childID), 10));
    }

    @Override
    public void paint(Graphics g) {
        GraphicsUtils.drawThatGoofyExpressionShape(g, Color.MAGENTA, Color.BLACK, position.subtract(Global.cameraPos), new RectVector(getCascadingWidth(), getCascadingHeight()));
        double elementOffset = leftSpace;
        for (BlockExpression expression : expressions) {
            if (expression == null) {
                GraphicsUtils.drawThatGoofyExpressionShape(g, Color.WHITE, Color.BLACK, position.add(new RectVector(elementOffset, 10)).subtract(Global.cameraPos), defaultSize);
                elementOffset += defaultSize.getX() + expressionSpacing;
            } else {
                // paint the child at its slot computed from the parent's current position
                // (same convention as the null placeholder above), so a moved parent
                // can never leave a child behind or misalign it with null siblings.
                Vector previousPosition = expression.position;
                expression.position = position.add(new RectVector(elementOffset, 10));
                expression.paint(g);
                expression.position = previousPosition;
                elementOffset += expression.getCascadingWidth() + expressionSpacing;
            }
        }
    }
}
