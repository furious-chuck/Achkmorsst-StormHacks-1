import java.awt.*;

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
     * Searches this statement's packed child slots for the deepest block under the
     * given world-space point. Slot positions are recomputed from this statement's
     * current position (the same layout convention paint() uses), so children always
     * report up-to-date hit areas even after the parent has been moved or resized.
     * A slot whose own box does not contain the point is skipped outright: this is
     * what keeps a free-floating (detached/dragged) atomic sitting on top of the row
     * from being mistaken for a genuine child of this container — such a block is not
     * packed here anymore, so hitting it must never hide the container underneath it.
     */
    private Hoverable findHoveredChild(Vector mousePos) {
        if (this instanceof ExpressionPackingStatement eps) {
            double elementOffset = eps.leftSpace;
            for (int i = 0; i < eps.expressions.length; i++) {
                BlockExpression expression = eps.expressions[i];
                if (expression == null) {
                    // an empty slot is itself a droppable target: report the statement
                    // containing it so a dragged atomic can be snapped back into this slot
                    if (eps.slotContainsPoint(i, mousePos)) {
                        return this;
                    }
                    elementOffset += eps.defaultSize.getX() + eps.expressionSpacing;
                    continue;
                }
                // child slot computed from the parent's current position, matching paint()
                Vector previousPosition = expression.position;
                expression.position = position.add(new RectVector(elementOffset, 10));
                Hoverable found = null;
                if (expression.containsPoint(mousePos)) {
                    found = expression.findHoveredBlock(mousePos);
                }
                expression.position = previousPosition;
                if (found != null) {
                    return found;
                }
                elementOffset += expression.getCascadingWidth() + eps.expressionSpacing;
            }
        } else {
            for (Expression exp : getChildExpressions()) {
                if (exp instanceof BlockExpression be && be.containsPoint(mousePos)) {
                    Hoverable found = be.findHoveredBlock(mousePos);
                    if (found != null) {
                        return found;
                    }
                }
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
    }

    public void moveSelfAndAllChildrenTo(Vector newPosition) {
        Vector positionChange = newPosition.subtract(position);
        moveSelfAndAllChildrenBy(positionChange);
    }

    public void connectNextStatement(BlockStatement nextStatement) {
        nextStatement.position = this.position.add(new RectVector(0, getCascadingHeight()));
        followingStatement = nextStatement;
    }

    public double getTotalStatementStackHeight() {
        if (followingStatement instanceof BlockStatement bs) {
            return getCascadingHeight() + bs.getTotalStatementStackHeight();
        }
        return getCascadingHeight();
    }

}
