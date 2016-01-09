package edu.sysu.ncps.servlet;

import javax.servlet.annotation.WebServlet;

import edu.sysu.ncps.servlet.base.BaseJSONBean;
import edu.sysu.ncps.servlet.base.BaseParaBean;
import edu.sysu.ncps.servlet.base.BaseServlet;

@WebServlet("/ncp")
public class NCPServlet extends BaseServlet<NCPServlet.NCPBean, NCPServlet.NCPJSON> {

	private static final long serialVersionUID = 1L;

	public static class NCPBean extends BaseParaBean {

	}

	public static class NCPJSON extends BaseJSONBean {
		public Boolean success;
		public String _errorMsg;
	}

	@Override
	protected void main(NCPBean para, NCPJSON json, Servlet servlet) throws Exception {

	}
	
	@Override
	protected void handleException(Servlet servlet, Exception e) {
		
	}

}
