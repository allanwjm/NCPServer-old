package edu.sysu.ncps.servlet;

import edu.sysu.ncps.servlet.base.BaseJSONBean;
import edu.sysu.ncps.servlet.base.BaseServlet;

public abstract class NCPServlet<P, J> extends BaseServlet<NCPPara, NCPJSON> {

	private static final long serialVersionUID = 1L;

	public static class NCPErrorJSON extends BaseJSONBean {
		public Boolean success;
		public String exception;
		public String message;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void main(NCPPara para, NCPJSON json, Servlet servlet) throws Exception {
		ncpService((P) para, (J) json, servlet);
		json.success = true;
	}

	protected abstract void ncpService(P para, J json, Servlet servlet) throws Exception;

	@Override
	protected void handleException(Servlet servlet, Exception e) throws Exception {
		e.printStackTrace();
		NCPErrorJSON error = new NCPErrorJSON();
		error.success = false;
		error.exception = e.getClass().getName();
		error.message = e.getMessage();
		servlet.forcePrintJSON(error);
	}

}
