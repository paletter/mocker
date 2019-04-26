package com.paletter.stdy.tcg.ast;

import java.util.ArrayList;
import java.util.List;

import com.sun.tools.javac.tree.JCTree.JCReturn;

public class ReturnBranch {

	private List<ConditionStore> conditions = new ArrayList<>();
	private JCReturn jcReturn;
	
	public void addCondition(ConditionStore cs) {
		conditions.add(cs);
	}
	
	public void over(JCReturn jcReturn) {
		this.jcReturn = jcReturn;
	}
}
