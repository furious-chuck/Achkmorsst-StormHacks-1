public abstract class AtomicExpression implements Expression {
    @Override 
    public Expression[] getChildExpressions() {
        return new Expression[] {};
    }
}
