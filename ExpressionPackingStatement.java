import java.awt.*;

public abstract class ExpressionPackingStatement extends BlockStatement {

    BlockExpression[] expressions;

    double leftSpace = 10;
    double expressionSpacing = 10;
    double rightSpace = 10;

    double verSpace = 10;

    final Vector defaultSize = new RectVector(50, 30);

    // Layout accessors (kept in sync with ExpressionPackingExpression's): every
    // paint()/hit-test loop that walks these slots — including the duplicated ones in
    // Inputs and BlockStatement — must read them through these methods so any subclass
    // that changes its layout still paints and hit-tests consistently.

    double leftSpaceForLayout() {
        return leftSpace;
    }

    double slotSpacingForLayout() {
        return expressionSpacing;
    }

    /**
     * World-space position of the top-left corner of the bounding box of slot {@code i}
     * — exactly where paint() draws it (and where setChildElement packs the child).
     */
    public Vector getSlotPositionAt(int index) {
        return position.add(new RectVector(getWidthUpToExpressionAt(index), verSpace));
    }

    @Override
    public double getCascadingHeight() {
        if (expressions.length == 0) {
            return defaultSize.getY() + verSpace * 2;
        }
        double maxSize = 0;
        for (BlockExpression exp : expressions) {
            double height = exp == null ? defaultSize.getY() : exp.getCascadingHeight();
            if (height > maxSize) {
                maxSize = height;
            }
        }
        return maxSize + verSpace * 2;
    }

    @Override
    public double getCascadingWidth() {
        if (expressions.length == 0) {
            return defaultSize.getX() + leftSpace + rightSpace;
        }
        double total = leftSpace + rightSpace;
        for (BlockExpression exp : expressions) {
            total += exp == null ? defaultSize.getX() : exp.getCascadingWidth();
        }
        total += expressionSpacing * (expressions.length - 1);
        return total;
    }

    public double getWidthUpToExpressionAt(int index) {
        if (expressions.length <= index) {
            Util.unableToCan();
        }
        double total = leftSpace;
        for (int i = 0; i < index; i++) {
            total += expressions[i] == null ? defaultSize.getX() : expressions[i].getCascadingWidth();
        }
        total += expressionSpacing * index;
        return total;
    }

    /**
     * Returns the slot index whose horizontal span contains the given world-space x,
     * or -1 if the point is outside this container's packed row. Used when dropping a
     * dragged atomic back into this container so it lands in the slot under the cursor.
     */
    public int getSlotIndexForWorldX(double worldX) {
        double localX = worldX - position.getX();
        double offset = leftSpaceForLayout();
        for (int i = 0; i < expressions.length; i++) {
            double w = expressions[i] == null ? defaultSize.getX() : expressions[i].getCascadingWidth();
            if (localX >= offset && localX <= offset + w) {
                return i;
            }
            offset += w + slotSpacingForLayout();
        }
        return -1;
    }

    /**
     * Returns true if the given world-space point lies within the bounding box of the
     * slot at the given index (empty placeholder slots included). Slots are laid out
     * left-to-right starting at leftSpace with expressionSpacing between them, and sit
     * verSpace below the top of the container, matching paint()'s convention.
     */
    public boolean slotContainsPoint(int index, Vector p) {
        if (index < 0 || index >= expressions.length) {
            return false;
        }
        double localX = p.getX() - position.getX();
        double localY = p.getY() - position.getY();
        double offset = getWidthUpToExpressionAt(index);
        double w = expressions[index] == null ? defaultSize.getX() : expressions[index].getCascadingWidth();
        double h = expressions[index] == null ? defaultSize.getY() : expressions[index].getCascadingHeight();
        return localX >= offset && localX <= offset + w && localY >= verSpace && localY <= verSpace + h;
    }

    @Override
    public void setChildElement(int childID, Expression newExpression) {
        BlockExpression old = expressions[childID];
        if (newExpression == null) {
            // clearing a slot: detach the old child's parent link so it becomes free-floating
            if (old != null && old.getParentExpression() == this) {
                old.parentExpression = null;
            }
            expressions[childID] = null;
            return;
        }
        BlockExpression be = (BlockExpression) newExpression;
        // if the block we're packing is already sitting in a different slot of this
        // same container, clear that old slot first so it doesn't appear twice
        if (old != be) {
            int previousIndex = indexOfChild(be);
            if (previousIndex >= 0 && previousIndex != childID) {
                expressions[previousIndex] = null;
            }
            // whatever used to occupy the target slot is no longer our child
            if (old != null && old.getParentExpression() == this) {
                old.parentExpression = null;
            }
        }
        expressions[childID] = be;
        be.parentExpression = this;
        be.position = position.add(new RectVector(getWidthUpToExpressionAt(childID), 10));
    }

    public void paintMainShape(Graphics g) {
        GraphicsUtils.drawStatementShape(g, Color.MAGENTA, Color.BLACK, position.subtract(Global.cameraPos), new RectVector(getCascadingWidth(), getCascadingHeight()));
    }

    @Override
    public void paint(Graphics g) {
        paintMainShape(g);
        double elementOffset = leftSpaceForLayout();
        for (BlockExpression expression : expressions) {
            if (expression == null) {
                GraphicsUtils.drawThatGoofyExpressionShape(g, Color.WHITE, Color.BLACK, position.add(new RectVector(elementOffset, 10)).subtract(Global.cameraPos), defaultSize);
                elementOffset += defaultSize.getX() + slotSpacingForLayout();
            } else {
                // paint the child at its slot computed from the parent's current position
                // (same convention as the null placeholder above), so a moved parent
                // can never leave a child behind or misalign it with null siblings.
                Vector previousPosition = expression.position;
                expression.position = position.add(new RectVector(elementOffset, 10));
                expression.paint(g);
                expression.position = previousPosition;
                elementOffset += expression.getCascadingWidth() + slotSpacingForLayout();
            }
        }

        if (followingStatement instanceof BlockStatement bs) {
            bs.paint(g);
        }
    }
}
