package com.paletter.stdy.mockito;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class MockitoTest2 {

	@InjectMocks
	ParentMocker m;
	
	@Mock
	ChildrenMocker m2;
	
	@Test
	public void test() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(m2.getName()).thenReturn("b");
		System.out.println(m.goMock("a", 0));
//		Assert.assertEquals(m.test(), "1");
	}
}
