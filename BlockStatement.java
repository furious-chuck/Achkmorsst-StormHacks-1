import java.awt.*;

public abstract class BlockStatement extends CascadeCompiledStatement implements Paintable {

    Vector position; // note: position should be disregarded whenever this is attached to something else!

    public Color mainColor;
    public Color outlineColor;
    public Color textColor;

    public String text;
    public Expression[] childExpressions;

    // public double getMaxChildHeight

    @Override
    public Expression[] getChildExpressions() {
        return childExpressions;
    }

}
