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
        
        BlockStatement decl = new VarDeclaration(new AtomicExpression.ClassBlock("int"), new AtomicExpression.VarBlock("x"), new AtomicExpression.Numbers.IntNumber(0));
        WhileLoop whloop = new WhileLoop(null, null, null);
        BlockExpression xLessThan10 = new BinaryOperation(new Expression[] {new AtomicExpression.VarBlock("x"), new AtomicExpression.Numbers.IntNumber(10)}, "<");
        BlockStatement printer = new PrintCall(new AtomicExpression.VarBlock("x"));
        BlockStatement increaser = new Assigner(new AtomicExpression.VarBlock("x"), null, "+");
        BlockExpression one = new AtomicExpression.Numbers.IntNumber(1);
        
        increaser.setChildElement(1, one);
        printer.connectNextStatement(increaser);
        whloop.connectConnectedStatement(printer);
        whloop.setChildElement(0, xLessThan10);
        decl.connectNextStatement(whloop);

        BlockManager bm = new BlockManager(
                new ArrayList<BlockStatement>(Arrays.asList(new BlockStatement[] { decl })),
                new ArrayList<BlockExpression>(Arrays.asList(new BlockExpression[] {  }))
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
