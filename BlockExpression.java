import java.awt.*;

public abstract class BlockExpression implements Expression, Paintable {

    Vector position; // note: position should be disregarded whenever this is attached to something else!

    public Color mainColor;
    public Color secondaryColor;
    public Color outlineColor;
    public Color textColor;

}
