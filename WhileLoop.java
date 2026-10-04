public class WhileLoop extends CascadeCompiledStatement implements StatementContainer {
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
        return null;
    }

    @Override
    public Expression[] getChildExpressions() {
        return new Expression[] { condition };
    }

    @Override
    public void setChildElement(int childID, Expression newExpression) {
        if (childID != 0) Util.unableToCan();
        if (!(newExpression instanceof BlockExpression)) Util.unableToCan();
        BlockExpression be = (BlockExpression) newExpression;
        condition = be;
        be.parentExpression = this;
    }
}
