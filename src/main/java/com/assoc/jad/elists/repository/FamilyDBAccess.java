package com.assoc.jad.elists.repository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.assoc.jad.elists.repository.model.Family;
import com.assoc.jad.elists.tools.ShopListStatic;

public class FamilyDBAccess {
	
	private Family	family;
	private String 	infomsg		="";
	private boolean SQLStatusOK = false;
	private String 	familyKey;
	
	public FamilyDBAccess(Family family) {
		this.family = family;
	}
	public FamilyDBAccess() {
		family = new Family();
	}
	private boolean familyExist() {
		SQLStatusOK=false;
		String Lastname  = ShopListStatic.specialChars(family.getLastname());
		String Firstname = ShopListStatic.specialChars(family.getFirstname());

		familyKey = family.getLastname()+".";

		Family familyDB = new Family();
		DataBaseAccess dbacc = new DataBaseAccess(familyDB.getClass(), familyDB);
		String sql = "select * from families where lastname='" + Lastname + "' and " +
				"firstname ='" + Firstname + "' and " +
				"email ='" + family.getEmail() + "'";
		List<Object> wrkList = dbacc.readSql(sql); 
		if (wrkList == null || wrkList.size() == 0) return false;
		
		infomsg = "and identical family already exist ";
		family = (Family)wrkList.get(0);   //TODO there might be more than one row.
		familyKey += family.getId();
		return true;
	}
	private Family familyViaId(Long id) {
		SQLStatusOK=false;
		infomsg = "";
		Family familyDB = new Family();
		DataBaseAccess dbacc = new DataBaseAccess(familyDB.getClass(), familyDB);
		String sql = "select * from families where id='" + id+"'";
		List<Object> wrkList = dbacc.readSql(sql); 
		if (wrkList == null || wrkList.size() == 0) {
			infomsg = "there are no family for id="+id;
			return null;
		}
		SQLStatusOK=true;
		return (Family)wrkList.get(0);
	}
	private List<Object> familyWithEmail(String email) {
		SQLStatusOK=false;
		infomsg = "";
		Family familyDB = new Family();
		DataBaseAccess dbacc = new DataBaseAccess(familyDB.getClass(), familyDB);
		String sql = "select * from families where email='" + email+"'";
		List<Object> wrkList = dbacc.readSql(sql); 
		if (wrkList == null || wrkList.size() == 0) {
			infomsg = "there are no family tree for email="+email;
			return null;
		}
		SQLStatusOK=true;
		return wrkList;
	}
	private boolean insertFamily() {
		SQLStatusOK=false;

		Date date = new Date();
		family.setCreatedate(new Timestamp(date.getTime()));
		family.setLastname(ShopListStatic.specialChars(family.getLastname()));
		family.setFirstname(ShopListStatic.specialChars(family.getFirstname()));

		DataBaseAccess dbacc = new DataBaseAccess(family.getClass(), family);
		String sql = "select * from families where id='" + family.getId() + "'";
		if (!(SQLStatusOK = dbacc.insertSql(sql, "families"))) {
			setInfomsg("failed to add to family table family= "+ family.getLastname());
			return false;
		}
		familyKey = family.getLastname()+"."+family.getId();
		setInfomsg("successfully added a new family root: "+ShopListStatic.undoSpecialChars(familyKey));
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
	public Family getFamily() {
		return family;
	}
	public Family getFamily(Long id) {
		return familyViaId(id);
	}
	public void setFamily(Family family) {
		this.family = family;
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
