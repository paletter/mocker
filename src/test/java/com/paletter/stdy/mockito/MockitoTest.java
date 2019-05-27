package com.paletter.stdy.mockito;

import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

public class MockitoTest {

	public static void main(String[] args) {
		
//		MockitoTest mt = Mockito.mock(MockitoTest.class);
//		Mockito.when(mt.getName()).thenReturn("1");
//		System.out.println(mt.getName());
		
//		Mockito.when(MockitoTest.get()).thenReturn("2");
		
//		System.out.println(MockitoTest.get());
		
		PowerMockito.mockStatic(Mockitoer.class);
//		PowerMockito.when(MockitoTest.get()).thenReturn("2");
//		System.out.println(MockitoTest.get());
	}
	
}
