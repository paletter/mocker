package com.paletter.stdy.tcg;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.paletter.stdy.tcg.ast.ConditionStore;
import com.paletter.stdy.tcg.ast.MethodAnalysis;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.parser.Parser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;

public class AppTest2 {


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

			System.out.println("2: " + methodTree.getName());
			MethodAnalysis ma = new MethodAnalysis();
			
			// Method Parameter
			Map<Name, Object> args = new HashMap<Name, Object>();
			
			for (VariableTree arg : methodTree.getParameters()) {
				JCVariableDecl argVd = (JCVariableDecl) arg;
				args.put(argVd.name, argVd);
			}
			
			ConditionStore goTrueCs = null;
			
			BlockTree body = methodTree.getBody();
			List<? extends StatementTree> sl = body.getStatements();
			for (StatementTree ss : sl) {
				
				System.out.println(ss);
				
				// IF
				if (ss instanceof JCIf) {
				
					JCIf ji = (JCIf) ss;
					
					// Go True
					goTrueCs = new ConditionStore(ji);
					
					JCBlock thenpart = (JCBlock) ji.thenpart;
					for (StatementTree st : thenpart.stats) {
						if (st instanceof JCIf) {
							JCIf ji2 = (JCIf) st;
							analyseIf(ji2, goTrueCs);
						}
					}
					
					if (ji.elsepart != null) {
						JCIf elseJi = (JCIf) ji.elsepart;
						analyseIf(elseJi, null);
					}
					
				}
				
				if (ss instanceof JCVariableDecl) {
					JCVariableDecl vd = (JCVariableDecl) ss;
					args.put(vd.getName(), vd);
				}
			}
			
			System.out.println(ma);
		}
		
		private void analyseIf(JCIf ji, ConditionStore beforeCs) {
			ConditionStore cs = new ConditionStore(ji);
			
			if (beforeCs != null) beforeCs.link(cs);
			JCBlock thenpart = (JCBlock) ji.thenpart;
			for (StatementTree st : thenpart.stats) {
				if (st instanceof JCIf) {
					analyseIf(ji, cs);
				}
			}
			
			if (ji.elsepart != null) {
				JCIf elseJi = (JCIf) ji.elsepart;
				analyseIf(elseJi, beforeCs);
			}
		}
	}
		
}
