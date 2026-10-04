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

    // --- drag state ---
    // the root block currently being dragged (outermost ancestor of the grabbed
    // block), or null when nothing is being dragged
    static Hoverable draggedBlock = null;
    // screen-space mouse position captured on the frame the drag started; the
    // grabbed block is moved by (current mouse - this anchor) each frame so it
    // stays glued to the cursor without jitter
    static Vector dragStartMousePos = null;
    // world-space position of the grabbed block when the drag started
    static Vector dragStartBlockPos = null;
    // the block (if any) the mouse was hovering over on the previous frame during
    // a drag, used to log drop-target changes without spamming the terminal
    static Hoverable lastDropTarget = null;
    // true when the grabbed block is an atomic/nested expression that has been torn
    // out of its parent and is now free-floating; set by handleDragStart(), read by
    // endDrag() so it can register the block as a new root in the BlockManager
    static boolean draggedBlockIsDetached = false;

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

    /**
     * Returns true if the given (world-space) block lies somewhere inside the
     * subtree of the given root statement: the statement itself, any nested child
     * expression, or any statement connected below it. Used by dragging so the
     * block being dragged is never reported as its own drop target.
     */
    private static boolean isWithinStatement(BlockStatement root, Hoverable block) {
        if (root == block) {
            return true;
        }
        for (Expression exp : root.getChildExpressions()) {
            if (exp instanceof BlockExpression be && isWithinSubtree(be, block)) {
                return true;
            }
        }
        if (root.followingStatement instanceof BlockStatement bs && isWithinStatement(bs, block)) {
            return true;
        }
        return false;
    }

    private static boolean isWithinSubtree(BlockExpression root, Hoverable block) {
        if (root == block) {
            return true;
        }
        for (Expression exp : root.getChildExpressions()) {
            if (exp instanceof BlockExpression be && isWithinSubtree(be, block)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the block under the mouse that would receive a dropped block, ignoring
     * the dragged block's own subtree. Returns null when hovering over empty space.
     */
    private static Hoverable findDropTarget(BlockManager blockManager, Vector worldMousePos,
                                            Hoverable draggedRoot) {
        Hoverable deepest = null;
        double deepestArea = Double.MAX_VALUE;

        for (BlockStatement bs : blockManager.statements) {
            if (isWithinStatement(bs, draggedRoot)) {
                continue; // don't let the dragged tree be its own drop target
            }
            Hoverable found = bs.findHoveredBlock(worldMousePos);
            if (found != null && !isWithinStatement(bs, found)) {
                found = null;
            }
            if (found != null) {
                double area = found.getCascadingWidth() * found.getCascadingHeight();
                if (area < deepestArea) {
                    deepest = found;
                    deepestArea = area;
                }
            }
        }
        for (BlockExpression be : blockManager.expressions) {
            if (isWithinSubtree(be, draggedRoot)) {
                continue;
            }
            Hoverable found = be.findHoveredBlock(worldMousePos);
            if (found != null && !isWithinSubtree(be, found)) {
                found = null;
            }
            if (found != null) {
                double area = found.getCascadingWidth() * found.getCascadingHeight();
                if (area < deepestArea) {
                    deepest = found;
                    deepestArea = area;
                }
            }
        }
        return deepest;
    }

    static void handleDragStart() {
        // called from the mousePressed AWT event: arm a potential drag on whatever
        // block is under the cursor right now (if any)
        if (!mouseHeld) return;
        // NOTE: the camera offset is intentionally NOT applied here. Blocks store
        // absolute positions that are already used directly by the hover detection
        // (containsPoint) and by moveSelfAndAllChildrenTo, so drag deltas must be
        // computed in the same coordinate space to keep the block glued to the cursor.
        Hoverable grabbed = getHoveredBlock(Display.activeBlockManager);
        if (grabbed != null) {
            currentlyDraggingABlock = true;
            draggedBlockIsDetached = false;

            if (grabbed instanceof BlockExpression be && be.getParentBlock() != null) {
                // Atomic statements (numbers, strings, class blocks) and any other
                // nested expression can be dragged OUT of their parent: detach it
                // first (leaving an empty slot/placeholder behind), promote it to a
                // free-floating root, then drag just that block instead of the whole
                // enclosing statement tree.
                Vector grabPos = be.getPosition().clone();
                be.detachFromParent();
                if (Display.activeBlockManager != null
                        && !Display.activeBlockManager.expressions.contains(be)) {
                    Display.activeBlockManager.expressions.add(be);
                }
                // detachFromParent may have re-snapped the slot position; restore the
                // spot the block was visually at when we grabbed it
                be.moveSelfAndAllChildrenTo(grabPos);
                draggedBlock = be;
                draggedBlockIsDetached = true;
                System.out.println("detached atomic block from its parent: " + be.getBlockName());
            } else {
                // top-level statement or already-free expression: drag its whole tree
                draggedBlock = grabbed.getRootAncestor();
            }

            dragStartMousePos = mousePos.clone();
            dragStartBlockPos = draggedBlock.getPosition().clone();
            System.out.println("started dragging block: " + draggedBlock.getBlockName());
        } else {
            currentlyDraggingABlock = false;
        }
    }

    /**
     * Call once per frame (after Inputs.mousePos has been updated). Handles the
     * rest of the drag lifecycle started by handleDragStart():
     *  - held + moved -> move the grabbed block's whole root tree with the cursor
     *  - release      -> drop the block, reporting the hovered drop target
     */
    static void handleDrag(BlockManager blockManager) {
        // button released while a drag was in progress -> drop the block
        if (!mouseHeld && currentlyDraggingABlock) {
            endDrag(blockManager);
            return;
        }

        // --- mid-drag: follow the cursor ---
        if (draggedBlock != null && mouseHeld) {
            Vector delta = mousePos.subtract(dragStartMousePos);
            Vector target = dragStartBlockPos.add(delta);
            if (draggedBlockIsDetached && draggedBlock instanceof BlockExpression be) {
                // detached atomic: its position is recomputed from the parent's slot
                // layout every frame while packed, but now that it is a free root we
                // must persist the new position ourselves. moveSelfAndAllChildrenTo
                // also carries any expressions nested inside the dragged atomic.
                be.moveSelfAndAllChildrenTo(target);
            } else if (draggedBlock instanceof BlockStatement bs) {
                bs.moveSelfAndAllChildrenTo(target);
            } else if (draggedBlock instanceof BlockExpression be) {
                be.moveSelfAndAllChildrenTo(target);
            }

            // report which block the cursor is over as a potential drop target
            double worldX = mousePos.getX() + Global.cameraPos.getX();
            double worldY = mousePos.getY() + Global.cameraPos.getY();
            Hoverable dropTarget = findDropTarget(blockManager,
                    new RectVector(worldX, worldY), draggedBlock);
            if (dropTarget != lastDropTarget) {
                if (dropTarget != null) {
                    System.out.println("droppable over block: " + dropTarget.getBlockName());
                } else if (lastDropTarget != null) {
                    System.out.println("no longer over a drop target");
                }
                lastDropTarget = dropTarget;
            }
        }
    }

    /**
     * Releases the dragged block: reports where it was dropped and clears drag state.
     * (Snapping/reattaching into the drop target's slot will come later.)
     */
    private static void endDrag(BlockManager blockManager) {
        System.out.println("dropped block: " + draggedBlock.getBlockName()
                + " at (" + (int) draggedBlock.getPosition().getX()
                + ", " + (int) draggedBlock.getPosition().getY() + ")"
                + (lastDropTarget != null
                        ? " over block: " + lastDropTarget.getBlockName()
                        : " on empty space"));
        draggedBlock = null;
        dragStartMousePos = null;
        dragStartBlockPos = null;
        lastDropTarget = null;
        draggedBlockIsDetached = false;
        currentlyDraggingABlock = false;
    }
}
