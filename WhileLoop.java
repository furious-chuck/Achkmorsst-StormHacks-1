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

    private double getHeaderCascadingHeight() {
        if (condition instanceof BlockExpression be) {
            return 10 + be.getCascadingHeight();
        }
        return 10 + 30;
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
    }
}
