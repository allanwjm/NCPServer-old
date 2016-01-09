package edu.sysu.ncps.servlet;

import javax.servlet.annotation.WebServlet;

@WebServlet("/complain")
public class ComplainServlet extends NCPServlet<ComplainServlet.ComplainPara, ComplainServlet.ComplainJSON> {

	private static final long serialVersionUID = 1L;

	public static class ComplainPara extends NCPPara {
		public String _test;
		public Integer _haha;
	}

	public static class ComplainJSON extends NCPJSON {
		public String result;
	}

	@Override
	protected void ncpService(ComplainPara para, ComplainJSON json, Servlet servlet) throws Exception {
		json.result = para._test + para._haha;
	}
}
