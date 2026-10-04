import java.util.ArrayList;

public class Inputs {
    private Inputs() {}

    static boolean
        wHeld,
        aHeld,
        sHeld,
        dHeld;

    static Vector mousePos = new RectVector(0, 0);
    static boolean mouseHeld = false;
    static boolean currentlyDraggingABlock = false;
    static boolean mousePressed = false;
    static boolean mouseHeldLastFrame = false;

    // tolerance (world pixels) for considering two stacked statements visually
    // connected: within this distance of the exact snap position, dropping a block
    // under another one reconnects them; beyond it they are separate programs
    static final double CONNECTION_SNAP_TOLERANCE = 12.0;

    // the block (if any) the mouse was hovering over last frame, used to detect
    // hover enter/exit transitions so we don't spam the terminal every frame
    static Hoverable hoveredBlockLastFrame = null;

    // --- drag state ---
    // the root block currently being dragged (outermost ancestor of the grabbed
    // block), or null when nothing is being dragged
    static Hoverable draggedBlock = null;
    // true while the dragged root is a statement that was torn out of a connected
    // stack by splitChainAround(); endDrag() then decides whether to reconnect it
    // (dropped back on the chain) or keep the blocks separate (dropped away)
    static boolean draggedStatementWasSplit = false;
    // true when the split-out statement still has a following-statement tail hanging
    // below it: that tail travels WITH the block during the drag (pulling the middle
    // of a chain drags everything beneath the grab point along), so handleDrag() must
    // move the whole subtree instead of just the grabbed block itself
    static boolean draggedStatementCarriesTail = false;
    // the following-statement tail the grabbed block carried when it was split out
    // of its old stack; kept so a drop onto empty space can restore it intact
    static BlockStatement draggedStatementTail = null;
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
    // the innermost block actually under the cursor when the drag started (which may
    // be a nested child of draggedBlock); used by endDrag() to reattach the ORIGINAL
    // grabbed block into the slot it was displaced from when the user merely clicked
    // (pressed and released without moving), so a simple click never makes a block
    // vanish from the canvas
    static Hoverable originallyGrabbedBlock = null;
    // world-space position of the grabbed block at press time; if the block is still
    // there at release, the "drag" was really just a click
    static Vector originalBlockPos = null;
    // the container (and slot index) the grabbed atomic was packed in at press time,
    // captured BEFORE detachFromParent() clears them; used by restoreClickedBlock() to
    // put a merely-clicked block straight back into the exact hole it came from
    static HasChildExpressions originalParent = null;
    static int originalSlot = -1;
    // the statement whose bottom connector the grabbed statement was plugged into at
    // press time (null when it was the head of its stack), and the tail that hung
    // below it; captured by splitChainAround() so a click-without-movement can undo
    // the split exactly, and so dropping the torn-out middle back onto the upper
    // part of its old stack can re-plug the leftover tail where it came from
    static BlockStatement draggedStatementPredecessor = null;
    static BlockStatement draggedStatementOriginalFollowing = null;

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
        return getHoveredBlock(blockManager, false);
    }

    /**
     * Hover detection variant. When `duringDrag` is true, the block currently being
     * dragged (and its whole subtree) is excluded from the hit-test: while a drag is
     * in progress its grabbed copy floats on top of everything at the cursor, and it
     * must never shadow the genuine packed blocks underneath — otherwise pressing on
     * one container and dragging over another would make the hovered-block search
     * report the stale position of the dragged copy instead of the real target.
     */
    static Hoverable getHoveredBlock(BlockManager blockManager, boolean duringDrag) {
        if (blockManager == null || mousePos == null) {
            return null;
        }

        double worldX = mousePos.getX() + Global.cameraPos.getX();
        double worldY = mousePos.getY() + Global.cameraPos.getY();
        Vector worldMousePos = new RectVector(worldX, worldY);

        Hoverable deepest = null;
        double deepestArea = Double.MAX_VALUE;

        for (BlockStatement bs : blockManager.statements) {
            if (duringDrag && draggedBlock != null && isWithinStatement(bs, draggedBlock)) {
                continue; // the dragged tree must not claim the hover while it moves
            }
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
            if (duringDrag && draggedBlock != null
                    && (be == draggedBlock || isWithinSubtree(be, draggedBlock))) {
                continue; // same for free-floating roots being dragged
            }
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
        // statements packed INSIDE this one as a body (e.g. the single statement of a
        // WhileLoop's cascade storage) belong to this subtree just like chained ones:
        // without this, grabbing such an inner statement would find no predecessor
        // link to cut and the backend would keep seeing it connected to its container
        if (root instanceof StatementContainer sc
                && sc.getContainedStatement() instanceof BlockStatement inner
                && inner != root
                && isWithinStatement(inner, block)) {
            return true;
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
     * Returns the deepest block packed inside the given statement's own body (its
     * child expression slots, searched recursively) that contains the point, or null.
     * Unlike findHoveredHere(), this never claims the statement itself for a point
     * that is not over one of its real children — so while dragging a detached atomic
     * that visually overlaps an enclosing container, the hidden original still wins
     * the hit-test and can be restored when the user just clicks without moving.
     */
    private static Hoverable findPackedChildAt(BlockStatement root, Vector p) {
        if (!root.containsPoint(p)) {
            return null;
        }
        Hoverable direct = root.findHoveredChild(p);
        if (direct != null && direct != root) {
            return direct;
        }
        // the point may fall in a gap between packed rows of a container statement:
        // check the statements stored inside it (e.g. WhileLoop bodies) as well
        if (root instanceof StatementContainer sc
                && sc.getContainedStatement() instanceof BlockStatement inner
                && inner != root) {
            Hoverable found = findPackedChildAt(inner, p);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * Finds the block under the mouse that would receive a dropped block, ignoring
     * the dragged block's own subtree. Returns null when hovering over empty space.
     */
    private static Hoverable findDropTarget(BlockManager blockManager, Vector worldMousePos,
                                            Hoverable draggedRoot) {
        Hoverable deepest = null;
        double deepestArea = Double.MAX_VALUE;

        // pass 1: genuine blocks packed inside containers take priority over the
        // containers themselves — a detached copy floating on top of a parent must
        // not shadow the original still sitting in its slot
        for (BlockStatement bs : blockManager.statements) {
            if (isWithinStatement(bs, draggedRoot)) {
                continue; // don't let the dragged tree be its own drop target
            }
            Hoverable found = findPackedChildAt(bs, worldMousePos);
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
        if (deepest != null) {
            return deepest;
        }

        // pass 2: no packed child was under the cursor; fall back to the container
        // shapes themselves (their outer shape / empty placeholder slots), which is
        // where a dragged atomic can actually be reattached
        deepestArea = Double.MAX_VALUE;
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
        if (grabbed instanceof BlockExpression grabbedBe && Display.activeBlockManager != null) {
            // Pressing on a STACKED statement must grab the STATEMENT, never an atomic
            // expression merely packed inside it: Assigner-style blocks pack their
            // name/value slots right across the middle of their own body, so the plain
            // "deepest block wins" hover frequently reports the StringExpression "b"
            // instead of the Assigner underneath it. Grabbing that atomic detaches it
            // from its parent and drags only the tiny word around — the second block
            // of A->B->C then visually stays put while its tail C is left hanging in
            // mid-air, exactly the "third block doesn't follow the second one when I
            // drag it away" bug. If the press point also lies inside the bounds of the
            // statement this atomic is genuinely packed in, promote the grab to that
            // statement: the user meant to move the whole block. Atomics floating over
            // empty space or foreign containers keep going through the detach path.
            HasChildExpressions packedIn = grabbedBe.getParentExpression();
            if (packedIn instanceof BlockStatement ownerBs
                    && ownerBs == findStatementOwner(grabbedBe)) {
                double worldX = mousePos.getX() + Global.cameraPos.getX();
                double worldY = mousePos.getY() + Global.cameraPos.getY();
                if (ownerBs.containsPoint(new RectVector(worldX, worldY))) {
                    grabbed = ownerBs;
                }
            }
        }
        if (grabbed != null) {
            currentlyDraggingABlock = true;
            draggedBlockIsDetached = false;
            originallyGrabbedBlock = grabbed;
            originalBlockPos = grabbed.getPosition().clone();

            if (grabbed instanceof BlockExpression be && be.getParentBlock() != null) {
                // remember exactly where this atomic lives BEFORE detaching it: the
                // click-restore path needs the container + slot, and detachFromParent
                // clears both. (For packed children of an ExpressionPackingStatement the
                // stored position can be stale — paint()/hit-tests recompute slots from
                // the parent — so prefer the live layout when available.)
                HasChildExpressions preParent = be.getParentExpression();
                originalParent = preParent;
                originalSlot = preParent.indexOfChild(be);
                if (originalSlot < 0 && preParent instanceof ExpressionPackingStatement epsPre) {
                    for (int i = 0; i < epsPre.expressions.length; i++) {
                        if (epsPre.expressions[i] == be) {
                            originalSlot = i;
                            break;
                        }
                    }
                }
                if (originalSlot < 0 && preParent != null) {
                    Vector pp = be.getPosition();
                    originalSlot = slotIndexAtPosition(preParent, be, pp);
                }

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
                // snap the freed block onto the exact rectangle it was drawn in, so the
                // detached copy stays visually glued to the spot under the cursor even
                // if its stored position had drifted away from the parent's slot layout
                // (a stale position would otherwise make the block teleport/jump out
                // from under the mouse the instant the button goes down).
                if (preParent instanceof ExpressionPackingStatement eps
                        && originalSlot >= 0 && originalSlot < eps.expressions.length
                        && eps.expressions[originalSlot] == null) {
                    be.moveSelfAndAllChildrenTo(
                            eps.position.add(new RectVector(eps.getWidthUpToExpressionAt(originalSlot), 10)));
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

                if (grabbed instanceof BlockStatement grabbedBs) {
                    // Grabbing a statement that lives inside a connected stack must
                    // actually TEAR it out of that stack. Until now only the position
                    // changed: the links above (`prev.followingStatement == grabbed`)
                    // and below (`grabbed.followingStatement`) stayed intact, so the
                    // backend still saw the moved-apart blocks as connected — they kept
                    // compiling as one program even though they no longer touch.
                    if (draggedBlock != grabbed) {
                        // an outer container (e.g. the WhileLoop wrapping this block's
                        // body): dragging moves the whole container instead; it stays
                        // logically connected to its contents, so nothing to split.
                        draggedBlock = grabbed;
                    } else {
                        splitChainAround(grabbedBs, Display.activeBlockManager);
                        // The tail hanging below the grabbed block travels WITH it —
                        // pulling the middle of a chain drags everything beneath the
                        // grab point along, Scratch-style. So the grabbed statement is
                        // now the head of its own subtree and the drag must move that
                        // whole subtree (packed children + following chain), not just
                        // the single block. Without this, grabbing the second block of
                        // A->B->C left C visually stranded in mid-air while B floated
                        // away with nothing attached to it.
                        draggedStatementCarriesTail = draggedStatementWasSplit
                                && grabbedBs.getFollowingBlockStatement() != null;
                    }
                }
            }

            dragStartMousePos = mousePos.clone();
            dragStartBlockPos = draggedBlock.getPosition().clone();
            System.out.println("started dragging block: " + draggedBlock.getBlockName());
        } else {
            currentlyDraggingABlock = false;
        }
    }

    /**
     * Tears the given statement out of every connected stack it belongs to, so that
     * dragging it away genuinely disconnects it in the backend instead of leaving the
     * old `followingStatement` links intact (the bug where moved-apart blocks still
     * compiled as one program). Handles both ways a stack can exist:
     *  - a chain of statements linked via followingStatement (A -> B -> C), where the
     *    grabbed block may be the head, the tail, or somewhere in the middle;
     *  - a statement packed as another container's body (e.g. an Assigner inside a
     *    WhileLoop's body slot — containers keep their body in named fields rather
     *    than in the BlockManager's root list).
     * The grabbed block keeps whatever was attached below it (it travels with the
     * drag, like pulling the middle of a chain); everything left behind stays put
     * and becomes its own independent root program.
     */
    private static void splitChainAround(BlockStatement grabbed, BlockManager bm) {
        boolean split = false;

        // remember the tail we are dragging along so a drop onto empty space can
        // restore it if no reconnect happens
        draggedStatementTail = grabbed.getFollowingBlockStatement();

        // 1) cut any link that points INTO the grabbed block from above
        if (bm != null) {
            for (BlockStatement root : new ArrayList<>(bm.statements)) {
                BlockStatement prev = statementPrecedingInChain(root, grabbed);
                if (prev != null) {
                    prev.disconnectNextStatement();
                    split = true;
                }
            }
        }
        // also walk down into nested container bodies (WhileLoop bodies etc.)
        if (grabbed.getParentBlock() instanceof BlockStatement outerContainer
                && detachBodyFromContainer(outerContainer, grabbed)) {
            split = true;
        }

        // 2) the grabbed block itself is now the head of its own chain: promote it
        if (bm != null && !bm.statements.contains(grabbed)) {
            bm.statements.add(grabbed);
            split = true;
        }

        // 3) the tail hanging below the grabbed block travels WITH the drag, so the
        //    stack left behind must not still reference it — nothing else may claim
        //    the grabbed block's subtree as part of its own chain anymore. (The tail
        //    stays reachable through `grabbed`, which step 2 promoted to a root.)

        draggedStatementWasSplit = split;
    }

    /**
     * Returns the top-level BlockStatement root whose subtree (packed statements,
     * nested container bodies and following-statement chains) contains the given
     * expression via statement parent links, or null when the expression is not
     * packed inside any managed statement at all (e.g. it lives directly inside a
     * free-floating expression, or floats over empty canvas). Used by grab-time
     * promotion so pressing on an atomic word that sits inside a stacked block
     * grabs the whole block instead of detaching just the word.
     */
    private static BlockStatement findStatementOwner(BlockExpression be) {
        if (Display.activeBlockManager == null) {
            return null;
        }
        for (BlockStatement root : Display.activeBlockManager.statements) {
            if (statementSubtreeContains(root, be)) {
                return root;
            }
        }
        return null;
    }

    /**
     * True when `target` is packed somewhere inside the statement tree rooted at
     * `root`: as one of its direct child expressions, inside a nested container's
     * body, or anywhere in its following-statement chain.
     */
    private static boolean statementSubtreeContains(BlockStatement root, BlockExpression target) {
        for (Expression exp : root.getChildExpressions()) {
            if (exp instanceof BlockExpression be && isWithinSubtree(be, target)) {
                return true;
            }
        }
        if (root instanceof StatementContainer sc
                && sc.getContainedStatement() instanceof BlockStatement inner
                && inner != root
                && statementSubtreeContains(inner, target)) {
            return true;
        }
        if (root.followingStatement instanceof BlockStatement next
                && statementSubtreeContains(next, target)) {
            return true;
        }
        return false;
    }

    /**
     * Returns the statement directly above `target` in the chain rooted at `root`
     * (walking followingStatement links), or null if target is not in that chain.
     */
    private static BlockStatement statementPrecedingInChain(BlockStatement root,
                                                             BlockStatement target) {
        for (BlockStatement cur = root; cur != null; cur = cur.getFollowingBlockStatement()) {
            if (cur.getFollowingBlockStatement() == target) {
                return cur;
            }
        }
        return null;
    }

    /**
     * If `body` is currently stored as a direct child statement of the container
     * `outer` (in whichever named field that container uses), clears that field so
     * the dragged body no longer belongs to the container. Returns true when such a
     * link existed and was removed.
     */
    private static boolean detachBodyFromContainer(BlockStatement outer, BlockStatement body) {
        if (outer instanceof WhileLoop wl && wl.cascadeStatementStorage == body) {
            wl.cascadeStatementStorage = null;
            return true;
        }
        return false;
    }

    /**
     * Decides what happens to a dragged STATEMENT that was torn out of a stack and
     * dropped over empty space (no expression-slot target): if it ended up stacked
     * directly under another statement — close enough to look connected — snap it
     * into place and reconnect, so the backend matches the picture; otherwise keep
     * the blocks separate, leaving the dragged chain exactly where the user released
     * it. This is the fix for blocks that visually moved apart but stayed connected
     * in the compiled program: grab already severed the old links (splitChainAround),
     * and this decides whether any link is re-established at the drop point.
     */
    private static void finalizeStatementDrop(BlockStatement bs, BlockManager blockManager) {
        BlockStatement snapTarget = findStatementSnapTarget(bs, blockManager);
        if (snapTarget != null) {
            // reconnect under the block we were dropped onto; connectNextStatement
            // snaps our whole carried chain into place below it
            snapTarget.connectNextStatement(bs);
            System.out.println("reconnected statement under " + snapTarget.getBlockName());
        } else {
            System.out.println("statement dropped away from the stack: kept disconnected");
        }
        if (blockManager != null && !blockManager.statements.contains(bs)) {
            blockManager.statements.add(bs);
        }
    }

    /**
     * Finds the statement whose bottom edge the dragged block `bs` is resting on:
     * scans every root chain in the manager (excluding bs's own chain) for the
     * deepest statement whose bounding box overlaps the dragged block's box while
     * sitting ABOVE its top edge. Dropping onto that statement means "connect me
     * underneath you". Returns null when nothing qualifies (dropped in empty space).
     */
    private static BlockStatement findStatementSnapTarget(BlockStatement bs,
                                                           BlockManager blockManager) {
        if (blockManager == null) {
            return null;
        }
        double left = bs.position.getX();
        double right = left + bs.getCascadingWidth();
        double top = bs.position.getY();
        BlockStatement best = null;
        double bestBottom = -Double.MAX_VALUE;
        for (BlockStatement root : blockManager.statements) {
            if (root == bs || root.chainContains(bs)) {
                continue; // never snap onto our own chain
            }
            for (BlockStatement cur = root; cur != null;
                     cur = cur.getFollowingBlockStatement()) {
                if (cur == bs) {
                    break;
                }
                double cLeft = cur.position.getX();
                double cRight = cLeft + cur.getCascadingWidth();
                double cTop = cur.position.getY();
                double cBottom = cTop + cur.getCascadingHeight();
                boolean horizontalOverlap = cLeft < right && cRight > left;
                boolean aboveDragged = cTop < top && cBottom <= top + CONNECTION_SNAP_TOLERANCE;
                if (horizontalOverlap && aboveDragged && cBottom > bestBottom) {
                    best = cur;
                    bestBottom = cBottom;
                }
            }
        }
        return best;
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

                // A plain press-and-hold without movement keeps the block exactly where
                // it was drawn — visually nothing changed, but logically it now floats
                // free over the very hole it came from. If we ran the generic hover scan
                // here, the container underneath would claim the cursor point and grab
                // *that* block on the next press, tearing the canvas apart around a
                // half-detached copy. Instead, keep re-grabbing the dragged block itself
                // so holding/releasing repeatedly on an unmoved atomic stays stable.
                boolean unmoved = originalBlockPos != null
                        && Math.abs(be.getPosition().getX() - originalBlockPos.getX()) < 0.5
                        && Math.abs(be.getPosition().getY() - originalBlockPos.getY()) < 0.5;
                if (unmoved) {
                    draggedBlock = be;
                    originallyGrabbedBlock = be;
                    dragStartMousePos = mousePos.clone();
                    dragStartBlockPos = be.getPosition().clone();
                    if (!blockManager.expressions.contains(be)) {
                        blockManager.expressions.add(be);
                    }
                }
            } else if (draggedBlock instanceof BlockStatement bs) {
                if (draggedStatementWasSplit && !draggedStatementCarriesTail) {
                    // the grabbed statement was torn out of its old stack at press
                    // time AND nothing hangs below it: only move IT (paint/hit-testing
                    // lay its packed children out from its live position), so the rest
                    // of the canvas stays exactly where it was
                    bs.moveSelfOnlyTo(target);
                } else {
                    // either a whole connected tree is being dragged (never split), or
                    // the split-out block carries its following-statement tail with it:
                    // pulling the middle of A->B->C must drag B *and* C away together,
                    // keeping them stacked, instead of stranding C in mid-air
                    bs.moveSelfAndAllChildrenTo(target);
                }
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
        // A press-and-release without any movement is a *click*, not a drag. The grab
        // at press time already tore the atomic out of its parent (leaving an empty
        // slot), so unless we put it right back, clicking a block would make it
        // vanish from the canvas. Detect "the dragged block never moved" and restore
        // the original grabbed block to the exact slot it came from.
        if (draggedBlock != null && originalBlockPos != null
                && Math.abs(draggedBlock.getPosition().getX() - originalBlockPos.getX()) < 0.5
                && Math.abs(draggedBlock.getPosition().getY() - originalBlockPos.getY()) < 0.5) {
            if (draggedStatementWasSplit) {
                // a statement clicked inside a stack: undo the split done at grab time
                restoreSplitStatement(blockManager);
            } else {
                restoreClickedBlock(blockManager);
            }
            clearDragState();
            return;
        }

        // A detached atomic that was dragged around but never landed on a valid slot
        // would otherwise stay stuck as a free-floating root: the next press on its old
        // container grabs the container itself, and dragging then rips every packed
        // child out of it (they all vanish from view). So before dropping it into empty
        // space for good, give it one last chance to go home to the exact slot it was
        // grabbed from — if that hole is still empty and nothing else sits over it.
        if (draggedBlockIsDetached && draggedBlock instanceof BlockExpression beHome
                && beHome.getParentExpression() == null
                && originalParent != null && originalSlot >= 0
                && originalSlot < originalParent.getChildExpressions().length
                && originalParent.getChildExpressions()[originalSlot] == null) {
            boolean holeStillUnderCursor = false;
            double worldX = mousePos.getX() + Global.cameraPos.getX();
            double worldY = mousePos.getY() + Global.cameraPos.getY();
            Vector cursorWorld = new RectVector(worldX, worldY);
            if (originalParent instanceof ExpressionPackingStatement epsH) {
                holeStillUnderCursor = epsH.slotContainsPoint(originalSlot, cursorWorld);
            } else if (originalParent instanceof WhileLoop wlH) {
                holeStillUnderCursor = wlH.headerContainsPoint(cursorWorld);
            } else if (originalParent instanceof ExpressionPackingExpression epeH) {
                holeStillUnderCursor = epeH.slotContainsPoint(originalSlot, cursorWorld);
            }
            if (holeStillUnderCursor) {
                beHome.attachToParent(originalParent, originalSlot);
                if (blockManager != null && blockManager.expressions.contains(beHome)) {
                    blockManager.expressions.remove(beHome);
                }
                System.out.println("atomic dropped without a target: sent back to its "
                        + "original slot in " + originalParent.getClass().getSimpleName());
                clearDragState();
                return;
            }
        }

        System.out.println("dropped block: " + draggedBlock.getBlockName()
                + " at (" + (int) draggedBlock.getPosition().getX()
                + ", " + (int) draggedBlock.getPosition().getY() + ")"
                + (lastDropTarget != null
                        ? " over block: " + lastDropTarget.getBlockName()
                        : " on empty space"));

        // try to snap a free-floating atomic back into the container it was dropped on
        if (draggedBlock instanceof BlockExpression be && be.getParentExpression() == null) {
            // The cursor position is only an approximation of where the block ended up:
            // the drag delta is computed from the raw (unconverted) screen coordinates,
            // which can carry a constant offset relative to the window-relative mouse
            // position. Anchor the hit-test on the block's ACTUAL current position
            // instead — its top-left corner plus half its size, i.e. its center — so the
            // drop lands in whatever container/slot the block itself is really sitting in.
            Vector grabPoint = be.getPosition().add(
                    new RectVector(be.getCascadingWidth() / 2.0, be.getCascadingHeight() / 2.0));
            HasChildExpressions newParent = findReattachSlot(blockManager, be, grabPoint);
            if (newParent == null) {
                // second chance: the block may overlap a container's slot even though its
                // center misses it — test every corner of the dragged block's bounding box
                for (int i = 0; newParent == null && i < 4; i++) {
                    double cx = (i % 2 == 0) ? be.getPosition().getX()
                            : be.getPosition().getX() + be.getCascadingWidth();
                    double cy = (i < 2) ? be.getPosition().getY()
                            : be.getPosition().getY() + be.getCascadingHeight();
                    newParent = findReattachSlot(blockManager, be, new RectVector(cx, cy));
                }
            }
            if (newParent != null) {
                int slot = slotIndexFor(newParent, be, grabPoint);
                if (slot < 0) {
                    // no exact slot matched the anchor point; fall back to the first hole
                    // the container has (or its single slot for containers like WhileLoop)
                    slot = fallbackSlotFor(newParent);
                }
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

        // a statement torn out of a connected stack: decide whether the drop point
        // reconnects it under another block or leaves the stacks properly separated
        if (draggedStatementWasSplit && draggedBlock instanceof BlockStatement splitBs) {
            finalizeStatementDrop(splitBs, blockManager);
        }

        draggedBlock = null;
        dragStartMousePos = null;
        dragStartBlockPos = null;
        lastDropTarget = null;
        currentlyDraggingABlock = false;
        clearDragState();
    }

    /**
     * Resets all per-drag bookkeeping fields. Called at the end of every drop (and
     * every restored click) so the next press starts from a clean slate.
     */
    private static void clearDragState() {
        draggedBlock = null;
        dragStartMousePos = null;
        dragStartBlockPos = null;
        lastDropTarget = null;
        draggedBlockIsDetached = false;
        draggedStatementWasSplit = false;
        draggedStatementCarriesTail = false;
        draggedStatementTail = null;
        currentlyDraggingABlock = false;
        originallyGrabbedBlock = null;
        originalBlockPos = null;
        originalParent = null;
        originalSlot = -1;
    }

    /**
     * Puts a merely-clicked block back exactly where it was grabbed from, undoing the
     * detach performed at press time. The block never moved during the "drag", so we
     * walk the tree to find the container whose slot still sits at the block's
     * original position and re-pack it there (restoring the parent back-link and
     * removing it from the free-floating roots again). If no such hole can be found
     * the block simply remains a visible free-floating root — either way it can never
     * silently disappear from the canvas.
     */
    private static void restoreClickedBlock(BlockManager blockManager) {
        if (blockManager == null || originallyGrabbedBlock == null) {
            return;
        }
        if (!(originallyGrabbedBlock instanceof BlockExpression origBe)) {
            return; // statements are never detached on grab, nothing to restore
        }
        if (origBe.getParentExpression() != null) {
            return; // somehow already re-packed; leave it alone
        }
        // fast path: we recorded the exact container + slot at press time, so put the
        // block straight back into the hole it came from (if it is still empty)
        if (originalParent != null && originalSlot >= 0
                && originalSlot < originalParent.getChildExpressions().length
                && originalParent.getChildExpressions()[originalSlot] == null) {
            origBe.attachToParent(originalParent, originalSlot);
            if (blockManager.expressions.contains(origBe)) {
                blockManager.expressions.remove(origBe);
            }
            System.out.println("click without movement: restored " + origBe.getBlockName()
                    + " back into " + originalParent.getClass().getSimpleName()
                    + " slot " + originalSlot);
        } else {
            // fallback: walk the tree for any empty slot matching the original position
            HasChildExpressions host = findSlotAtPosition(blockManager.statements,
                    origBe, originalBlockPos);
            if (host == null) {
                host = findSlotInExpressions(blockManager.expressions, origBe, originalBlockPos);
            }
            if (host != null) {
                int slot = slotIndexAtPosition(host, origBe, originalBlockPos);
                if (slot >= 0 && host.getChildExpressions()[slot] == null) {
                    origBe.attachToParent(host, slot);
                    if (blockManager.expressions.contains(origBe)) {
                        blockManager.expressions.remove(origBe);
                    }
                    System.out.println("click without movement: restored " + origBe.getBlockName()
                            + " back into " + host.getClass().getSimpleName() + " slot " + slot);
                }
            }
        }
        // keep the dragged copy consistent with the (possibly re-attached) original
        if (origBe.getParentExpression() != null && draggedBlock == origBe) {
            draggedBlockIsDetached = false;
        }
    }

    /**
     * Puts a merely-clicked STATEMENT that was split out of its stack back exactly
     * where it came from: reconnects it at its old spot in the chain (restoring both
     * the link from the block above and the tail it carried away) and removes it from
     * the root list again. Only runs when the grabbed statement never moved — a plain
     * click must not tear a program apart.
     */
    private static void restoreSplitStatement(BlockManager blockManager) {
        if (!(draggedBlock instanceof BlockStatement bs) || blockManager == null) {
            return;
        }
        // re-link the block that used to point at us, if it is still sitting directly
        // above our original position
        Vector startPos = originalBlockPos != null ? originalBlockPos : bs.getPosition();
        for (BlockStatement root : blockManager.statements) {
            if (root == bs || root.chainContains(bs)) {
                continue;
            }
            for (BlockStatement cur = root; cur != null;
                     cur = cur.getFollowingBlockStatement()) {
                double bottom = cur.position.getY() + cur.getCascadingHeight();
                boolean linesUp = Math.abs(cur.position.getX() - startPos.getX()) < 0.5
                        && Math.abs(bottom - startPos.getY()) < 0.5;
                if (linesUp) {
                    cur.followingStatement = bs;
                    break;
                }
            }
        }
        // the chain we left behind became a root of its own at grab time; it now
        // hangs below us again, so demote it back into our subtree
        BlockStatement leftoverRoot = getLeftoverTailRoot(blockManager, bs);
        if (leftoverRoot != null) {
            blockManager.statements.remove(leftoverRoot);
        }
        // we are no longer an independent root program
        blockManager.statements.remove(bs);
        System.out.println("click without movement: statement " + bs.getBlockName()
                + " put back into its connected stack");
    }

    /**
     * Finds the root in `bm` that is actually the tail of `bs`'s own chain (created
     * when bs was split out of the stack), so it can be demoted once they reconnect.
     */
    private static BlockStatement getLeftoverTailRoot(BlockManager bm, BlockStatement bs) {
        BlockStatement tail = bs.getFollowingBlockStatement();
        if (tail == null) {
            return null;
        }
        for (BlockStatement root : bm.statements) {
            if (root == bs) {
                continue;
            }
            if (root == tail || root.chainContains(tail)) {
                return root;
            }
        }
        return null;
    }

    private static HasChildExpressions findSlotAtPosition(ArrayList<BlockStatement> roots,
                                                           BlockExpression target, Vector p) {
        for (BlockStatement bs : roots) {
            HasChildExpressions found = findSlotAtPosition(bs, target, p);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * Recursively searches the statement rooted at bs (its packed expression slots,
     * its nested containers' bodies, and its following-statement chain) for a
     * container that has an empty slot whose paint-time layout position equals the
     * given world point. Returns that container, or null.
     */
    private static HasChildExpressions findSlotAtPosition(BlockStatement bs,
                                                           BlockExpression target, Vector p) {
        if (bs == null) {
            return null;
        }
        if (bs instanceof ExpressionPackingStatement eps) {
            double elementOffset = eps.leftSpace;
            for (int i = 0; i < eps.expressions.length; i++) {
                BlockExpression expression = eps.expressions[i];
                if (expression == null) {
                    Vector slotPos = bs.position.add(new RectVector(elementOffset, 10));
                    if (Math.abs(slotPos.getX() - p.getX()) < 0.5
                            && Math.abs(slotPos.getY() - p.getY()) < 0.5) {
                        return eps;
                    }
                    elementOffset += eps.defaultSize.getX() + eps.expressionSpacing;
                } else {
                    elementOffset += expression.getCascadingWidth() + eps.expressionSpacing;
                }
            }
        }
        if (bs instanceof WhileLoop wl && wl.condition == null) {
            Vector slotPos = wl.position.add(new RectVector(10, 10));
            if (Math.abs(slotPos.getX() - p.getX()) < 0.5
                    && Math.abs(slotPos.getY() - p.getY()) < 0.5) {
                return wl;
            }
        }
        // search packed child expressions (e.g. a BinaryOperation inside a slot)
        for (Expression exp : bs.getChildExpressions()) {
            if (exp instanceof BlockExpression be) {
                HasChildExpressions found = findSlotInSubtree(be, target, p);
                if (found != null) {
                    return found;
                }
            }
        }
        // search statements stored inside this one (container bodies)
        if (bs instanceof StatementContainer sc
                && sc.getContainedStatement() instanceof BlockStatement inner
                && inner != bs) {
            HasChildExpressions found = findSlotAtPosition(inner, target, p);
            if (found != null) {
                return found;
            }
        }
        if (bs.followingStatement instanceof BlockStatement next) {
            return findSlotAtPosition(next, target, p);
        }
        return null;
    }

    /**
     * Expression-tree counterpart of findSlotAtPosition(): walks a packed expression
     * subtree looking for the empty slot that matches the target's original position.
     */
    private static HasChildExpressions findSlotInSubtree(BlockExpression be,
                                                          BlockExpression target, Vector p) {
        if (be instanceof ExpressionPackingExpression epe) {
            double elementOffset = epe.leftSpace;
            for (int i = 0; i < epe.expressions.length; i++) {
                BlockExpression expression = epe.expressions[i];
                if (expression == null) {
                    Vector slotPos = be.position.add(new RectVector(elementOffset, 10));
                    if (Math.abs(slotPos.getX() - p.getX()) < 0.5
                            && Math.abs(slotPos.getY() - p.getY()) < 0.5) {
                        return epe;
                    }
                    elementOffset += epe.defaultSize.getX() + epe.expressionSpacing;
                } else {
                    elementOffset += expression.getCascadingWidth() + epe.expressionSpacing;
                }
            }
        }
        for (Expression exp : be.getChildExpressions()) {
            if (exp instanceof BlockExpression child) {
                HasChildExpressions found = findSlotInSubtree(child, target, p);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static HasChildExpressions findSlotInExpressions(ArrayList<BlockExpression> roots,
                                                              BlockExpression target, Vector p) {
        for (BlockExpression be : roots) {
            if (be == target) {
                continue;
            }
            HasChildExpressions found = findSlotInSubtree(be, target, p);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * Returns the index of the slot in the given container whose layout position
     * equals p, or -1 if none matches.
     */
    private static int slotIndexAtPosition(HasChildExpressions parent, BlockExpression target,
                                           Vector p) {
        if (parent instanceof ExpressionPackingStatement eps) {
            for (int i = 0; i < eps.expressions.length; i++) {
                Vector slotPos = eps.position
                        .add(new RectVector(eps.getWidthUpToExpressionAt(i), 10));
                if (Math.abs(slotPos.getX() - p.getX()) < 0.5
                        && Math.abs(slotPos.getY() - p.getY()) < 0.5) {
                    return i;
                }
            }
        } else if (parent instanceof ExpressionPackingExpression epe) {
            for (int i = 0; i < epe.expressions.length; i++) {
                Vector slotPos = epe.position
                        .add(new RectVector(epe.getWidthUpToExpressionAt(i), 10));
                if (Math.abs(slotPos.getX() - p.getX()) < 0.5
                        && Math.abs(slotPos.getY() - p.getY()) < 0.5) {
                    return i;
                }
            }
        } else if (parent instanceof WhileLoop wl) {
            Vector slotPos = wl.position.add(new RectVector(10, 10));
            if (Math.abs(slotPos.getX() - p.getX()) < 0.5
                    && Math.abs(slotPos.getY() - p.getY()) < 0.5) {
                return 0;
            }
        }
        return -1;
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
            // The condition slot accepts a block when the cursor is over the header row.
            // An empty condition slot is always droppable from the header; an occupied
            // one also accepts a same-typed replacement dropped directly onto it (the
            // incumbent gets displaced to a free root by endDrag). Never offer the slot
            // when the cursor is over the body gap below the header.
            boolean overHeader = wl.headerContainsPoint(worldMousePos);
            boolean overCondition = wl.conditionContainsPoint(worldMousePos);
            if ((overHeader || overCondition) && slotAcceptsType(dragged, wl.condition)) {
                return wl;
            }
        }
        return null;
    }

    /**
     * Picks a slot for the dragged block when no exact slot matched under the drop
     * point: the first empty slot of the container, or its single slot for containers
     * like WhileLoop whose only expression slot lives at index 0. Returns -1 when the
     * container has no usable slot.
     */
    private static int fallbackSlotFor(HasChildExpressions parent) {
        if (parent instanceof ExpressionPackingStatement eps) {
            return eps.getFirstEmptyChildSlot();
        }
        if (parent instanceof BlockStatement bs) {
            return bs.getFirstEmptyChildSlot();
        }
        Expression[] kids = parent.getChildExpressions();
        if (kids != null && kids.length > 0) {
            for (int i = 0; i < kids.length; i++) {
                if (kids[i] == null) {
                    return i;
                }
            }
            return 0; // fully-occupied single-slot containers (e.g. WhileLoop)
        }
        return -1;
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
