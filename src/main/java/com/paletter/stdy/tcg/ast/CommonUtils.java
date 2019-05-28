package com.paletter.stdy.tcg.ast;

import java.lang.reflect.Method;

import com.sun.source.tree.MethodTree;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

public class CommonUtils {

	public static String COMMON_STRING = "";
	public static Integer COMMON_INTEGER = 0;
	
	public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
	
	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}
	
	public static String toLowerCaseFirstChar(String str) {
		if (isEmpty(str)) return str;
		return Character.isLowerCase(str.charAt(0)) ? str : 
			(new StringBuilder()).append(Character.toLowerCase(str.charAt(0))).append(str.substring(1)).toString();
	}

	public static String toUpperCaseFirstChar(String str) {
		if (isEmpty(str)) return str;
		return Character.isUpperCase(str.charAt(0)) ? str : 
			(new StringBuilder()).append(Character.toUpperCase(str.charAt(0))).append(str.substring(1)).toString();
	}
	
	public static boolean matchMethodParams(Method m1, MethodTree m2) {
		if (m1 == null || m2 == null) return false;
		if (m1.getParameterTypes().length != m2.getParameters().size()) return false;
		
		for (int i = 0; i < m1.getParameterTypes().length; i ++) {
			Class<?> param1 = m1.getParameterTypes()[i];
			JCVariableDecl param2 = (JCVariableDecl) m2.getParameters().get(i);
			if (!param1.getSimpleName().equals(param2.vartype.toString()))
				return false;
		}
		
		return true;
	}
}
