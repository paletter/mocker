package com.paletter.stdy;

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
  public void testGetMockVal8Case0() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(cm.getName3(Mockito.any())).thenReturn("testString");
    Assert.assertEquals(parentMocker.getMockVal8(), "testString");
  }

  @Test
  public void testGetMockVal7Case0() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(cm.getId(1)).thenReturn(1);
    Mockito.when(cm.getName(1)).thenReturn("testString");
    Assert.assertEquals(parentMocker.getMockVal7(1), "testString");
  }

  @Test
  public void testGetMockVal6Case0() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(cm.getId(1)).thenReturn(1);
    Mockito.when(cm.getName(1)).thenReturn("testString");
    Assert.assertEquals(parentMocker.getMockVal6(), "testString");
  }

  @Test
  public void testGetMockVal5Case0() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(cm.getId(2)).thenReturn(1);
    Mockito.when(cm.getName(1)).thenReturn("testString");
    Assert.assertEquals(parentMocker.getMockVal5(), "testString");
  }

  @Test
  public void testGetMockVal4Case0() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(cm.getId()).thenReturn(1);
    Mockito.when(cm.getName(1)).thenReturn("testString");
    Assert.assertEquals(parentMocker.getMockVal4(), "testString");
  }

  @Test
  public void testGetMockValCase0() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(cm.getName()).thenReturn("testString");
    Assert.assertEquals(parentMocker.getMockVal(), "testString");
  }

  @Test
  public void testGetMockVal2Case0() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(cm.getName(1)).thenReturn("testString");
    Assert.assertEquals(parentMocker.getMockVal2(), "testString");
  }

  @Test
  public void testGetMockVal3Case0() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(cm.getName("abs")).thenReturn("testString");
    Assert.assertEquals(parentMocker.getMockVal3(), "testString");
  }

  @Test
  public void testGetString7Case0() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(cm.getName(1)).thenReturn("testString");
    parentMocker.getString7();
  }

  @Test
  public void testGetString6Case0() {
    MockitoAnnotations.initMocks(this);
    parentMocker.getString6();
  }

  @Test
  public void testGetString5Case0() {
    MockitoAnnotations.initMocks(this);
    Assert.assertEquals(parentMocker.getString5("testString"), "testString");
  }

  @Test
  public void testGetString4Case0() {
    MockitoAnnotations.initMocks(this);
    Assert.assertEquals(parentMocker.getString4(), "aaa");
  }

  @Test
  public void testGetString3Case0() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(cm.getName()).thenReturn("testString");
    Assert.assertEquals(parentMocker.getString3(), "testString");
  }

  @Test
  public void testGetString2Case0() {
    MockitoAnnotations.initMocks(this);
    Assert.assertEquals(parentMocker.getString2(), "abc");
  }

  @Test
  public void testGetStringCase0() {
    MockitoAnnotations.initMocks(this);
    Assert.assertEquals(parentMocker.getString(), "1");
  }

  @Test
  public void testGetIntegerCase0() {
    MockitoAnnotations.initMocks(this);
    Assert.assertEquals(parentMocker.getInteger(), Integer.valueOf(1));
  }

  @Test
  public void testGetPIntCase0() {
    MockitoAnnotations.initMocks(this);
    Assert.assertEquals(parentMocker.getPInt(), 1);
  }

  @Test
  public void testGetLongCase0() {
    MockitoAnnotations.initMocks(this);
    Assert.assertEquals(parentMocker.getLong(), Long.valueOf(1l));
  }

  @Test
  public void testGetPLongCase0() {
    MockitoAnnotations.initMocks(this);
    Assert.assertEquals(parentMocker.getPLong(), 1l);
  }

  @Test
  public void testGetFloatCase0() {
    MockitoAnnotations.initMocks(this);
    Assert.assertEquals(parentMocker.getFloat(), Float.valueOf(1.0f));
  }

  @Test
  public void testGetDoubleCase0() {
    MockitoAnnotations.initMocks(this);
    Assert.assertEquals(parentMocker.getDouble(), Double.valueOf(1.0d));
  }

  @Test
  public void testGetBigDecimalCase0() {
    MockitoAnnotations.initMocks(this);
  }
}
