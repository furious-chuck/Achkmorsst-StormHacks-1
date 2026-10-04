import java.awt.*;

public class BinaryOperation extends ExpressionPackingExpression {

    String expressionSymbol;

    public BinaryOperation(Expression[] elements, String operator) {
        if (elements.length != 2) Util.unableToCan();
        this.expressions = new BlockExpression[2];
        setChildElement(0, elements[0]);
        setChildElement(1, elements[1]);
        expressionSymbol = operator;
    }

    public BinaryOperation(Expression e0, Expression e1, String operator) {
        this(new Expression[] {e0, e1}, operator);
    }


    @Override
    public String compile() {
        return "";
    }
}
