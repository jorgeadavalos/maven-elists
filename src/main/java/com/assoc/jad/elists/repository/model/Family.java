package com.assoc.jad.elists.repository.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * This class represents a row in families table in our application's  model.
 * 
 * @author jad
 * 
 */
public class Family implements Comparable<Family>, Serializable {

	private static final long serialVersionUID = 1L;

	private long id;
	private Timestamp createdate;
	private String email;
	private String firstname;
	private String lastname;
	
	public Family() {
	}

	public int compareTo(Family o) {
		return this.email.compareTo(o.email); //&& this.lastname2.compareTo(o.lastname2);
	}
	@Override
	public String toString() {
		return id+email;
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
		this.id = id;
	}
	public long getId() {
		return id;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
}
