public class WhileLoop implements Statement, StatementContainer {
  public Statement cascadeStatementStorage;
  public Statement nextStatement;
  public Expression condition;

  public WhileLoop(Expression condition, Statement cascadeStatementStorage, Statement nextStatement) {
    this.condition = condition;
    this.cascadeStatementStorage = cascadeStatementStorage;
  }  

  //todo
}
