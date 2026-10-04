public abstract class CascadeCompiledStatement implements Statement {

    Statement followingStatement;

    @Override
    public Statement getFollowingStatement() {
        return followingStatement;
    }

    @Override
    public String cascadeCompile() {
        return compile() + "\n" + (followingStatement == null ? "" : followingStatement.cascadeCompile());
    }

}
