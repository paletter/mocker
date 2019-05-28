package com.paletter.stdy.tcg.ast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.mockito.Mockito;

import com.paletter.stdy.tcg.ast.store.GCFieldStore;
import com.squareup.javapoet.CodeBlock;
import com.sun.source.tree.StatementTree;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCReturn;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

public class ReturnBranch {
	
	private MethodAnalysis methodAnalysis;
	private List<JCVariableDecl> variables = new ArrayList<JCVariableDecl>();
	private List<StatementTree> statementTrees = new ArrayList<StatementTree>();
	
	public ReturnBranch(MethodAnalysis methodAnalysis) {
		this.methodAnalysis = methodAnalysis;
	}

	public void addStatement(StatementTree st) {
		statementTrees.add(st);
	}

	public JCVariableDecl findVariable(String name) {
		for (JCVariableDecl jv : variables) {
			if (jv.name.toString().equals(name)) return jv;
		}
		return null;
	}
	
	public CodeBlock generateCode() {
		
		CodeBlock.Builder cb = CodeBlock.builder();
		
		String imFieldName = methodAnalysis.getClassAnalysis().getGcImFieldName();
		String methodName = methodAnalysis.getMethodTree().getName().toString();
		Class<?> returnType = methodAnalysis.getMethod().getReturnType();
		ClassTypeMatcher returnTypeCtm = ClassTypeMatcher.get(returnType);
		Map<String, PreMockVarStatement> branchPreMockVarStatementMap = new HashMap<String, PreMockVarStatement>();
		
		// Mock input arguments
		Map<String, MockInputArg> mockInputArgs = new HashMap<String, MockInputArg>();
		for (int i = 0; i < methodAnalysis.getMethod().getParameterTypes().length; i ++) {
			Class<?> argClass = methodAnalysis.getMethod().getParameterTypes()[i];
			JCVariableDecl jvd = (JCVariableDecl) methodAnalysis.getMethodTree().getParameters().get(i);
			String argName = jvd.name.toString();
			if (argClass.equals(String.class)) {
				mockInputArgs.put(argName, new MockInputArg(argClass, argName, CommonUtils.COMMON_STRING));
			} else if (argClass.equals(Integer.class)) {
				mockInputArgs.put(argName, new MockInputArg(argClass, argName, CommonUtils.COMMON_INTEGER));
			}
		}
		
		for (StatementTree st : statementTrees) {
			
			if (st instanceof JCVariableDecl) {
				JCVariableDecl jv = (JCVariableDecl) st;
				
				if (jv.init instanceof JCMethodInvocation) {
					JCMethodInvocation jmi = (JCMethodInvocation) jv.init;
					if (jmi.meth instanceof JCFieldAccess) {
						JCFieldAccess jfa = (JCFieldAccess) jmi.meth;
						JCIdent selected = (JCIdent) jfa.selected;
						
						if (this.findVariable(selected.getName().toString()) == null) {
							GCFieldStore classFs = methodAnalysis.getClassAnalysis().findGCField(selected.getName().toString());
							branchPreMockVarStatementMap.put(jv.name.toString(), new PreMockVarStatement(classFs, jv));
						}
					}
				}
			}
			
			if (st instanceof JCReturn) {
				JCReturn jr = (JCReturn) st;
				
				if (jr.expr instanceof JCIdent) {
					
					JCIdent rji = (JCIdent) jr.expr;
					
					if (branchPreMockVarStatementMap.containsKey(rji.name.toString())) {
						
						PreMockVarStatement pms = branchPreMockVarStatementMap.get(rji.name.toString());
						
						Object assertVal = returnTypeCtm.getCommonValue();
						
						// Mock variable
						cb.addStatement("$T.when($L.$L()).thenReturn($L)", Mockito.class, pms.getMockVariableName(), pms.getMockMethod(), returnTypeCtm.processAssertStatementArg(assertVal));
						
						// Assert return
						cb.add(createAssertReturnStatement(assertVal, mockInputArgs.values()));
					}
					
				} else if (jr.expr instanceof JCLiteral) {
					
					JCLiteral jl = (JCLiteral) jr.expr;
					cb.addStatement("$T.assertEquals($L.$L(), $L)", Assert.class, imFieldName, methodName, returnTypeCtm.processAssertStatementArg(jl.getValue()));
				}
			}
		}
		
		return cb.build();
	}
	
	private CodeBlock createAssertReturnStatement(Object assertVal, Collection<MockInputArg> mockInputArgs) {
		CodeBlock.Builder cb = CodeBlock.builder();
		
		String imFieldName = methodAnalysis.getClassAnalysis().getGcImFieldName();
		String methodName = methodAnalysis.getMethodTree().getName().toString();
		
		Class<?> returnType = methodAnalysis.getMethod().getReturnType();
		
		StringBuilder sb = new StringBuilder();
		sb.append("$T.assertEquals($L.$L(");
		for (MockInputArg mia : mockInputArgs) {
			ClassTypeMatcher ctm = ClassTypeMatcher.get(mia.getArgClass());
			sb.append(ctm.getCommonValueWithProcessStatementArg());
			sb.append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("), $L)");
		
		cb.addStatement(sb.toString(), Assert.class, imFieldName, methodName, ClassTypeMatcher.get(returnType).processAssertStatementArg(assertVal));
		return cb.build();
	}
	
	class MockInputArg {
		
		private Class<?> argClass;
		private String argName;
		private Object mockVal;

		public MockInputArg(Class<?> argClass, String argName, Object mockVal) {
			this.argClass = argClass;
			this.argName = argName;
			this.mockVal = mockVal;
		}

		public Class<?> getArgClass() {
			return argClass;
		}

		public void setArgClass(Class<?> argClass) {
			this.argClass = argClass;
		}

		public Object getMockVal() {
			return mockVal;
		}

		public void setMockVal(Object mockVal) {
			this.mockVal = mockVal;
		}
		
	}

	class PreMockVarStatement {
		
		private GCFieldStore fs;
		private JCVariableDecl jv;
		
		private String mockVariableName;
		private String mockMethod;
		
		private String varDecName;

		public PreMockVarStatement(GCFieldStore fs, JCVariableDecl jv) {
			this.fs = fs;
			this.jv = jv;
			
			mockVariableName = fs.getName();
			
			if (jv.init instanceof JCMethodInvocation) {
				JCMethodInvocation jmi = (JCMethodInvocation) jv.init;
				if (jmi.meth instanceof JCFieldAccess) {
					JCFieldAccess jfa = (JCFieldAccess) jmi.meth;
					mockMethod = jfa.name.toString();
				}
			}
			
			varDecName = jv.name.toString();
		}

		public GCFieldStore getFs() {
			return fs;
		}

		public String getMockVariableName() {
			return mockVariableName;
		}

		public String getMockMethod() {
			return mockMethod;
		}

		public JCVariableDecl getJv() {
			return jv;
		}

		public String getVarDecName() {
			return varDecName;
		}
		
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (StatementTree st : statementTrees) {
			sb.append(st.toString());
			sb.append("\n");
		}
		return sb.toString();
	}

}
