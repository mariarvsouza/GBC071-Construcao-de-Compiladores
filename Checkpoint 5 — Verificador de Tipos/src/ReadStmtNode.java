/** Comando read(ID);. O vínculo do destino fica no mapa do VisitorIdentificacao. */
public final class ReadStmtNode extends StmtNode {
    public final String varID;

    public ReadStmtNode(String varID, int line, int col) {
        super(line, col);
        this.varID = varID;
    }

    @Override public <T> T accept(Visitor<T> visitor) {
        return visitor.visitReadStmt(this);
    }
}
