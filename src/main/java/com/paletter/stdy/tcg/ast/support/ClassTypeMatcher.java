package com.paletter.stdy.tcg.ast.support;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public enum ClassTypeMatcher {

	PINT(true, "int", Integer.class),
	PLONG(true, "long", Long.class),
	PFLOAT(true, "float", Float.class),
	PDOUBLE(true, "double", Double.class),
	
	INTEGER(false, "Integer", Integer.class),
	LONG(false, "Long", Long.class),
	FLOAT(false, "Float", Float.class),
	DOUBLE(false, "Double", Double.class),
	BIGDECIMAL(false, "BigDecimal", BigDecimal.class),
	STRING(false, "String", String.class),
	
	OBJECT(false, null, Object.class),
	;
	
	private boolean isPrimitive;
	private String name;
	private Class<?> cla;
	
	private String processStatementSuffix = "";
	private String processStatementPrefix = "";
	
	private String processAssertSuffix = "";
	private String processAssertPrefix = "";
	private Object commonValue = null;
	private Object randomValue = null;

	private ClassTypeMatcher(boolean isPrimitive, String name, Class<?> cla) {
		this.isPrimitive = isPrimitive;
		this.name = name;
		this.cla = cla;
		
		Integer randomInt = 10000 + new Random().nextInt(99999);
		
		if (cla.equals(Integer.class)) {
			
			commonValue = 1;
			randomValue = randomInt;
			
			if (!isPrimitive) {
				processAssertPrefix = "Integer.valueOf(";
				processAssertSuffix = ")";
			}
			
		} else if (cla.equals(Long.class)) {
			
			commonValue = 1L;
			randomValue = Long.valueOf(randomInt);
			
			if (!isPrimitive) {
				processAssertPrefix = "Long.valueOf(";
				processAssertSuffix = "l)";
			} else {
				processAssertSuffix = "l";				
			}
			
			processStatementSuffix = "l";
			
		} else if (cla.equals(Float.class)) {
			
			commonValue = 1F;
			randomValue = Float.valueOf(randomInt);
			
			if (!isPrimitive) {
				processAssertPrefix = "Float.valueOf(";
				processAssertSuffix = "f)";
			} else {
				processAssertSuffix = "f";				
			}
			
			processStatementSuffix = "f";
			
		} else if (cla.equals(Double.class)) {
			
			commonValue = 1D;
			randomValue = Double.valueOf(randomInt);
			
			if (!isPrimitive) {
				processAssertPrefix = "Double.valueOf(";
				processAssertSuffix = "d)";
			} else {
				processAssertSuffix = "d";				
			}
			
			processStatementSuffix = "d";
			
		} else if (cla.equals(String.class)) {
			
			commonValue = "testString";
			randomValue = String.valueOf(randomInt);
			
			processAssertPrefix = "\"";
			processAssertSuffix = "\"";
			
			processStatementPrefix = "\"";
			processStatementSuffix = "\"";
			
		} else if (cla.equals(BigDecimal.class)) {
			
			commonValue = new BigDecimal(1);
			randomValue = BigDecimal.valueOf(randomInt);

			processAssertPrefix = "BigDecimal.valueOf(";
			processAssertSuffix = ")";
			
			processStatementPrefix = "BigDecimal.valueOf(";
			processStatementSuffix = ")";
		}
	}

	public String processStatementArg(Object arg) {
		if (arg == null) return null;
		return processStatementPrefix + arg + processStatementSuffix;
	}
	
	public String processAssertStatementArg(Object arg) {
		if (arg == null) return null;
		return processAssertPrefix + arg + processAssertSuffix;
	}
	
	public Object getRandomValue() {
		return randomValue;
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
	
	private static Map<String, ClassTypeMatcher> cacheName = new HashMap<String, ClassTypeMatcher>();
	
	public static ClassTypeMatcher get(String name) {
		
		if (cacheName.containsKey(name)) return cacheName.get(name);
		
		for (ClassTypeMatcher ctm : ClassTypeMatcher.values()) {
			if (ctm.name.equals(name)) {
				cacheName.put(name, ctm);
				return ctm;
			}
		}
		
		return ClassTypeMatcher.OBJECT;
	}

	public String getNumTypeSuffixKeyword() {
		return processAssertSuffix;
	}
	
}
