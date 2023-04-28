package com.assoc.jad.elists.bean;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

@SessionScoped
@Named("scratchPadBean")
public class ScratchPadBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private String saveSelectedList;

	public String getSaveSelectedList() {
		return saveSelectedList;
	}

	public void setSaveSelectedList(String saveSelectedList) {
		this.saveSelectedList = saveSelectedList;
	}
}
