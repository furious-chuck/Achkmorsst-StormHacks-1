import java.awt.*;

public abstract class BlockExpression implements Expression, Paintable, Hoverable {

    Vector position = new RectVector();

    public Color mainColor;
    public Color secondaryColor;
    public Color outlineColor;
    public Color textColor;

    public HasChildExpressions parentExpression;

    public HasChildExpressions getParentExpression() {
        return parentExpression;
    }

    @Override
    public Vector getPosition() {
        return position;
    }

    /**
     * The statement/expression this block is packed inside of, or null if it is a
     * root in the BlockManager.
     */
    @Override
    public Hoverable getParentBlock() {
        // the parent is always a BlockStatement/BlockExpression (which are Hoverable);
        // fall back to null for any non-block parent, treating this as a root
        return parentExpression instanceof Hoverable h ? h : null;
    }

    @Override
    public String getBlockName() {
        return getClass().getSimpleName();
    }

    @Override
    public boolean containsPoint(Vector p) {
        return p.getX() >= position.getX() &&
               p.getX() <= position.getX() + getCascadingWidth() &&
               p.getY() >= position.getY() &&
               p.getY() <= position.getY() + getCascadingHeight();
    }

    /**
     * Finds the innermost hoverable block under the given world-space mouse position:
     * this expression's children first (deepest block wins), then its own bounding box.
     */
    @Override
    public Hoverable findHoveredBlock(Vector mousePos) {
        for (Expression exp : getChildExpressions()) {
            if (exp instanceof BlockExpression be) {
                Hoverable found = be.findHoveredBlock(mousePos);
                if (found != null) {
                    return found;
                }
            }
        }
        if (containsPoint(mousePos)) {
            return this;
        }
        return null;
    }

    // public abstract double getCascadingHeight();
    // public abstract double getCascadingWidth();

    public void moveSelfAndAllChildrenBy(Vector positionChange) {
        position = position.add(positionChange);
        for (Expression exp : getChildExpressions()) {
            if (exp instanceof BlockExpression be) {
                be.moveSelfAndAllChildrenBy(positionChange);
            }
        }
    }

    public void moveSelfAndAllChildrenTo(Vector newPosition) {
        Vector positionChange = newPosition.subtract(position);
        moveSelfAndAllChildrenBy(positionChange);
    }

    /**
     * Detaches this expression from whatever statement/expression it is currently
     * packed inside of (clearing the parent's slot so a hole appears where the block
     * used to be) and promotes it to a free-floating root. Does nothing if this
     * expression is already a root. Used when dragging an atomic out of its parent.
     */
    public void detachFromParent() {
        if (parentExpression == null) {
            return; // already a root, nothing to detach
        }
        HasChildExpressions oldParent = parentExpression;
        parentExpression = null;
        if (oldParent instanceof BlockStatement bs) {
            // statements may keep their children in private fields (Assigner) or
            // expose them via getChildExpressions(); both are handled here.
            bs.removeDirectChildExpression(this);
        } else {
            Expression[] siblings = oldParent.getChildExpressions();
            for (int i = 0; i < siblings.length; i++) {
                if (siblings[i] == this) {
                    oldParent.setChildElement(i, null); // clears our slot in the parent
                    break;
                }
            }
        }
    }

    /**
     * Reattaches this expression into the given parent container at the specified
     * slot index. This is the inverse of detachFromParent(): it sets up the parent
     * link and packs us inside the parent's layout so the parent (and any outer
     * containers above it) dynamically resize to accommodate us. The actual slot
     * assignment (and position snap) is performed by the parent's setChildElement.
     * <p>
     * If this expression is currently packed inside a different parent, it is first
     * detached from that parent (leaving an empty slot behind there). If it is
     * already attached to the requested parent, we simply move to the new slot.
     */
    public void attachToParent(HasChildExpressions newParent, int childID) {
        if (newParent == null) {
            Util.unableToCan("cannot attach a block to a null parent");
        }
        if (childID < 0 || childID >= newParent.getChildExpressions().length) {
            Util.unableToCan("slot index " + childID + " out of range for parent "
                    + (newParent instanceof Hoverable h ? h.getBlockName() : newParent.getClass().getSimpleName()));
        }
        if (parentExpression != null && parentExpression != newParent) {
            // moving from one parent to another: tear ourselves out of the old one first
            detachFromParent();
        }
        // establish the back-link before the parent's setChildElement runs so that
        // any cascading layout/resize triggered by it sees us as its child
        parentExpression = newParent;
        newParent.setChildElement(childID, this);
    }

}
