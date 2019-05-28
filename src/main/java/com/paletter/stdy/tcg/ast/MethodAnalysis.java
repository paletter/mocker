package com.paletter.stdy.tcg.ast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;

import org.junit.Test;
import org.mockito.MockitoAnnotations;

import com.squareup.javapoet.MethodSpec;
import com.sun.source.tree.MethodTree;

public class MethodAnalysis {

	private ClassAnalysis classAnalysis;
	private MethodTree methodTree;
	private Method method;
	
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

	public List<MethodSpec> generateCode() {
		
		List<MethodSpec> methods = new ArrayList<MethodSpec>();
		
		for (ReturnBranch rb : rbs) {
			
			MethodSpec.Builder mb = 
					MethodSpec.methodBuilder("test" + CommonUtils.toUpperCaseFirstChar(methodTree.getName().toString()))
					.addModifiers(Modifier.PUBLIC)
					.addAnnotation(Test.class);
			
			mb.addStatement("$T.initMocks(this)", MockitoAnnotations.class);
			mb.addCode(rb.generateCode());
			
			methods.add(mb.build());
		}
		
		return methods;
	}

	public ClassAnalysis getClassAnalysis() {
		return classAnalysis;
	}
	
}
