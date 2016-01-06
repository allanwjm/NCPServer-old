package edu.sysu.ncps.servlet;

import javax.servlet.annotation.WebServlet;

import org.mura.json.JSONObject;

@WebServlet("/test")
public class TestServlet extends BaseServlet {

	private static final long serialVersionUID = 1L;

	protected class TestParaWrapper extends ParaWrapper {
		protected String rString;
		protected Integer oInt;
	}

	@Override
	protected ParaWrapper createParaWrapper() {
		return new TestParaWrapper();
	}

	@Override
	protected String main(ParaWrapper wrapper, JSONObject json) throws Exception {
		TestParaWrapper para = (TestParaWrapper) wrapper;
		json.put("str", para.rString);
		json.put("int", para.oInt);
		return null;
	}
}
