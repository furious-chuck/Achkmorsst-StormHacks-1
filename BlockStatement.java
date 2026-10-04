import java.awt.*;
import java.util.ArrayList;

public abstract class BlockStatement extends CascadeCompiledStatement implements Paintable, Hoverable {

    Vector position = new RectVector(); // note: position should be disregarded whenever this is attached to something else!

    public Color mainColor;
    public Color outlineColor;
    public Color textColor;

    public String text;
    public Expression[] childExpressions;

    @Override
    public Vector getPosition() {
        return position;
    }

    /**
     * Statement roots have no parent block, so they are their own root ancestor.
     */
    @Override
    public Hoverable getParentBlock() {
        return null;
    }

    @Override
    public String getBlockName() {
        return getClass().getSimpleName();
    }

    // public double getMaxChildHeight

    @Override
    public Expression[] getChildExpressions() {
        // never hand out a raw null array: callers iterate this contractually, and
        // statements whose constructors don't populate childExpressions would
        // otherwise blow up with an NPE mid-drag. Holes (nulls left by detached
        // children) are preserved so slot indices stay aligned with setChildElement.
        if (childExpressions == null || childExpressions.length == 0) {
            // expression-packing statements (Assigner, VarDeclaration, ...) keep their
            // real slots in the `expressions` array; report those instead of the empty
            // field above so detaching an atomic can find (and blank) its slot. Without
            // this, the parent kept painting a clone of the block inside its own shape
            // while the dragged copy floated free, and never resized to fit.
            if (this instanceof ExpressionPackingStatement eps) {
                return eps.expressions;
            }
            if (childExpressions == null) {
                return new Expression[0];
            }
        }
        return childExpressions;
    }

