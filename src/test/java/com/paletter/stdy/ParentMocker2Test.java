package com.paletter.stdy;

import com.paletter.stdy.mockito.ChildrenMocker;
import com.paletter.stdy.mockito.ParentMocker2;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ParentMocker2Test {
  @InjectMocks
  private ParentMocker2 parentMocker2;

  @Mock
  private ChildrenMocker cm;

  @Test
  public void testIf2Case0() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(cm.getId()).thenReturn(1);
    Assert.assertEquals(parentMocker2.if2(), "a");
  }

  @Test
  public void testIf2Case1() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(cm.getId()).thenReturn(100);
    Assert.assertEquals(parentMocker2.if2(), "b");
  }
}
