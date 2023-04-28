package com.assoc.jad.elists.repository.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * This class represents a row in stores table in our application's  model.
 * 
 * @author jad
 * 
 */
public class Items implements Comparable<Items>, Serializable {

	private static final long serialVersionUID = 1L;

	private long id;
	private long familyid;
	private long storeid;
	private String name;
	private int active;
	private Timestamp createdate;
	private Timestamp upddate;
	private String descriptions;
	private int buyingorder;
	private long photoid;
	private boolean temp;
	private String itemcategory;

	public Items() {
	}

	public int compareTo(Items o) {
		return this.name.compareTo(o.name) ; //&& this.lastname.compareTo(o.lastname);
	}
	@Override
	public String toString() {
		return name;
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
	public long getStoreid() {
		return storeid;
	}
	public void setStoreid(long storeid) {
		this.storeid = storeid;
	}
	public void setStoreid(Long storeid) {
		this.storeid = storeid;
	}
	public int getActive() {
		return active;
	}
	public void setActive(int active) {
		this.active = active;
	}
	public void setActive(Integer active) {
		this.active = active;
	}
	public String getDescriptions() {
		return descriptions;
	}
	public void setDescriptions(String descriptions) {
		this.descriptions = descriptions;
	}
	public int getBuyingorder() {
		return buyingorder;
	}
	public void setBuyingorder(int buyingorder) {
		this.buyingorder = buyingorder;
	}
	public void setBuyingorder(Integer buyingorder) {
		this.buyingorder = buyingorder;
	}

	public long getPhotoid() {
		return photoid;
	}
	public void setPhotoid(long photoid) {
		this.photoid = photoid;
	}
	public void setPhotoid(Long photoid) {
		this.photoid = photoid;
	}
	public boolean getTemp() {
		return temp;
	}
	public void setTemp(boolean temp) {
		this.temp = temp;
	}
	public void setTemp(Boolean temp) {
		this.temp = temp;
	}

	public Timestamp getUpddate() {
		return upddate;
	}

	public void setUpddate(Timestamp upddate) {
		this.upddate = upddate;
	}

	public String getItemcategory() {
		return itemcategory;
	}

	public void setItemcategory(String itemcategory) {
		this.itemcategory = itemcategory;
	}

}
