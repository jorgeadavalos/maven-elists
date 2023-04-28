package com.assoc.jad.elists.repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.assoc.jad.elists.repository.model.ItemPhotos;
import com.assoc.jad.elists.repository.model.Items;
import com.assoc.jad.elists.repository.model.Shoplist;
import com.assoc.jad.elists.repository.model.Stores;
import com.assoc.jad.elists.tools.ShopListStatic;

public class StoreDBAccess {
	private Shoplist	shoplist;
	private String 	infomsg		="";
	private boolean SQLStatus = false;
	private boolean familyinDB = false;
	private Stores store = new Stores();
	private Items items = new Items();
	private HashMap<String, HashMap<String,Object>> storeShopList = new HashMap<String,HashMap<String,Object>>();

	public StoreDBAccess(Shoplist shoplist) {
		this.shoplist = shoplist;
		familyinDB = familyExist();
		storeShopList = new HashMap<String,HashMap<String,Object>>();
	}
	@SuppressWarnings("unused")
	private StoreDBAccess() {
	}

	private boolean familyExist() {
		SQLStatus = false;
		String listname = "";
		infomsg = "shoplist has not been initialized. Please enter your email";
		if (shoplist == null || shoplist.getListname() == null ) return false;
		if (shoplist.getListname() != null )
			listname = ShopListStatic.specialChars(shoplist.getListname());

		Shoplist familyDB = new Shoplist();
		DataBaseAccess dbacc = new DataBaseAccess(familyDB.getClass(), familyDB);
		String sql = "select * from shoplists where listname='" + listname + "' and " +
				"email ='" + shoplist.getEmail() + "'";
		List<Object> wrkList = dbacc.readSql(sql); 
		infomsg = "shoplist does not exit for shoplist id="+shoplist.getId();
		if (wrkList == null || wrkList.size() == 0) return false;
		infomsg = "";
		SQLStatus = true;
		return true;
	}
	private boolean addStore(String name) {
		setSQLStatusOK(false);
		infomsg = "failed to add to store row to table-stores="+name+" for shoplist= "+ shoplist.getListname();
		if (name.trim().length() == 0) return false;
		name  = ShopListStatic.specialChars(name);

		Date date = new Date();
		store.setCreatedate(new Timestamp(date.getTime()));
		store.setFamilyid(shoplist.getId());
		store.setName(name);

		DataBaseAccess dbacc = new DataBaseAccess(store.getClass(), store);
		String sql = "select * from stores where familyid='" + shoplist.getId()+ "' and " +
				"name ilike '" + name + "'";
		List<Object> wrkList = dbacc.readSql(sql); 
		infomsg = "shoplist does not exit for shoplist id="+shoplist.getId();
		if (wrkList == null || wrkList.size() == 0) {
			if (!(SQLStatus = dbacc.insertSql(sql, "stores"))) {
				infomsg = "failed to add to store row to table-stores="+name+" for shoplist= "+ shoplist.getListname();
				return false;
			}
			return true;
		}
		
		store = (Stores)wrkList.get(0);
		infomsg = "";
		setSQLStatusOK(true);
		return true;
	}
	private boolean itemExist(String name,DataBaseAccess dbacc) {
		name  = ShopListStatic.specialChars(name);

		String sql = "select * from items where familyid='" + shoplist.getId() + "' "+
				" and storeid ='"+ store.getId() +"'" + 
				" and name ilike '"+ name +"'";
		List<Object> wrkList = dbacc.readSql(sql); 
		if (wrkList == null || wrkList.size() == 0) return false;
		
		infomsg = "identical item="+name+"already exist for shopping list for store="+store.getId();
		System.out.println(infomsg);
		return true;
	}
	private void bldPartialItem(String name) {
		Date date = new Date();
		items.setCreatedate(new Timestamp(date.getTime()));
		items.setName(ShopListStatic.specialChars(name));
		items.setFamilyid(shoplist.getId());
		items.setStoreid(store.getId());
		items.setPhotoid(-1);
		items.setUpddate(new Timestamp(date.getTime()));
	}
	private boolean insertItem(String name,DataBaseAccess dbacc) {
		SQLStatus=false;
		bldPartialItem(name);
		String sql = "select * from items where id='" + shoplist.getId() + "'";
		if (!(SQLStatus = dbacc.insertSql(sql, "items"))) {
			infomsg = "failed to add to item="+name+" to store="+store.getName()+" for shoplist= "+ shoplist.getListname();
			return false;
		}
		return true;
	}
	private boolean deleteItem(String name,DataBaseAccess dbacc) {
		SQLStatus=false;

		String sql = "delete from items where familyid='" + shoplist.getId() + "' and "+
					 "storeid ='"+store.getId()+"' and name='"+ ShopListStatic.specialChars(name) + "'";
		if (!(SQLStatus = dbacc.deleteSql(sql))) {
			infomsg = "failed to delete item="+name+" from store="+store.getName()+" for shoplist= "+ shoplist.getListname();
			return false;
		}
		return true;
	}
	private boolean updateItem() {
		SQLStatus=false;
		DataBaseAccess dbacc = new DataBaseAccess(items.getClass(), items);

		String sql = "select * from items where familyid='" + shoplist.getId()+ "' and " +
				"storeid ='" + store.getId() + "' and "+
				"id ='" + items.getId() + "'";

		if (!(SQLStatus = dbacc.updateSql(sql, "items"))) {
			infomsg = "failed to update "+ sql;
			Exception e = new Exception(infomsg);
			e.printStackTrace();
			return false;
		}
		return true;
	}
	private boolean addItems(List<String> list) {
		DataBaseAccess dbacc = new DataBaseAccess(items.getClass(), items);
		String previous = "";
		for (int i=0;i<list.size();i++) {
			if (previous.equals(list.get(i)) || list.get(i).trim().length() == 0) continue;
			previous = list.get(i).trim();
			if(itemExist(list.get(i),dbacc)) continue;
			
			if(!insertItem(list.get(i),dbacc)) return false;
		}
		return true;
	}
	private boolean addItems(ArrayList<JSONObject> list) {
		DataBaseAccess dbacc = new DataBaseAccess(items.getClass(), items);
		String previous = "";
		for (int i=0;i<list.size();i++) {
			JSONObject obj = list.get(i);
			String name = ((String) obj.get("name")).trim();
			if (previous.equals(name) || name.length() == 0) continue;
			previous = name;
			if(itemExist(name,dbacc)) continue;
			
			bldPartialItem(name);
			this.items.setItemcategory((String) obj.get("category"));
			if(!insertItem(name,dbacc)) return false;
		}
		return true;
	}
	private boolean deleteItems(List<String> list) {
		DataBaseAccess dbacc = new DataBaseAccess(items.getClass(), items);
		for (int i=0;i<list.size();i++) {
			if (list.get(i).trim().length() == 0) continue;
			
			if(!deleteItem(list.get(i),dbacc)) return false;
		}
		return true;
	}
	private boolean resyncItemPhoto(HashMap<String, Long> photoSyncHash) {
		//get shoplist shopping list item based on name
		//if there is at least one return;
		// no item with name; check if photo id == -1 return it has no photo
		// delete photo from photos table.
		infomsg = "";
		DataBaseAccess dbaccI = new DataBaseAccess(items.getClass(), items);
		ItemPhotos itemPhotos =  new ItemPhotos();
		DataBaseAccess dbaccP = new DataBaseAccess(itemPhotos.getClass(), itemPhotos);
		for (String name: photoSyncHash.keySet()) {
			Long photoid = (Long)photoSyncHash.get(name);
			if (photoid == null || photoid == -1) continue;
			
			String sql = "select *  from items where familyid='" + shoplist.getId() + "' and "+
					 "photoid="+ photoid;
			List<Object> itemList = dbaccI.readSql(sql);
			if (itemList != null && itemList.size() > 0) continue;
			sql = "delete from itemphotos where familyid='" + shoplist.getId() + "' and "+
					 "id ="+photoid;
			if (!(SQLStatus = dbaccP.deleteSql(sql))) {
				infomsg = "failed to delete photoid="+name+" for shoplist= "+ shoplist.getListname();
				return false;
			}
		}
		return true;
	}
	private List<Object> getAllItemsFromStore(long id) {
		if (!familyinDB) return null;
		
		DataBaseAccess dbacc = new DataBaseAccess(items.getClass(), items);
		String sql = "select * from items where familyid='"+shoplist.getId()+"' and storeid='" + id +"'";
		return dbacc.readSql(sql); 
	}
	private void setAllStores() {
		infomsg = "";
		setSQLStatusOK(false);
		DataBaseAccess dbacc = new DataBaseAccess(store.getClass(), store);
		String sql = "select * from stores where familyid='" + shoplist.getId()+"'";
		List<Object> storeList = dbacc.readSql(sql); 
		if (storeList == null || storeList.size() == 0) {
			infomsg = "there are no stores for this 'shoplist' id="+shoplist.getId()+" name="+shoplist.getListname();
			return;
		}
		for (int i=0;i<storeList.size();i++) {
			store = (Stores)storeList.get(i);
			List<Object> itemList = this.getAllItemsFromStore(store.getId());
			if (itemList == null || storeList.size() == 0) continue;
			HashMap<String,Object> wrkHashMap = new HashMap<String,Object>();
			for (int j=0;j<itemList.size();j++) {
				Items wrkItem = (Items)itemList.get(j);
				wrkHashMap.put(wrkItem.getName(), wrkItem);
			}
			if (wrkHashMap.size() > 0)
				storeShopList.put(store.getName(), wrkHashMap);
		}
		setSQLStatusOK(true);
	}
	private List<Object> getStoreItemsUsingStoreName(String storename) {
		if (storename == null || storename.length() == 0) return null;
		
		storename  = ShopListStatic.specialChars(storename);

		DataBaseAccess dbacc = new DataBaseAccess(store.getClass(), store);
		String sql = "select * from stores where familyid='" + shoplist.getId()+ "' and " +
				"name ='" + storename + "'";
		List<Object> wrkList = dbacc.readSql(sql);
		
		infomsg = "shoplist does not exit for shoplist id="+shoplist.getId();
		if (wrkList == null || wrkList.size() == 0) return null;
		store = (Stores)wrkList.get(0);
		return getAllItemsFromStore(store.getId());
	}
	
