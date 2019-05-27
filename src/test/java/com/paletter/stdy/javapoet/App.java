package com.paletter.stdy.javapoet;

import java.io.File;
import java.io.IOException;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

public class App {

	public static void main(String[] args) throws IOException {
		
		TypeSpec type = TypeSpec.classBuilder("JPTest").addModifiers(Modifier.PUBLIC).build();
		JavaFile file = JavaFile.builder("com.paletter.stdy.javapoet", type).build();
		File f = new File("src/test/java");
		file.writeTo(f);
	}
}
