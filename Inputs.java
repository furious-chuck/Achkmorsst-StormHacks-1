public class Inputs {
    private Inputs() {}

    static Vector mousePos = new RectVector(0, 0);
    static boolean mouseHeld = false;
    static boolean currentlyDraggingABlock = false;
    static boolean mousePressed = false;
    static boolean mouseHeldLastFrame = false;

    // the block (if any) the mouse was hovering over last frame, used to detect
    // hover enter/exit transitions so we don't spam the terminal every frame
    static Hoverable hoveredBlockLastFrame = null;

    /**
     * Determines whether the mouse is currently hovering over a block, and if so,
     * which one. Searches every block managed by the given BlockManager (including
     * nested child expressions and connected statement chains) and returns the
     * innermost/deepest block under the cursor, or null if the mouse is over nothing.
     * <p>
     * Prints to the terminal when the mouse enters a block (naming that block) and
     * when it leaves one.
     */
    static Hoverable getHoveredBlock(BlockManager blockManager) {
        if (blockManager == null || mousePos == null) {
            return null;
        }

        double worldX = mousePos.getX() + Global.cameraPos.getX();
        double worldY = mousePos.getY() + Global.cameraPos.getY();
        Vector worldMousePos = new RectVector(worldX, worldY);

        Hoverable deepest = null;
        double deepestArea = Double.MAX_VALUE;

        for (BlockStatement bs : blockManager.statements) {
            Hoverable found = bs.findHoveredBlock(worldMousePos);
            if (found != null) {
                double area = found.getCascadingWidth() * found.getCascadingHeight();
                if (area < deepestArea) {
                    deepest = found;
                    deepestArea = area;
                }
            }
        }
        for (BlockExpression be : blockManager.expressions) {
            Hoverable found = be.findHoveredBlock(worldMousePos);
            if (found != null) {
                double area = found.getCascadingWidth() * found.getCascadingHeight();
                if (area < deepestArea) {
                    deepest = found;
                    deepestArea = area;
                }
            }
        }

        // report hover transitions to the terminal
        if (deepest != hoveredBlockLastFrame) {
            if (hoveredBlockLastFrame != null) {
                System.out.println("mouse stopped hovering over block: "
                        + hoveredBlockLastFrame.getBlockName());
            }
            if (deepest != null) {
                System.out.println("mouse is hovering over block: " + deepest.getBlockName()
                        + " at (" + (int) worldMousePos.getX() + ", " + (int) worldMousePos.getY() + ")");
            }
            hoveredBlockLastFrame = deepest;
        }

        return deepest;
    }
}
