package com.paletter.stdy.tcg.ast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.mockito.Mockito;

import com.paletter.stdy.tcg.ast.store.GCFieldStore;
import com.paletter.stdy.tcg.ast.store.GCMethodArgStore;
import com.paletter.stdy.tcg.ast.store.GCMethodInputArgStore;
import com.squareup.javapoet.CodeBlock;
import com.sun.source.tree.StatementTree;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCReturn;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

public class ReturnBranchV3 {
	
	private MethodAnalysis methodAnalysis;
	private List<StatementTree> statementTrees = new ArrayList<StatementTree>();
	private Map<String, GCMethodArgStore> methodInputArgs = new HashMap<String, GCMethodArgStore>();
	private Map<String, GCMethodArgStore> methodInsideArgs = new HashMap<String, GCMethodArgStore>();
	
	public ReturnBranchV3(MethodAnalysis methodAnalysis) {
		this.methodAnalysis = methodAnalysis;
		
		// Mock input arguments
		for (GCMethodInputArgStore mias : methodAnalysis.getInputArgs().values()) {
			ClassTypeMatcher ctm = ClassTypeMatcher.get(mias.getArgCla());
			if (ctm != null) {
				methodInputArgs.put(mias.getArgName(), new GCMethodArgStore(mias.getArgName(), mias.getArgCla(), ctm.getCommonValue()));
			} else {
				methodInputArgs.put(mias.getArgName(), new GCMethodArgStore(mias.getArgName(), mias.getArgCla(), null));
			}
		}
	}

	public void addStatement(StatementTree st) {
		statementTrees.add(st);
	}

	public CodeBlock generateCode() {
		
		try {
			
			for (StatementTree st : statementTrees) {
				
				// Static method run code
				if (st instanceof JCExpressionStatement) return null;
				
				if (st instanceof JCVariableDecl) {
					JCVariableDecl jv = (JCVariableDecl) st;
					if (jv.init instanceof JCMethodInvocation) {
						JCMethodInvocation jmi = (JCMethodInvocation) jv.init;
						if (jmi.meth instanceof JCFieldAccess) {
							JCFieldAccess jfa = (JCFieldAccess) jmi.meth;
							// If static method JCVariableDecl 
							if (!(jfa.selected instanceof JCIdent)) return null;
						}
					}
				}
			}
			
			CodeBlock.Builder cb = CodeBlock.builder();
			
			Class<?> returnType = methodAnalysis.getMethod().getReturnType();
			ClassTypeMatcher returnTypeCtm = ClassTypeMatcher.get(returnType);
			
			boolean isReturn = false;
			for (StatementTree st : statementTrees) {
				
				if (st instanceof JCVariableDecl) {
					JCVariableDecl jv = (JCVariableDecl) st;
					
					String argName = jv.name.toString();
					String argType = jv.vartype.toString();
					
					GCMethodArgStore mas = new GCMethodArgStore(argName, argType);
					
					if (jv.init instanceof JCLiteral) {
						JCLiteral jvInit = (JCLiteral) jv.init;
						Object initVal = jvInit.value;
						mas.setValue(initVal);
					}
					
					if (jv.init instanceof JCMethodInvocation) {
						JCMethodInvocation jmi = (JCMethodInvocation) jv.init;
						if (jmi.meth instanceof JCFieldAccess) {
							
							// Mock variable
							ClassTypeMatcher ctm = ClassTypeMatcher.get(argType);
							if (!ctm.equals(ClassTypeMatcher.OBJECT)) {
								Object expectVal = ctm.getCommonValue();
								cb.add(createMockStatement(jmi, ctm.processStatementArg(expectVal)));
								
								mas.setValue(expectVal);
							} else {
								cb.add(createMockStatement(jmi, "null"));
								
								mas.setValue(null);
							}
						}
					}
					
					methodInsideArgs.put(argName, mas);
				}
				
				if (st instanceof JCReturn) {
					
					isReturn = true;
					JCReturn jr = (JCReturn) st;
					
					if (jr.expr == null) {
						
						cb.add(createNotReturnStatement(methodInputArgs.values()));
						
					} else if (jr.expr instanceof JCIdent) {
						
						JCIdent rji = (JCIdent) jr.expr;
						
						FindVar findVar = findVar(rji.name.toString());
						// Assert return
						cb.add(createAssertReturnStatement(findVar.getVal(), methodInputArgs.values()));
					
					} else if (jr.expr instanceof JCMethodInvocation) {
						
						JCMethodInvocation jmi = (JCMethodInvocation) jr.expr;
						if (jmi.meth instanceof JCFieldAccess) {
							
							// Mock variable
							Object expectVal = returnTypeCtm.getCommonValue();
							cb.add(createMockStatement(jmi, returnTypeCtm.processStatementArg(expectVal)));
							
							// Assert return
							cb.add(createAssertReturnStatement(expectVal, methodInputArgs.values()));
						}
						
					} else if (jr.expr instanceof JCLiteral) {
						
						JCLiteral jl = (JCLiteral) jr.expr;
						// Assert return
						cb.add(createAssertReturnStatement(jl.getValue(), methodInputArgs.values()));
					}
				}
			}
			
			if (!isReturn) cb.add(createNotReturnStatement(methodInputArgs.values()));
			
			return cb.build();
		
		} catch (Throwable e) {
			return null;
		}
	}
	
