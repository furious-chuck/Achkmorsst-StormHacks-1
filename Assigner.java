import java.awt.*;
import java.util.Objects;

public class Assigner extends ExpressionPackingStatement {

    boolean hasOperator;
    String shortHandOperator;


    public Assigner(Expression lValue, Expression rValue) {
        expressions = new BlockExpression[2];
        setChildElement(0, lValue);
        setChildElement(1, rValue);
        hasOperator = false;
    }

    public Assigner(Expression lValue, Expression rValue, String shortHandOperator) {
        expressions = new BlockExpression[2];
        setChildElement(0, lValue);
        setChildElement(1, rValue);
        hasOperator = true;
        if (
                !Objects.equals(shortHandOperator, "+") &&
                !Objects.equals(shortHandOperator, "-") &&
                !Objects.equals(shortHandOperator, "*") &&
                !Objects.equals(shortHandOperator, "/")
        ) {
            throw new IllegalArgumentException("What is this operator broewski");
        }
        this.shortHandOperator = shortHandOperator;
    }

    @Override
    public String compile() {
        return expressions[0].compile() + " " + (hasOperator ? shortHandOperator : "") + "= " + expressions[1].compile() + ";\n";
    }
}
