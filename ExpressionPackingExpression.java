import java.awt.*;

public abstract class ExpressionPackingExpression extends BlockExpression {

    BlockExpression[] expressions;

    double leftSpace = 10;
    double expressionSpacing = 10;
    double rightSpace = 10;

    double verSpace = 10;

    final Vector defaultSize = new RectVector(50, 30);

    /**
     * Half of the horizontal space taken up by each gap between two packed slots.
     * Subclasses that draw something inside those gaps (e.g. BinaryOperation paints
     * its operator symbol there) widen the gaps by setting this to a larger value so
     * the drawing never overlaps the neighbouring blocks. The extra width is applied
     * symmetrically around the centre of every gap, which keeps the existing slot
     * positions (leftSpace + i*(default+expressionSpacing) style layouts) valid for
     * hit-testing and child placement.
     */
    double halfGapExpansion = 0;

    /**
     * Total horizontal expansion added to every gap between two packed slots.
     */
    double gapExpansion() {
        return halfGapExpansion * 2;
    }

    // Layout accessors: the paint()/hit-test loops (including the duplicated ones in
    // Inputs) must always read these instead of the raw fields, so subclasses that
    // override any of them (e.g. BinaryOperation widening its gaps for the operator
    // symbol) keep visual layout and hit-testing perfectly in sync.

    double leftSpaceForLayout() {
        return leftSpace;
    }

    double slotSpacingForLayout() {
        return expressionSpacing + gapExpansion();
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
        total += (expressionSpacing + gapExpansion()) * (expressions.length - 1);
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
        // the gap expansion is centred on each gap, so every slot after the first one
        // is shifted right by exactly one half-expansion (and the last slot sits before
        // a trailing half-expansion, which getCascadingWidth adds back on the right)
        total += (expressionSpacing + gapExpansion()) * index - halfGapExpansion;
        return total;
    }

    /**
     * Returns the slot index whose horizontal span contains the given world-space x,
     * or -1 if the point is outside this container's packed row. Used when dropping a
     * dragged atomic back into this container so it lands in the slot under the cursor.
     */
    public int getSlotIndexForWorldX(double worldX) {
        double localX = worldX - position.getX();
        // the first slot starts at leftSpace; each following slot is shifted by one
        // half-expansion (the gap expansion is centred on the gaps), so normalise the
        // coordinate back into the un-expanded layout before measuring the slots
        double offset = leftSpaceForLayout() - halfGapExpansion;
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
    public Expression[] getChildExpressions() {
        return expressions;
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
        GraphicsUtils.drawThatGoofyExpressionShape(g, Color.MAGENTA, Color.BLACK, position.subtract(Global.cameraPos), new RectVector(getCascadingWidth(), getCascadingHeight()));
    }

    /**
     * Hook for subclasses that want to draw something on top of the container's shape
     * (e.g. BinaryOperation paints its operator symbol in the gap between the two
     * packed slots). Called from paint() after the main shape is drawn but before the
     * child blocks/placeholder slots are painted, so children always sit on top.
     */
    protected void paintOverMainShape(Graphics g) {
    }

    @Override
    public void paint(Graphics g) {
        paintMainShape(g);
        paintOverMainShape(g);
        double elementOffset = leftSpaceForLayout() - halfGapExpansion;
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
    }
}
