import java.awt.*;
import java.util.Objects;

public class Assigner extends ExpressionPackingStatement {

    boolean hasOperator;
    String shortHandOperator;

    // colours the "=" band is painted with (the packed atomics always sit on top of it)
    private static final Color SYMBOL_OUTLINE_COLOR = Color.BLACK;
    private static final Color SYMBOL_TEXT_COLOR = Color.BLACK;

    /**
     * The assignment operator shown between the two atomic expressions: a plain "="
     * for a simple assignment, or "+=", "-=", "*=", "/=" for the shorthand forms.
     */
    public String getAssignmentSymbol() {
        return (hasOperator ? shortHandOperator : "") + "=";
    }

    public Assigner(Expression lValue, Expression rValue) {
        expressions = new BlockExpression[2];
        setChildElement(0, lValue);
        setChildElement(1, rValue);
        hasOperator = false;
        initLayout();
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
        initLayout();
    }

    private void initLayout() {
        mainColor = Color.MAGENTA;
        outlineColor = Color.BLACK;
        textColor = Color.BLACK;
        // widen the gap between the two slots so the "=" band never overlaps either
        // atomic block (same trick BinaryOperation uses for its operator symbols)
        halfGapExpansion = Math.max(0, (BinaryOperation.SYMBOL_BAND_WIDTH - expressionSpacing) / 2.0);
    }

    @Override
    protected void paintOverMainShape(Graphics g) {
        // paint the "=" sign in the widened gap between the two atomic expressions
        double slot0Width = expressions[0] == null ? defaultSize.getX() : expressions[0].getCascadingWidth();
        BinaryOperation.paintSymbolInFirstGap(g, position, leftSpaceForLayout(), slot0Width, expressionSpacing,
                getCascadingHeight(), getAssignmentSymbol(), SYMBOL_OUTLINE_COLOR, SYMBOL_TEXT_COLOR);
    }

    @Override
    public String compile() {
        return expressions[0].compile() + " " + getAssignmentSymbol() + " " + expressions[1].compile() + ";\n";
    }
}
