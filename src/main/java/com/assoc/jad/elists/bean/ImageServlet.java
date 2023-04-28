package com.assoc.jad.elists.bean;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.assoc.jad.elists.repository.DataBaseAccess;
import com.assoc.jad.elists.repository.model.ItemPhotos;
import com.assoc.jad.elists.repository.model.Items;

@WebServlet(urlPatterns = "/image/*", loadOnStartup = 1, asyncSupported = true)

public class ImageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String HTMLWrapper = "<html><body>%s </body></html>";
	private String infomsg;

	public void init(ServletConfig config) {
		System.out.println("ImageServlet has been initialized");
	}

	@Override
	protected void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
		
		JSONObject jsonObj;
		String jsonitem = request.getParameter("jsonitem");
		if (jsonitem == null)
			return;
		try {
			jsonObj = (JSONObject) new JSONParser().parse(jsonitem);
		} catch (ParseException e) {
			throw new RuntimeException("Unable to parse json " + jsonitem);
		}
		ItemPhotos itemPhoto = new ItemPhotos();
		String sql = "select * from itemphotos where familyid="
				+ jsonObj.get("familyid") + " and id="
				+ jsonObj.get("photoid");
		DataBaseAccess dbacc = new DataBaseAccess(itemPhoto.getClass(),itemPhoto);
		List<Object> wrkObjs = dbacc.readSql(sql);
		if (wrkObjs.size() == 0) {
			infomsg = "file for "+jsonObj.get("name")+" not found sql="+sql;
			infomsg = String.format(HTMLWrapper, infomsg);
			response.getOutputStream().write(infomsg.getBytes());
			resetItemPhotoPointer(jsonObj);
			return;
		}
		itemPhoto = (ItemPhotos)wrkObjs.get(0);
		String ext = "jpg";
		int ndx = itemPhoto.getName().lastIndexOf('.');
		if (ndx != -1) ext = itemPhoto.getName().substring(++ndx);
		response.setContentType("image/"+ext);
		response.setContentLength(itemPhoto.getPhoto().length);
		response.getOutputStream().write(itemPhoto.getPhoto());
	}

	private void resetItemPhotoPointer(JSONObject jsonObj) {
		Items item = new Items();
		String sql = "select * from items where familyid="
				+ jsonObj.get("familyid") + " and photoid="
				+ jsonObj.get("photoid");
		DataBaseAccess dbacc = new DataBaseAccess(item.getClass(),item);
		List<Object> wrkObjs = dbacc.readSql(sql);
		for (int i=0;i<wrkObjs.size();i++) {
			item = (Items)wrkObjs.get(i);
			item.setPhotoid(-1);
			dbacc = new DataBaseAccess(item.getClass(),item);
			if (!dbacc.updateSql(sql,"items")) return;
		}
	}

	public String getInfomsg() {
		return infomsg;
	}

	public void setInfomsg(String infomsg) {
		this.infomsg = infomsg;
	}
}
