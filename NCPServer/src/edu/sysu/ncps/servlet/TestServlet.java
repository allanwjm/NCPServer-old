package edu.sysu.ncps.servlet;

import javax.servlet.annotation.WebServlet;

@WebServlet("/test")
public class TestServlet extends BaseServlet<TestServlet.Bean, TestServlet.JSON> {

	private static final long serialVersionUID = 1L;

	public static class Bean extends BaseServlet.BaseParaBean {
		public String _str;
		public Integer _i;
	}

	public static class JSON extends BaseServlet.BaseJSONBean {
		public String result;
	}

	@Override
	protected void main(Bean para, JSON json, Servlet servlet) throws Exception {
		json.result = para._str + "(" + para._i + ")";
	}
}
