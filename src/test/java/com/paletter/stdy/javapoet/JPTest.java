package com.paletter.stdy.javapoet;

import com.paletter.stdy.mockito.ChildrenMocker;
import com.paletter.stdy.mockito.ParentMocker;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class JPTest {
  @InjectMocks
  private ParentMocker m;

  @Mock
  private ChildrenMocker m2;

  @Test
  public void test() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(m2.getName()).thenReturn("b");
//    Assert.assertEquals(m.test(), 100L);
  }
}
