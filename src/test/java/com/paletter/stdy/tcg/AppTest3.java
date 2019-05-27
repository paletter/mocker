package com.paletter.stdy.tcg;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.paletter.stdy.mockito.ParentMocker;
import com.paletter.stdy.tcg.ast.ClassAnalysis;
import com.paletter.stdy.tcg.ast.ConditionStore;
import com.paletter.stdy.tcg.ast.MethodAnalysis;
import com.paletter.stdy.tcg.ast.ReturnBranch;
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

public class AppTest3 {


	public static void main(String[] args) throws Throwable {

//		System.out.println(ParentMocker.class.getResource(""));
//		
//		File f = new File("src/test/java/com/paletter/stdy/mockito/ParentMocker.java");
//		System.out.println(f.exists());
		
		String file = "src/test/java/com/paletter/stdy/mockito/ParentMocker.java";

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
			
			ClassAnalysis ca = new ClassAnalysis();
			
			for (Tree tree : node.getMembers()) {
				if (tree instanceof MethodTree) {

					MethodTree methodTree = (MethodTree) tree;
					
					MethodAnalysis ma = new MethodAnalysis(methodTree);
					
					if (methodTree.getName().toString().equals("goMock")) {
						ReturnBranch rb = analyseMethod(methodTree);
						ma.addReturnBranch(rb);
					}
				}
				
				if (tree instanceof JCVariableDecl) {
					JCVariableDecl jc = (JCVariableDecl) tree;
					ca.addVariable(jc);
				}
				
			}
			
			return super.visitClass(node, arg1);
		}

		private ReturnBranch analyseMethod(MethodTree methodTree) {

			// Method Parameter
			Map<Name, Object> args = new HashMap<Name, Object>();
			
			for (VariableTree arg : methodTree.getParameters()) {
				JCVariableDecl argVd = (JCVariableDecl) arg;
				args.put(argVd.name, argVd);
			}

			ReturnBranch rb = new ReturnBranch();
			
			BlockTree body = methodTree.getBody();
			List<? extends StatementTree> sl = body.getStatements();
			for (StatementTree ss : sl) {
				
				// IF
				if (!(ss instanceof JCIf)) {
				
					rb.addStatement(ss);
					
					System.out.println(ss);
				}
			}
			
			return rb;
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
