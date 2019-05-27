package com.paletter.stdy.mockito;

public class ParentMocker {

	private String user;
	private ChildrenMocker cm;
	
	public Integer goMock(String arg1, Integer arg2) {
		
		cm.setName(arg1);
		String name = cm.getName();
		user = "b";
		
		if (name.equals("a")) {
			return 1;
		}
		
		return 10;
	}
	
	public Integer test() {
		return 100;
	}
}
