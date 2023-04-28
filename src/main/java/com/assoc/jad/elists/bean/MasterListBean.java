package com.assoc.jad.elists.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.assoc.jad.elists.repository.ShoplistDBAccess;
import com.assoc.jad.elists.repository.StoreDBAccess;
import com.assoc.jad.elists.repository.model.Items;
import com.assoc.jad.elists.repository.model.Shoplist;
import com.assoc.jad.elists.tools.ShopListStatic;

@SessionScoped
@Named("masterListBean")
public class MasterListBean implements Serializable {
	private static final long serialVersionUID = 1L;

	public final static String divContainer = "<div id='%s' style='display:none'>";
	public final static String divEnd = "</div>";

	@Inject
	private FamilyBean familyBean;
	
	private String infomsg;
	private HashMap<String, HashMap<String, Object>> storeShopList = new HashMap<String, HashMap<String, Object>>();
	private StringBuilder familyShopListChecked;
	private Shoplist shoplist;
	private String storename;
	private String navigator;
	private HashMap<String, List<String>> revampHash = new HashMap<String, List<String>>();
	private HashMap<String, Long> photoSyncHash = new HashMap<String, Long>();

	private String getHTMLParam(String name) {
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext external = context.getExternalContext();
		HttpServletRequest request = (HttpServletRequest) external.getRequest();
		return request.getParameter(name);
	}

	private boolean itHasAFamily() {
//		ExternalContext external = FacesContext.getCurrentInstance().getExternalContext();
//		HttpServletRequest req = (HttpServletRequest) external.getRequest();
//		FamilyBean familyBean = (FamilyBean) req.getSession().getAttribute("familyBean");
//		infomsg = "there is no shoplist associated with this request";
//		if (familyBean == null || familyBean.getShoplist() == null || familyBean.getShoplist().getListname() == null) {
//			if (familyBean != null)
//				familyBean.setInfomsg(infomsg);
//			return false;
//		}
		infomsg = "";
		shoplist = familyBean.getShoplist();
		return true;
	}

	private JSONArray getJSONParm() {
		JSONArray jsonObjs = null;
		String jsonArray = this.getHTMLParam("jsonarray").replaceAll("\\\\","");
		if (jsonArray == null || jsonArray.length() == 0)
			return null;

		if (jsonArray.charAt(0) == '"') jsonArray = jsonArray.substring(1); 
		int len = jsonArray.length();
		if (jsonArray.charAt(--len) == '"') jsonArray = jsonArray.substring(0,len); 
		try {
			jsonObjs = (JSONArray) new JSONParser().parse(jsonArray);
		} catch (ParseException e) {
			throw new RuntimeException("Unable to parse json " + jsonArray);
		}
		return jsonObjs;
	}

	private void updateTables() {
		navigator = "selectFamily";
		if (shoplist == null || shoplist.getListname() == null)
			if (!rebuildBeans()) return;

		JSONArray jsonObjs = getJSONParm();
		if (jsonObjs == null)
			return;

		StoreDBAccess sdba = new StoreDBAccess(shoplist);
		for (String key : storeShopList.keySet()) {
			sdba.updateStoreItemsJson(key, jsonObjs);
			sdba.addDeleteTempItems(key,jsonObjs);
		}
		loadAllStores();
		navigator = "storeList";
	}
	private boolean rebuildBeans() {
		String wrkEmail = checkEmailFromCookies();
		if (wrkEmail == null || wrkEmail.length() == 0) return false;

		ShoplistDBAccess familyDBAccess = new ShoplistDBAccess();
		List<Object> wrkList = familyDBAccess.getfamilyWithEmail(wrkEmail);
		if (wrkList == null || wrkList.size() == 0) return false;
		
		String listname = getHTMLParam("listname");
		if (listname == null || listname.length() == 0) return false;
		
		for (int i=0;i<wrkList.size();i++) {
			shoplist = (Shoplist)wrkList.get(i);
			if (shoplist.getListname().equals(listname)) break;
			shoplist = null;
		}
		if ( shoplist == null) return false;

		ExternalContext external = FacesContext.getCurrentInstance().getExternalContext();
		HttpServletRequest req = (HttpServletRequest) external.getRequest();
		FamilyBean familyBean = new FamilyBean();
		familyBean.setFamily(shoplist);
		req.getSession().setAttribute("familyBean",familyBean);
		
		loadAllStores();
		
		return true;
	}

