package edu.sysu.ncps.servlet;

import javax.servlet.annotation.WebServlet;

import edu.sysu.ncps.servlet.base.BaseJSONBean;
import edu.sysu.ncps.servlet.base.BaseParaBean;
import edu.sysu.ncps.servlet.base.BaseServlet;

@WebServlet("/test")
public class TestServlet extends BaseServlet<TestServlet.Bean, TestServlet.JSON> {

	private static final long serialVersionUID = 1L;

	public static class Bean extends BaseParaBean {
		public String _str;
		public Integer _i;
	}

	public static class JSON extends BaseJSONBean {
		public String result;
	}

	@Override
	protected void main(Bean para, JSON json, Servlet servlet) throws Exception {
		json.result = para._str + "(" + para._i + ")";
	}
}
