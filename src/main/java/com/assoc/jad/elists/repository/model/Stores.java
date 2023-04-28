package com.assoc.jad.elists.repository.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * This class represents a row in stores table in our application's  model.
 * 
 * @author jad
 * 
 */
public class Stores implements Comparable<Stores>, Serializable {

	private static final long serialVersionUID = 1L;

	private long id;
	private long familyid;
	private String name;
	private Timestamp createdate;

	public Stores() {
	}

	public int compareTo(Stores o) {
		return this.name.compareTo(o.name) ; //&& this.lastname.compareTo(o.lastname);
	}
	@Override
	public String toString() {
		return id+name;
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

}
