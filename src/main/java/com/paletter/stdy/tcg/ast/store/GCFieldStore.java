package com.paletter.stdy.tcg.ast.store;

import java.lang.reflect.Field;

public class GCFieldStore {

	private Field field;
	private String name;
	private Object val;
	
	public GCFieldStore(Field field, String name) {
		this.field = field;
		this.name = name;
	}

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getVal() {
		return val;
	}

	public void setVal(Object val) {
		this.val = val;
	}
	
}
