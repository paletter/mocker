package com.paletter.stdy.tcg.ast.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Modifier;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.paletter.stdy.tcg.ast.store.GCFieldStore;
import com.paletter.stdy.tcg.ast.support.ClassTypeMatcher;
import com.paletter.stdy.tcg.ast.support.CommonUtils;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

public class ClassAnalysis {

	private String targetMockFilePath = null;
	private Class<?> clazz;
	private Map<String, Object> variables = new HashMap<String, Object>();
	private List<MethodAnalysis> methods = new ArrayList<MethodAnalysis>();
	
	private List<GCFieldStore> gcFieldStores = new ArrayList<GCFieldStore>();
	
	private String gcImFieldName;
	private String gcClassPath;
	
	public ClassAnalysis(Class<?> clazz, String gcClassPath, String targetMockFilePath) {
		this.clazz = clazz;
		this.gcClassPath = gcClassPath;
		this.targetMockFilePath = targetMockFilePath;
	}

	public void addVariable(JCVariableDecl jv) {
		String argName = jv.name.toString();
		
		variables.put(argName, null);
		
		if (jv.init instanceof JCLiteral) {
			JCLiteral jvInit = (JCLiteral) jv.init;
			Object initVal = jvInit.value;
			variables.put(argName, initVal);
		}
	}
	
	public void addMethod(MethodAnalysis m) {
		methods.add(m);
	}
	
	public String getGcImFieldName() {
		return gcImFieldName;
	}
	
	public Class<?> getClazz() {
		return clazz;
	}
	
	public GCFieldStore findGCField(String name) {
		for (GCFieldStore fs : gcFieldStores) {
			if (fs.getField().getName().equals(name)) return fs;
		}
		return null;
	}
	
	public void setTargetMockFilePath(String targetMockFilePath) {
		this.targetMockFilePath = targetMockFilePath;
	}

	public void generateCode() throws IOException {

		List<FieldSpec> fieldSpecs = new ArrayList<FieldSpec>();
		
		// 1 FiledSpec
		// 1.1 InjectMock Main Class
		gcImFieldName = CommonUtils.toLowerCaseFirstChar(clazz.getSimpleName());
		fieldSpecs.add(
				FieldSpec.builder(clazz, gcImFieldName, Modifier.PRIVATE)
				.addAnnotation(InjectMocks.class)
				.build());
		// 1.2 Mock field
		for (Field field : clazz.getDeclaredFields()) {
			
			String name = field.getName();
			GCFieldStore fs = new GCFieldStore(field, name);
			if (variables.containsKey(field.getName())) 
				fs.setVal(variables.get(name));
			gcFieldStores.add(fs);
			
			if (!ClassTypeMatcher.get(field.getType()).equals(ClassTypeMatcher.OBJECT)) continue;
			
			fieldSpecs.add(
					FieldSpec.builder(field.getType(), name, Modifier.PRIVATE)
					.addAnnotation(Mock.class)
					.build());
		}
		
		// 2 MethodSpec
		List<MethodSpec> methodSpecs = new ArrayList<MethodSpec>();
		for (MethodAnalysis ma : methods) {
			methodSpecs.addAll(ma.generateCode());
		}
		
		// Create file
		TypeSpec type = TypeSpec.classBuilder(clazz.getSimpleName() + "Test")
				.addModifiers(Modifier.PUBLIC)
				.addFields(fieldSpecs)
				.addMethods(methodSpecs)
				.build();
		
		if (gcClassPath.endsWith(".")) gcClassPath = gcClassPath.substring(0, gcClassPath.length() - 1);
		JavaFile javeFile = JavaFile.builder(gcClassPath, type).build();
		File file = null;
		if (targetMockFilePath == null) file = new File("src/test/java");
		else file = new File(targetMockFilePath);
		javeFile.writeTo(file);
	}
}
