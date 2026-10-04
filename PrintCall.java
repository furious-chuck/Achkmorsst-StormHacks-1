public class PrintCall extends ExpressionPackingStatement {

    public PrintCall(Expression value) {
        leftSpace = 100;
        expressions = new BlockExpression[1];
        setChildElement(0, value);
    }

    @Override
    public String compile() {
        return "System.out.println(" + expressions[0].compile() + ");\n";
    }
    
}
