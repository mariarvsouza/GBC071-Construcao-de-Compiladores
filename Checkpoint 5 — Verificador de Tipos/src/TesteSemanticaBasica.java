import java.util.*;
public class TesteSemanticaBasica {
 static void expect(boolean b,String s){if(!b)throw new AssertionError(s);}
 public static void main(String[]a){
  VarDeclNode x = new VarDeclNode("number","x",new IntLitNode(1,1,1),1,1);
  VarDeclNode y = new VarDeclNode("real","y",new BinExprNode(new IdExprNode("x",2,1),"+",new RealLitNode(2.5,2,1),2,1),2,1);
  VarDeclNode bad = new VarDeclNode("number","z",new RealLitNode(3.5,3,1),3,1);
  IfStmtNode wrong = new IfStmtNode(new IdExprNode("x",4,1),new BlockNode(List.of(new PrintStmtNode(new IdExprNode("x",5,1),5,1)),4,1),null,4,1);
  FuncDeclNode f = new FuncDeclNode("number","f",List.of(new ParamNode("number","p",6,1)), new BlockNode(List.of(new ReturnStmtNode(new IdExprNode("p",7,1),7,1)),6,1),6,1);
  ProgramNode pr = new ProgramNode(List.of(x,y,bad,wrong,f),1,1);
  VisitorIdentificacao id=new VisitorIdentificacao();pr.accept(id);
  expect(id.getErros().isEmpty(),"identificacao: "+id.getErros());
  new VisitorPreparacaoTipos(id).preparar(pr);
  VisitorVerificadorTipos v=new VisitorVerificadorTipos(id);v.verificar(pr);
  System.out.println("erros="+v.getErros());
  System.out.println("avisos="+v.getAvisos());
  expect(v.getErros().size()==2,"numero de erros");
  expect(y.initExpr.type.equals(Type.REAL),"promocao real");
  expect(v.getErros().get(0).contains("inicializacao"),"erro init");
  expect(v.getErros().get(1).contains("if"),"erro if");
  System.out.println("PASSOU: tipagem expressao, inicializacao, if e retorno");
 }
}
