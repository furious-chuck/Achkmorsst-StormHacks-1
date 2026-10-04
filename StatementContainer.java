public interface StatementContainer {
    Statement getContainedStatement();
    void connectConnectedStatement(Statement st);
}
