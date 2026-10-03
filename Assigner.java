import java.util.Objects;

public class Assigner extends CascadeCompiledStatement {

    boolean hasOperator;
    String shortHandOperator;
    Expression lValue;
    Expression rValue;


    public Assigner(Expression lValue, Expression rValue) {
        this.lValue = lValue;
        this.rValue = rValue;
        hasOperator = false;
    }

    public Assigner(Expression lValue, Expression rValue, String shortHandOperator) {
        this.lValue = lValue;
        this.rValue = rValue;
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

}
