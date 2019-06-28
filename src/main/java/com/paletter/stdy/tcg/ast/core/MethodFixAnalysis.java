package com.paletter.stdy.tcg.ast.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Modifier;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import com.paletter.stdy.tcg.ast.store.GCMethodInputArgStore;
import com.paletter.stdy.tcg.ast.support.ClassTypeMatcher;
import com.paletter.stdy.tcg.ast.support.CommonUtils;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.sun.source.tree.MethodTree;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

public class MethodFixAnalysis extends MethodAnalysis {

	private ClassAnalysis classAnalysis;
	private MethodTree methodTree;
	private Method method;
	private Map<String, GCMethodInputArgStore> inputArgs;
	
	private List<ReturnBranch> rbs = new ArrayList<ReturnBranch>();
	
	public MethodFixAnalysis(ClassAnalysis classAnalysis, MethodTree methodTree) {
		super(classAnalysis, methodTree);
		
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
	
	@Override
	public List<MethodSpec> generateCode() {
		
		List<MethodSpec> methods = new ArrayList<MethodSpec>();

		MethodSpec.Builder mb = 
				MethodSpec.methodBuilder("test" + CommonUtils.toUpperCaseFirstChar(methodTree.getName().toString()))
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(Test.class);

		CodeBlock.Builder cb = CodeBlock.builder();
		cb.addStatement("$T.initMocks(this)", MockitoAnnotations.class);
		
		List<String> argNams = new ArrayList<String>();
		for (int i = 0; i < method.getParameterCount(); i ++) {

			Class<?> paramClass = method.getParameterTypes()[i];
			ClassTypeMatcher pcCtm = ClassTypeMatcher.get(paramClass);
			
			// 1 New parameter code
			String instanceName = "arg" + i;
			argNams.add(instanceName);
			
			if (paramClass.getName().equals("javax.servlet.http.HttpServletRequest")) {
				
				cb.addStatement("$T $L = new org.springframework.mock.web.MockHttpServletRequest()", paramClass, instanceName);
			
			} else if (pcCtm.equals(ClassTypeMatcher.OBJECT)) {
			
				cb.addStatement("$T $L = new $T()", paramClass, instanceName, paramClass);
				
				// 2 Set parameter properties code
				for (Method method : paramClass.getDeclaredMethods()) {
					if (method.getName().startsWith("set")) {
						Class<?> setParamClass = method.getParameterTypes()[0];
						ClassTypeMatcher ctm = ClassTypeMatcher.get(setParamClass);
						cb.addStatement("$L.$L($L)", instanceName, method.getName(), ctm.getCommonValueWithProcessStatementArg());
					}
				}
				
			} else if (!pcCtm.equals(ClassTypeMatcher.OBJECT)) {
			
				cb.addStatement("$T $L = $L", paramClass, instanceName, pcCtm.getCommonValueWithProcessStatementArg());
			}
		}
		
		Class<?> returnType = method.getReturnType();
		if (returnType != null && !returnType.equals(void.class)) {
			
			// 3 Add return code
			String returnValName = "returnVal";
			StringBuilder returnCode = new StringBuilder();
			returnCode.append("$T $L = $L.$L(");
			for (String argName : argNams) {
				returnCode.append(argName);
				returnCode.append(",");
			}
			returnCode.deleteCharAt(returnCode.length() - 1);
			returnCode.append(")");
			cb.addStatement(returnCode.toString(), returnType, returnValName, classAnalysis.getGcImFieldName(), method.getName());
			
			// 4 Assert code
			for (Method m : returnType.getMethods()) {
				if (m.getName().equals("getCode")) {
					Class<?> rtType = m.getReturnType();
					
					if (rtType.isPrimitive()) {
						cb.addStatement("$T.assertEquals($L.getCode(), 0)", Assert.class, returnValName);
					} else if (rtType.equals(Integer.class)) {
						cb.addStatement("$T.assertEquals($L.getCode(), Integer.valueOf(0))", Assert.class, returnValName);
					}
				}
			}
		}
		
		mb.addCode(cb.build());
		
		methods.add(mb.build());
		
		return methods;
	}

	public ClassAnalysis getClassAnalysis() {
		return classAnalysis;
	}
	
}
