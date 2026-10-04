import java.util.ArrayList;
import java.util.Arrays;

/**
 * Reproduces the "hold the mouse down on an atomic and it disappears" bug:
 * press on a packed atomic, keep the button held for several frames without
 * moving (a plain click-and-hold), then release. The block must never vanish:
 * while held it should be visible (either still in its slot or as a dragged
 * copy glued to the cursor) and after release it must be back in its slot.
 */
public class TestHoldStill {
    static int failures = 0;

    static void check(boolean cond, String msg) {
        if (!cond) { System.out.println("FAIL: " + msg); failures++; }
        else System.out.println("ok:   " + msg);
    }

    /**
     * True if paint() would actually draw `target` this frame: either it is a
     * free-floating root in the manager, or it is packed inside a container that
     * recursively walks down to it (statements paint their whole slot layout and
     * following-statement chains; packing expressions paint their children).
     */
    static boolean isPainted(BlockManager bm, BlockExpression target) {
        for (BlockStatement bs : bm.statements) {
            if (statementDraws(bs, target)) return true;
        }
        for (BlockExpression be : bm.expressions) {
            if (be == target || expressionDraws(be, target)) return true;
        }
        return false;
    }

    static boolean statementDraws(BlockStatement bs, BlockExpression target) {
        if (bs instanceof ExpressionPackingStatement eps) {
            for (BlockExpression child : eps.expressions) {
                if (child == null) continue;
                if (child == target || expressionDraws(child, target)) return true;
            }
        } else {
            for (Expression exp : bs.getChildExpressions()) {
                if (exp instanceof BlockExpression be
                        && (be == target || expressionDraws(be, target))) {
                    return true;
                }
            }
        }
        if (bs instanceof StatementContainer sc
                && sc.getContainedStatement() instanceof BlockStatement inner
                && inner != bs && statementDraws(inner, target)) {
            return true;
        }
        if (bs.getFollowingStatement() instanceof BlockStatement next) {
            return statementDraws(next, target);
        }
        return false;
    }

    static boolean expressionDraws(BlockExpression be, BlockExpression target) {
        for (Expression exp : be.getChildExpressions()) {
            if (exp instanceof BlockExpression child
                    && (child == target || expressionDraws(child, target))) {
                return true;
            }
        }
        return false;
    }

    /** true if the block is logically attached somewhere (slot or free root) */
    static boolean isManaged(BlockManager bm, BlockExpression target) {
        if (target.getParentExpression() != null) return true;
        return bm.expressions.contains(target) || bm.statements.contains(target);
    }

    /**
     * Simulates one press-release cycle at world point (wx, wy). Returns a short
     * description of every atomic that became invisible/unmanaged afterwards.
     */
    static String checkInvisibleAfterPressRelease(BlockManager bm, double wx, double wy) {
        double camX = Global.cameraPos.getX(), camY = Global.cameraPos.getY();
        Inputs.mouseHeld = true;
        Inputs.mousePos = new RectVector(wx - camX, wy - camY);
        Inputs.handleDragStart();
        for (int frame = 0; frame < 3; frame++) {
            Inputs.handleDrag(bm);
        }
        Inputs.mouseHeld = false;
        Inputs.handleDrag(bm);

        StringBuilder bad = new StringBuilder();
        java.util.HashSet<BlockExpression> all = new java.util.HashSet<>();
        collect(bm, all);
        for (BlockExpression be : all) {
            if (!isPainted(bm, be) || !isManaged(bm, be)) {
                bad.append(" ").append(be.getBlockName()).append("@")
                   .append((int) be.getPosition().getX()).append(",")
                   .append((int) be.getPosition().getY()).append(";");
            }
        }
        return bad.toString();
    }

    static void collect(BlockManager bm, java.util.HashSet<BlockExpression> out) {
        for (BlockStatement bs : bm.statements) collect(bs, out);
        for (BlockExpression be : bm.expressions) { out.add(be); collect(be, out); }
    }
    static void collect(BlockStatement bs, java.util.HashSet<BlockExpression> out) {
        if (bs instanceof ExpressionPackingStatement eps) {
            for (BlockExpression child : eps.expressions) {
                if (child != null) { out.add(child); collect(child, out); }
            }
        } else {
            for (Expression exp : bs.getChildExpressions()) {
                if (exp instanceof BlockExpression be) { out.add(be); collect(be, out); }
            }
        }
        if (bs instanceof StatementContainer sc
                && sc.getContainedStatement() instanceof BlockStatement inner
                && inner != bs) collect(inner, out);
        if (bs.getFollowingStatement() instanceof BlockStatement next) collect(next, out);
    }
    static void collect(BlockExpression be, java.util.HashSet<BlockExpression> out) {
        for (Expression exp : be.getChildExpressions()) {
            if (exp instanceof BlockExpression child) { out.add(child); collect(child, out); }
        }
    }

