public class WhileLoop extends CascadeCompiledStatement implements StatementContainer {
    public Statement cascadeStatementStorage;
    public Statement nextStatement;
    public Expression condition;

    public WhileLoop(Expression condition, Statement cascadeStatementStorage, Statement nextStatement) {
        this.condition = condition;
        this.cascadeStatementStorage = cascadeStatementStorage;
    }

    @Override
    public String compile() {
        return "";
    }

    @Override
    public Statement getContainedStatement() {
        return null;
    }

}
