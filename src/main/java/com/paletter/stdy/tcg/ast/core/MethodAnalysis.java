package com.paletter.stdy.tcg.ast.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Modifier;

import org.junit.Test;
import org.mockito.MockitoAnnotations;

import com.paletter.stdy.tcg.ast.store.GCMethodInputArgStore;
import com.paletter.stdy.tcg.ast.support.CommonUtils;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.tree.JCTree.JCParens;
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
		if (method.getParameterTypes() != null) {
			for (int i = 0; i < method.getParameterTypes().length; i ++) {
				Class<?> argClass = method.getParameterTypes()[i];
				JCVariableDecl jvd = (JCVariableDecl) this.methodTree.getParameters().get(i);
				String argName = jvd.name.toString();
				inputArgs.put(argName, new GCMethodInputArgStore(argClass, argName));
			}
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
	
	private List<ReturnBranch> newRbs = new ArrayList<ReturnBranch>();
	public void addStatement(StatementTree ss) {
		if (rbs == null || rbs.isEmpty()) rbs.add(new ReturnBranch(this));
		
		newRbs = new ArrayList<ReturnBranch>();
		for (ReturnBranch rb : rbs) {
			addStatementToReturnBranch(ss, rb);
		}
		
		rbs.addAll(newRbs);
	}
	
	private void addStatementToReturnBranch(StatementTree ss, ReturnBranch rb) {
		
		if (ss instanceof JCIf) {
			
			JCIf jif = (JCIf) ss;
			analyseIf(jif, rb);
			
		} else {
		
			rb.addStatement(ss);
		}
	}
	
	private void analyseIf(JCIf jif, ReturnBranch parentRb) {

		JCParens cond = (JCParens) jif.cond;
		JCBlock thenpart  = (JCBlock) jif.thenpart;
		
		// If branch
		ReturnBranch newIfRb = parentRb.split();
		newIfRb.addPreIfConds(cond, true);
		for (StatementTree st : thenpart.stats) addStatementToReturnBranch(st, newIfRb);
		newRbs.add(newIfRb);
		
		// Else if branch
		if (jif.elsepart != null && jif.elsepart instanceof JCIf) {
			JCIf elsepart = (JCIf) jif.elsepart;
			
			ReturnBranch newParentRb = parentRb.split();
			newParentRb.addPreIfConds(cond, false);
			
			analyseIf(elsepart, newParentRb);
		}
		
		// Else branch
		if (jif.elsepart != null && jif.elsepart instanceof JCBlock) {
			JCBlock elsepart = (JCBlock) jif.elsepart;
			
			ReturnBranch newElseRb = parentRb.split();
			newElseRb.addPreIfConds(cond, false);
			for (StatementTree st : elsepart.stats) addStatementToReturnBranch(st, newElseRb);
			newRbs.add(newElseRb);
		}
	}
	
	public List<MethodSpec> generateCode() {
		
		List<MethodSpec> methods = new ArrayList<MethodSpec>();

		int caseIndex = 0;
		for (int i = 0; i < rbs.size(); i ++) {
			
			ReturnBranch rb = rbs.get(i);
			
			CodeBlock rbCode = rb.generateCode();
			if (rbCode == null && i < rbs.size() - 1) continue;
			
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
