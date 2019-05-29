package com.paletter.stdy.javapoet;

import com.paletter.stdy.mockito.ChildrenMocker;
import com.paletter.stdy.mockito.ParentMocker;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ParentMockerTest {
  @InjectMocks
  private ParentMocker parentMocker;

  @Mock
  private ChildrenMocker cm;

  @Test
  public void testGetMockVal8() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(cm.getName3(Mockito.any())).thenReturn("testString");
    Assert.assertEquals(parentMocker.getMockVal8(), "testString");
  }
}
