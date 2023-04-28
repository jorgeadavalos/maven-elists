package com.assoc.jad.elists.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.primefaces.model.UploadedFile;

public class PersonalFileItems {

	private List<String> rowList = new ArrayList<String>();
	private UploadedFile uploadedFile = null;
	private HashMap<String, List<String>> storeShopList = new HashMap<String,List<String>>();

	public PersonalFileItems(UploadedFile uploadedFile) {
		this.uploadedFile = uploadedFile;
		accessSHopList();
	}

	private void ReadExcel() {
		Workbook wb   = null;
		try {			
			wb = WorkbookFactory.create(uploadedFile.getInputstream());
		    XSSFSheet sheet = (XSSFSheet) wb.getSheetAt(0);
			Iterator<?> rows = sheet.iterator();
			while (rows.hasNext()) {
				XSSFRow row = (XSSFRow) rows.next();
				Iterator<?> cells = row.cellIterator();
				StringBuilder onerow = new StringBuilder("");
				int prevCellNdx = 0;
				while (cells.hasNext()) {
					XSSFCell cell = (XSSFCell) cells.next();
					for (;prevCellNdx<cell.getColumnIndex();prevCellNdx++) onerow.append(';');
					onerow.append(cell.getStringCellValue());
				}
				rowList.add(onerow.toString());
			}
			wb.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (EncryptedDocumentException e) {
			e.printStackTrace();
		}
	}
	/*
	 * first line contains the store's name 
	 * all other lines contain the item's names with a check marks 
	 * on the column that points to the first line,which is 
	 * the store's name
	 */
	private void bldHashItems() {
		if (rowList.size() == 0) return;
		
		/* headers start at one to line up with products checks*/
		String[] headers = rowList.get(0).split(";");
		for (int i=0;i<headers.length;i++) {
			if (headers[i].trim().length() == 0) continue;
			storeShopList.put(headers[i],new ArrayList<String>());
		}
		
		/* start on 1 to select only item's; skip headers */
		for (int i=1;i<this.rowList.size();i++) {
			String[] itemStores = rowList.get(i).split(";");
			/* start on 1 to skip item name */
			for (int j=1;j<itemStores.length;j++) {
				if (itemStores[j] == null || itemStores[j].length() == 0) continue;
				storeShopList.get(headers[j]).add(itemStores[0]);
			}
		}
	}

	private void accessSHopList() {
		if (uploadedFile.getFileName().indexOf(".xls") != -1) ReadExcel();
		bldHashItems();
	}
	/*
	 * getters and setters
	 */

	public HashMap<String, List<String>> getStoreShopList() {
		return storeShopList;
	}

	public void setStoreShopList(HashMap<String, List<String>> storeShopList) {
		this.storeShopList = storeShopList;
	}

}