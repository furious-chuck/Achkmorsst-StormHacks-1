import java.util.ArrayList;
import java.util.Arrays;

/**
 * Reproduces the "blocks stay connected in the backend after being moved apart"
 * bug: drag one statement out of a vertically connected stack and drop it far
 * away. Visually the blocks are no longer touching, but the backend must also
 * forget the connection (followingStatement / cascadeCompile), otherwise the
 * moved block is still compiled as part of the original program.
 */
public class TestMoveApart {
    static int failures = 0;

    static void check(boolean cond, String msg) {
        if (!cond) { System.out.println("FAIL: " + msg); failures++; }
        else System.out.println("ok:   " + msg);
    }

    /** true if `block` sits somewhere inside the statement chain rooted at bs */
    static boolean isInChain(BlockStatement bs, BlockStatement block) {
        for (BlockStatement cur = bs; cur != null;
                 cur = cur.getFollowingStatement() instanceof BlockStatement n ? n : null) {
            if (cur == block) return true;
        }
        return false;
    }

    static boolean chainContains(BlockManager bm, BlockStatement block) {
        for (BlockStatement root : bm.statements) {
            if (isInChain(root, block)) return true;
        }
        return false;
    }

    public static void main(String[] args) {
        double camX = Global.cameraPos.getX(), camY = Global.cameraPos.getY();

        // ===== CASE 1: middle block pulled out of an A->B->C stack =====
        {
            Assigner a = new Assigner(new AtomicExpression.StringExpression("a"),
                    new AtomicExpression.Numbers.IntNumber(1));
            Assigner b = new Assigner(new AtomicExpression.StringExpression("b"),
                    new AtomicExpression.Numbers.IntNumber(2));
            Assigner c = new Assigner(new AtomicExpression.StringExpression("c"),
                    new AtomicExpression.Numbers.IntNumber(3));
            a.connectNextStatement(b);
            b.connectNextStatement(c);
            a.moveSelfAndAllChildrenTo(new RectVector(0, 0));

            BlockManager bm = new BlockManager(new ArrayList<>(Arrays.asList(a)),
                    new ArrayList<>());
            Display.activeBlockManager = bm;

            double grabX = b.position.getX() + b.getCascadingWidth() / 2.0;
            double grabY = b.position.getY() + b.getCascadingHeight() / 2.0;
            Inputs.mouseHeld = true;
            Inputs.mousePos = new RectVector(grabX - camX, grabY - camY);
            Inputs.handleDragStart();

            // move B far below everything so it clearly touches nothing
            double destX = 500, destY = 900;
            Inputs.mousePos = new RectVector(destX - camX + b.getCascadingWidth() / 2.0,
                                             destY - camY + b.getCascadingHeight() / 2.0);
            Inputs.handleDrag(bm);
            Inputs.mouseHeld = false;
            Inputs.handleDrag(bm);

            check(!chainContains(bm, b), "middle block no longer reachable from any root chain");
            check(a.getFollowingStatement() != b, "A is not connected to B anymore");
            check(!(b.getFollowingStatement() instanceof BlockStatement)
                    || b.getFollowingStatement() != c, "B is not connected to C anymore");
            check(bm.statements.contains(b), "moved block became its own root statement");
            check(Math.abs(b.position.getY() - destY) < 1.0, "dropped block stayed where released");
        }

        // ===== CASE 2: bottom block dragged away from A->B =====
        {
            Assigner a = new Assigner(new AtomicExpression.StringExpression("a"),
                    new AtomicExpression.Numbers.IntNumber(1));
            Assigner b = new Assigner(new AtomicExpression.StringExpression("b"),
                    new AtomicExpression.Numbers.IntNumber(2));
            a.connectNextStatement(b);
            a.moveSelfAndAllChildrenTo(new RectVector(0, 0));

            BlockManager bm = new BlockManager(new ArrayList<>(Arrays.asList(a)),
                    new ArrayList<>());
            Display.activeBlockManager = bm;

            double grabX = b.position.getX() + b.getCascadingWidth() / 2.0;
            double grabY = b.position.getY() + b.getCascadingHeight() / 2.0;
            Inputs.mouseHeld = true;
            Inputs.mousePos = new RectVector(grabX - camX, grabY - camY);
            Inputs.handleDragStart();

            Inputs.mousePos = new RectVector(600 - camX + b.getCascadingWidth() / 2.0,
                                             700 - camY + b.getCascadingHeight() / 2.0);
            Inputs.handleDrag(bm);
            Inputs.mouseHeld = false;
            Inputs.handleDrag(bm);

            check(a.getFollowingStatement() != b, "top block forgot the block dragged away");
            check(chainContains(bm, b) && !isInChain(a, b), "detached block lives as its own root");
        }

        // ===== CASE 3: re-drop the moved block back under the stack -> reconnect =====
        {
            Assigner a = new Assigner(new AtomicExpression.StringExpression("a"),
                    new AtomicExpression.Numbers.IntNumber(1));
            Assigner b = new Assigner(new AtomicExpression.StringExpression("b"),
                    new AtomicExpression.Numbers.IntNumber(2));
            a.connectNextStatement(b);
            a.moveSelfAndAllChildrenTo(new RectVector(0, 0));

            BlockManager bm = new BlockManager(new ArrayList<>(Arrays.asList(a)),
                    new ArrayList<>());
            Display.activeBlockManager = bm;

            // drag B away
            double grabX = b.position.getX() + b.getCascadingWidth() / 2.0;
            double grabY = b.position.getY() + b.getCascadingHeight() / 2.0;
            Inputs.mouseHeld = true;
            Inputs.mousePos = new RectVector(grabX - camX, grabY - camY);
            Inputs.handleDragStart();
            Inputs.mousePos = new RectVector(600 - camX + b.getCascadingWidth() / 2.0,
                                             700 - camY + b.getCascadingHeight() / 2.0);
            Inputs.handleDrag(bm);
            Inputs.mouseHeld = false;
            Inputs.handleDrag(bm);

            // now drag it right back underneath A so their edges snap together
            double targetY = a.position.getY() + a.getCascadingHeight();
            double targetX = a.position.getX();
            Inputs.mouseHeld = true;
            Inputs.mousePos = new RectVector(b.position.getX() + b.getCascadingWidth() / 2.0 - camX,
                                             b.position.getY() + b.getCascadingHeight() / 2.0 - camY);
            Inputs.handleDragStart();
            Inputs.mousePos = new RectVector(targetX + b.getCascadingWidth() / 2.0 - camX,
                                             targetY + b.getCascadingHeight() / 2.0 - camY);
            Inputs.handleDrag(bm);
            Inputs.mouseHeld = false;
            Inputs.handleDrag(bm);

            check(a.getFollowingStatement() == b, "re-dropped block reconnects to the stack");
            check(isInChain(bm.statements.get(0), b), "connected block is part of A's chain again");
        }

        System.out.println(failures == 0 ? "ALL TESTS PASSED" : failures + " FAILURES");
        System.exit(failures == 0 ? 0 : 1);
    }
}