    static void scenario(String name, BlockManager bm, BlockExpression atomic) {
        double camX = Global.cameraPos.getX(), camY = Global.cameraPos.getY();
        Vector start = atomic.getPosition().clone();
        double grabOffX = atomic.getCascadingWidth() / 2.0;
        double grabOffY = atomic.getCascadingHeight() / 2.0;
        double screenX = start.getX() - camX + grabOffX;
        double screenY = start.getY() - camY + grabOffY;

        // --- press exactly on the atomic ---
        Inputs.mouseHeld = true;
        Inputs.mousePos = new RectVector(screenX, screenY);
        Inputs.handleDragStart();

        // --- hold still for several frames (no movement at all) ---
        for (int frame = 0; frame < 5; frame++) {
            Inputs.handleDrag(bm);
            check(isPainted(bm, atomic), name + ": visible while held (frame " + frame + ")");
            check(isManaged(bm, atomic), name + ": managed while held (frame " + frame + ")");
        }

        // --- release without having moved ---
        Inputs.mouseHeld = false;
        Inputs.handleDrag(bm);

        check(isPainted(bm, atomic), name + ": visible after release");
        check(isManaged(bm, atomic), name + ": managed after release");
        check(atomic.getParentExpression() != null, name + ": reattached to a parent after release");
        Vector end = atomic.getPosition();
        check(Math.abs(end.getX() - start.getX()) < 0.5 && Math.abs(end.getY() - start.getY()) < 0.5,
                name + ": ended up back at its original slot position");
    }

    public static void main(String[] args) {
        // scenario A: Assigner slots (like the user's program)
        {
            AtomicExpression.StringExpression lhs = new AtomicExpression.StringExpression("");
            AtomicExpression.Numbers.DoubleNumber rhs = new AtomicExpression.Numbers.DoubleNumber(1021);
            Assigner a = new Assigner(lhs, rhs);
            a.moveSelfAndAllChildrenTo(new RectVector(0, 0));
            BlockManager bm = new BlockManager(new ArrayList<>(Arrays.asList(a)), new ArrayList<>());
            Display.activeBlockManager = bm;
            scenario("Assigner-rhs", bm, rhs);
        }
        // scenario B: VarDeclaration middle slot
        {
            AtomicExpression.ClassBlock cls = new AtomicExpression.ClassBlock("potatopotato");
            AtomicExpression.Numbers.IntNumber name = new AtomicExpression.Numbers.IntNumber(10);
            AtomicExpression.Numbers.DoubleNumber val = new AtomicExpression.Numbers.DoubleNumber(10.1);
            VarDeclaration vd = new VarDeclaration(cls, name, val);
            vd.moveSelfAndAllChildrenTo(new RectVector(0, 0));
            BlockManager bm = new BlockManager(new ArrayList<>(Arrays.asList(vd)), new ArrayList<>());
            Display.activeBlockManager = bm;
            scenario("VarDeclaration-name", bm, name);
        }
        // scenario C: nested atomic inside a BinaryOperation inside a statement slot
        {
            AtomicExpression.Numbers.IntNumber inner = new AtomicExpression.Numbers.IntNumber(42);
            BinaryOperation bin = new BinaryOperation(inner,
                    new AtomicExpression.Numbers.DoubleNumber(1.5), "+");
            Assigner host = new Assigner(new AtomicExpression.StringExpression("x"), bin);
            host.moveSelfAndAllChildrenTo(new RectVector(0, 0));
            BlockManager bm = new BlockManager(new ArrayList<>(Arrays.asList(host)), new ArrayList<>());
            Display.activeBlockManager = bm;
            scenario("Nested-inner", bm, inner);
        }
        // scenario D: WhileLoop condition
        {
            AtomicExpression.Numbers.DoubleNumber cond = new AtomicExpression.Numbers.DoubleNumber(100.0);
            Statement body = new Assigner(new AtomicExpression.StringExpression(""),
                    new AtomicExpression.Numbers.DoubleNumber(1));
            WhileLoop wl = new WhileLoop(cond, body, null);
            wl.moveSelfAndAllChildrenTo(new RectVector(0, 0));
            BlockManager bm = new BlockManager(new ArrayList<>(Arrays.asList(wl)), new ArrayList<>());
            Display.activeBlockManager = bm;
            scenario("WhileLoop-cond", bm, cond);
        }

        System.out.println(failures == 0 ? "ALL TESTS PASSED" : failures + " FAILURES");
        System.exit(failures == 0 ? 0 : 1);
    }
}
