import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

public class BlockManager implements Paintable {


    ArrayList<BlockStatement> statements;
    ArrayList<BlockExpression> expressions;
    SideBar sidebar = new SideBar();


    StaticScreenPosButton[] buttons = new StaticScreenPosButton[] {
            new StaticScreenPosButton(new RectVector(225, 75), "To .java"),
            new StaticScreenPosButton(new RectVector(325, 75), "To .class"),
            new StaticScreenPosButton(new RectVector(425, 75), "Run!")
    };


    public BlockManager(ArrayList<BlockStatement> statements, ArrayList<BlockExpression> expressions) {
        this.statements = statements;
        this.expressions = expressions;
    }

    public boolean canCompile() {
        return (statements.size() == 1) && (expressions.isEmpty());
    }

    public void compileOnce() {
        if (!canCompile()) Util.unableToCan();
        File compiledFile = new File("CompiledFiles/Compiled.java");
        try {
            boolean success = compiledFile.createNewFile();
            if (!success) {
                compiledFile.delete();
                compiledFile.createNewFile();
            }
        } catch (IOException e) {
            Util.unableToCan(e.getMessage());
        }

        String compilationResult = "public class Compiled {\npublic static void main(String[] args) {\n";
        compilationResult += statements.getFirst().cascadeCompile();
        compilationResult += "}\n}";
        try {
            Files.write(Paths.get("CompiledFiles/Compiled.java"), compilationResult.getBytes());
        } catch (IOException e) {
            Util.unableToCan();
        }
    }

    public void compileTwice() {
        compileOnce();
        Terminal.executeCommand("javac CompiledFiles/Compiled.java");
        //todo: output result (new display?), add way to fail.
    }

    public void compileAndRun() {
        compileTwice();
        Terminal.executeCommand("java CompiledFiles/Compiled");
        //todo: output result (new display?), add way to fail.
        Global.display.pushMessage("--------------");
        Global.display.pushMessage("Running your code...");
    }

    // todo: finis the compileTwice function that compiles .java file as well.
    // todo: finish compileAndRun function that also runs the program and displays the output.
    // todo: create buttons for each.

    public void update() {
        if (Inputs.mousePressed) {
            if (buttons[0].isHoveredOver()) {
                System.out.println(1);
                compileOnce();
            } else if (buttons[1].isHoveredOver()) {
                compileTwice();
            } else if (buttons[2].isHoveredOver()) {
                compileAndRun();
            }
        }
    }

    private static class StaticScreenPosButton implements Paintable {
        Vector position;
        Vector size;
        Vector corner;
        String text;
        Vector stringOffset;

        public StaticScreenPosButton(Vector position, String text) {
            this.position = position;
            this.size = new RectVector(text.length() * Consts.LETTER_WIDTH + 20, Consts.LETTER_HEIGHT + 20);
            this.stringOffset = position.add(new RectVector(10, 10 + Consts.LETTER_HEIGHT));
            this.text = text;
            corner = position.add(size);
        }

        @Override
        public void paint(Graphics g) {
            g.setColor(Color.GREEN);
            g.fillRect((int) position.getX(), (int) position.getY(), (int) size.getX(), (int) size.getY());
            g.setColor(Color.BLACK);
            g.drawRect((int) position.getX(), (int) position.getY(), (int) size.getX(), (int) size.getY());
            g.drawString(text, (int) stringOffset.getX(), (int) stringOffset.getY());
        }

        public boolean isHoveredOver() {
            return (
                    position.getX() < Inputs.mousePos.getX() && Inputs.mousePos.getX() < corner.getX() &&
                    position.getY() < Inputs.mousePos.getY() && Inputs.mousePos.getY() < corner.getY()
            );
        }
    }


    @Override
    public void paint(Graphics g) {
        sidebar.paint(g);

        update();

        for (StaticScreenPosButton button : buttons) {
            button.paint(g);
        }

        for (BlockStatement bs : statements) {
            bs.paint(g);
        }
        for (BlockExpression be : expressions) {
            be.paint(g);
        }
    }
}
