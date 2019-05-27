package com.paletter.stdy.mockito;

public class ParentMocker {

	ChildrenMocker cm;
	
	public Integer goMock(String arg1, Integer arg2) {
		cm.setName(arg1);
		String name = cm.getName();
		
		if (name.equals("a")) {
			return 1;
		}
		
		return 0;
	}
}
