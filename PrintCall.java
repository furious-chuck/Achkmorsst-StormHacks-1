import java.awt.Color;
import java.awt.Graphics;

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

    @Override 
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.BLACK);
        g.drawString("print", (int) (position.getX() - Global.cameraPos.getX()) + 10, (int) (position.getY() - Global.cameraPos.getY()) + 10 + Consts.LETTER_HEIGHT);
    }
    
}
