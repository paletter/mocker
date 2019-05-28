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
  public void testGoMock() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(cm.getName()).thenReturn("");
    Assert.assertEquals(parentMocker.goMock(""), "");
  }
}
