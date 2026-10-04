import java.awt.*;

public class BinaryOperation extends ExpressionPackingExpression {

    String expressionSymbol;

    // width of the (highlighted) band the operator symbol is painted on, centred in
    // the gap between the two packed atomic expressions
    static final double SYMBOL_BAND_WIDTH = 30;

    public BinaryOperation(Expression[] elements, String operator) {
        if (elements.length != 2) Util.unableToCan();
        this.expressions = new BlockExpression[2];
        setChildElement(0, elements[0]);
        setChildElement(1, elements[1]);
        expressionSymbol = operator;
        mainColor = Color.MAGENTA;
        outlineColor = Color.BLACK;
        textColor = Color.BLACK;
        // widen the gap so the symbol band never overlaps either atomic block
        halfGapExpansion = Math.max(0, (SYMBOL_BAND_WIDTH - expressionSpacing) / 2.0);
    }

    public BinaryOperation(Expression e0, Expression e1, String operator) {
        this(new Expression[] {e0, e1}, operator);
    }

    /**
     * Convenience factory: an addition block ("a + b"). The plus sign is painted in
     * the gap between the two atomic expressions.
     */
    public static BinaryOperation addition(Expression left, Expression right) {
        return new BinaryOperation(left, right, "+");
    }

    /**
     * Convenience factory: a subtraction block ("a - b"), with the minus sign shown
     * between the two atomic expressions.
     */
    public static BinaryOperation subtraction(Expression left, Expression right) {
        return new BinaryOperation(left, right, "-");
    }

    /**
     * Convenience factory: a multiplication block ("a * b"), with the times sign
     * shown between the two atomic expressions.
     */
    public static BinaryOperation multiplication(Expression left, Expression right) {
        return new BinaryOperation(left, right, "*");
    }

    /**
     * Convenience factory: a division block ("a / b"), with the division sign shown
     * between the two atomic expressions.
     */
    public static BinaryOperation division(Expression left, Expression right) {
        return new BinaryOperation(left, right, "/");
    }

    @Override
    protected void paintOverMainShape(Graphics g) {
        // draw the operator symbol on a highlighted band sitting exactly in the middle
        // of the gap between the two atomic expressions (the gap was widened by
        // halfGapExpansion to make room for it). Everything is converted to screen
        // space via Global.cameraPos, matching paintMainShape().
        Vector screenPos = position.subtract(Global.cameraPos);
        double slot0Width = expressions[0] == null ? defaultSize.getX() : expressions[0].getCascadingWidth();
        double gapCenterX = screenPos.getX() + leftSpaceForLayout() + slot0Width + expressionSpacing / 2.0;
        double height = getCascadingHeight();
        int bandX = (int) Math.round(gapCenterX - SYMBOL_BAND_WIDTH / 2.0);
        int bandY = (int) Math.round(screenPos.getY());
        int bandW = (int) Math.round(SYMBOL_BAND_WIDTH);
        int bandH = (int) Math.round(height);

        g.setColor(Color.WHITE);
        g.fillRect(bandX, bandY, bandW, bandH);
        g.setColor(outlineColor);
        g.drawRect(bandX, bandY, bandW, bandH);

        String text = getSymbolDisplayString();
        Font font = g.getFont().deriveFont(Font.BOLD, 14f);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics(font);
        int textX = bandX + (bandW - fm.stringWidth(text)) / 2;
        int textY = bandY + (bandH - fm.getHeight()) / 2 + fm.getAscent();
        g.setColor(textColor);
        g.drawString(text, textX, textY);
    }

    /**
     * The string actually painted inside the block: the raw operator, with friendlier
     * glyphs for the common arithmetic symbols.
     */
    public String getSymbolDisplayString() {
        if (expressionSymbol == null) {
            return "?";
        }
        return switch (expressionSymbol) {
            case "*" -> "×";
            case "/" -> "÷";
            default -> expressionSymbol;
        };
    }

    public String getExpressionSymbol() {
        return expressionSymbol;
    }

    @Override
    public String compile() {
        if (expressions[0] == null || expressions[1] == null) {
            // incomplete binary block: emit parentheses around whatever half exists so
            // the surrounding expression stays syntactically valid
            String left = expressions[0] == null ? "" : expressions[0].compile();
            String right = expressions[1] == null ? "" : expressions[1].compile();
            return "(" + left + expressionSymbol + right + ")";
        }
        return "(" + expressions[0].compile() + " " + expressionSymbol + " " + expressions[1].compile() + ")";
    }
}
