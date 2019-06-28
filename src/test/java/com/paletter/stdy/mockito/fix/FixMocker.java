package com.paletter.stdy.mockito.fix;

public class FixMocker {

	public FixResp test1(FixHttpReq hreq, FixReq req) {
		FixResp resp = new FixResp();
		resp.setCode(0);
		return resp;
	}
}
