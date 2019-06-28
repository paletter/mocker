package com.paletter.stdy.tcg.ast.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.mockito.Mockito;

import com.paletter.stdy.tcg.ast.store.GCFieldStore;
import com.paletter.stdy.tcg.ast.store.GCMethodArgStore;
import com.paletter.stdy.tcg.ast.store.GCMethodInputArgStore;
import com.paletter.stdy.tcg.ast.support.ClassTypeMatcher;
import com.squareup.javapoet.CodeBlock;
import com.sun.source.tree.StatementTree;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCParens;
import com.sun.tools.javac.tree.JCTree.JCReturn;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

public class ReturnBranch {
	
	public static final Integer METHOD_INPUT_ARG = 1;
	public static final Integer METHOD_INSIDE_ARG = 2;
	public static final Integer CLASS_FIELD_ARG = 3;
	
	private MethodAnalysis methodAnalysis;
	private List<StatementTree> statementTrees = new ArrayList<StatementTree>();
	private Map<String, GCMethodArgStore> methodInputArgs = new HashMap<String, GCMethodArgStore>();
	private Map<String, GCMethodArgStore> methodInsideArgs = new HashMap<String, GCMethodArgStore>();
	
	private Map<FindVar, Object> preVars = new HashMap<FindVar, Object>();
	
	private List<IfConditionStore> preIfConds = new ArrayList<IfConditionStore>();
	
	public ReturnBranch(MethodAnalysis methodAnalysis, ReturnBranch rb) {
		this(methodAnalysis);
		
		this.preIfConds.addAll(rb.getPreIfConds());
		this.statementTrees.addAll(rb.getStatementTrees());
	}
	
	public ReturnBranch(MethodAnalysis methodAnalysis) {
		this.methodAnalysis = methodAnalysis;
		
		initMethodInputArg();
	}
	
	public ReturnBranch split() {
		ReturnBranch splitrb = new ReturnBranch(methodAnalysis);
		splitrb.getPreIfConds().addAll(this.getPreIfConds());
		splitrb.getStatementTrees().addAll(this.statementTrees);
		return splitrb;
	}

	public void addStatement(StatementTree st) {
		statementTrees.add(st);
	}
	
	public void addStatement(List<JCStatement> sts) {
		statementTrees.addAll(sts);
	}

