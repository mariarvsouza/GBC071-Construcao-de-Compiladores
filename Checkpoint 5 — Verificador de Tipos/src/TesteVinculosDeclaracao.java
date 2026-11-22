import java.util.List;
public class TesteVinculosDeclaracao {
    public static void main(String[] args) {
        VarDeclNode global = new VarDeclNode("number", "x", null, 1, 1);
        VarDeclNode duplicada = new VarDeclNode("number", "x", null, 2, 1);
        VarDeclNode interna = new VarDeclNode("number", "x", null, 4, 3);
        IdExprNode usoInterno = new IdExprNode("x", 5, 12);
        IdExprNode usoGlobal = new IdExprNode("x", 7, 12);
        VarDeclNode y = new VarDeclNode("number", "y", usoInterno, 5, 3);
        VarDeclNode z = new VarDeclNode("number", "z", usoGlobal, 7, 1);
        ProgramNode programa = new ProgramNode(List.of(global, duplicada, new BlockNode(List.of(interna, y),3,1),z),1,1);
        VisitorIdentificacao vis = new VisitorIdentificacao();
        programa.accept(vis);
        if (vis.getSimboloDeclaracao(global) == null) throw new AssertionError("global ausente");
        if (vis.getSimboloDeclaracao(duplicada) != null) throw new AssertionError("redeclaração foi registrada");
        if (vis.getSimboloDeclaracao(interna) != usoInterno.decl) throw new AssertionError("uso interno não vinculado");
        if (vis.getSimboloDeclaracao(global) != usoGlobal.decl) throw new AssertionError("uso global não vinculado");
        if (vis.getErros().size() != 1) throw new AssertionError("erros esperados: 1, veio "+vis.getErros());
        System.out.println("PASSOU: vínculos de declarações e sombreamento");
    }
}
