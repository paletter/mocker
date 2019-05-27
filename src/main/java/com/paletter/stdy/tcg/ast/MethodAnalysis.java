package com.paletter.stdy.tcg.ast;

import java.util.ArrayList;
import java.util.List;

import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

public class MethodAnalysis {

	private List<JCVariableDecl> variables = new ArrayList<JCVariableDecl>();
	private List<ReturnBranch> rbs = new ArrayList<ReturnBranch>();
	
	public void addReturnBranch(ReturnBranch rb) {
		rbs.add(rb);
	}
}
