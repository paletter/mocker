package com.paletter.stdy.tcg.ast;

import java.util.ArrayList;
import java.util.List;

import com.sun.source.tree.StatementTree;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

public class ReturnBranch {
	
	private List<JCVariableDecl> variables = new ArrayList<JCVariableDecl>();
	
	private List<StatementTree> statementTrees = new ArrayList<StatementTree>();
	
	public void addStatement(StatementTree st) {
		statementTrees.add(st);
		
		if (st instanceof JCVariableDecl) {
			JCVariableDecl jc = (JCVariableDecl) st;
			variables.add(jc);
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
