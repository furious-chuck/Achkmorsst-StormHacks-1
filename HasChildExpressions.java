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
        for (int i = 0; i < kids.length; i++) {
            if (kids[i] == child) {
                return i;
            }
        }
        return -1;
    }

}