	private CodeBlock createMockStatement(JCMethodInvocation jmi, Object expectVal) {
		JCFieldAccess jfa = (JCFieldAccess) jmi.meth;
		JCIdent selected = (JCIdent) jfa.selected;
		
		String mockVariableName = selected.getName().toString();
		String mockMethod = jfa.name.toString();
		StringBuilder sb = new StringBuilder();
		if (jmi.args == null || jmi.args.isEmpty()) {
			
			sb.append("$T.when($L.$L())");
		} else {
			
			sb.append("$T.when($L.$L(");
			for (Object arg : jmi.args) {
				
				if (arg instanceof JCLiteral) {
					
					JCLiteral argJl = (JCLiteral) arg;
					ClassTypeMatcher ctm = ClassTypeMatcher.get(argJl.value.getClass());
					sb.append(ctm.processStatementArg(argJl.value));
					
				} else if (arg instanceof JCIdent) {
					
					JCIdent argJi = (JCIdent) arg;
					FindVar findVar = findVar(argJi.getName().toString());
					
					if (findVar.getVal() == null)  {
						
						sb.append("Mockito.any()");	
					} else {
						
						ClassTypeMatcher ctm = ClassTypeMatcher.get(findVar.getVal().getClass());
						if (ctm.equals(ClassTypeMatcher.OBJECT)) {
							sb.append("Mockito.any()");
						} else {
							sb.append(ctm.processStatementArg(findVar.getVal()));
						}
					}
				
				} else if (arg instanceof JCFieldAccess) {
					
					JCFieldAccess argJfa = (JCFieldAccess) arg;
					sb.append(argJfa.toString());
					
				} else {
					
					sb.append("null");
				}
				
				sb.append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append("))");
		}
		
		sb.append(".thenReturn($L)");
		CodeBlock cb = CodeBlock.builder()
				.addStatement(sb.toString(), Mockito.class, mockVariableName, mockMethod, expectVal)
				.build();
		return cb;
	}
	
	private FindVar findVar(String name) {
		
		if (methodInsideArgs.containsKey(name)) {
			GCMethodArgStore mas = methodInsideArgs.get(name);
			FindVar rlt = new FindVar();
			rlt.setName(mas.getArgName());
			rlt.setVal(mas.getValue());
			return rlt;
		}
		
		if (methodInputArgs.containsKey(name)) {
			GCMethodArgStore mas = methodInputArgs.get(name);
			FindVar rlt = new FindVar();
			rlt.setCla(mas.getArgClass());
			rlt.setVal(mas.getValue());
			return rlt;
		}
		
		GCFieldStore classFs = methodAnalysis.getClassAnalysis().findGCField(name);
		if (classFs != null) {
			FindVar rlt = new FindVar();
			rlt.setCla(classFs.getField().getType());
			rlt.setVal(classFs.getVal());
			return rlt;
		}
		
		return null;
	}

	class FindVar {
		
		private String name;
		private Class<?> cla;
		private Object val;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Object getVal() {
			return val;
		}

		public void setVal(Object val) {
			this.val = val;
		}

		public Class<?> getCla() {
			return cla;
		}

		public void setCla(Class<?> cla) {
			this.cla = cla;
		}
		
	}
	
	private CodeBlock createAssertReturnStatement(Object assertVal, Collection<GCMethodArgStore> mockInputArgs) {
		CodeBlock.Builder cb = CodeBlock.builder();
		
		String imFieldName = methodAnalysis.getClassAnalysis().getGcImFieldName();
		String methodName = methodAnalysis.getMethodTree().getName().toString();
		
		Class<?> returnType = methodAnalysis.getMethod().getReturnType();
		
		StringBuilder sb = new StringBuilder();
		if (mockInputArgs == null || mockInputArgs.isEmpty()) {
			
			sb.append("$T.assertEquals($L.$L(), $L)");
			
		} else {
			
			sb.append("$T.assertEquals($L.$L(");
			for (GCMethodArgStore mas : mockInputArgs) {
				ClassTypeMatcher ctm = ClassTypeMatcher.get(mas.getArgClass());
				sb.append(ctm.getCommonValueWithProcessStatementArg());
				sb.append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append("), $L)");
		}
		
		cb.addStatement(sb.toString(), Assert.class, imFieldName, methodName, ClassTypeMatcher.get(returnType).processAssertStatementArg(assertVal));
		return cb.build();
	}
	
	private CodeBlock createNotReturnStatement(Collection<GCMethodArgStore> mockInputArgs) {
		CodeBlock.Builder cb = CodeBlock.builder();
		
		String imFieldName = methodAnalysis.getClassAnalysis().getGcImFieldName();
		String methodName = methodAnalysis.getMethodTree().getName().toString();
		
		StringBuilder sb = new StringBuilder();
		if (mockInputArgs == null || mockInputArgs.isEmpty()) {
			
			sb.append("$L.$L()");
			
		} else {
			
			sb.append("$L.$L()");
			for (GCMethodArgStore mas : mockInputArgs) {
				ClassTypeMatcher ctm = ClassTypeMatcher.get(mas.getArgClass());
				sb.append(ctm.getCommonValueWithProcessStatementArg());
				sb.append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(")");
		}
		
		cb.addStatement(sb.toString(), imFieldName, methodName);
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
