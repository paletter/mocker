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
  public void testIf6Case0() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(cm.getId()).thenReturn(1);
    Assert.assertEquals(parentMocker2.if6(), "d");
  }

  @Test
  public void testIf6Case1() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(cm.getId()).thenReturn(100);
    Mockito.when(cm.getId2()).thenReturn(1000);
    Mockito.when(cm.getId3()).thenReturn(0);
    Assert.assertEquals(parentMocker2.if6(), "a");
  }

  @Test
  public void testIf6Case2() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(cm.getId()).thenReturn(100);
    Mockito.when(cm.getId2()).thenReturn(1000);
    Mockito.when(cm.getId3()).thenReturn(1);
    Assert.assertEquals(parentMocker2.if6(), "b");
  }

  @Test
  public void testIf6Case3() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(cm.getId()).thenReturn(100);
    Mockito.when(cm.getId2()).thenReturn(1);
    Assert.assertEquals(parentMocker2.if6(), "c");
  }

  @Test
  public void testIf5Case0() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(cm.getId()).thenReturn(100);
    Mockito.when(cm.getId2()).thenReturn(1000);
    Mockito.when(cm.getId3()).thenReturn(0);
    Assert.assertEquals(parentMocker2.if5(), "a");
  }

  @Test
  public void testIf5Case1() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(cm.getId()).thenReturn(100);
    Mockito.when(cm.getId2()).thenReturn(1000);
    Mockito.when(cm.getId3()).thenReturn(40760);
    Assert.assertEquals(parentMocker2.if5(), "e");
  }

  @Test
  public void testIf5Case2() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(cm.getId()).thenReturn(100);
    Mockito.when(cm.getId2()).thenReturn(40760);
    Assert.assertEquals(parentMocker2.if5(), "b");
  }

  @Test
  public void testIf5Case3() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(cm.getId()).thenReturn(500);
    Assert.assertEquals(parentMocker2.if5(), "c");
  }

  @Test
  public void testIf5Case4() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(cm.getId()).thenReturn(40760);
    Assert.assertEquals(parentMocker2.if5(), "d");
  }

  @Test
  public void testIf4Case0() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(cm.getId()).thenReturn(100);
    Assert.assertEquals(parentMocker2.if4(), "a");
  }

  @Test
  public void testIf4Case1() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(cm.getId()).thenReturn(500);
    Assert.assertEquals(parentMocker2.if4(), "b");
  }

  @Test
  public void testIf4Case2() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(cm.getId()).thenReturn(40760);
    Assert.assertEquals(parentMocker2.if4(), "c");
  }

  @Test
  public void testIf3Case0() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(cm.getId()).thenReturn(100);
    Assert.assertEquals(parentMocker2.if3(), "b");
  }

  @Test
  public void testIf3Case1() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(cm.getId()).thenReturn(40760);
    Assert.assertEquals(parentMocker2.if3(), "c");
  }

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

  @Test
  public void testIf1Case0() {
    MockitoAnnotations.initMocks(this);
    Assert.assertEquals(parentMocker2.if1(), "a");
  }

  @Test
  public void testIf1Case1() {
    MockitoAnnotations.initMocks(this);
    Assert.assertEquals(parentMocker2.if1(), "a");
  }
}
