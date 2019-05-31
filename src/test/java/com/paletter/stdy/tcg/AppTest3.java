package com.paletter.stdy.tcg;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.paletter.stdy.tcg.ast.ClassAnalysis;
import com.paletter.stdy.tcg.ast.ConditionStore;
import com.paletter.stdy.tcg.ast.MethodAnalysis;
import com.paletter.stdy.tcg.ast.ReturnBranch;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ImportTree;
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
		
//		String filePath = "src/test/java/com/paletter/stdy/mockito/ParentMocker.java";
		String fileBasicUrl = "src/test/java/";
		String filePath = "com/paletter/stdy/mockito/ParentMocker.java";

		String classPath = filePath.replaceAll("/", ".");
		classPath = classPath.replaceAll(".java", "");
		Class<?> c2 = Class.forName(classPath);
		
		Context c = new Context();
		JavacFileManager.preRegister(c);
		ParserFactory factory = ParserFactory.instance(c);

		FileInputStream f = new FileInputStream(fileBasicUrl + filePath);
		FileChannel ch = f.getChannel();
		ByteBuffer buffer = ch.map(MapMode.READ_ONLY, 0, ch.size());

		Parser parser = factory.newParser(Charset.defaultCharset().decode(buffer), true, false, true);
		JCCompilationUnit unit = parser.parseCompilationUnit();
		
		ClassAnalysis ca = new ClassAnalysis(c2, "com.paletter.stdy.javapoet", null);
		unit.accept(new MethodScanner(ca), null);
		
		ca.generateCode();
	}

	static class MethodScanner extends TreeScanner<List<String>, List<String>> {

		private ClassAnalysis ca;
		
		public MethodScanner(ClassAnalysis ca) {
			this.ca = ca;
		}
		
		@Override
		public List<String> visitImport(ImportTree arg0, List<String> arg1) {
			return super.visitImport(arg0, arg1);
		}

		@Override
		public List<String> visitClass(ClassTree node, List<String> arg1) {
			
			for (Tree tree : node.getMembers()) {
				if (tree instanceof MethodTree) {

					MethodTree methodTree = (MethodTree) tree;
					
					MethodAnalysis ma = new MethodAnalysis(ca, methodTree);
					
//					if (methodTree.getName().toString().equals("getMockVal8")) {
						ReturnBranch rb = analyseMethod(ma);
						ma.addReturnBranch(rb);
//					}
					
					ca.addMethod(ma);
				}
				
				if (tree instanceof JCVariableDecl) {
					JCVariableDecl jc = (JCVariableDecl) tree;
					ca.addVariable(jc);
				}
				
			}
			
			return super.visitClass(node, arg1);
		}

		private ReturnBranch analyseMethod(MethodAnalysis ma) {

			MethodTree methodTree = ma.getMethodTree();
			
			// Method Parameter
			Map<Name, Object> args = new HashMap<Name, Object>();
			
			for (VariableTree arg : methodTree.getParameters()) {
				JCVariableDecl argVd = (JCVariableDecl) arg;
				args.put(argVd.name, argVd);
			}

			List<ReturnBranch> rbs = new ArrayList<ReturnBranch>();
			ReturnBranch rb = new ReturnBranch(ma);
			rbs.add(rb);
			
			BlockTree body = methodTree.getBody();
			List<? extends StatementTree> sl = body.getStatements();
			for (StatementTree ss : sl) {

				if (ss instanceof JCIf) {
					
					ReturnBranch newRb = new ReturnBranch(ma, rb);
					newRb.addStatement(ss);
					
				} else {
				
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
