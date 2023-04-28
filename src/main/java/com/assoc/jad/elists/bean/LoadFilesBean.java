package com.assoc.jad.elists.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.primefaces.model.UploadedFile;

import com.assoc.jad.elists.repository.StoreDBAccess;
import com.assoc.jad.elists.repository.model.ItemPhotos;
import com.assoc.jad.elists.repository.model.Items;
import com.assoc.jad.elists.repository.model.Shoplist;
import com.assoc.jad.elists.tools.PersonalFileItems;
import com.assoc.jad.elists.tools.ShopListStatic;

@SessionScoped
@Named("loadFilesBean")
public class LoadFilesBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private UploadedFile uploadedFile;
	private String mainPhoto;
	private String infomsg;
	private Shoplist shoplist;
	@Inject
	private MasterListBean masterListBean;
	@Inject
	private FamilyBean familyBean;
	private String email;
	
	private boolean itHasAFamily() {
		infomsg = "there is no shoplist associated with this request";
		shoplist = null;
//		ExternalContext external = FacesContext.getCurrentInstance().getExternalContext();
//		HttpServletRequest req  = (HttpServletRequest) external.getRequest();
//		masterListBean = (MasterListBean)req.getSession().getAttribute("masterListBean");
//		FamilyBean familyBean = (FamilyBean)req.getSession().getAttribute("familyBean");
//		if (masterListBean == null) {
//			masterListBean = new MasterListBean();
//			req.getSession().setAttribute("masterListBean",masterListBean);
//		}
		if (masterListBean.getShoplist() == null) {
			if (familyBean != null)
				masterListBean.setShoplist(familyBean.getShoplist());
		}
		shoplist = masterListBean.getShoplist();
		if (shoplist == null ) {
			if (familyBean != null) familyBean.setInfomsg(infomsg);
			ShopListStatic.redirect(ShopListStatic.SELECTFAMILY);
			return false;
		}
		infomsg = "";
		return true;
	}	
	private boolean itHasEmail() {
		infomsg = "there is no Email associated with this request";
		ExternalContext external = FacesContext.getCurrentInstance().getExternalContext();
		HttpServletRequest req  = (HttpServletRequest) external.getRequest();
		FamilyBean familyBean = (FamilyBean)req.getSession().getAttribute("familyBean");
		if (familyBean != null) {
			this.email = familyBean.getEmail();
		}
		if (email == null ) return false;
		infomsg = "";
		return true;
	}	
	private String getHTMLParam(String name) {
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext external = context.getExternalContext();
		HttpServletRequest request = (HttpServletRequest) external.getRequest();
		return request.getParameter(name);
	}
	public void uploadFile() {
		if (!itHasEmail()) return;
		if (uploadedFile == null || !itHasAFamily()) return;

		PersonalFileItems pfc = new PersonalFileItems(uploadedFile);
		HashMap<String, List<String>> storesLists = pfc.getStoreShopList();

		StoreDBAccess storesDBA = new StoreDBAccess(shoplist);
		if (!storesDBA.isSQLStatusOK()) {//shoplist does not exist in DB
			ShopListStatic.redirect(ShopListStatic.SELECTFAMILY);
			return;
		} else {
			for (String key: storesLists.keySet()) {
				storesDBA.addList(key,storesLists.get(key));
			}
			storesDBA.loadAllStores();
			masterListBean.setStoreShopList(storesDBA.getStoreShopList());
		}
	}
	public void uploadItemPhoto(ActionEvent event) {
		uploadItemPhoto();	
	}
	public void uploadItemPhoto() {
		if (uploadedFile == null || !itHasAFamily()) return;
		JSONObject jsonitem=null;
		try {
			jsonitem = (JSONObject) new JSONParser().parse(getHTMLParam("jsonitem"));
		} catch (ParseException e) {
			throw new RuntimeException("Unable to parse json " + jsonitem);
		}
		String photoname = uploadedFile.getFileName();
		int ndx = photoname.lastIndexOf('.');
		String ext = "";
		String itemName = (String)jsonitem.get("name");
		if (ndx != -1 ) ext = photoname.substring(++ndx);
		photoname = itemName+"."+ext;
		ItemPhotoBean itemPhotobean = new ItemPhotoBean(shoplist);
		ItemPhotos photo = itemPhotobean.updatePhoto(uploadedFile, photoname);
		if (photo == null) {
			masterListBean.setInfomsg(itemPhotobean.getInfomsg());
			ShopListStatic.redirect(ShopListStatic.STORELIST);
			return;
		}
		
		HashMap<String, HashMap<String, Object>> familySHopList = masterListBean.getStoreShopList();
		StoreDBAccess storeDBAccess = new StoreDBAccess(shoplist);
		for (String keyStore: familySHopList.keySet()) {
			HashMap<String, Object> storeItems = familySHopList.get(keyStore);
				Items item = (Items) storeItems.get(itemName);
				if (item == null) continue;
				item.setPhotoid(photo.getId());
				if (!storeDBAccess.updateStoreItem(item)) {
					masterListBean.setInfomsg(storeDBAccess.getInfomsg());
					ShopListStatic.redirect(ShopListStatic.STORELIST);
					return;
				}
		}
		masterListBean.setInfomsg("successfully added photo to database");
		//ShopListStatic.redirect(ShopListStatic.STORELIST);
	}
/*
 * getters and setters
 */
	public UploadedFile getUploadedFile() {
		return uploadedFile;
	}
	public void setUploadedFile(UploadedFile uploadedFile) {
		this.uploadedFile = uploadedFile;
	}
	public String getMainPhoto() {
		return mainPhoto;
	}
	public void setMainPhoto(String mainPhoto) {
		this.mainPhoto = mainPhoto;
	}
	public String getInfomsg() {
		return infomsg;
	}
	public void setInfomsg(String infomsg) {
		this.infomsg = infomsg;
	}
}
