import java.awt.*;

public abstract class BlockStatement extends CascadeCompiledStatement implements Paintable {

    Vector position = new RectVector(); // note: position should be disregarded whenever this is attached to something else!

    public Color mainColor;
    public Color outlineColor;
    public Color textColor;

    public String text;
    public Expression[] childExpressions;

    // public double getMaxChildHeight

    @Override
    public Expression[] getChildExpressions() {
        return childExpressions;
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
        if (followingStatement instanceof BlockStatement bs) {
            bs.moveSelfAndAllChildrenBy(positionChange);
        }
    }

    public void moveSelfAndAllChildrenTo(Vector newPosition) {
        Vector positionChange = newPosition.subtract(position);
        moveSelfAndAllChildrenBy(positionChange);
    }

    public void connectNextStatement(BlockStatement nextStatement) {
        nextStatement.position = this.position.add(new RectVector(0, getCascadingHeight()));
        followingStatement = nextStatement;
    }

}