	public void addPreIfConds(JCParens jp, boolean positive) {
		preIfConds.add(new IfConditionStore(jp, positive));
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
			
			// PreAnalyse if
			analysePreCond();
			
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
								
								if (preVars.containsKey(new FindVar(argName, METHOD_INSIDE_ARG))) {
									Object expectVal = preVars.get(new FindVar(argName, METHOD_INSIDE_ARG));
									cb.add(createMockStatement(jmi, ctm.processStatementArg(expectVal)));
									mas.setValue(expectVal);
								} else {
									Object expectVal = ctm.getCommonValue();
									cb.add(createMockStatement(jmi, ctm.processStatementArg(expectVal)));
									mas.setValue(expectVal);
								}
								
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
					
					break;
				}
			}
			
			if (!returnType.equals(void.class) && !isReturn) return null;
			
			if (!isReturn) cb.add(createNotReturnStatement(methodInputArgs.values()));
			
			return cb.build();
		
		} catch (Throwable e) {
			return null;
		}
	}

	private void analysePreCond() {
		
		Set<FindVar> methodInsideFvs = new HashSet<FindVar>();
		for (StatementTree st : statementTrees) {

			if (st instanceof JCVariableDecl) {
				JCVariableDecl jv = (JCVariableDecl) st;
				String argName = jv.name.toString();
				if (jv.init instanceof JCMethodInvocation) {
					JCMethodInvocation jmi = (JCMethodInvocation) jv.init;
					if (jmi.meth instanceof JCFieldAccess) {
						methodInsideFvs.add(new FindVar(argName, METHOD_INSIDE_ARG));
					}
				}
			}
		}
		
		// Do negative condition first
		for (IfConditionStore ics : preIfConds) {
			JCParens jp = ics.getJp();
			
			// Negative condition
			if (!ics.positive) {

				if (jp.expr instanceof JCBinary) {
					
					JCBinary jb = (JCBinary) jp.expr;
					
					// i == 1
					if (jb.lhs instanceof JCIdent && jb.rhs instanceof JCLiteral) {
						JCIdent lhs = (JCIdent) jb.lhs;
						JCLiteral rhs = (JCLiteral) jb.rhs;
						ClassTypeMatcher rhsCtm = ClassTypeMatcher.get(rhs.value.getClass());
						if (rhsCtm.equals(ClassTypeMatcher.OBJECT)) {
							throw new RuntimeException("analyseIf fail/1");
						}
						
						FindVar findVar = findVar(lhs.getName().toString());
						if (methodInsideFvs.contains(new FindVar(lhs.getName().toString(), METHOD_INSIDE_ARG))) {
							
							// MethodInsideArg
							preVars.put(new FindVar(lhs.getName().toString(), METHOD_INSIDE_ARG), rhsCtm.getRandomValue());
							
						} else if (findVar != null && findVar.getType().equals(METHOD_INPUT_ARG)) {
							
							// MethodInputArg
							methodInputArgs.get(findVar.getName()).setValue(rhsCtm.getRandomValue());
							
						} else if (findVar != null && findVar.getType().equals(CLASS_FIELD_ARG)) {
							
							// ClassFieldArg
							preVars.put(new FindVar(lhs.getName().toString(), CLASS_FIELD_ARG), rhsCtm.getRandomValue());
						}
					}
				}
			}
		}
		
		// Then do positive condition
		for (IfConditionStore ics : preIfConds) {
			JCParens jp = ics.getJp();
			
			// Positive condition
			if (ics.positive) {
				
				if (jp.expr instanceof JCBinary) {
					
					JCBinary jb = (JCBinary) jp.expr;
					
					// i == 1
					if (jb.lhs instanceof JCIdent && jb.rhs instanceof JCLiteral) {
						JCIdent lhs = (JCIdent) jb.lhs;
						JCLiteral rhs = (JCLiteral) jb.rhs;
						ClassTypeMatcher ctm = ClassTypeMatcher.get(rhs.value.getClass());
						if (ctm.equals(ClassTypeMatcher.OBJECT)) {
							throw new RuntimeException("analyseIf fail/1");
						}
						
						FindVar findVar = findVar(lhs.getName().toString());
						if (methodInsideFvs.contains(new FindVar(lhs.getName().toString(), METHOD_INSIDE_ARG))) {
							
							// MethodInsideArg
							preVars.put(new FindVar(lhs.getName().toString(), METHOD_INSIDE_ARG), rhs.value);
							
						} else if (findVar != null && findVar.getType().equals(METHOD_INPUT_ARG)) {
							
							// MethodInputArg
							methodInputArgs.get(findVar.getName()).setValue(rhs.value);
							
						} else if (findVar != null && findVar.getType().equals(CLASS_FIELD_ARG)) {
							
							// ClassFieldArg
							preVars.put(new FindVar(lhs.getName().toString(), CLASS_FIELD_ARG), rhs.value);
						}
					}
				}
			}
		}
	}
	
	private void analyseIf() {
		
		Set<FindVar> methodInsideFvs = new HashSet<FindVar>();
		for (StatementTree st : statementTrees) {

			if (st instanceof JCVariableDecl) {
				JCVariableDecl jv = (JCVariableDecl) st;
				String argName = jv.name.toString();
				if (jv.init instanceof JCMethodInvocation) {
					JCMethodInvocation jmi = (JCMethodInvocation) jv.init;
					if (jmi.meth instanceof JCFieldAccess) {
						methodInsideFvs.add(new FindVar(argName, METHOD_INSIDE_ARG));
					}
				}
			}
			
			if (st instanceof JCIf) {
				JCIf jif = (JCIf) st;

				if (jif.cond instanceof JCParens) {
					
					JCParens jp = (JCParens) jif.cond;
					
					if (jp.expr instanceof JCBinary) {
						
						JCBinary jb = (JCBinary) jp.expr;
						
						// i == 1
						if (jb.lhs instanceof JCIdent && jb.rhs instanceof JCLiteral) {
							JCIdent lhs = (JCIdent) jb.lhs;
							JCLiteral rhs = (JCLiteral) jb.rhs;
							ClassTypeMatcher ctm = ClassTypeMatcher.get(rhs.value.getClass());
							if (ctm.equals(ClassTypeMatcher.OBJECT)) {
								throw new RuntimeException("analyseIf fail/1");
							}
							
							FindVar findVar = findVar(lhs.getName().toString());
							if (methodInsideFvs.contains(new FindVar(lhs.getName().toString(), METHOD_INSIDE_ARG))) {
								preVars.put(new FindVar(lhs.getName().toString(), METHOD_INSIDE_ARG), rhs.value);
							} else if (findVar != null && findVar.getType().equals(METHOD_INPUT_ARG)) {
								methodInputArgs.get(findVar.getName()).setValue(rhs.value);
							} else if (findVar != null && findVar.getType().equals(CLASS_FIELD_ARG)) {
								preVars.put(new FindVar(lhs.getName().toString(), CLASS_FIELD_ARG), rhs.value);
							}
						}
					}
				}
			}
		}
		
		while (filterIfStatement()) {
			
		}
	}
	
	private boolean filterIfStatement() {
		boolean isHaveIf = false;
		List<StatementTree> newSts = new ArrayList<StatementTree>();
		for (StatementTree st : statementTrees) {
			if (st instanceof JCIf) {
				JCIf jif = (JCIf) st;
				JCBlock jb = (JCBlock) jif.thenpart;
				newSts.addAll(jb.stats);
			} else {
				newSts.add(st);
			}
		}
		statementTrees = newSts;
		return isHaveIf;
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
	
	private void initMethodInputArg() {

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
	
	private FindVar findVar(String name) {
		
		if (methodInsideArgs.containsKey(name)) {
			GCMethodArgStore mas = methodInsideArgs.get(name);
			FindVar rlt = new FindVar();
			rlt.setName(mas.getArgName());
			rlt.setVal(mas.getValue());
			rlt.setType(METHOD_INSIDE_ARG);
			return rlt;
		}
		
		if (methodInputArgs.containsKey(name)) {
			GCMethodArgStore mas = methodInputArgs.get(name);
			FindVar rlt = new FindVar();
			rlt.setCla(mas.getArgClass());
			rlt.setVal(mas.getValue());
			rlt.setType(METHOD_INPUT_ARG);
			return rlt;
		}
		
		GCFieldStore classFs = methodAnalysis.getClassAnalysis().findGCField(name);
		if (classFs != null) {
			FindVar rlt = new FindVar();
			rlt.setCla(classFs.getField().getType());
			rlt.setVal(classFs.getVal());
			rlt.setType(CLASS_FIELD_ARG);
			return rlt;
		}
		
		return null;
	}

	class FindVar {
		
		private String name;
		private Class<?> cla;
		private Object val;
		private Integer type;

		public FindVar() {
			super();
		}
		
		public FindVar(String name, Integer type) {
			super();
			this.name = name;
			this.type = type;
		}

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

		public Integer getType() {
			return type;
		}

		public void setType(Integer type) {
			this.type = type;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) return false;
			if (!(obj instanceof FindVar)) return false;
			FindVar fv = (FindVar) obj;
			return this.name.equals(fv.getName()) && this.type.equals(fv.getType());
		}

		@Override
		public int hashCode() {
			return name.hashCode();
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
				sb.append(ctm.processStatementArg(mas.getValue()));
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
	
	public MethodAnalysis getMethodAnalysis() {
		return methodAnalysis;
	}

	public List<StatementTree> getStatementTrees() {
		return statementTrees;
	}
	
	class IfConditionStore {
		private JCParens jp;
		private boolean positive;
		
		public IfConditionStore(JCParens jp, boolean positive) {
			this.jp = jp;
			this.positive = positive;
		}

		public JCParens getJp() {
			return jp;
		}

		public void setJp(JCParens jp) {
			this.jp = jp;
		}

		public boolean isPositive() {
			return positive;
		}

		public void setPositive(boolean positive) {
			this.positive = positive;
		}

		@Override
		public String toString() {
			return jp.toString() + "," + positive;
		}
	}
	
	public List<IfConditionStore> getPreIfConds() {
		return preIfConds;
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
