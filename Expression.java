public interface Expression extends Compilable, HasChildExpressions {
    HasChildExpressions getParentExpression();
}
