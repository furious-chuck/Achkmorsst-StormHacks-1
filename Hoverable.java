/**
 * Anything that occupies a rectangle in world space (position .. position + cascading
 * size) and can therefore be hovered over with the mouse.
 */
public interface Hoverable {

    Vector getPosition();

    double getCascadingWidth();

    double getCascadingHeight();

    /**
     * Returns true if the given world-space point lies within this block's bounding box.
     */
    boolean containsPoint(Vector p);

    /**
     * Finds the innermost hoverable block under the given world-space mouse position:
     * checks this block's children first (so the deepest/most specific block wins),
     * then this block's own bounding box. Returns null if nothing is hovered.
     */
    Hoverable findHoveredBlock(Vector mousePos);

    /**
     * A human-readable name for this block, used when reporting hovers to the terminal.
     */
    String getBlockName();

    /**
     * The parent this block is packed inside of (as an expression slot), or null if
     * this block is a root in the BlockManager. Used by dragging so grabbing a nested
     * child drags its whole outermost root instead of tearing it out of its parent.
     */
    Hoverable getParentBlock();

    /**
     * Returns the outermost ancestor of this block (walking up through parent
     * expressions); for a root block this returns itself.
     */
    default Hoverable getRootAncestor() {
        Hoverable current = this;
        Hoverable parent = current.getParentBlock();
        while (parent != null) {
            current = parent;
            parent = current.getParentBlock();
        }
        return current;
    }
}
