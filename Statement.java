public interface Statement extends Compilable, HasChildExpressions {
    Statement getFollowingStatement();
    String cascadeCompile();
}
