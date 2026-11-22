import java.util.*;
public class TesteSemanticaFuncoes {
 static void ok(boolean b,String x){if(!b)throw new AssertionError(x);}
 public static void main(String[]args){
  FuncDeclNode f = new FuncDeclNode("real","f",List.of(new ParamNode("real","a",1,1)),
      new BlockNode(List.of(new ReturnStmtNode(new IdExprNode("a",2,1),2,1)),1,1),1,1);
  VarDeclNode x = new VarDeclNode("real","x",new CallFuncExprNode("f",List.of(new IntLitNode(1,3,1)),3,1),3,1);
  VarDeclNode b = new VarDeclNode("bool","b",null,4,1);
  ReadStmtNode r = new ReadStmtNode("b",5,1);
  VarDeclNode y = new VarDeclNode("number","y",new CallFuncExprNode("f",List.of(new StringLitNode("abc",6,1)),6,1),6,1);
  ProgramNode p = new ProgramNode(List.of(f,x,b,r,y),1,1);
  VisitorIdentificacao id=new VisitorIdentificacao();p.accept(id);
  ok(id.getErros().isEmpty(),"identificacao "+id.getErros());
  new VisitorPreparacaoTipos(id).preparar(p);
  VisitorVerificadorTipos v=new VisitorVerificadorTipos(id);v.verificar(p);
  System.out.println(v.getErros());
  ok(v.getErros().size()==2,"errors "+v.getErros());
  ok(v.getErros().get(0).contains("read"),"read");
  ok(v.getErros().get(1).contains("argumento 1"),"args");
  System.out.println("PASSOU: funcao, promocao, read, erro de argumento e sem cascata");
 }
}
