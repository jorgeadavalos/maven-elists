package com.assoc.jad.elists.tools;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;

import com.assoc.jad.elists.repository.DataBaseAccess;
import com.assoc.jad.elists.repository.model.Stores;

public class ConfigStoreSalesJson implements Runnable {
	HashMap<String, Object> storeObjs = new HashMap<String, Object>();

	public ConfigStoreSalesJson() {
	}

	private void getAllStores() {
		Stores store = new Stores();
		DataBaseAccess dbacc = new DataBaseAccess(store.getClass(), store);
		String sql = "select * from stores";
		List<Object> storeList = dbacc.readSql(sql);
		if (storeList == null || storeList.size() == 0) {
			System.err.println("ConfigStoreSalesJson::getAllStores Store table is empty");
			return;
		}
		for (int i = 0; i < storeList.size(); i++) {
			store = (Stores) storeList.get(i);
			storeObjs.put(store.getName().toUpperCase(), store);
		}
	}

	private void bldStoresJsons() {
		for (String key: storeObjs.keySet()) {
			Stores store = (Stores) storeObjs.get(key);
			StringBuilder name = new StringBuilder(store.getName());
			byte cap = (byte) name.charAt(0);
			cap = (byte)(0xdf & cap);
			name.setCharAt(0, (char) cap);

			String clazzname = "com.assoc.jad.shoplist.tools.SalesJson"+ name.toString();
			try {
					Class<?> clazz = Class.forName(clazzname);
					Constructor<?> cons = clazz.getConstructor();
					SalesJson salesJson = (SalesJson) cons.newInstance();
					salesJson.run(store.getName());
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
	}

	public void run() {
		getAllStores();
		bldStoresJsons();

/*		int ndx = line.indexOf("=");
		if (ndx == -1) return;
		CRDTabPanelJar jarTools = new CRDTabPanelJar(Logger);
		jarTools.scrollPane = scrollPane;
		BatchReport.listClasses.put(search, jarTools);*/
	}
}
