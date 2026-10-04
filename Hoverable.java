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
}
