package com.paletter.stdy.tcg.ast;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public enum ClassTypeMatcher {

	PINT(true, "int", Integer.class),
	PLONG(true, "long", Long.class),
	PFLOAT(true, "float", Float.class),
	PDOUBLE(true, "double", Double.class),
	
	INTEGER(false, null, Integer.class),
	LONG(false, null, Long.class),
	FLOAT(false, null, Float.class),
	DOUBLE(false, null, Double.class),
	BIGDECIMAL(false, null, BigDecimal.class),
	STRING(false, null, String.class),
	
	OBJECT(false, null, Object.class),
	;
	
	private boolean isPrimitive;
	private String name;
	private Class<?> cla;
	
	private String processAssertSuffix = "";
	private String processAssertPrefix = "";
	private Object commonValue = null;

	private ClassTypeMatcher(boolean isPrimitive, String name, Class<?> cla) {
		this.isPrimitive = isPrimitive;
		this.name = name;
		this.cla = cla;
		
		if (cla.equals(Integer.class)) {
			
			commonValue = 0;
			
			if (!isPrimitive) {
				processAssertPrefix = "Integer.valueOf(";
				processAssertSuffix = ")";
			}
			
		} else if (cla.equals(Long.class)) {
			
			commonValue = 0L;
			
			if (!isPrimitive) {
				processAssertPrefix = "Long.valueOf(";
				processAssertSuffix = "l)";
			} else {
				processAssertSuffix = "l";				
			}
			
		} else if (cla.equals(Float.class)) {
			
			commonValue = 0F;

			if (!isPrimitive) {
				processAssertPrefix = "Float.valueOf(";
				processAssertSuffix = "f)";
			} else {
				processAssertSuffix = "f";				
			}
			
		} else if (cla.equals(Double.class)) {
			
			commonValue = 0D;
			
			if (!isPrimitive) {
				processAssertPrefix = "Double.valueOf(";
				processAssertSuffix = "d)";
			} else {
				processAssertSuffix = "d";				
			}
			
		} else if (cla.equals(String.class)) {
			
			commonValue = "testString";
			
			processAssertPrefix = "\"";
			processAssertSuffix = "\"";
			
		} else if (cla.equals(BigDecimal.class)) {
			
			commonValue = BigDecimal.ZERO;

			processAssertPrefix = "BigDecimal.valueOf(";
			processAssertSuffix = ")";
		}
	}

	public String processAssertStatementArg(Object arg) {
		if (arg == null) return null;
		return processAssertPrefix + arg + processAssertSuffix;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getCommonValue() {
		return (T) commonValue;
	}
	
	public String getCommonValueWithProcessStatementArg() {
		return processAssertStatementArg(getCommonValue());
	}
	
	private static Map<Class<?>, ClassTypeMatcher> cache = new HashMap<Class<?>, ClassTypeMatcher>();
	
	public static ClassTypeMatcher get(Class<?> cla) {
		
		if (cache.containsKey(cla)) return cache.get(cla);
		
		for (ClassTypeMatcher ctm : ClassTypeMatcher.values()) {
			if (cla.isPrimitive()) {
				if (ctm.isPrimitive && ctm.name.equals(cla.getName())) cache.put(cla, ctm);
			} else {
				if (!ctm.isPrimitive && ctm.cla.equals(cla)) cache.put(cla, ctm);
			}
			
			if (cache.containsKey(cla)) return cache.get(cla); 
		}
		
		return ClassTypeMatcher.OBJECT;
	}

	public String getNumTypeSuffixKeyword() {
		return processAssertSuffix;
	}
	
}
