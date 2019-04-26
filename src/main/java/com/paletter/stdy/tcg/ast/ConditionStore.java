package com.paletter.stdy.tcg.ast;

import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCIf;

public class ConditionStore {

	private JCIf ji;
	private ConditionStore next;

	public ConditionStore(JCIf ji) {
		this.ji = ji;
	}

	public JCIf getJi() {
		return ji;
	}

	public void setJi(JCIf ji) {
		this.ji = ji;
	}

	public void link(ConditionStore next) {
		this.next = next;
	}
}
