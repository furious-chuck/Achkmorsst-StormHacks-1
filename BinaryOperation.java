public class BinaryOperation implements Expression {

    String expressionSymbol;

    Expression child0;
    Expression child1;

    @Override
    public Expression[] getChildExpressions() {
        return new Expression[] { child0, child1 };
    }

    @Override 
    public String compile() {
        return "(" + child0.compile() + " " + expressionSymbol + " " + child1.compile() + ")";
    }

}
