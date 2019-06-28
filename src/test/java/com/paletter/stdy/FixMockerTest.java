package com.paletter.stdy;

import com.paletter.stdy.mockito.fix.FixHttpReq;
import com.paletter.stdy.mockito.fix.FixMocker;
import com.paletter.stdy.mockito.fix.FixReq;
import com.paletter.stdy.mockito.fix.FixResp;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class FixMockerTest {
  @InjectMocks
  private FixMocker fixMocker;

  @Test
  public void testTest1() {
    MockitoAnnotations.initMocks(this);
    FixHttpReq arg0 = new FixHttpReq();
    FixReq arg1 = new FixReq();
    arg1.setName("testString");
    FixResp returnVal = fixMocker.test1(arg0,arg1);
    Assert.assertEquals(returnVal.getCode(), 0);
  }
}
