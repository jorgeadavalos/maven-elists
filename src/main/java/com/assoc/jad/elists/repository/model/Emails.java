package com.assoc.jad.elists.repository.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * This class represents a row in stores table in our application's  model.
 * 
 * @author jad
 * 
 */
public class Emails implements Comparable<Emails>, Serializable {

	private static final long serialVersionUID = 1L;

	private long id;
	private long familyid;
	private String email;
	private Timestamp createdate;

	public Emails() {
	}

	public int compareTo(Emails o) {
		return this.email.compareTo(o.email) ; //&& this.lastname.compareTo(o.lastname);
	}
	@Override
	public String toString() {
		return familyid+email;
	}

	public void setCreatedate(Timestamp createdate) {
		this.createdate = createdate;
	}
	public Timestamp getCreatedate() {
		return createdate;
	}
	public void setId(Long id) {
		this.id = id.longValue();
	}
	public void setId(Integer id) {
		this.id = id.longValue();
	}
	public long getId() {
		return id;
	}
	public long getFamilyid() {
		return familyid;
	}
	public void setFamilyid(long familyid) {
		this.familyid = familyid;
	}
	public void setFamilyid(Long familyid) {
		this.familyid = familyid;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

}
