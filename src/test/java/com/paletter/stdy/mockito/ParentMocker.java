package com.paletter.stdy.mockito;

import java.math.BigDecimal;
import java.util.Date;

public class ParentMocker {

	private Integer id2;
	private ChildrenMocker cm;

	public String getMockVal8() {
		Date d = new Date();
		TestDTO dto = new TestDTO();
		return cm.getName3(dto);
	}
	
	public String getMockVal7(Integer i) {
		Integer id = cm.getId(i);
		return cm.getName(id);
	}
	
	public String getMockVal6() {
		Integer i = 1;
		Integer id = cm.getId(i);
		return cm.getName(id);
	}
	
	public String getMockVal5() {
		Integer id = cm.getId(2);
		return cm.getName(id);
	}
	
	public String getMockVal4() {
		Integer id = cm.getId();
		return cm.getName(id);
	}
	
	public String getMockVal() {
		return cm.getName();
	}
	
	public String getMockVal2() {
		Integer id = 1;
		return cm.getName(id);
	}
	
	public String getMockVal3() {
		return cm.getName("abs");
	}
	
	public void getString7() {
		Integer id = 1;
		String s = cm.getName(id);
	}
	
	public void getString6() {
		return;
	}
	
	public String getString5(String s) {
		return s;
	}
	
	String s2 = "aaa";
	public String getString4() {
		return s2;
	}
	
	public String getString3() {
		String s = cm.getName();
		return s;
	}
	
	public String getString2() {
		String s = "abc";
		return s;
	}
	
	public String getString() {
		return "1";
	}
	
	public Integer getInteger() {
		return 1;
	}
	
	public int getPInt() {
		return 1;
	}
	
	public Long getLong() {
		return 1l;
	}
	
	public long getPLong() {
		return 1l;
	}
	
	public Float getFloat() {
		return 1f;
	}
	
//	public float getPFloat() {
//		return 1f;
//	}
	
	public Double getDouble() {
		return 1d;
	}
	
//	public double getPDouble() {
//		return 1d;
//	}

	public BigDecimal getBigDecimal() {
		return new BigDecimal(10);
	}
}
