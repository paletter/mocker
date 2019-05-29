package com.paletter.stdy.tcg.ast.store;

public class GCMethodArgStore {

	private String argName;
	private String argType;
	private Class<?> argClass;
	private Object value;
	
	public GCMethodArgStore(String argName, String argType) {
		this.argName = argName;
		this.argType = argType;
	}

	public GCMethodArgStore(String argName, Class<?> argClass, Object value) {
		this.argName = argName;
		this.argClass = argClass;
		this.value = value;
	}

	public String getArgName() {
		return argName;
	}

	public void setArgName(String argName) {
		this.argName = argName;
	}

	public String getArgType() {
		return argType;
	}

	public void setArgType(String argType) {
		this.argType = argType;
	}

	public Class<?> getArgClass() {
		return argClass;
	}

	public void setArgClass(Class<?> argClass) {
		this.argClass = argClass;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
	
}
