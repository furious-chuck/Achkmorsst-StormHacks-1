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
            // findHoveredHere() (not findHoveredBlock) so a root statement's hit area
            // stops at its own bounds instead of also claiming every statement that is
            // connected below it — otherwise each lower block produces duplicate hits
            // from every ancestor root and can never win the smallest-area contest.
            Hoverable found = bs.findHoveredHere(worldMousePos);
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
                be.detachFromParent();
                if (Display.activeBlockManager != null
                        && !Display.activeBlockManager.expressions.contains(be)) {
                    Display.activeBlockManager.expressions.add(be);
                }
                // NOTE: we deliberately do NOT re-snap the block to grabPos here. The
                // drag anchors below (dragStartMousePos/dragStartBlockPos) are recorded
                // AFTER this call from the block's CURRENT position, so the block stays
                // glued to the cursor from the very first frame. Forcing it back to its
                // old slot position instead would desynchronise the anchor pair and make
                // the block jump away from the cursor — hover/drop-target detection then
                // follows the stale cursor point instead of the block, which is what made
                // dropping INTO other blocks fail.
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
     * Releases the dragged block. If a free-floating atomic expression was dropped
     * over an empty (or occupied) slot of an outer container, it is reattached into
     * that slot via attachToParent(), which snaps it into place and dynamically
     * resizes the parent — and every container around it — to accommodate it.
     * Otherwise the block stays wherever it was dropped. Clears drag state either way.
     */
    private static void endDrag(BlockManager blockManager) {
        System.out.println("dropped block: " + draggedBlock.getBlockName()
                + " at (" + (int) draggedBlock.getPosition().getX()
                + ", " + (int) draggedBlock.getPosition().getY() + ")"
                + (lastDropTarget != null
                        ? " over block: " + lastDropTarget.getBlockName()
                        : " on empty space"));

        // try to snap a free-floating atomic back into the container it was dropped on
        if (draggedBlock instanceof BlockExpression be && be.getParentExpression() == null) {
            Vector grabPoint = new RectVector(
                    mousePos.getX() + Global.cameraPos.getX(),
                    mousePos.getY() + Global.cameraPos.getY());
            HasChildExpressions newParent = findReattachSlot(blockManager, be, grabPoint);
            if (newParent != null) {
                int slot = slotIndexFor(newParent, be, grabPoint);
                if (slot >= 0) {
                    // remember who (if anyone) occupies that slot: attachToParent will
                    // displace them and clear their parent link, so they must become a
                    // free-floating root of their own instead of silently vanishing
                    Expression incumbent = newParent.getChildExpressions()[slot];
                    be.attachToParent(newParent, slot);
                    if (blockManager.expressions.contains(be)) {
                        blockManager.expressions.remove(be); // no longer a free root
                    }
                    if (incumbent instanceof BlockExpression incumbentBe
                            && incumbentBe.getParentExpression() == null
                            && !blockManager.expressions.contains(incumbentBe)) {
                        blockManager.expressions.add(incumbentBe);
                    }
                    System.out.println("reattached atomic block into "
                            + newParent.getClass().getSimpleName() + " slot " + slot);
                }
            }
        }

        draggedBlock = null;
        dragStartMousePos = null;
        dragStartBlockPos = null;
        lastDropTarget = null;
        draggedBlockIsDetached = false;
        currentlyDraggingABlock = false;
    }

    /**
     * Finds the innermost container with an open slot under the cursor that can
     * accept the dragged expression, or null if there is no valid reattach target.
     * A container qualifies only if some slot accepts the block type (see
     * slotAcceptsType) and the dragged block isn't inside that container already.
     */
    private static HasChildExpressions findReattachSlot(BlockManager blockManager,
                                                        BlockExpression dragged,
                                                        Vector worldMousePos) {
        HasChildExpressions best = null;
        double bestArea = Double.MAX_VALUE;
        for (BlockStatement bs : blockManager.statements) {
            if (isWithinStatement(bs, dragged)) {
                continue; // can't drop into ourselves
            }
            // search only this statement's own slots/shape (see findDropTarget): walking
            // the following-statement chain here too would make every ancestor root of a
            // connected stack claim the same lower block, so drops into any block but the
            // first in the chain could never resolve to a unique slot.
            Hoverable hit = bs.findHoveredHere(worldMousePos);
            if (hit == null || !isWithinStatement(bs, hit)) {
                continue;
            }
            HasChildExpressions hce = reattachSlotInContainer((Object) hit, dragged, worldMousePos);
            if (hce == null && hit != bs) {
                // cursor is over an occupied child block, but the enclosing statement
                // may still accept it (e.g. replacing a same-typed sibling in another
                // slot of the same row); fall back to the root statement itself.
                hce = reattachSlotInContainer(bs, dragged, worldMousePos);
            }
            if (hce != null) {
                double area = bs.getCascadingWidth() * bs.getCascadingHeight();
                if (area < bestArea) {
                    best = hce;
                    bestArea = area;
                }
            }
        }
        for (BlockExpression be : blockManager.expressions) {
            if (isWithinSubtree(be, dragged)) {
                continue;
            }
            HasChildExpressions hce = reattachSlotInContainer(be, dragged, worldMousePos);
            if (hce != null) {
                double area = be.getCascadingWidth() * be.getCascadingHeight();
                if (area < bestArea) {
                    best = hce;
                    bestArea = area;
                }
            }
        }
        return best;
    }

    /**
     * If the given container has a slot under worldMousePos that accepts the dragged
     * block type, returns the container (as the reattach target); otherwise null.
     */
    private static HasChildExpressions reattachSlotInContainer(Object block,
                                                               BlockExpression dragged,
                                                               Vector worldMousePos) {
        if (block instanceof ExpressionPackingStatement eps) {
            int slot = eps.getSlotIndexForWorldX(worldMousePos.getX());
            if (slot >= 0 && eps.slotContainsPoint(slot, worldMousePos)
                    && slotAcceptsType(dragged, eps.expressions[slot])) {
                return eps;
            }
        } else if (block instanceof ExpressionPackingExpression epe) {
            int slot = epe.getSlotIndexForWorldX(worldMousePos.getX());
            if (slot >= 0 && epe.slotContainsPoint(slot, worldMousePos)
                    && slotAcceptsType(dragged, epe.expressions[slot])) {
                return epe;
            }
        } else if (block instanceof WhileLoop wl) {
            // only offer the condition slot when the cursor is actually over the header
            // row (or the empty condition placeholder itself), never over the body gap
            boolean overConditionRow = wl.conditionContainsPoint(worldMousePos)
                    || (!wl.hasCondition() && wl.headerContainsPoint(worldMousePos));
            if (!wl.hasCondition() && overConditionRow && slotAcceptsType(dragged, null)) {
                return wl;
            }
        }
        return null;
    }

    /**
     * Resolves the slot index to attach the dragged block into within the target
     * container, preferring the exact slot under the cursor.
     */
    private static int slotIndexFor(HasChildExpressions parent, BlockExpression dragged,
                                    Vector worldMousePos) {
        if (parent instanceof ExpressionPackingStatement eps) {
            int slot = eps.getSlotIndexForWorldX(worldMousePos.getX());
            if (slot >= 0 && eps.slotContainsPoint(slot, worldMousePos)) {
                return slot;
            }
        } else if (parent instanceof ExpressionPackingExpression epe) {
            int slot = epe.getSlotIndexForWorldX(worldMousePos.getX());
            if (slot >= 0 && epe.slotContainsPoint(slot, worldMousePos)) {
                return slot;
            }
        } else if (parent instanceof WhileLoop) {
            return 0; // the condition slot is WhileLoop's only expression slot
        }
        return -1;
    }

    /**
     * Type check for whether the dragged block may occupy a slot. Empty slots accept
     * any expression; occupied slots only accept a block whose outermost type matches
     * the incumbent's (e.g. a number replaces a number, not a class-name block).
     */
    private static boolean slotAcceptsType(BlockExpression dragged, Expression incumbent) {
        if (incumbent == null) {
            return true;
        }
        return dragged.getClass() == incumbent.getClass();
    }
}
