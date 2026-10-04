import java.awt.*;
import java.util.Objects;

public class VarDeclaration extends ExpressionPackingStatement {

    public VarDeclaration(Expression classBlock, Expression lValue, Expression rValue) {
        expressions = new BlockExpression[3];
        setChildElement(0, classBlock);
        setChildElement(1, lValue);
        setChildElement(2, rValue);
    }

    @Override
    public String compile() {
        return expressions[0].compile() + " " + expressions[1].compile() + " = " + expressions[2].compile() + ";\n";
    }
}
