import java.awt.*;

public class WhileLoop extends BlockStatement implements StatementContainer {
    public Statement cascadeStatementStorage;
    public Statement nextStatement;
    public Expression condition;

    public WhileLoop(Expression condition, Statement cascadeStatementStorage, Statement nextStatement) {
        setChildElement(0, condition);
        this.cascadeStatementStorage = cascadeStatementStorage;
        this.nextStatement = nextStatement;
    }

    @Override
    public String compile() {
        return "";
    }

    @Override
    public Statement getContainedStatement() {
        return cascadeStatementStorage;
    }

    @Override
    public Expression[] getChildExpressions() {
        return new Expression[] { condition };
    }

    @Override
    public void setChildElement(int childID, Expression newExpression) {
        if (childID != 0) Util.unableToCan();
        Expression old = condition;
        if (newExpression == null) {
            // clearing the slot: detach the old child's parent link so it floats free
            if (old instanceof BlockExpression beOld && beOld.getParentExpression() == this) {
                beOld.parentExpression = null;
            }
            condition = null;
            return;
        }
        if (!(newExpression instanceof BlockExpression)) Util.unableToCan();
        BlockExpression be = (BlockExpression) newExpression;
        if (old != be) {
            int previousIndex = indexOfChild(be);
            if (previousIndex >= 0 && previousIndex != childID) {
                condition = null;
            }
            if (old instanceof BlockExpression beOld && beOld.getParentExpression() == this) {
                beOld.parentExpression = null;
            }
        }
        be.parentExpression = this;
        be.moveSelfAndAllChildrenTo(this.position.add(new RectVector(10, 10)));
        condition = be;
        if (cascadeStatementStorage instanceof BlockStatement bs) {
            bs.moveSelfAndAllChildrenTo(position.add(new RectVector(10, 10 + getHeaderCascadingHeight())));
        }
    }

    /**
     * True when the while-loop's condition slot currently holds an expression.
     */
    public boolean hasCondition() {
        return condition != null;
    }

    /**
     * Returns true if the given world-space point lies within the bounding box of the
     * current condition expression (its live position, which setChildElement keeps in
     * sync with this loop). Used by drag/drop so a dragged block floating over the
     * loop's body gap is not mistaken for a drop onto the condition.
     */
    public boolean conditionContainsPoint(Vector p) {
        return condition instanceof BlockExpression be && be.containsPoint(p);
    }

    /**
     * Returns true if the given world-space point lies within this loop's header band
     * (position.y .. position.y + header height), i.e. the strip where the condition
     * slot lives — as opposed to the cascaded body area below it. Mirrors the same
     * geometry paint() uses for the shape-with-gap.
     */
    public boolean headerContainsPoint(Vector p) {
        return p.getX() >= position.getX() &&
               p.getX() <= position.getX() + getCascadingWidth() &&
               p.getY() >= position.getY() &&
               p.getY() <= position.getY() + getHeaderCascadingHeight();
    }

    private double getHeaderCascadingHeight() {
        if (condition instanceof BlockExpression be) {
            return 20 + be.getCascadingHeight();
        }
        return 20 + 30;
    }

    // public void addInternalStorageElement

    @Override
    public double getCascadingHeight() {
        if (cascadeStatementStorage instanceof BlockStatement bs) {
            return getHeaderCascadingHeight() + bs.getTotalStatementStackHeight() + 10;
        }
        return getHeaderCascadingHeight() + 20 + 10;
    }

    @Override
    public double getCascadingWidth() {
        if (condition instanceof BlockExpression be) {
            return 100 + be.getCascadingWidth();
        }
        return 100 + 50;
    }

    @Override
    public void paint(Graphics g) {
        GraphicsUtils.drawStatementShapeWithGap(g, Color.BLUE, Color.BLACK, position.subtract(Global.cameraPos), getCascadingWidth(), getHeaderCascadingHeight(), getTotalStatementStackHeight(), 10);
        if (condition == null) {
            GraphicsUtils.drawThatGoofyExpressionShape(g, Color.WHITE, Color.BLACK, position.add(new RectVector(10, 10)).subtract(Global.cameraPos), new RectVector(50, 30));
        } else if (condition instanceof BlockExpression be) {
            be.paint(g);
        } else Util.unableToCan();

        if (cascadeStatementStorage instanceof BlockStatement bs) {
            bs.paint(g);
        }

        if (followingStatement instanceof BlockStatement bs) {
            bs.paint(g);
        }
    }
}
