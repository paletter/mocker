package com.paletter.stdy.tcg;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.util.List;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.parser.Parser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCParens;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;

public class Start {

	public static void main(String[] args) throws Throwable {

		String file = "C:\\ProjectPath\\company\\sdk-center-login\\src\\main\\java\\com\\bilibili\\sdk\\controller\\api\\client\\ApiController.java";

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
					
					if (methodTree.getName().toString().equals("userInfo")) {
						
						System.out.println("2: " + methodTree.getName());
						BlockTree body = methodTree.getBody();
						List<? extends StatementTree> sl = body.getStatements();
						for (StatementTree ss : sl) {
							
							System.out.println(ss);
							
							if (ss instanceof JCIf) {
								
								JCIf ji = (JCIf) ss;
								
								if (ji.cond instanceof JCParens) {
									JCParens cond = (JCParens) ji.cond;
									if (cond.expr instanceof JCMethodInvocation) {
										// ValidUtils.isNullOrEmpty(commonReq.getAccessKeyOriginal())
										JCMethodInvocation condExpr = (JCMethodInvocation) cond.expr;
										
										for (JCExpression arg : condExpr.args) {
											// commonReq.getAccessKeyOriginal()
											if (arg instanceof JCMethodInvocation) {
												JCMethodInvocation argJmi = (JCMethodInvocation) arg;
												JCFieldAccess meth = (JCFieldAccess) argJmi.meth;
												Name methName = meth.name;
												JCIdent methIdent = (JCIdent) meth.selected;
											}
										}
									}
								}
								
								
//								JCBinary condExpr = (JCBinary) cond.expr;
//								JCIdent ident = (JCIdent) conditionJCPB.lhs;
//								JCLiteral literal = (JCLiteral) conditionJCPB.rhs;
//								System.out.println(ident.name.toString());
//								System.out.println(conditionJCPB.getTag());
//								System.out.println(literal.value);
//								
//								System.out.println(ji.getCondition());
	//							System.out.println(ji.elsepart);
	//							System.out.println(ji.thenpart);
							} else {
							}
						}
					
					}
					
				}
			}
			
			return super.visitClass(node, arg1);
		}
		
		
	}
}
