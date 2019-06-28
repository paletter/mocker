package com.paletter.stdy.tcg;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.util.List;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.parser.Parser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;

public class AppTest {


	public static void main(String[] args) throws Throwable {

		String file = "C:\\ProjectPath\\company\\sdk-center-login\\src\\common\\java\\com\\bilibili\\utils\\ValidUtils.java";

		Context c = new Context();
		JavacFileManager.preRegister(c);
		ParserFactory factory = ParserFactory.instance(c);

		FileInputStream f = new FileInputStream(file);
		FileChannel ch = f.getChannel();
		ByteBuffer buffer = ch.map(MapMode.READ_ONLY, 0, ch.size());

		Parser parser = factory.newParser(Charset.defaultCharset().decode(buffer), true, false, true);
		JCCompilationUnit unit = parser.parseCompilationUnit();
		
		unit.accept(new MethodScanner(), null);
	}

	static class MethodScanner extends TreeScanner<List<String>, List<String>> {

		@Override
		public List<String> visitClass(ClassTree node, List<String> arg1) {
			
			System.out.println("1: " + node.getSimpleName());
			
			for (Tree tree : node.getMembers()) {
				if (tree instanceof MethodTree) {

					MethodTree methodTree = (MethodTree) tree;
					
					if (methodTree.getName().toString().equals("isNullOrEmpty")) {
	
						analyseMethod(methodTree);
					}
				}
			}
			
			return super.visitClass(node, arg1);
		}

		private void analyseMethod(MethodTree methodTree) {

//			System.out.println("2: " + methodTree.getName());
//			ReturnBranch rb = new ReturnBranch(null);
//			
//			// Method Parameter
//			Map<Name, Object> args = new HashMap<Name, Object>();
//			
//			for (VariableTree arg : methodTree.getParameters()) {
//				JCVariableDecl argVd = (JCVariableDecl) arg;
//				args.put(argVd.name, argVd);
//			}
//			
//			BlockTree body = methodTree.getBody();
//			List<? extends StatementTree> sl = body.getStatements();
//			for (StatementTree ss : sl) {
//				
//				System.out.println(ss);
//				
//				// IF
//				if (ss instanceof JCIf) {
//					
//					// if (null == value)								
//					JCIf ji = (JCIf) ss;
//					
//					// 1 If Condition
//					if (ji.cond instanceof JCParens) {
//						JCParens cond = (JCParens) ji.cond;
//						
//						if (cond.expr instanceof JCMethodInvocation) {
//							
//							JCMethodInvocation condExpr = (JCMethodInvocation) cond.expr;
//							
//							for (JCExpression arg : condExpr.args) {
//								// commonReq.getAccessKeyOriginal()
//								if (arg instanceof JCMethodInvocation) {
//									JCMethodInvocation argJmi = (JCMethodInvocation) arg;
//									JCFieldAccess meth = (JCFieldAccess) argJmi.meth;
//									Name methName = meth.name;
//									JCIdent methIdent = (JCIdent) meth.selected;
//								}
//							}
//							
//						} else if (cond.expr instanceof JCBinary) {
//							
//							JCBinary condExpr = (JCBinary) cond.expr;
//							
//							JCLiteral ident = (JCLiteral) condExpr.lhs;
//							JCIdent rhs = (JCIdent) condExpr.rhs;
//							Name rhsName = rhs.getName();
//							Tag opcode = condExpr.getTag();
//							
//							Object arg = args.get(rhsName);
//							if (arg instanceof JCVariableDecl) {
//							}
//						}
//					}
//					
//					// Part Code
//					JCBlock thenpart = (JCBlock) ji.thenpart;
//					for (StatementTree st : thenpart.stats) {
//						if (st instanceof JCReturn) {
////							rb.over((JCReturn) st);
//						}
//					}
//					
//				}
//				
//				if (ss instanceof JCVariableDecl) {
//					JCVariableDecl vd = (JCVariableDecl) ss;
//					args.put(vd.getName(), vd);
//				}
//			}
		}
	}
		
}
