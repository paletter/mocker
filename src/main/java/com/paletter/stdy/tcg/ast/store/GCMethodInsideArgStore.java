package com.paletter.stdy.tcg.ast.store;

public class GCMethodInsideArgStore {

	private Class<?> argCla;
	private String argName;
	
	public GCMethodInsideArgStore(Class<?> argCla, String argName) {
		this.argCla = argCla;
		this.argName = argName;
	}

	public Class<?> getArgCla() {
		return argCla;
	}

	public void setArgCla(Class<?> argCla) {
		this.argCla = argCla;
	}

	public String getArgName() {
		return argName;
	}

	public void setArgName(String argName) {
		this.argName = argName;
	}

}