	public List<Object> getStoreItems(long id) {
		return getAllItemsFromStore(id);
	}
	public void addList(String storeName,List<String> list) {
		if (!familyinDB) return;
		if (!addStore(storeName)) return;
		if (!addItems(list)) return;
		infomsg = "success adding item list to store="+store.getName()+" for shoplist= "+ shoplist.getListname();

	}
	public void addList(String storeName,ArrayList<JSONObject> list) {
		if (!familyinDB) return;
		if (!addStore(storeName)) return;
		if (!addItems(list)) return;
		infomsg = "success adding item list to store="+store.getName()+" for shoplist= "+ shoplist.getListname();

	}
	public void deleteList(String storeName,List<String> list) {
		if (!familyinDB) return;
		if (!addStore(storeName)) return;
		if (!deleteItems(list)) return;
		infomsg = "success deleted items from list to store="+store.getName()+" for shoplist= "+ shoplist.getListname();
	}
	public void updateList(String storeName,List<String> list) {
		if (!familyinDB) return;
		if (!addStore(storeName)) return;
		if (!addItems(list)) return;
		infomsg = "success update items from list to store="+store.getName()+" for shoplist= "+ shoplist.getListname();
	}
	public boolean isSQLStatusOK() {
		return SQLStatus;
	}
	public void loadAllStores() {	
		if (!familyinDB) return;
		setAllStores();
	}
	public boolean updateStoreItem(Items item) {
		this.items = item;
		this.store = new Stores();
		store.setId(item.getStoreid());
		return updateItem();
	}

