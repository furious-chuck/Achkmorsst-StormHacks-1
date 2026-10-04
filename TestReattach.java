import java.util.ArrayList;
import java.util.Arrays;

public class TestReattach {
    static int failures = 0;

    static void check(boolean cond, String msg) {
        if (!cond) { System.out.println("FAIL: " + msg); failures++; }
        else System.out.println("ok:   " + msg);
    }

    // Simulates a full drag lifecycle for an atomic packed somewhere in the tree:
    // press on the block -> detach (or grab its root statement) -> move to target
    // (mouse glued to the block centre) -> release. Mouse positions are kept in
    // screen space exactly like the real app: world == screen + cameraPos.
    static boolean simulateDragOutAndBack(BlockManager bm, BlockExpression atomic,
                                          double targetWorldX, double targetWorldY) {
        double camX = Global.cameraPos.getX(), camY = Global.cameraPos.getY();
        Vector start = atomic.getPosition();
        double grabOffX = atomic.getCascadingWidth() / 2.0;   // grab at block centre
        double grabOffY = atomic.getCascadingHeight() / 2.0;

        boolean wasNested = atomic.getParentExpression() != null;

        Inputs.mouseHeld = true;
        Inputs.mousePos = new RectVector(start.getX() - camX + grabOffX,
                                         start.getY() - camY + grabOffY);
        Inputs.handleDragStart();

        if (wasNested) {
            check(atomic.getParentExpression() == null, "atomic detached from parent");
            check(bm.expressions.contains(atomic), "atomic promoted to free root");
        }

        // mid-drag frame: mouse moved so the block's top-left lands on the target slot
        Inputs.mousePos = new RectVector(targetWorldX - camX + grabOffX,
                                         targetWorldY - camY + grabOffY);
        Inputs.handleDrag(bm);

        // release frame
        Inputs.mouseHeld = false;
        Inputs.handleDrag(bm);

        return atomic.getParentExpression() != null && !bm.expressions.contains(atomic);
    }

    public static void main(String[] args) {
        // ===== BLOCK 1: Assigner (ExpressionPackingStatement) =====
        {
            AtomicExpression.Numbers.IntNumber name = new AtomicExpression.Numbers.IntNumber(5);
            AtomicExpression.Numbers.DoubleNumber val = new AtomicExpression.Numbers.DoubleNumber(3.5);
            Assigner a = new Assigner(name, val);
            a.moveSelfAndAllChildrenTo(new RectVector(0, 0));
            BlockManager bm = new BlockManager(new ArrayList<>(Arrays.asList(a)),
                    new ArrayList<>());
            Display.activeBlockManager = bm;
            Vector p = val.getPosition();
            boolean ok = simulateDragOutAndBack(bm, val, p.getX(), p.getY());
            check(ok, "Assigner: atomic taken out and put back in");
            check(a.getChildExpressions()[1] == val, "Assigner: slot restored");
            check(val.getParentExpression() == a, "Assigner: back-link restored");
        }
        // ===== BLOCK 2: WhileLoop condition slot =====
        {
            AtomicExpression.Numbers.DoubleNumber cond = new AtomicExpression.Numbers.DoubleNumber(100.0);
            Statement body = new Assigner(new AtomicExpression.StringExpression(""),
                    new AtomicExpression.Numbers.DoubleNumber(1));
            WhileLoop wl = new WhileLoop(cond, body, null);
            wl.moveSelfAndAllChildrenTo(new RectVector(0, 0));
            BlockManager bm = new BlockManager(new ArrayList<>(Arrays.asList(wl)),
                    new ArrayList<>());
            Display.activeBlockManager = bm;
            Vector p = cond.getPosition();
            boolean ok = simulateDragOutAndBack(bm, cond, p.getX(), p.getY());
            check(ok, "WhileLoop: atomic taken out and put back in");
            check(wl.condition == cond, "WhileLoop: condition slot restored");
            check(cond.getParentExpression() == wl, "WhileLoop: back-link restored");
        }
        // ===== BLOCK 3: VarDeclaration middle slot =====
        {
            AtomicExpression.ClassBlock cls = new AtomicExpression.ClassBlock("potatopotato");
            AtomicExpression.Numbers.IntNumber name = new AtomicExpression.Numbers.IntNumber(10);
            AtomicExpression.Numbers.DoubleNumber val = new AtomicExpression.Numbers.DoubleNumber(10.1);
            VarDeclaration vd = new VarDeclaration(cls, name, val);
            vd.moveSelfAndAllChildrenTo(new RectVector(0, 0));
            BlockManager bm = new BlockManager(new ArrayList<>(Arrays.asList(vd)),
                    new ArrayList<>());
            Display.activeBlockManager = bm;
            Vector p = name.getPosition();
            boolean ok = simulateDragOutAndBack(bm, name, p.getX(), p.getY());
            check(ok, "VarDeclaration: atomic taken out and put back in");
            check(vd.getChildExpressions()[1] == name, "VarDeclaration: slot restored");
        }
        // ===== BLOCK 4: nested - atomic inside BinaryOperation inside Assigner slot =====
        {
            AtomicExpression.Numbers.IntNumber inner = new AtomicExpression.Numbers.IntNumber(42);
            BinaryOperation bin = new BinaryOperation(inner,
                    new AtomicExpression.Numbers.DoubleNumber(1.5), "+");
            Assigner host = new Assigner(new AtomicExpression.StringExpression("x"), bin);
            host.moveSelfAndAllChildrenTo(new RectVector(0, 0));
            BlockManager bm = new BlockManager(new ArrayList<>(Arrays.asList(host)),
                    new ArrayList<>());
            Display.activeBlockManager = bm;

            Vector p = inner.getPosition().clone();
            boolean ok = simulateDragOutAndBack(bm, inner, p.getX(), p.getY());
            check(ok, "Nested: atomic returns to BinaryOperation slot");
            check(inner.getParentExpression() == bin, "Nested: back-link points at BinaryOperation");
        }
        // ===== BLOCK 5: cross-container - atomic from Assigner into WhileLoop header =====
        {
            AtomicExpression.Numbers.DoubleNumber cond = new AtomicExpression.Numbers.DoubleNumber(100.0);
            Statement body = new Assigner(new AtomicExpression.StringExpression(""),
                    new AtomicExpression.Numbers.IntNumber(9));
            WhileLoop wl = new WhileLoop(cond, body, null);
            wl.moveSelfAndAllChildrenTo(new RectVector(0, 0));

            AtomicExpression.Numbers.DoubleNumber other = new AtomicExpression.Numbers.DoubleNumber(2.0);
            Assigner holder = new Assigner(other, new AtomicExpression.Numbers.IntNumber(1));
            holder.moveSelfAndAllChildrenTo(new RectVector(0, 400));

            BlockManager bm = new BlockManager(
                    new ArrayList<>(Arrays.asList(holder, wl)), new ArrayList<>());
            Display.activeBlockManager = bm;

            Vector cp = cond.getPosition();
            boolean ok = simulateDragOutAndBack(bm, other, cp.getX(), cp.getY());
            check(ok && wl.condition == other, "Cross: atomic landed in WhileLoop condition slot");
            check(other.getParentExpression() == wl, "Cross: back-link points at WhileLoop");
            check(bm.expressions.contains(cond), "Cross: displaced condition became a free root");
        }

        System.out.println(failures == 0 ? "ALL TESTS PASSED" : failures + " FAILURES");
        System.exit(failures == 0 ? 0 : 1);
    }
}
