import java.util.Objects;

public class Assigner extends CascadeCompiledStatement {

    boolean hasOperator;
    String shortHandOperator;
    BlockExpression lValue;
    BlockExpression rValue;


    public Assigner(Expression lValue, Expression rValue) {
        setChildElement(0, lValue);
        setChildElement(1, rValue);
        hasOperator = false;
    }

    public Assigner(Expression lValue, Expression rValue, String shortHandOperator) {
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
        return lValue.compile() + " " + (hasOperator ? shortHandOperator : "") + "= " + rValue;
    }

    @Override
    public Expression[] getChildExpressions() {
        return new Expression[] { lValue, rValue };
    }

    @Override
    public void setChildElement(int childID, Expression newExpression) {
        if (!(newExpression instanceof BlockExpression)) {
            Util.unableToCan();
        }
        BlockExpression be = (BlockExpression) newExpression;
        if (childID == 0) {
            lValue = be;
            lValue.parentExpression = this;
        } else if (childID == 1) {
            rValue = be;
            rValue.parentExpression = this;
        } else {
            Util.unableToCan();
        }
    }
}
