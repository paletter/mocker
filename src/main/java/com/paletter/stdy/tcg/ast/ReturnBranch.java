package com.paletter.stdy.tcg.ast;

import java.util.ArrayList;
import java.util.List;

import com.sun.source.tree.StatementTree;

public class ReturnBranch {
	
	List<StatementTree> statementTrees = new ArrayList<StatementTree>();
	
	public void addStatement(StatementTree st) {
		statementTrees.add(st);
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
