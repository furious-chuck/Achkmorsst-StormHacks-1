public class Assigner extends CascadeCompiledStatement {

    boolean hasOperator;
    String shortHandOperator;
    Expression lValue;
    Expression rValue;

    @Override
    public String compile() {
        return lValue.compile() + " " + (hasOperator ? shortHandOperator : "") + "= " + rValue;
    }

}
