import java.awt.*;
import java.util.ArrayList;

public class BlockManager implements Paintable {


    ArrayList<BlockStatement> statements;
    ArrayList<BlockExpression> expressions;
    SideBar sidebar = new SideBar();


    public BlockManager(ArrayList<BlockStatement> statements, ArrayList<BlockExpression> expressions) {
        this.statements = statements;
        this.expressions = expressions;
    }


    public void update() {

    }


    @Override
    public void paint(Graphics g) {
        sidebar.paint(g);
        for (BlockStatement bs : statements) {
            bs.paint(g);
        }
        for (BlockExpression be : expressions) {
            be.paint(g);
        }
    }
}
