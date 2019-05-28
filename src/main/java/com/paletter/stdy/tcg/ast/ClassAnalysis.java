package com.paletter.stdy.tcg.ast;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.paletter.stdy.mockito.ChildrenMocker;
import com.paletter.stdy.tcg.ast.store.GCFieldStore;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

public class ClassAnalysis {

	private Class<?> clazz;
	private List<JCVariableDecl> variables = new ArrayList<JCVariableDecl>();
	private List<MethodAnalysis> methods = new ArrayList<MethodAnalysis>();
	
	private List<GCFieldStore> gcFieldStores = new ArrayList<GCFieldStore>();
	
	private String gcImFieldName;
	
	public ClassAnalysis(Class<?> clazz) {
		this.clazz = clazz;
	}

	public void addVariable(JCVariableDecl v) {
		variables.add(v);
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
	
	public JCVariableDecl findVariable(String name) {
		for (JCVariableDecl jv : variables) {
			if (jv.name.toString().equals(name)) return jv;
		}
		return null;
	}
	
	public GCFieldStore findGCField(String name) {
		for (GCFieldStore fs : gcFieldStores) {
			if (fs.getField().getName().equals(name)) return fs;
		}
		return null;
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
			
			String name = CommonUtils.toLowerCaseFirstChar(field.getName());
			gcFieldStores.add(new GCFieldStore(field, name));
			
			fieldSpecs.add(
					FieldSpec.builder(ChildrenMocker.class, name, Modifier.PRIVATE)
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
		
		JavaFile javeFile = JavaFile.builder("com.paletter.stdy.javapoet", type).build();
		File file = new File("src/test/java");
		javeFile.writeTo(file);
	}
}
