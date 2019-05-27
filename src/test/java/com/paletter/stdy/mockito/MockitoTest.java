package com.paletter.stdy.mockito;

import org.mockito.Mockito;

public class MockitoTest {

	public static void main(String[] args) {

//		MockitoTest mt = Mockito.mock(MockitoTest.class);
//		Mockito.when(mt.getName()).thenReturn("1");
//		System.out.println(mt.getName());
		
//		Mockito.when(MockitoTest.get()).thenReturn("2");
		
//		System.out.println(MockitoTest.get());

//		ParentMocker m = Mockito.mock(ParentMocker.class);
//		m.goMock("", 0);
		
//		ParentMocker m = new ParentMocker();
//		System.out.println(m.test());
		
		ParentMocker2 m = Mockito.mock(ParentMocker2.class);
		System.out.println(m.goMock());
	}
}
