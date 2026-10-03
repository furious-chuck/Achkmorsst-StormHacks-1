import java.awt.*;

public abstract class BlockStatement extends CascadeCompiledStatement implements Paintable {

    public Color mainColor;
    public Color outlineColor;
    public Color textColor;

    public String text;
    public Expression[] childExpressions;

    public Expression[] getChildExpressions() {
        return childExpressions;
    }

    @Override
    public void paint(Graphics g) {

    }
}
