package com.paletter.stdy.tcg.ast;

import java.util.ArrayList;
import java.util.List;

import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

public class ClassAnalysis {

	private List<JCVariableDecl> variables = new ArrayList<JCVariableDecl>();
	
	public void addVariable(JCVariableDecl v) {
		variables.add(v);
	}
}
