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
	private JCReturn jr;
	
	public ReturnBranch(MethodAnalysis methodAnalysis) {
		this.methodAnalysis = methodAnalysis;
	}

	public void addStatement(StatementTree st) {
		statementTrees.add(st);
		
		if (st instanceof JCVariableDecl) {
			JCVariableDecl jc = (JCVariableDecl) st;
			variables.add(jc);
		}
		
		if (st instanceof JCReturn) {
			JCReturn jr = (JCReturn) st;
			this.jr = jr;
		}
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
		List<PreMockStatement> pmss = new ArrayList<PreMockStatement>();
		
		// Mock input arguments
		Map<String, MockInputArg> mockInputArgs = new HashMap<String, MockInputArg>();
		for (int i = 0; i < methodAnalysis.getMethod().getParameterTypes().length; i ++) {
			Class<?> argClass = methodAnalysis.getMethod().getParameterTypes()[i];
			JCVariableDecl jvd = (JCVariableDecl) methodAnalysis.getMethodTree().getParameters().get(i);
			String argName = jvd.name.toString();
			if (argClass.equals(String.class)) {
				mockInputArgs.put(argName, new MockInputArg(argClass, argName, CommonUtils.COMMON_STRING));
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
							pmss.add(new PreMockStatement(classFs, jv));
						}
					}
				}
			}
			
			if (st instanceof JCReturn) {
				JCReturn jr = (JCReturn) st;
				this.jr = jr;
				
				if (jr.expr instanceof JCIdent) {
					
					JCIdent rji = (JCIdent) jr.expr;
					
					for (PreMockStatement pms : pmss) {
						if (pms.getVarDecName().equals(rji.name.toString())) {
							
							if (returnType.isPrimitive()) {
								
								
							} else {
								
								if (returnType.equals(String.class)) {
									String assertVal = CommonUtils.COMMON_STRING;
									cb.addStatement("$T.when($L.$L()).thenReturn($S)", Mockito.class, pms.getMockVariableName(), pms.getMockMethod(), assertVal);
									cb.add(createAssertReturnStatement(assertVal, mockInputArgs.values()));
								}
							}
						}
					}
					
				} else if (jr.expr instanceof JCLiteral) {
					
					JCLiteral jl = (JCLiteral) jr.expr;
					
					if (returnType.isPrimitive()) {
						
						if (returnType.getName().equals("int")) {
							cb.addStatement("$T.assertEquals($L.$L(), $L)", Assert.class, imFieldName, methodName, jl.getValue());
						} else if (returnType.getName().equals("long")) {
							cb.addStatement("$T.assertEquals($L.$L(), $LL)", Assert.class, imFieldName, methodName, jl.getValue());
						} else if (returnType.getName().equals("float")) {
							cb.addStatement("$T.assertEquals($L.$L(), $LF)", Assert.class, imFieldName, methodName, jl.getValue());
						} else if (returnType.getName().equals("double")) {
							cb.addStatement("$T.assertEquals($L.$L(), $LD)", Assert.class, imFieldName, methodName, jl.getValue());
						}
						
					} else {
						
						if (returnType.equals(Integer.class)) {
							cb.addStatement("$T.assertEquals($L.$L(), $L)", Assert.class, imFieldName, methodName, jl.getValue());
						} else if (returnType.equals(Long.class)) {
							cb.addStatement("$T.assertEquals($L.$L(), $LL)", Assert.class, imFieldName, methodName, jl.getValue());
						} else if (returnType.equals(Float.class)) {
							cb.addStatement("$T.assertEquals($L.$L(), $LF)", Assert.class, imFieldName, methodName, jl.getValue());
						} else if (returnType.equals(Double.class)) {
							cb.addStatement("$T.assertEquals($L.$L(), $LD)", Assert.class, imFieldName, methodName, jl.getValue());
						} else if (returnType.equals(String.class)) {
							cb.addStatement("$T.assertEquals($L.$L(), $S)", Assert.class, imFieldName, methodName, jl.getValue());
						}
					}
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
			if (mia.getArgClass().equals(String.class)) {
				sb.append("\"");
				sb.append(mia.getMockVal().toString());
				sb.append("\"");
			} else {
				sb.append(mia.getMockVal().toString());
			}
			
			sb.append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("), $L)");
		
		if (returnType.equals(String.class)) {
			cb.addStatement(sb.toString(), Assert.class, imFieldName, methodName, "\"" + assertVal + "\"");
		}
		
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

	class PreMockStatement {
		
		private GCFieldStore fs;
		private JCVariableDecl jv;
		
		private String mockVariableName;
		private String mockMethod;
		
		private String varDecName;

		public PreMockStatement(GCFieldStore fs, JCVariableDecl jv) {
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
