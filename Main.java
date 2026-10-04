import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {

        BlockStatement be = new VarDeclaration(new AtomicExpression.ClassBlock("potatopotato"), new AtomicExpression.Numbers.IntNumber(10), new AtomicExpression.Numbers.DoubleNumber(10.1));
        BlockStatement ba = new Assigner(new AtomicExpression.StringExpression(""), new AtomicExpression.Numbers.DoubleNumber(1021));
        BlockStatement bo = new Assigner(new AtomicExpression.StringExpression(""), new AtomicExpression.Numbers.DoubleNumber(1022));

        be.connectNextStatement(ba);

        BlockStatement we = new WhileLoop(new AtomicExpression.Numbers.DoubleNumber(100.0), be, bo);

        BlockManager bm = new BlockManager(
                new ArrayList<BlockStatement>(Arrays.asList(new BlockStatement[] { we })),
                new ArrayList<BlockExpression>(Arrays.asList(new BlockExpression[] {}))
        );

        Clock c = new Clock(10);
        

        for (int i = 0; i < 1080; i++) {
            Global.display.paintQueue = new Paintable[] { bm };
            Global.display.update();
            c.tick();
            bm.statements.get(0).moveSelfAndAllChildrenBy(new RectVector(1, 0));
        }

    }

}
