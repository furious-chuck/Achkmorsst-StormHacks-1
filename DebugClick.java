import java.util.*;

public class DebugClick {
    static double camX = Global.cameraPos.getX(), camY = Global.cameraPos.getY();
    static BlockManager bm;

    static void press(double wx, double wy) {
        Inputs.mouseHeld = true;
        Inputs.mousePos = new RectVector(wx - camX, wy - camY);
        Inputs.handleDragStart();
    }
    static void move(double wx, double wy) {
        Inputs.mousePos = new RectVector(wx - camX, wy - camY);
        Inputs.handleDrag(bm);
    }
    static void release() {
        Inputs.mouseHeld = false;
        Inputs.handleDrag(bm);
    }
    static void report(String tag, BlockExpression e) {
        boolean vis = (e.getParentExpression() != null) || bm.expressions.contains(e);
        System.out.println("  " + tag + ": " + (vis ? "VISIBLE" : ">>> DISAPPEARED <<<")
            + " pos=(" + (int)e.getPosition().getX() + "," + (int)e.getPosition().getY() + ")");
    }

    public static void main(String[] args) {
        // Q: drag cls out of VarDeclaration and drop it back onto the SAME statement's empty slot
        System.out.println("== Q: cls out and back onto vd row ==");
        {
            AtomicExpression.ClassBlock cls = new AtomicExpression.ClassBlock("potatopotato");
            AtomicExpression.Numbers.IntNumber name = new AtomicExpression.Numbers.IntNumber(10);
            AtomicExpression.Numbers.DoubleNumber val = new AtomicExpression.Numbers.DoubleNumber(10.1);
            VarDeclaration vd = new VarDeclaration(cls, name, val);
            vd.moveSelfAndAllChildrenTo(new RectVector(0, 0));
            bm = new BlockManager(new ArrayList<>(Arrays.asList(vd)), new ArrayList<>());
            Display.activeBlockManager = bm;
            Vector p = cls.getPosition();
            press(p.getX()+5, p.getY()+5);
            move(p.getX()+40, p.getY()+30); // small drag, still over the vd row area
            release();
            report("cls", cls); report("name", name); report("val", val);
        }
        // R: same but with the parent MOVED after child detach? skip. Try instead: grab IntNumber in Assigner,
        // drop it into WhileLoop condition while loop is elsewhere -> displaced DoubleNumber cond must stay visible
        System.out.println("== R: int from assigner -> wl cond, displaced cond check ==");
        {
            AtomicExpression.Numbers.IntNumber nm = new AtomicExpression.Numbers.IntNumber(5);
            AtomicExpression.Numbers.DoubleNumber v = new AtomicExpression.Numbers.DoubleNumber(3.5);
            Assigner a = new Assigner(nm, v);
            a.moveSelfAndAllChildrenTo(new RectVector(0, 0));
            AtomicExpression.Numbers.DoubleNumber cond = new AtomicExpression.Numbers.DoubleNumber(100.0);
            Statement body = new Assigner(new AtomicExpression.StringExpression("b"), new AtomicExpression.Numbers.DoubleNumber(1));
            WhileLoop wl = new WhileLoop(cond, body, null);
            wl.moveSelfAndAllChildrenTo(new RectVector(0, 400));
            bm = new BlockManager(new ArrayList<>(Arrays.asList(a, wl)), new ArrayList<>());
            Display.activeBlockManager = bm;
            Vector p = nm.getPosition();
            Vector cp = cond.getPosition();
            press(p.getX()+5, p.getY()+5);
            move(cp.getX()+5, cp.getY()+5);
            release();
            report("nm(dropped)", nm); report("cond(displaced)", cond); report("v", v);
        }
        // S: drop block exactly onto its own original position via full drag path (not click)
        System.out.println("== S: drag cls away then back to exact original slot ==");
        {
            AtomicExpression.ClassBlock cls = new AtomicExpression.ClassBlock("potatopotato");
            AtomicExpression.Numbers.IntNumber name = new AtomicExpression.Numbers.IntNumber(10);
            AtomicExpression.Numbers.DoubleNumber val = new AtomicExpression.Numbers.DoubleNumber(10.1);
            VarDeclaration vd = new VarDeclaration(cls, name, val);
            vd.moveSelfAndAllChildrenTo(new RectVector(0, 0));
            bm = new BlockManager(new ArrayList<>(Arrays.asList(vd)), new ArrayList<>());
            Display.activeBlockManager = bm;
            Vector p = cls.getPosition();
            press(p.getX()+5, p.getY()+5);
            move(p.getX()+200, p.getY()+200);
            move(p.getX()+5, p.getY()+5); // back to start
            release();
            report("cls", cls); report("name", name); report("val", val);
        }
    }
}
