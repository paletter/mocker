package com.paletter.stdy.tcg.ast;

import java.util.ArrayList;
import java.util.List;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

public class MethodAnalysis {

	private MethodTree methodTree;
	
	private List<ReturnBranch> rbs = new ArrayList<ReturnBranch>();

	public MethodAnalysis(MethodTree methodTree) {
		this.methodTree = methodTree;
	}

	public void addReturnBranch(ReturnBranch rb) {
		rbs.add(rb);
	}
}
