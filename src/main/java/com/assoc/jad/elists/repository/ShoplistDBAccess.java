package com.assoc.jad.elists.repository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.assoc.jad.elists.repository.model.Shoplist;
import com.assoc.jad.elists.tools.ShopListStatic;

public class ShoplistDBAccess {
	
	private Shoplist	shoplist;
	private String 	infomsg		="";
	private boolean SQLStatusOK = false;
	private String 	familyKey;
	
	public ShoplistDBAccess(Shoplist shoplist) {
		this.shoplist = shoplist;
	}
	public ShoplistDBAccess() {
		shoplist = new Shoplist();
	}
	private boolean familyExist() {
		SQLStatusOK=false;
		String listname = ShopListStatic.specialChars(shoplist.getListname());

		familyKey = shoplist.getListname()+".";

		Shoplist familyDB = new Shoplist();
		DataBaseAccess dbacc = new DataBaseAccess(familyDB.getClass(), familyDB);
		String sql = "select * from shoplists where listname='" + listname + "' and " +
				"email ='" + shoplist.getEmail() + "'";
		List<Object> wrkList = dbacc.readSql(sql); 
		if (wrkList == null || wrkList.size() == 0) return false;
		
		infomsg = "and identical shoplist already exist ";
		shoplist = (Shoplist)wrkList.get(0);   //TODO there might be more than one row.
		familyKey += shoplist.getId();
		return true;
	}
	private Shoplist familyViaId(Long id) {
		SQLStatusOK=false;
		infomsg = "";
		Shoplist familyDB = new Shoplist();
		DataBaseAccess dbacc = new DataBaseAccess(familyDB.getClass(), familyDB);
		String sql = "select * from shoplists where id='" + id+"'";
		List<Object> wrkList = dbacc.readSql(sql); 
		if (wrkList == null || wrkList.size() == 0) {
			infomsg = "there are no shoplist for id="+id;
			return null;
		}
		SQLStatusOK=true;
		return (Shoplist)wrkList.get(0);
	}
	private List<Object> familyWithEmail(String email) {
		SQLStatusOK=false;
		infomsg = "";
		Shoplist familyDB = new Shoplist();
		DataBaseAccess dbacc = new DataBaseAccess(familyDB.getClass(), familyDB);
		String sql = "select * from shoplists where email ilike '" + email+"'";
		List<Object> wrkList = dbacc.readSql(sql); 
		if (wrkList == null || wrkList.size() == 0) {
			infomsg = "there are no shoplist tree for email="+email;
			return null;
		}
		SQLStatusOK=true;
		return wrkList;
	}
	private boolean insertFamily() {
		SQLStatusOK=false;

		Date date = new Date();
		shoplist.setCreatedate(new Timestamp(date.getTime()));
		shoplist.setListname(ShopListStatic.specialChars(shoplist.getListname()));

		DataBaseAccess dbacc = new DataBaseAccess(shoplist.getClass(), shoplist);
		String sql = "select * from shoplists where id='" + shoplist.getId() + "'";
		if (!(SQLStatusOK = dbacc.insertSql(sql, "shoplists"))) {
			setInfomsg("failed to add to shoplist table shoplist= "+ shoplist.getListname());
			return false;
		}
		familyKey = shoplist.getListname()+"."+shoplist.getId();
		setInfomsg("successfully added a new shoplist root: "+ShopListStatic.undoSpecialChars(familyKey));
		return true;
	}
	public void createFamily() {
		if (familyExist()) return;
		insertFamily();
	}
	public void updateEmails() {
		if (familyExist()) return;
		insertFamily();
		
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
	public Shoplist getFamily() {
		return shoplist;
	}
	public Shoplist getFamily(Long id) {
		return familyViaId(id);
	}
	public void setFamily(Shoplist shoplist) {
		this.shoplist = shoplist;
	}
	public boolean isSQLStatusOK() {
		return SQLStatusOK;
	}
	public String getFamilyKey() {
		return familyKey;
	}
	public void setFamilyKey(String familyKey) {
		this.familyKey = familyKey;
	}
	public List<Object> getfamilyWithEmail(String email) {
		return familyWithEmail(email);
	}
}
