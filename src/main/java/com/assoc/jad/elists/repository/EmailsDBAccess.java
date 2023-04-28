package com.assoc.jad.elists.repository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.assoc.jad.elists.repository.model.Emails;
import com.assoc.jad.elists.repository.model.Shoplist;
import com.assoc.jad.elists.tools.ShopListStatic;

public class EmailsDBAccess {
	
	private Shoplist	shoplist;
	private String 	infomsg		="";
	private boolean SQLStatusOK = false;
	private String 	shoplistKey;
	private Emails emails = new Emails();

	public EmailsDBAccess(Shoplist shoplist) {
		this.shoplist = shoplist;
	}
	@SuppressWarnings("unused")
	private EmailsDBAccess() {
		shoplist = null;
	}
	private boolean familyExist() {
		SQLStatusOK=false;
		if (shoplist == null) return false;
		
		String listname  = ShopListStatic.specialChars(shoplist.getListname());

		shoplistKey = shoplist.getListname()+".";

		Shoplist familyDB = new Shoplist();
		DataBaseAccess dbacc = new DataBaseAccess(familyDB.getClass(), familyDB);
		String sql = "select * from shoplists where listname='" + listname + "' and " +
				"email ='" + shoplist.getEmail() + "'";
		List<Object> wrkList = dbacc.readSql(sql); 
		if (wrkList == null || wrkList.size() == 0) return false;
		
		infomsg = "OK shoplist exist ";
		shoplist = (Shoplist)wrkList.get(0);
		shoplistKey += shoplist.getId();
		SQLStatusOK=true;
		return true;
	}
	private List<Object> familyWithEmail(String email) {
		SQLStatusOK=false;
		infomsg = "";
		Shoplist familyDB = new Shoplist();
		DataBaseAccess dbacc = new DataBaseAccess(familyDB.getClass(), familyDB);
		String sql = "select * from shoplists where email='" + email+"'";
		List<Object> wrkList = dbacc.readSql(sql); 
		if (wrkList == null || wrkList.size() == 0) {
			infomsg = "there are no shoplist tree for email="+email;
		}
		return wrkList;
	}
	private boolean emailExist(String email,DataBaseAccess dbacc) {
		String sql = "select * from emails where familyid='" + shoplist.getId() + "' and email='"+email+"'";
		List<Object> wrkList = dbacc.readSql(sql); 
		if (wrkList == null || wrkList.size() == 0) return false;
		
		infomsg = "and identical email already exist for shopping list for shoplist="+shoplist.getId();
		System.out.println(infomsg);
		return true;
	}
	private boolean insertEmail(String email,DataBaseAccess dbacc) {
		SQLStatusOK=false;

		Date date = new Date();
		emails.setCreatedate(new Timestamp(date.getTime()));
		emails.setEmail(email);
		emails.setFamilyid(shoplist.getId());

		String sql = "select * from emails where id='" + shoplist.getId() + "'";
		if (!(SQLStatusOK = dbacc.insertSql(sql, "emails"))) {
			setInfomsg("failed to add to emails to table for shoplist= "+ shoplist.getListname());
			return false;
		}
		shoplistKey = shoplist.getListname()+"."+shoplist.getId();
		setInfomsg("successfully added an email record to shoplist: "+ShopListStatic.undoSpecialChars(shoplistKey));
		return true;
	}
	public void addEmails(String emailList) {
		if (!familyExist()) return;
		infomsg = "";
		
		DataBaseAccess dbacc = new DataBaseAccess(emails.getClass(), emails);
		String[] allEmails = emailList.split(System.lineSeparator());
		String previous = "";
		for (int i=0;i<allEmails.length;i++) {
			if (previous.equals(allEmails[i]) || allEmails[i].trim().length() == 0) continue;
			previous = allEmails[i].trim();
			if(emailExist(allEmails[i],dbacc)) continue;
			
			if(!insertEmail(allEmails[i],dbacc)) return;
		}
		
	}
	/*
	 * getters and setters
	 */
	public String getInfomsg() {
		return infomsg;
	}
	public void setInfomsg(String infomsg) {
		this.infomsg = infomsg;
	}
	public Shoplist getShoplist() {
		return shoplist;
	}
	public void setShoplist(Shoplist shoplist) {
		this.shoplist = shoplist;
	}
	public boolean isSQLStatusOK() {
		return SQLStatusOK;
	}
	public String getShoplistKey() {
		return shoplistKey;
	}
	public void setShoplistKey(String shoplistKey) {
		this.shoplistKey = shoplistKey;
	}
	public List<Object> getfamilyWithEmail(String email) {
		return familyWithEmail(email);
	}
}
