import java.awt.*;

public abstract class BlockStatement extends CascadeCompiledStatement implements Paintable, Hoverable {

    Vector position = new RectVector(); // note: position should be disregarded whenever this is attached to something else!

    public Color mainColor;
    public Color outlineColor;
    public Color textColor;

    public String text;
    public Expression[] childExpressions;

    @Override
    public Vector getPosition() {
        return position;
    }

    /**
     * Statement roots have no parent block, so they are their own root ancestor.
     */
    @Override
    public Hoverable getParentBlock() {
        return null;
    }

    @Override
    public String getBlockName() {
        return getClass().getSimpleName();
    }

    // public double getMaxChildHeight

    @Override
    public Expression[] getChildExpressions() {
        return childExpressions;
    }

    public abstract double getCascadingHeight();
    public abstract double getCascadingWidth();

    /**
     * Returns true if the given world-space point lies within this block's bounding
     * rectangle (position .. position + cascading size). Children are excluded: they
     * are checked separately so the deepest (most specific) block wins.
     */
    @Override
    public boolean containsPoint(Vector p) {
        return p.getX() >= position.getX() &&
               p.getX() <= position.getX() + getCascadingWidth() &&
               p.getY() >= position.getY() &&
               p.getY() <= position.getY() + getCascadingHeight();
    }

    /**
     * Finds the innermost hoverable block under the given world-space mouse position.
     * The child expressions are laid out relative to this statement at paint time, so
     * we recompute their current slots here (same convention as paint()) and search
     * them first, so the deepest/most specific block wins. Then we check our own shape,
     * and finally walk down the connected following-statement chain.
     * Returns null if the mouse is not hovering over any block.
     */
    @Override
    public Hoverable findHoveredBlock(Vector mousePos) {
        if (this instanceof ExpressionPackingStatement eps) {
            double elementOffset = eps.leftSpace;
            for (int i = 0; i < eps.expressions.length; i++) {
                BlockExpression expression = eps.expressions[i];
                if (expression == null) {
                    // an empty slot is itself a droppable target: report the statement
                    // containing it so a dragged atomic can be snapped back into this slot
                    if (eps.slotContainsPoint(i, mousePos)) {
                        return this;
                    }
                    elementOffset += eps.defaultSize.getX() + eps.expressionSpacing;
                    continue;
                }
                // child slot computed from the parent's current position, matching paint()
                Vector previousPosition = expression.position;
                expression.position = position.add(new RectVector(elementOffset, 10));
                Hoverable found = expression.findHoveredBlock(mousePos);
                expression.position = previousPosition;
                if (found != null) {
                    return found;
                }
                elementOffset += expression.getCascadingWidth() + eps.expressionSpacing;
            }
        } else {
            for (Expression exp : getChildExpressions()) {
                if (exp instanceof BlockExpression be) {
                    Hoverable found = be.findHoveredBlock(mousePos);
                    if (found != null) {
                        return found;
                    }
                }
            }
        }
        // then our own shape
        if (containsPoint(mousePos)) {
            return this;
        }
        // finally, any statement connected below us
        if (followingStatement instanceof BlockStatement bs) {
            return bs.findHoveredBlock(mousePos);
        }
        return null;
    }

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

    public double getTotalStatementStackHeight() {
        if (followingStatement instanceof BlockStatement bs) {
            return getCascadingHeight() + bs.getTotalStatementStackHeight();
        }
        return getCascadingHeight();
    }

}
