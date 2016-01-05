package edu.sysu.ncps.servlet;

import javax.servlet.annotation.WebServlet;

import org.mura.json.JSONObject;

@WebServlet("/test")
public class TestServlet extends BaseServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected String check(JSONObject para) throws Exception {
		return null;
	}

	@Override
	protected String main(JSONObject para, JSONObject json) throws Exception {
		json.put("paraString", para.toString());
		return null;
	}

}
