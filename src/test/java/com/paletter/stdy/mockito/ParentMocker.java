package com.paletter.stdy.mockito;

import java.math.BigDecimal;

public class ParentMocker {

	private ChildrenMocker cm;
	
	public String goMock(String str1, Integer str2) {
		
		String name = cm.getName();
		
		if (name.equals("a")) {
			return null;
		}
		
		return name;
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
	
	public float getPFloat() {
		return 1f;
	}
	
	public Double getDouble() {
		return 1d;
	}
	
	public double getPDouble() {
		return 1d;
	}

	public BigDecimal getBigDecimal() {
		return new BigDecimal(10);
	}
}
