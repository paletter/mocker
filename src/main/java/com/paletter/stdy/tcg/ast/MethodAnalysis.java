package com.paletter.stdy.tcg.ast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Modifier;

import org.junit.Test;
import org.mockito.MockitoAnnotations;

import com.paletter.stdy.tcg.ast.store.GCMethodInputArgStore;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.sun.source.tree.MethodTree;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

public class MethodAnalysis {

	private ClassAnalysis classAnalysis;
	private MethodTree methodTree;
	private Method method;
	private Map<String, GCMethodInputArgStore> inputArgs;
	
	private List<ReturnBranch> rbs = new ArrayList<ReturnBranch>();

	public MethodAnalysis(ClassAnalysis classAnalysis, MethodTree methodTree) {
		this.classAnalysis = classAnalysis;
		this.methodTree = methodTree;

		// Match Method & MethodTree
		for (Method m : classAnalysis.getClazz().getMethods()) {
			if (m.getName().equals(methodTree.getName().toString())
				&& CommonUtils.matchMethodParams(m, methodTree)) {
				method = m;
				break;
			}
		}
		
		// Input arguments
		inputArgs = new HashMap<String, GCMethodInputArgStore>();
		for (int i = 0; i < method.getParameterTypes().length; i ++) {
			Class<?> argClass = method.getParameterTypes()[i];
			JCVariableDecl jvd = (JCVariableDecl) this.methodTree.getParameters().get(i);
			String argName = jvd.name.toString();
			inputArgs.put(argName, new GCMethodInputArgStore(argClass, argName));
		}
	}

	public void addReturnBranch(ReturnBranch rb) {
		rbs.add(rb);
	}
	
	public MethodTree getMethodTree() {
		return methodTree;
	}

	public Method getMethod() {
		return method;
	}
	
	// InputArgs
	public GCMethodInputArgStore getInputArg(String name) {
		return inputArgs.get(name);
	}

	public Map<String, GCMethodInputArgStore> getInputArgs() {
		return inputArgs;
	}

	public List<MethodSpec> generateCode() {
		
		List<MethodSpec> methods = new ArrayList<MethodSpec>();

		int caseIndex = 0;
		for (ReturnBranch rb : rbs) {
			
			CodeBlock rbCode = rb.generateCode();
			if (rbCode == null && caseIndex > 0) continue;
			
			MethodSpec.Builder mb = 
					MethodSpec.methodBuilder("test" + CommonUtils.toUpperCaseFirstChar(methodTree.getName().toString()) + "Case" + caseIndex)
					.addModifiers(Modifier.PUBLIC)
					.addAnnotation(Test.class);
			
			mb.addStatement("$T.initMocks(this)", MockitoAnnotations.class);
			
			if (rbCode != null) mb.addCode(rbCode);
			
			methods.add(mb.build());
			
			caseIndex ++;
		}
		
		return methods;
	}

	public ClassAnalysis getClassAnalysis() {
		return classAnalysis;
	}
	
}
