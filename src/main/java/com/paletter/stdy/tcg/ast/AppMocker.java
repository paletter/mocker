package com.paletter.stdy.tcg.ast;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.paletter.stdy.tcg.ast.core.ClassAnalysis;
import com.paletter.stdy.tcg.ast.core.MethodAnalysis;
import com.paletter.stdy.tcg.ast.core.MethodFixAnalysis;
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
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;

public class AppMocker {

	private static int MOCK_MODE = 1;
	
	public static void startSmartMock(String path, String gcPath) throws Exception {
		MOCK_MODE = 1;
		startMock(path, gcPath);
	}
	
	public static void startFixMock(String path, String gcPath) throws Exception {
		MOCK_MODE = 2;
		startMock(path, gcPath);
	}
	
	public static void startMock(String path, String gcPath) throws Exception {
		
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		String rootPath = path.replaceAll("\\.", "/");
		
		Enumeration<URL> urls = classLoader.getResources(rootPath);
		while (urls.hasMoreElements()) {
			
			URL url = urls.nextElement();
			if (url != null) {
				String protocol = url.getProtocol();
				String pkgPath = url.getPath();
				
				if (protocol.equals("file")) {
					
					List<File> fileList = findFiles(pkgPath);
					for (File file : fileList) {
						if (file.getPath().contains("test")) continue;
						
						int subIndex = file.getPath().indexOf(rootPath.replace("/", "\\"));
						String classPath = file.getPath().substring(subIndex);
						classPath = classPath.replace(".class", ".java");

						doMock("src\\main\\java\\", classPath, gcPath);
					}
				}
			}
		}
	}

	private static List<File> findFiles(String path) {
		
		List<File> fileList = new ArrayList<File>();
		File file = new File(path);
		if (file.exists()) {
			
			if (file.isFile()) {
				fileList.add(file);
			}
			
			if (file.isDirectory()) {
				for (String fileName : file.list()) {
					fileList.addAll(findFiles(path + "/" + fileName));
				}
			}
		}
		
		return fileList;
	}
	
	public static void doMock(String fileBasicUrl, String filePath) throws Exception {
		doMock(fileBasicUrl, filePath, filePath);
	}
	
	public static void doMock(String fileBasicUrl, String filePath, String gcPath) throws Exception {
		
		String classPath = filePath.replaceAll("/", ".");
		classPath = classPath.replaceAll("\\\\", ".");
		classPath = classPath.replaceAll(".java", "");
		Class<?> c2 = Class.forName(classPath);
		
		Context c = new Context();
		JavacFileManager.preRegister(c);
		ParserFactory factory = ParserFactory.instance(c);

		FileInputStream f = null;
		try {
			
			f = new FileInputStream(fileBasicUrl + filePath);
			FileChannel ch = f.getChannel();
			ByteBuffer buffer = ch.map(MapMode.READ_ONLY, 0, ch.size());
	
			Parser parser = factory.newParser(Charset.defaultCharset().decode(buffer), true, false, true);
			JCCompilationUnit unit = parser.parseCompilationUnit();
			
			ClassAnalysis ca = new ClassAnalysis(c2, gcPath, null);
			unit.accept(new MethodScanner(ca), null);
			
			ca.generateCode();
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (f != null) f.close();
			} catch (Exception e2) {
			}
		}
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
					
					// Smart Mocker
					if (MOCK_MODE == 1) {
						MethodAnalysis ma = new MethodAnalysis(ca, methodTree);
						
						analyseMethod(ma);
						
						ca.addMethod(ma);
					}
					
					// Fix Mocker
					if (MOCK_MODE == 2) {
						MethodFixAnalysis ma = new MethodFixAnalysis(ca, methodTree);
						
						ca.addMethod(ma);
					}
				}
				
				if (tree instanceof JCVariableDecl) {
					JCVariableDecl jc = (JCVariableDecl) tree;
					ca.addVariable(jc);
				}
				
			}
			
			return super.visitClass(node, arg1);
		}

		private void analyseMethod(MethodAnalysis ma) {

			MethodTree methodTree = ma.getMethodTree();
			
			// Method Parameter
			Map<Name, Object> args = new HashMap<Name, Object>();
			
			for (VariableTree arg : methodTree.getParameters()) {
				JCVariableDecl argVd = (JCVariableDecl) arg;
				args.put(argVd.name, argVd);
			}

			BlockTree body = methodTree.getBody();
			List<? extends StatementTree> sl = body.getStatements();
			for (StatementTree ss : sl) {
				ma.addStatement(ss);
			}
		}
	}
}
