import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {

        /*
        BlockStatement bs = new VarDeclaration(new AtomicExpression.ClassBlock("int"), new AtomicExpression.VarBlock("x"), new AtomicExpression.Numbers.IntNumber(10));
        BlockStatement ba = new Assigner(new AtomicExpression.StringExpression("second"), new AtomicExpression.Numbers.DoubleNumber(1021));
        BlockStatement bt = new Assigner(new AtomicExpression.StringExpression("third"), new AtomicExpression.Numbers.DoubleNumber(9));
        BlockStatement bo = new Assigner(new AtomicExpression.StringExpression("next"), new AtomicExpression.Numbers.DoubleNumber(1022));

        bt.connectNextStatement(bo);
        ba.connectNextStatement(bt);

        BlockStatement b1 = new Assigner(new AtomicExpression.VarBlock("x"), new AtomicExpression.Numbers.IntNumber(10), "+");
        bs.connectNextStatement(b1);
        */
       /*
        BlockExpression be = new AtomicExpression.Boolean.True();
        BlockStatement bs = new Assigner(new AtomicExpression.VarBlock("x"), new AtomicExpression.StringExpression("Hello"));
        BlockStatement ba = new Assigner(new AtomicExpression.StringExpression("second"), new AtomicExpression.Numbers.DoubleNumber(1021));
        BlockStatement bsba = new Assigner(new AtomicExpression.StringExpression("third"), new AtomicExpression.Numbers.DoubleNumber(9));
        bs.connectNextStatement(bsba);
        BlockStatement we = new WhileLoop(be, bs, ba);
        */
        /*
        BlockStatement as = new VarDeclaration(
                new AtomicExpression.ClassBlock("int"),
                new AtomicExpression.VarBlock("x"),
                new AtomicExpression.Numbers.IntNumber(10)
        );
         */
        BlockStatement bs = new VarDeclaration(new AtomicExpression.ClassBlock("int"), new AtomicExpression.VarBlock("x"), new AtomicExpression.Numbers.IntNumber(10));
        BlockManager bm = new BlockManager(
                new ArrayList<BlockStatement>(Arrays.asList(new BlockStatement[] { bs })),
                new ArrayList<BlockExpression>(Arrays.asList(new BlockExpression[] {}))
        );

        Clock c = new Clock(10);

        // give the display a reference to the block manager so each frame it can
        // detect which block (if any) the mouse is hovering over and print it
        Display.activeBlockManager = bm;
        for (int i = 0; i < 1080; i++) {
            Global.display.paintQueue = new Paintable[] { bm };
            Global.display.update();
            c.tick();
            // bm.statements.get(0).moveSelfAndAllChildrenBy(new RectVector(1, 0));
        }

    }

}
