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
        if (newExpression == null) {
            condition = null;
            return;
        }
        if (!(newExpression instanceof BlockExpression)) Util.unableToCan();
        BlockExpression be = (BlockExpression) newExpression;
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
        return 0;
    }

    @Override
    public void paint(Graphics g) {
        GraphicsUtils.drawStatementShapeWithGap(g, Color.BLUE, Color.BLACK, position.subtract(Global.cameraPos), getCascadingWidth(), getHeaderCascadingHeight(), getTotalStatementStackHeight(), 10);
    }
}
