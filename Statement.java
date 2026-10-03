public interface Statement extends Compilable {
    Statement getFollowingStatement();
    String cascadeCompile();
}
