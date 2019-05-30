package com.paletter.stdy.mockito;

public class ParentMocker2 {

	private ChildrenMocker cm;
	
	public String if2() {
		int i = cm.getId();
		if (i == 100) {
			return "b";
		}
		return "a";
	}
	
//	public String if1() {
//		int i = 1;
//		if (i == 1) {
//			return "a";
//		}
//		return "a";
//	}
}