	@SuppressWarnings("unchecked")
	private void addItemToDB() {
		JSONArray jsonObjs = getJSONParm();
		if (jsonObjs == null)
			return;

		StoreDBAccess sdba = new StoreDBAccess(shoplist);
		for (int i = 0; i < jsonObjs.size(); i++) {
			JSONObject jsonStore = (JSONObject) jsonObjs.get(i);
			ArrayList<JSONObject> itemList = (ArrayList<JSONObject>) jsonStore.get("items");
			if (itemList == null) continue;
			
			sdba.addList((String) jsonStore.get("name"), itemList);
		}
		this.infomsg = sdba.getInfomsg();
	}

	/**
	 * the boolean flag collectable determines what should be selected. delete
	 * function collects only false update function collects only true
	 * 
	 * @param collectable
	 */
	private void collectJson(boolean collectable) {
		JSONArray jsonObjs = null;
		String jsonArray = this.getHTMLParam("jsonarray");
		if (jsonArray == null || jsonArray.length() == 0)
			return;

		try {
			jsonObjs = (JSONArray) new JSONParser().parse(jsonArray);
		} catch (ParseException e) {
			throw new RuntimeException("Unable to parse json " + jsonArray);
		}
		for (int i = 0; i < jsonObjs.size(); i++) {
			JSONObject jsonObj = (JSONObject) jsonObjs.get(i);
			String itemName = (String) jsonObj.get("name");
			photoSyncHash.put(itemName, (Long) jsonObj.get("photoid"));
			JSONArray jsonstores = (JSONArray) jsonObj.get("stores");
			for (int j = 0; j < jsonstores.size(); j++) {
				boolean flag = (boolean) ((JSONObject) jsonstores.get(j)).get("exist");
				if (flag != collectable)
					continue; // it exist for this store but is not requested for delete only false are
								// deletes
				String store = (String) ((JSONObject) jsonstores.get(j)).get("name");
				List<String> itemList = revampHash.get(store);
				if (itemList == null)
					itemList = new ArrayList<String>();
				itemList.add(itemName);
				revampHash.put(store, itemList);
			}
		}
	}

