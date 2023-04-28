package com.assoc.jad.elists.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

public class ShopListStatic {
	public static final String JSSEPARATOR 		= "%";
	public static final String JSELEMENT		= "~";
	public static final String SELECTFAMILY		= "selectFamily.xhtml";
	public static final String SELECTFAMILY2 	= "selectFamily2.xhtml";
	public static final String STORELIST		= "storeList.xhtml";
	public static final String ADDSTORE 		= "addStore.xhtml";
	public static final String ADDSHOPPINGLIST 	= "addShoppingList.xhtml";
	public static final String NEWSHOPPINGLIST 	= "newShoppingList.xhtml";

	public static HashMap<String,ArrayList<JSONObject>> HashSales = new HashMap<String,ArrayList<JSONObject>>();

    public static String specialChars(String parm) {
    	String outParm = parm.replaceAll("'", "&#39;").replaceAll(",", "&#44;"); //.replaceAll("@", "&#64;");
    	return outParm;
    }
    public static synchronized String undoSpecialChars(String parm) {
    	if (parm == null) return "";
    	String outParm = parm.replaceAll("&#39;","'").replaceAll("&#44;", ","); //.replaceAll("&#64;", "@");
    	return outParm;
    }
    public static synchronized void redirect(String parm) {
		
		ExternalContext external = FacesContext.getCurrentInstance().getExternalContext();
		HttpServletResponse resp = (HttpServletResponse) external.getResponse();
		if ( external.isResponseCommitted()) return;
		
		String currentPage = ((HttpServletRequest) external.getRequest()).getRequestURI();
		if (parm == null || parm.length() == 0) parm = currentPage;
		if (parm.length() > 0 && currentPage.indexOf(parm) != -1) {
			return;
		}
		
		try {
			resp.sendRedirect(parm);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
}
