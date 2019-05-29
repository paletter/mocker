package com.paletter.stdy.mockito;

public class ChildrenMocker {

	private String name;

	public Integer getId() {
		return 100;
	}
	
	public Integer getId(Integer i) {
		return 100;
	}
	
	public String getName() {
		return name;
	}

	public String getName(Integer id) {
		return name;
	}
	
	public String getName(String id) {
		return name;
	}
	
	public String getName(ChildrenMocker cm) {
		return name;
	}
	
}