    /**
     * Finds the slot index of the given expression in this statement's direct child
     * list. First checks the explicit childExpressions array (which may contain null
     * holes after a detach), then falls back to the overridden getChildExpressions()
     * contract used by containers like WhileLoop that store children in named fields.
     */
    protected int indexOfOwnChildExpression(Expression target) {
        if (target == null) {
            return -1;
        }
        if (childExpressions != null) {
            for (int i = 0; i < childExpressions.length; i++) {
                if (childExpressions[i] == target) {
                    return i;
                }
            }
        }
        Expression[] kids = getChildExpressions();
        for (int i = 0; i < kids.length; i++) {
            if (kids[i] == target) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Removes the given child expression from this statement (clearing its slot and
     * parent link) without affecting anything else. Default implementation works for
     * statements that expose their children via getChildExpressions()/setChildElement;
     * subclasses that keep children in private fields (Assigner) override this.
     * Returns true if the child was found and removed.
     */
    public boolean removeDirectChildExpression(BlockExpression child) {
        // for expression-packing statements (Assigner, VarDeclaration, ...) the real
        // slots live in the `expressions` array, not in the (usually empty)
        // childExpressions field, so search those first. Without this, detaching an
        // atomic out of such a statement would find no slot to clear and leave a
        // clone of the block painted inside the parent while the original floats free.
        if (this instanceof ExpressionPackingStatement eps) {
            for (int i = 0; i < eps.expressions.length; i++) {
                if (eps.expressions[i] == child) {
                    eps.setChildElement(i, null); // blanks the slot and clears the back-link
                    if (child.getParentExpression() == this) {
                        child.parentExpression = null;
                    }
                    return true;
                }
            }
            return false;
        }
        int index = indexOfOwnChildExpression(child);
        if (index < 0) {
            return false;
        }
        setChildElement(index, null);
        // containers whose setChildElement doesn't clear the back-link themselves
        // should still leave the child detached; double-clearing is harmless.
        if (child.getParentExpression() == this) {
            child.parentExpression = null;
        }
        return true;
    }

    /**
     * Returns the slot index of an empty (null) child-expression slot, or -1 if all
     * slots are filled. Used to snap a dragged atomic back into the first hole its
     * parent left behind when it was detached.
     */
    public int getFirstEmptyChildSlot() {
        // packed statements keep their slots in `expressions`, not `childExpressions`
        if (this instanceof ExpressionPackingStatement eps) {
            for (int i = 0; i < eps.expressions.length; i++) {
                if (eps.expressions[i] == null) {
                    return i;
                }
            }
            return -1;
        }
        Expression[] kids = getChildExpressions();
        if (kids != null) {
            for (int i = 0; i < kids.length; i++) {
                if (kids[i] == null) {
                    return i;
                }
            }
        }
        return -1;
    }

    public abstract double getCascadingHeight();
    public abstract double getCascadingWidth();

    /**
     * Returns true if the given world-space point lies within this block's bounding
     * rectangle (position .. position + cascading size). Children are excluded: they
     * are checked separately so the deepest (most specific) block wins.
     */
    @Override
    public boolean containsPoint(Vector p) {
        return p.getX() >= position.getX() &&
               p.getX() <= position.getX() + getCascadingWidth() &&
               p.getY() >= position.getY() &&
               p.getY() <= position.getY() + getCascadingHeight();
    }

    /**
     * Finds the innermost hoverable block under the given world-space mouse position.
     * The child expressions are laid out relative to this statement at paint time, so
     * we recompute their current slots here (same convention as paint()) and search
     * them first, so the deepest/most specific block wins. Then we check our own shape,
     * and finally walk down the connected following-statement chain.
     * Returns null if the mouse is not hovering over any block.
     */
    @Override
    public Hoverable findHoveredBlock(Vector mousePos) {
        // children first — but only when the point is actually inside OUR bounds: a
        // dragged atomic floating over another statement's row must not be treated as
        // a child of that statement (it isn't packed in it), otherwise the hover/drop
        // search reports the foreign child instead of the container underneath it.
        if (containsPoint(mousePos)) {
            Hoverable found = findHoveredChild(mousePos);
            if (found != null) {
                return found;
            }
            return this;
        }
        // finally, any statement connected below us
        if (followingStatement instanceof BlockStatement bs) {
            return bs.findHoveredBlock(mousePos);
        }
        return null;
    }

    /**
     * Same search as findHoveredBlock(), but WITHOUT walking into the following-statement
     * chain: only this statement (its child slots and its own shape) is considered.
     * Dragging uses this so that a root statement's hit area stops at its own bounds —
     * otherwise every statement in a connected stack would claim the points belonging
     * to the statements below it, and each duplicate hit would win the "smallest area"
     * contest against the real target, making lower blocks impossible to drop onto.
     */
    public Hoverable findHoveredHere(Vector mousePos) {
        if (containsPoint(mousePos)) {
            Hoverable found = findHoveredChild(mousePos);
            if (found != null) {
                return found;
            }
            return this;
        }
        return null;
    }

    /**
     * The world-space rectangle paint() actually draws the child at `expression` in,
     * following this statement's slot layout (left-to-right starting at leftSpace,
     * verSpace below the top edge — the exact convention paint() uses). Returns null
     * when the given expression is not one of this statement's packed children.
     */
    private Vector paintedSlotPositionOf(BlockExpression expression) {
        if (this instanceof ExpressionPackingStatement eps) {
            double elementOffset = eps.leftSpace;
            for (int i = 0; i < eps.expressions.length; i++) {
                BlockExpression child = eps.expressions[i];
                if (child == expression) {
                    return position.add(new RectVector(elementOffset, 10));
                }
                elementOffset += (child == null ? eps.defaultSize.getX()
                        : child.getCascadingWidth()) + eps.expressionSpacing;
            }
            return null;
        }
        // non-packing statements (e.g. WhileLoop) lay their children out via
        // setChildElement, which keeps each child's stored position in sync with the
        // layout — but only while the child is actually packed here; a foreign block
        // must not be claimed at all
        for (Expression exp : getChildExpressions()) {
            if (exp == expression) {
                return expression.getPosition();
            }
        }
        return null;
    }

    /**
     * Searches this statement's packed child slots for the deepest block under the
     * given world-space point. Hit-testing follows paint(): a child is only ever hit
     * at the rectangle this statement actually draws it in (slot positions are
     * recomputed from our current position, exactly like paint() and the drag-restore
     * helpers do). This deliberately ignores the child's stale stored position, which
     * is what keeps a free-floating (detached/dragged) atomic that visually overlaps
     * this row from being mistaken for a genuine child of this container — such a
     * block is no longer packed here, so hitting it must never hide the container
     * underneath it.
     */
    public Hoverable findHoveredChild(Vector mousePos) {
        if (this instanceof ExpressionPackingStatement eps) {
            for (int i = 0; i < eps.expressions.length; i++) {
                BlockExpression expression = eps.expressions[i];
                if (expression == null) {
                    // an empty slot is itself a droppable target: report the statement
                    // containing it so a dragged atomic can be snapped back into this slot
                    if (eps.slotContainsPoint(i, mousePos)) {
                        return this;
                    }
                    continue;
                }
                // hit-test the child at its paint-time slot, matching paint()
                Vector slotPos = paintedSlotPositionOf(expression);
                if (slotPos == null) {
                    continue;
                }
                Vector previousPosition = expression.position;
                expression.position = slotPos;
                Hoverable found = null;
                if (expression.containsPoint(mousePos)) {
                    found = expression.findHoveredBlock(mousePos);
                }
                expression.position = previousPosition;
                if (found != null) {
                    return found;
                }
            }
            return null;
        }
        for (Expression exp : getChildExpressions()) {
            if (!(exp instanceof BlockExpression be)) {
                continue;
            }
            Vector slotPos = paintedSlotPositionOf(be);
            if (slotPos == null) {
                continue;
            }
            Vector previousPosition = be.position;
            be.position = slotPos;
            Hoverable found = null;
            if (be.containsPoint(mousePos)) {
                found = be.findHoveredBlock(mousePos);
            }
            be.position = previousPosition;
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    public void moveSelfAndAllChildrenBy(Vector positionChange) {
        position = position.add(positionChange);
        for (Expression exp : getChildExpressions()) {
            if (exp instanceof BlockExpression be) {
                be.moveSelfAndAllChildrenBy(positionChange);
            }
        }
        if (followingStatement instanceof BlockStatement bs) {
            bs.moveSelfAndAllChildrenBy(positionChange);
        }
        if (this instanceof StatementContainer sc) {
            ((BlockStatement) sc.getContainedStatement()).moveSelfAndAllChildrenBy(positionChange);
        }
    }

    public void moveSelfAndAllChildrenTo(Vector newPosition) {
        Vector positionChange = newPosition.subtract(position);
        moveSelfAndAllChildrenBy(positionChange);
    }

    /**
     * Moves ONLY this statement (not its packed children or the statements connected
     * below it) to a new world-space position. Used when a block has been torn out of
     * a stack and is being dragged as an independent root: its children must follow
     * visually, but paint()/hit-testing recompute child slots from the parent's live
     * position anyway, and the old tail keeps its own place in the chain.
     */
    public void moveSelfOnlyTo(Vector newPosition) {
        position = newPosition;
    }

    /**
     * The statement directly connected below this one, or null when this is the end
     * of a chain.
     */
    public BlockStatement getFollowingBlockStatement() {
        return followingStatement instanceof BlockStatement bs ? bs : null;
    }

    /**
     * Number of statements chained below this one (0 when nothing follows).
     */
    public int getChainLengthBelow() {
        int n = 0;
        for (BlockStatement cur = getFollowingBlockStatement(); cur != null;
                 cur = cur.getFollowingBlockStatement()) {
            n++;
        }
        return n;
    }

    /**
     * True if `target` appears somewhere in the following-statement chain starting
     * at this statement (excluding this statement itself).
     */
    public boolean chainContains(BlockStatement target) {
        for (BlockStatement cur = getFollowingBlockStatement(); cur != null;
                 cur = cur.getFollowingBlockStatement()) {
            if (cur == target) {
                return true;
            }
        }
        return false;
    }

    public void connectNextStatement(BlockStatement nextStatement) {
        if (nextStatement == null) {
            return; // connecting nothing is a no-op, not a crash
        }
        nextStatement.moveSelfAndAllChildrenTo(this.position.add(new RectVector(0, getCascadingHeight())));
        followingStatement = nextStatement;
    }

    /**
     * The statement whose bottom connector this statement is currently plugged into,
     * i.e. the statement directly above this one in some chain (searched across all
     * given roots), or null when this statement is the head of its stack. Used by the
     * drag logic so tearing a block out of the MIDDLE of a stack can re-plug the
     * upper part onto whatever used to hang below it.
     */
    public BlockStatement getPrecedingStatementInStack(ArrayList<BlockStatement> roots) {
        if (roots == null) {
            return null;
        }
        for (BlockStatement root : roots) {
            BlockStatement prev = getPrecedingStatementInChain(root);
            if (prev != null) {
                return prev;
            }
        }
        return null;
    }

    /**
     * Walks the following-statement chain rooted at `root` and returns the statement
     * directly above this one, or null if this statement is not in that chain.
     */
    private BlockStatement getPrecedingStatementInChain(BlockStatement root) {
        for (BlockStatement cur = root; cur != null; cur = cur.getFollowingBlockStatement()) {
            if (cur.getFollowingBlockStatement() == this) {
                return cur;
            }
        }
        return null;
    }

    /**
     * True when dropping this statement onto the bottom edge of `above` would be a
     * legal connection: `above` must have nothing attached below it already ("a block
     * can be connected at the bottom unless there is already a block occupying that
     * space") and this statement must be the head of its own carried stack (its tail
     * plugs into the dragged block's bottom, not into `above`).
     */
    public boolean canConnectBelow(BlockStatement above) {
        return above != null && above != this
                && above.getFollowingBlockStatement() == null;
    }

    /**
     * True when dropping this statement so that `below` plugs into ITS bottom edge
     * would be a legal connection: the space under this statement must be free and
     * `below` must be the head of its own stack.
     */
    public boolean canConnectAbove(BlockStatement below) {
        return getFollowingBlockStatement() == null
                && below != null && below != this;
    }

    /**
     * Breaks the connection between this statement and whatever is attached below
     * it, returning that block (or null when nothing follows). The detached tail
     * keeps its current position so it stays exactly where the user dragged it —
     * only the backend link is forgotten. This is what keeps cascadeCompile() and
     * paint() in sync with what is on screen: without it, a block moved away from
     * the stack still compiles as part of the original program.
     */
    public BlockStatement disconnectNextStatement() {
        if (!(followingStatement instanceof BlockStatement bs)) {
            return null;
        }
        followingStatement = null;
        return bs;
    }

    /**
     * True when the statement directly below this one in the connection chain
     * visually touches this statement's bottom edge again (within `tolerance`
     * pixels), i.e. the blocks are stacked like they were connected. Used by the
     * drag/drop logic to decide whether a moved-away block should stay detached
     * or snap back into the chain.
     */
    public boolean isVisuallyConnectedToNext(double tolerance) {
        if (!(followingStatement instanceof BlockStatement bs)) {
            return false;
        }
        double gapX = Math.abs(bs.position.getX() - position.getX());
        double gapY = Math.abs(bs.position.getY() - (position.getY() + getCascadingHeight()));
        return gapX <= tolerance && gapY <= tolerance;
    }

    public double getTotalStatementStackHeight() {
        if (followingStatement instanceof BlockStatement bs) {
            return getCascadingHeight() + bs.getTotalStatementStackHeight();
        }
        return getCascadingHeight();
    }

}
