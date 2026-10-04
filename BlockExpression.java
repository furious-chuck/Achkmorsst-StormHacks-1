import java.awt.*;

public abstract class BlockExpression implements Expression, Paintable {

    Vector position = new RectVector();

    public Color mainColor;
    public Color secondaryColor;
    public Color outlineColor;
    public Color textColor;

    public HasChildExpressions parentExpression;

    public HasChildExpressions getParentExpression() {
        return parentExpression;
    }

    public abstract double getCascadingHeight();
    public abstract double getCascadingWidth();

    public void moveSelfAndAllChildrenBy(Vector positionChange) {
        position = position.add(positionChange);
        for (Expression exp : getChildExpressions()) {
            if (exp instanceof BlockExpression be) {
                be.moveSelfAndAllChildrenBy(positionChange);
            }
        }
    }

    public void moveSelfAndAllChildrenTo(Vector newPosition) {
        Vector positionChange = newPosition.subtract(position);
        moveSelfAndAllChildrenBy(positionChange);
    }

}
