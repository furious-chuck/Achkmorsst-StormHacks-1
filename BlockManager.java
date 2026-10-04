import java.awt.*;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

public class BlockManager implements Paintable {


    ArrayList<BlockStatement> statements;
    ArrayList<BlockExpression> expressions;
    SideBar sidebar = new SideBar();


    public BlockManager(ArrayList<BlockStatement> statements, ArrayList<BlockExpression> expressions) {
        this.statements = statements;
        this.expressions = expressions;
    }

    public boolean canCompile() {
        return statements.size() == 1 && expressions.isEmpty();
    }

    public void compileOnce() {
        if (!canCompile()) Util.unableToCan();
        // todo: finish this method
    }

    public void compileTwice() {
        compileOnce();
        Terminal.executeCommand("javac Compiled.java");
        //todo: output result (new display?), add way to fail.
    }

    public void compileAndRun() {
        compileTwice();
        Terminal.executeCommand("java Compiled");
        //todo: output result (new display?), add way to fail.
    }

    // todo: finis the compileTwice function that compiles .java file as well.
    // todo: finish compileAndRun function that also runs the program and displays the output.
    // todo: create buttons for each.

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
