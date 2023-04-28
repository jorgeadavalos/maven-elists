package com.assoc.jad.elists.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.assoc.jad.elists.repository.EmailsDBAccess;
import com.assoc.jad.elists.repository.ShoplistDBAccess;
import com.assoc.jad.elists.repository.StoreDBAccess;
import com.assoc.jad.elists.repository.model.Shoplist;
import com.assoc.jad.elists.tools.ShopListStatic;

@SessionScoped
@Named("familyBean")
public class FamilyBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private Shoplist	shoplist 		= new Shoplist();
	private String 	infomsg="";
	private String 	familyKey;
	private String	emailList;
	private String	itemList;
	private String	navigator;
	private String email;
	private String storeName;
	private List<SelectItem> familyList  = new ArrayList<SelectItem>();
	private String familyId;

	@Inject
	MasterListBean masterListBean;
	
	public FamilyBean() {
	}
	@PostConstruct
	public void backupInstance() {
		HttpServletRequest req = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
		req.getSession().setAttribute("familyBean",this);
	}
	private void addFamilyItem(String name,Long id) {
		SelectItem item = new SelectItem();
		item.setLabel(name);
		item.setValue(id.toString());
		familyList.add(item);		
	}
	private String getHTMLParam(String name) {
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext external = context.getExternalContext();
		HttpServletRequest request = (HttpServletRequest) external.getRequest();
		String parm = request.getParameter(name);
		if (parm == null) {
			String formname =  request.getParameter("formname");
			parm = request.getParameter(formname+":"+name);
		}
		return parm;
	}
	private void resetCookie(String name) {
		HttpServletRequest req   =  (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
		HttpServletResponse resp =  (HttpServletResponse)FacesContext.getCurrentInstance().getExternalContext().getResponse();
		Cookie[] cookies = req.getCookies();
		if (cookies != null) 
			for (int i=0;i<cookies.length;i++) {
				if (name.equals(cookies[i].getName())) {
					cookies[i].setValue("");
		            cookies[i].setMaxAge(0);
		            resp.addCookie(cookies[i]);
					break;
				}
			}
	}
	private void getEmailFromCookie() {
		String wrkEmail = "";
		wrkEmail = getHTMLParam("email");
		if (wrkEmail == null) wrkEmail = getHTMLParam("parm"); //iphone interprets 'email' to start mail app
		if (wrkEmail == null) {
			HttpServletRequest req   =  (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
			Cookie[] cookies = req.getCookies();
			if (cookies != null) 
				for (int i=0;i<cookies.length;i++) {
					if ("email".equals(cookies[i].getName())) {
						wrkEmail = cookies[i].getValue();
						break;
					}
				}
		}
		if (wrkEmail != null) email = wrkEmail;
		getFamilyUsingEmail();
	}
	private void setEmailFromCookie() {
		HttpServletResponse resp =  (HttpServletResponse)FacesContext.getCurrentInstance().getExternalContext().getResponse();
		Cookie cookie = new Cookie("email",email);
		try {  //android fails when setting a cookie
	    resp.addCookie( cookie );
		}catch (Exception e) {}
	}
	private boolean isFamilyReady() {
		infomsg="";
		if (shoplist.getId() <= 0 || shoplist.getListname() == null) {
			infomsg = "needs to have a shoplist record. Click 'select a shoplist' from menu";
			//savedNavigation = getRedirect();
			return false;
		}
		getFamilyUsingId(); //resync shoplist object with possible changes to 'familyid'
		return true;
	}
	private boolean getMasterListBean() {
//		ExternalContext external = FacesContext.getCurrentInstance().getExternalContext();
//		HttpServletRequest req  = (HttpServletRequest) external.getRequest();
//		MasterListBean masterListBean = (MasterListBean)req.getSession().getAttribute("masterListBean");
//		if (masterListBean == null) {
//			masterListBean = new MasterListBean();
//			req.getSession().setAttribute("masterListBean",masterListBean);
//		}
		StoreDBAccess storesdba = new StoreDBAccess(shoplist);
		storesdba.loadAllStores();
		masterListBean.setStoreShopList(storesdba.getStoreShopList());
		masterListBean.setShoplist(shoplist);
		return true;
	}
	private void getFamilyUsingEmail() {
		if (email == null || email.length() == 0) return;
		if (shoplist != null && 
		(familyId != null && familyId.length() > 0) &&
		email.equals(shoplist.getEmail()) && shoplist.getId() == Long.valueOf(familyId)) return;
		
		ShoplistDBAccess familyDBAccess = new ShoplistDBAccess();
		List<Object> wrkList = familyDBAccess.getfamilyWithEmail(email);
		infomsg = familyDBAccess.getInfomsg();
		if (wrkList == null) {
			ShopListStatic.redirect(ShopListStatic.NEWSHOPPINGLIST);
			return;
		}
		
		getMasterListBean();
		if (wrkList.size() == 1) {
			shoplist = (Shoplist)wrkList.get(0);
			familyId = Long.toString(shoplist.getId());
			masterListBean.setShoplist(shoplist);
			ShopListStatic.redirect(ShopListStatic.STORELIST);
			return;
		}
		familyList  = new ArrayList<SelectItem>();
		for (int i=0;i<wrkList.size();i++) {
			shoplist = (Shoplist)wrkList.get(i);
			addFamilyItem(shoplist.getListname(),shoplist.getId());
		}
		infomsg = "Please click dropdown to choose a list";
		familyId = "";
		//navigator = FamilyBean.REDIRECTED;
		ShopListStatic.redirect(ShopListStatic.SELECTFAMILY2);
		return;
	}
	private void getFamilyUsingId() {
		if (familyId == null || familyId.length() == 0) return;
		if (shoplist.getId() == (Long.valueOf(familyId))) {
			ShopListStatic.redirect(ShopListStatic.STORELIST);
			return;
		}
		
		ShoplistDBAccess familyDBAccess = new ShoplistDBAccess();
		shoplist = familyDBAccess.getFamily(Long.valueOf(familyId));
		getMasterListBean();
		ShopListStatic.redirect(ShopListStatic.STORELIST);
	}
	private boolean addFamily() {
		ShoplistDBAccess familyDBAccess = new ShoplistDBAccess(shoplist);
		familyDBAccess.createFamily();
		infomsg = familyDBAccess.getInfomsg();
		familyKey = familyDBAccess.getFamilyKey();
		shoplist = familyDBAccess.getFamily();
		return familyDBAccess.isSQLStatusOK();
	}
	private void resetInfomsg() {
//		HttpServletRequest req  = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
//		MasterListBean masterListBean = (MasterListBean)req.getSession().getAttribute("masterListBean");
		infomsg = "";
		masterListBean.setInfomsg(infomsg);
	}
	
	
	public void familyViaEmail(ActionEvent event) {
		shoplist = new Shoplist();
		getEmailFromCookie();
	}
	public void selectedList(ActionEvent event) {
		getEmailFromCookie();
	}
	public void newShoppingList(ActionEvent event) {
		shoplist.setEmail(email);
		addFamily();
	}
	public void additemList(ActionEvent event) {
		resetInfomsg();
		if (storeName == null || storeName.length() == 0 || 
			itemList == null || itemList.trim().length() == 0) return;
		
		String[] wrkArray = itemList.split("(:|\\s+|\\n|\\t|;|,)");
		List<String> list = Arrays.asList(wrkArray);;

		StoreDBAccess storesdba = new StoreDBAccess(shoplist);
		storesdba.addList(storeName,list);
		infomsg = storesdba.getInfomsg();
	}

	public void addFamily(ActionEvent event) {
			navigator = "addFamily";
			if (addFamily()) navigator = "addEmails";
	}
	public void addEmails(ActionEvent event) {
		navigator = "getShoplist";
		familyId = null;  //shoplist already verified familyid is not releveant
		if (!isFamilyReady()) return;
		
		EmailsDBAccess emailsDBAccess = new EmailsDBAccess(shoplist);
		emailsDBAccess.addEmails(emailList);
		infomsg = emailsDBAccess.getInfomsg();
		familyKey = emailsDBAccess.getShoplistKey();
		shoplist = emailsDBAccess.getShoplist();
		navigator = "addEmails";
		if (emailsDBAccess.isSQLStatusOK()) navigator = "addShoppingList";
	}
	public String navigation() {
		return navigator;
	}
	/*
	 * getters and setters
	 */
	public Shoplist getShoplist() {
		return shoplist;
	}
	public void setFamily(Shoplist shoplist) {
		this.shoplist = shoplist;
	}
	public String getInfomsg() {
		return infomsg;
	}
	public void setInfomsg(String infomsg) {
		this.infomsg = infomsg;
	}
	public String getFamilyKey() {
		return ShopListStatic.undoSpecialChars(familyKey);
	}
	public void setFamilyKey(String familyKey) {
		this.familyKey = familyKey;
	}
	public void setEmail(String email) {
		setEmailFromCookie();
		this.email = email;
	}
	public String getEmail() {
		//getEmailFromCookie();
		return email;
	}
	public String getEmail2() {
		return email;
	}
	public void setEmail2(String email) {
		this.email = email;
	}
	public String getEmailList() {
		return emailList;
	}
	public void setEmailList(String emailList) {
		this.emailList = emailList;
	}
	public List<SelectItem> getFamilyList() {
		if (email == null || email.length() == 0) return familyList;

		familyList = new ArrayList<SelectItem>();
		ShoplistDBAccess familyDBAccess = new ShoplistDBAccess();
		List<Object> wrkList = familyDBAccess.getfamilyWithEmail(email);
		if (wrkList == null) {
			infomsg = "no shoplist for email="+email+" in DB";
			return familyList;
		}
		infomsg = familyDBAccess.getInfomsg();
		Shoplist wrkFamily = new Shoplist();
		for (int i=0;i<wrkList.size();i++) {
			wrkFamily = (Shoplist)wrkList.get(i);
			addFamilyItem(wrkFamily.getListname(),wrkFamily.getId());
		}
		return familyList;
	}
	public String getHtml() {
		if (navigator == null || navigator.length() == 0) navigator = "addFamily";
		return navigator;
	}
	public String getStoreName() {
		return storeName;
	}
	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}
	public String getFamilyId() {
		getFamilyUsingId();
		return familyId;
	}
	public void setFamilyId(String familyId) {
		this.familyId = familyId;
		getFamilyUsingId();
	}
	public void setResetFamily(String familyId) {
		resetCookie("email");
		email = "";
		this.familyId = null;
	}
	public String getResetFamily() {
//		HttpServletRequest req = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
//		MasterListBean masterListBean = (MasterListBean)req.getSession().getAttribute("masterListBean");
//		if (masterListBean != null) {
//			masterListBean.setStoreShopList(new HashMap<String, HashMap<String, Object>>());
//		}

		this.familyId = null;
		resetCookie("email");
		familyList  = new ArrayList<SelectItem>();
		email = "";
		resetInfomsg();
		return "";
	}
	public String getItemList() {
		return itemList;
	}
	public void setItemList(String itemList) {
		this.itemList = itemList;
	}
	public void checkURL() {
		String email = getHTMLParam("parm");
		if (email != null) 	{
			this.email = email;
			getFamilyUsingEmail();
		}
	}
}
