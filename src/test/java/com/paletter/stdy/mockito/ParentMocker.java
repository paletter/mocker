package com.paletter.stdy.mockito;

public class ParentMocker {

	private ChildrenMocker cm;
	
	public String goMock(String str1, Integer str2) {
		
		String name = cm.getName();
		
		if (name.equals("a")) {
			return null;
		}
		
		return name;
	}
	
	public String test() {
		return "1";
	}
	
	public ChildrenMocker test2() {
		return new ChildrenMocker();
	}
}
