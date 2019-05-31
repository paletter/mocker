package com.paletter.stdy.mockito;

public class ParentMocker2 {

	private ChildrenMocker cm;
	
	public String if6() {
		int i = cm.getId();
		if (i == 100) {
			int i2 = cm.getId2();
			if (i2 == 1000) {
				int i3 = cm.getId3();
				if (i3 == 00) {
					return "a";
				}
				return "b";
			}
			return "c";
		}
		return "d";
	}
	
	public String if5() {
		int i = cm.getId();
		if (i == 100) {
			
			int i2 = cm.getId2();
			if (i2 == 1000) {
				
				int i3 = cm.getId3();
				if (i3 == 00) {
					return "a";
				} else {
					return "e";
				}
			} else {
				return "b";
			}
			
		} else if (i == 500) {
			return "c";
		} else {
			return "d";
		}
	}
	
	public String if4() {
		int i = cm.getId();
		if (i == 100) {
			return "a";
		} else if (i == 500) {
			return "b";
		} else {
			return "c";
		}
	}
	
	public String if3() {
		int i = cm.getId();
		if (i == 100) {
			return "b";
		} else {
			return "c";
		}
	}
	
	public String if2() {
		int i = cm.getId();
		if (i == 100) {
			return "b";
		}
		return "a";
	}
	
	public String if1() {
		int i = 1;
		if (i == 1) {
			return "a";
		}
		return "a";
	}
}
