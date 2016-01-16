package edu.sysu.ncps.servlet;

import javax.servlet.annotation.WebServlet;

@WebServlet("/complain")
public class ComplainServlet extends NCPServlet<ComplainServlet.ComplainPara, ComplainServlet.ComplainJSON> {

	private static final long serialVersionUID = 1L;

	public static class ComplainPara extends NCPPara {
		public String _comment;
		public String _date;
		public Float _intensity;
		public String _address;
		public Float _latitude;
		public Float _longitude;
		public byte[] _image;
		public String _sfaType;
		public String _noiseType;
	}

	public static class ComplainJSON extends NCPJSON {
		public String comment;
		public String date;
		public Float intensity;
		public String address;
		public Float latitude;
		public Float longitude;
		public byte[] image;
		public String sfaType;
		public String noiseType;
	}

	@Override
	protected void ncpService(ComplainPara para, ComplainJSON json, Servlet servlet) throws Exception {
		json.comment = para._comment;
		json.date = para._date;
		json.intensity = para._intensity;
		json.address = para._address;
		json.latitude = para._latitude;
		json.longitude = para._longitude;
		json.image = para._image;
		json.sfaType = para._sfaType;
		json.noiseType = para._noiseType;
	}
}
