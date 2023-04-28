package com.assoc.jad.elists.bean;

import java.sql.Timestamp;
import java.util.List;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;

import org.primefaces.model.UploadedFile;

import com.assoc.jad.elists.repository.DataBaseAccess;
import com.assoc.jad.elists.repository.model.ItemPhotos;
import com.assoc.jad.elists.repository.model.Items;
import com.assoc.jad.elists.repository.model.Shoplist;

@SessionScoped
@Named("itemPhotoBean")
public class ItemPhotoBean {
	
	private Shoplist family;
	private String infomsg;

	public ItemPhotoBean(Shoplist family) {
		this.family = family;
	}
	public ItemPhotos updatePhoto(UploadedFile uploadedFile,String name ) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) return null;

		ItemPhotos photo = new ItemPhotos();
		DataBaseAccess dbacc = new DataBaseAccess(photo.getClass(),photo);
		String sql = "delete from itemphotos where familyid="+family.getId()+" and name='"+name+"'";
		if (!dbacc.deleteSql( sql)) {
			infomsg = "..problem deleting photo for familyid="+family.getId()+" name="+name+".. Will add";
		}
		java.util.Date dateBin = new java.util.Date();
		photo.setName(name);
		photo.setCreatedate(new Timestamp(dateBin.getTime()));
		photo.setFamilyid(family.getId());
		sql = "select * from itemphotos where id=1";
		try {
			if (!dbacc.insertByteArray(uploadedFile.getContents(),sql,"itemphotos")) {
				infomsg = "failed to add photo "+photo.getName()+"to photos table productid="+family.getId();
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		photo.setPhoto(uploadedFile.getContents());
		return photo;
	}
	public boolean resetItemsPhotoPointer(ItemPhotos itemPhoto) {
		Items item = new Items();
		DataBaseAccess dbacc = new DataBaseAccess(item.getClass(),item);
		String sql = "select * from items where photoid="+itemPhoto.getId();
		List<Object> wrkList = dbacc.readSql(sql); 
		for (int i=0;i<wrkList.size();i++) {
			item = (Items)wrkList.get(i);
			item.setPhotoid(-1);
			if (!(dbacc.updateSql(sql, "items"))) {
				infomsg = "failed to update "+ sql;
				Exception e = new Exception(infomsg);
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	/*
	 * getters and setters
	 */
	public String getInfomsg() {
		return infomsg;
	}
	public void setInfomsg(String infomsg) {
		this.infomsg = infomsg;
	}
}