	public void updateStoreItemsJson(String key, JSONArray  jsonItems) {
		List<Object> itemList = getStoreItemsUsingStoreName( key);
		if (itemList == null || itemList.size() == 0) return;

		for (int i=0;i<jsonItems.size();i++) {
			JSONObject jsonItem = (JSONObject) jsonItems.get(i);
			String desc = (String)jsonItem.get("desc");
			String name = (String)jsonItem.get("name");
			String category = (String)jsonItem.get("category");
			Boolean selected = (Boolean)jsonItem.get("selected");
			
			for (int j=0;j<itemList.size();j++) {
				items = (Items)itemList.get(j);
				if (!items.getName().equals(name)) continue;
				
				items.setDescriptions(desc);
				items.setItemcategory(category);
				items.setBuyingorder(((Long)jsonItem.get("buyingorder")).intValue());
				Date date = new Date();
				items.setUpddate(new Timestamp(date.getTime()));
				items.setActive(0);
				if (selected) items.setActive(1);
				updateItem();
				break;
			}
		}
	}
	public void addDeleteTempItems(String key, JSONArray jsonObjs) {

		DataBaseAccess dbacc = new DataBaseAccess(items.getClass(), items);
		for (int i=0;i<jsonObjs.size();i++) {
			JSONObject jsonItem = (JSONObject) jsonObjs.get(i);
			Boolean temporary = (Boolean)jsonItem.get("temporary");
			String name = (String)jsonItem.get("name");
			String category = (String)jsonItem.get("category");
			Boolean selected = (Boolean)jsonItem.get("selected");
			if (!temporary) continue;
			
			items.setTemp(temporary);
			items.setItemcategory(category);
			if (selected) items.setActive(1);
			if(selected ) {
				if(itemExist(name,dbacc)) continue;
				if(!insertItem(name,dbacc)) return;
			} else {
				if (!deleteItem(name, dbacc)) return;
			}
		}
		
	}
	public void resynchItemPhoto(HashMap<String, Long> photoSyncHash) {
		resyncItemPhoto(photoSyncHash);	
	}
/*
 * getters and setters
 */
	public void setSQLStatusOK(boolean sQLStatusOK) {
		SQLStatus = sQLStatusOK;
	}
	public String getInfomsg() {
		return infomsg;
	}
	public void setInfomsg(String infomsg) {
		this.infomsg = infomsg;
	}
	public HashMap<String, HashMap<String, Object>> getStoreShopList() {
		return storeShopList;
	}
	public void setStoreShopList(HashMap<String, HashMap<String, Object>> storeShopList) {
		loadAllStores();
		this.storeShopList = storeShopList;
	}
}
