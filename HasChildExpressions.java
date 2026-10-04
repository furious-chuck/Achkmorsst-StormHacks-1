public interface HasChildExpressions {

    Expression[] getChildExpressions();
    void setChildElement(int childID, Expression newExpression);

    /**
     * Returns the slot index of the given child expression within this container,
     * or -1 if the child is not packed inside this container. Used when dropping a
     * dragged block into a container that already has a different block packed at
     * that slot (re-parenting), so we can find the slot to overwrite.
     */
    default int indexOfChild(Expression child) {
        Expression[] kids = getChildExpressions();
        if (kids == null || child == null) {
            return -1;
        }
        for (int i = 0; i < kids.length; i++) {
            if (kids[i] == child) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Removes every trace of the given child from this container: blanks any slot
     * still pointing at it and clears its back-link, unconditionally. Used as a
     * safety net by detachFromParent() when the normal slot-clearing paths failed to
     * fully unregister a child — a half-detached block that is still listed as a
     * child of its old parent gets swallowed by that parent's layout and vanishes
     * from the canvas while being dragged around invisibly.
     */
    default void forceRemoveChildLink(BlockExpression child) {
        Expression[] kids = getChildExpressions();
        if (kids != null) {
            for (int i = 0; i < kids.length; i++) {
                if (kids[i] == child) {
                    setChildElement(i, null);
                }
            }
        }
        if (child.getParentExpression() == this) {
            child.parentExpression = null;
        }
    }

}
