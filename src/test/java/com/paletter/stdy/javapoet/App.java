package com.paletter.stdy.javapoet;

import java.io.File;
import java.io.IOException;

import javax.lang.model.element.Modifier;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.paletter.stdy.mockito.ChildrenMocker;
import com.paletter.stdy.mockito.ParentMocker;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

public class App {

	public static void main(String[] args) throws IOException {
		
		FieldSpec field = FieldSpec.builder(ParentMocker.class, "m", Modifier.PRIVATE)
				.addAnnotation(InjectMocks.class)
				.build();
		
		FieldSpec field2 = FieldSpec.builder(ChildrenMocker.class, "m2", Modifier.PRIVATE)
				.addAnnotation(Mock.class)
				.build();
		
		MethodSpec m = MethodSpec.methodBuilder("test").addModifiers(Modifier.PUBLIC)
				.addAnnotation(Test.class)
				.addStatement("$T.initMocks(this)", MockitoAnnotations.class)
				.addStatement("$T.when(m2.getName()).thenReturn(\"b\")", Mockito.class)
				.addStatement("$T.assertEquals(m.test(), 100L)", Assert.class)
				.build();

		TypeSpec type = TypeSpec.classBuilder("JPTest").addModifiers(Modifier.PUBLIC)
				.addField(field)
				.addField(field2)
				.addMethod(m)
				.build();
		
		JavaFile file = JavaFile.builder("com.paletter.stdy.javapoet", type)
				.build();
		File f = new File("src/test/java");
		file.writeTo(f);
	}
}