	/**
	 * to delete from database all the items in this json are not checked. Items
	 * must be unchecked and be in HashMap object as checked
	 */
	private void deleteItemFromDB() {
		this.collectJson(false);
		StoreDBAccess sdba = new StoreDBAccess(shoplist);
		for (String key : revampHash.keySet()) {
			sdba.deleteList(key, revampHash.get(key));
		}
		this.infomsg = sdba.getInfomsg();
		sdba.resynchItemPhoto(photoSyncHash);
		getRebuildMasterList();
		this.infomsg += sdba.getInfomsg();
	}
	private String checkEmailFromCookies() {
		String wrkEmail = "";
		HttpServletRequest req   =  (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
		Cookie[] cookies = req.getCookies();
		if (cookies != null) 
			for (int i=0;i<cookies.length;i++) {
				if ("email".equals(cookies[i].getName())) {
					wrkEmail = cookies[i].getValue();
					break;
				}
			}
		if (wrkEmail.length() == 0 ) {
			wrkEmail = getHTMLParam("email");
			if (wrkEmail == null) 
				this.infomsg = ". There is no email associated with this request";
		}
		return wrkEmail;
	}
	public void loadAllStores() {
		if (!itHasAFamily())
			return;
		StoreDBAccess store = new StoreDBAccess(shoplist);
		store.loadAllStores();
		storeShopList = store.getStoreShopList();
		if (storeShopList.size() == 0) {
			infomsg = "there are no stores associated with this email. Please enter info";
			ShopListStatic.redirect(ShopListStatic.ADDSHOPPINGLIST);
		}
	}

	public void updateCheckedList(ActionEvent event) {
		navigator = "selectFamily";
		updateTables();
	}

	public void addItems(ActionEvent event) {
		navigator = "mainNavigation";
		addItemToDB();
	}

	public void deleteItems(ActionEvent event) {
		navigator = "mainNavigation";
		deleteItemFromDB();
	}

	public void updateItems(ActionEvent event) {
		revampHash = new HashMap<String, List<String>>();
		navigator = "mainNavigation";
		this.collectJson(true);
		StoreDBAccess sdba = new StoreDBAccess(shoplist);
		for (String key : revampHash.keySet()) {
			sdba.updateList(key, revampHash.get(key));
		}
		this.infomsg = sdba.getInfomsg();
		getRebuildMasterList();
	}

	public String navigation() {
		return navigator;
	}

	/*
	 * getters and setters
	 */
	public String getInfomsg() {
		String temp = infomsg;
		return temp;
	}

	public void setInfomsg(String infomsg) {
		this.infomsg = infomsg;
	}

	public HashMap<String, HashMap<String, Object>> getStoreShopList() {
		if (!itHasAFamily()) {
			ShopListStatic.redirect(ShopListStatic.SELECTFAMILY);
			return null;
		}
		return storeShopList;
	}

	public void setStoreShopList(HashMap<String, HashMap<String, Object>> hashmap) {
		storeShopList = hashmap;
	}

	public String getStorename() {
		storename = getHTMLParam("storename");
		return storename;
	}

	public void setStorename(String storename) {
		this.storename = storename;
	}

	public Shoplist getShoplist() {
		return shoplist;
	}

	public void setShoplist(Shoplist shoplist) {
		this.shoplist = shoplist;
	}

	public String getFamilyShopListChecked() {
		return familyShopListChecked.toString();
	}

	public void setFamilyShopListChecked(String familyShopListChecked) {
		this.familyShopListChecked = new StringBuilder(familyShopListChecked);
	}

	public String getDescription() {
		String itemname = getHTMLParam("itemname");
		// String[] descriptions = this.getHTMLParamValues("descriptions");
		for (String key : storeShopList.keySet()) {
			HashMap<String, Object> storeList = storeShopList.get(key);
			for (String key2 : storeList.keySet()) {
				Items item = (Items) storeList.get(key2);
				if (item.getDescriptions().trim().length() == 0)
					continue;
				if (!item.getName().trim().equals(itemname))
					continue;
				return item.getDescriptions().trim();
			}
		}
		return "";
	}

	public Set<String> getAllStoresNames() {
		if (storeShopList.size() == 0) {
			this.infomsg = "there are no items for store please reenter email ";
			ShopListStatic.redirect(ShopListStatic.SELECTFAMILY);
			return null;
		}
		return storeShopList.keySet();
	}

	@SuppressWarnings("unchecked")
	public String getJsonObj() {
		if (storeShopList.size() == 0) {
			loadAllStores();
			if (storeShopList.size() == 0) {
				checkEmailFromCookies();
				ShopListStatic.redirect(ShopListStatic.SELECTFAMILY);
				return "";
			}
		}
		JSONObject jsonObj = new JSONObject();
		for (String key : storeShopList.keySet()) {
			ArrayList<JSONObject> list = new ArrayList<JSONObject>();
			HashMap<String, Object> objList = storeShopList.get(key);
			for (String key2 : objList.keySet()) {
				Items wrkItem = (Items) objList.get(key2);
				JSONObject wrkJson = new JSONObject();
				wrkJson.put("name", wrkItem.getName());
				if (wrkItem.getActive() == 0)
					wrkJson.put("selected", false);
				else
					wrkJson.put("selected", true);
				wrkJson.put("desc", wrkItem.getDescriptions());
				wrkJson.put("buyingorder", wrkItem.getBuyingorder());
				wrkJson.put("photoid", wrkItem.getPhotoid());
				wrkJson.put("familyid", wrkItem.getFamilyid());
				wrkJson.put("temporary", wrkItem.getTemp());
				wrkJson.put("category", wrkItem.getItemcategory());
				list.add(wrkJson);
			}
			jsonObj.put(key, JSONArray.toJSONString(list));
		}
		return jsonObj.toJSONString();
	}

	public String getRebuildMasterList() {
		this.storename = "";
		storeShopList = new HashMap<String, HashMap<String, Object>>();
		loadAllStores();
		ShopListStatic.redirect(ShopListStatic.STORELIST);
		return "";
	}

	@SuppressWarnings("unchecked")
	public String getJsonSales() {
		String store = this.getHTMLParam("store");
		JSONObject jsonObj = new JSONObject();
		jsonObj.put(store, JSONArray.toJSONString(ShopListStatic.HashSales.get(store)));
		return jsonObj.toJSONString();
	}
	public String getAddItemList() {
		addItemToDB();
		return "";
	}
	public String getUpdateSingleItem() {
		updateTables();
		return "";
	}
}
