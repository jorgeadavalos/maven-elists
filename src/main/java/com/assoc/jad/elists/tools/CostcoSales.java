package com.assoc.jad.elists.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class CostcoSales {

	private static final String DATETAG		= "Offers Valid";
	private static final String ITEMTAG		= 	"<div class=\"ftr-text\">"; 
	private static final String PRODNAME	= 	"<div class=\"prod\">";
	private static final String ENDTAG		= 	"ONLINE ONLY OFFERS";
	private static final String COSTCOJSON	= "/jadtemp/costcoSales.json";

	
	private ArrayList<JSONObject> jsonlist = new ArrayList<JSONObject>();
	private boolean endRun = false;
		
	private String bldDate(String dates) {
		String[] wrkDate = dates.split("/");
		int YYYY  = Integer.valueOf(wrkDate[2].trim())+2000;
		int month = Integer.valueOf(wrkDate[0].trim());
		month--;
		int date = Integer.valueOf(wrkDate[1].trim());
		Calendar calendar = new GregorianCalendar(YYYY,month,date);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(calendar.getTime());
	}
	@SuppressWarnings("unchecked")
	private void stripText(String block,JSONObject wrkJson) {
		String text = "";
		String desc = "";
		String name = "";
		int ndx1 = 0;
		int ndx2 = 0;
		ndx1 = block.indexOf(PRODNAME);
		if (ndx1 == -1) return;
		text = block.substring(0,ndx1);
		name = block.substring(++ndx1);

		while (ndx1 != -1 ) {
			ndx1 = text.indexOf('>',ndx2);
			if (ndx1 == -1) break;
			
			ndx2 = text.indexOf('<', ++ndx1);
			if (ndx2 == -1) ndx2 = text.length();
			desc += block.substring(ndx1,ndx2).trim()+" ";
		
		}
		ndx1 = name.indexOf('>');
		ndx2 = name.indexOf('<');
		if (ndx2 == -1) ndx2 = name.length();
		if (ndx1 != -1) name = name.substring(++ndx1,ndx2);
		wrkJson.put("desc", desc);
		wrkJson.put("name", name.trim());
	}

	private String readBlock(BufferedReader br,String tag) {
		String line = null;
		String block = "";
		try {
			while ((line = br.readLine()) != null) {
				if (line.indexOf(tag) != -1) break;
				block += line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		int ndx = block.indexOf(ENDTAG);
		if (ndx != -1) {
			endRun = true;
			return block.substring(0,ndx);
		}
		return block; 
	}
	private String readTags(BufferedReader br,String tag) {
		String line = null;
		int ndx  = -1;
		try {
			while ((line = br.readLine()) != null) {
				if ((ndx = line.indexOf(tag)) == -1) continue;
				return line.substring(ndx);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return line; 
	}
	@SuppressWarnings("unchecked")
	private void executeStore() {
		String COSTCOFILEIN	= "/jadtemp/Warehouse Savings _ Costco.html";
		int ndx = 0;
		BufferedReader br	= null;
		BufferedWriter bw	= null;
		File costcoFileIn = new File(COSTCOFILEIN);
		File costcoJson   = new File(COSTCOJSON);
		try {
			br = new BufferedReader(new FileReader(costcoFileIn));
			bw = new BufferedWriter(new FileWriter(costcoJson));
			String line = readTags(br,DATETAG);
			if (line == null) return;
			ndx = line.indexOf('<');
			if (ndx != -1) line = line.substring(DATETAG.length(),ndx);
			String[] dates = line.split("-");
			JSONObject wrkJson = new JSONObject();
			String from = bldDate(dates[0]);
			String to = bldDate(dates[1]);
			
			line = readTags(br,ITEMTAG);
			while (line.length() > 0) {
				line = readBlock(br,ITEMTAG);
				stripText(line,wrkJson);
				wrkJson.put("valid_from", from);
				wrkJson.put("valid_to",to);
				bw.write(wrkJson.toJSONString()+System.lineSeparator());
				if (endRun) break;
			}
			br.close();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) try { br.close(); } catch (IOException e) {}
			if (bw != null) try { bw.close(); } catch (IOException e) {}
		}
	}
	private void bldJsonList() {
		BufferedReader br	= null;
		File costcoJson   = new File(COSTCOJSON);
		String line = null;
		JSONParser parser = new JSONParser();
		try {
			br = new BufferedReader(new FileReader(costcoJson));
			while ((line = br.readLine()) != null) {
				JSONObject wrkJson =(JSONObject) parser.parse(line);
				jsonlist.add(wrkJson);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) try { br.close(); } catch (IOException e) {}
		}
	}	
	private void storeSalesPage() {
		Tools tools = new Tools();
		tools.CmdProcessor("/jadtemp","/Program Files (x86)/Mozilla Firefox/firefox.exe https://www.costco.com/warehouse-savings.html",false);
	}
	public static void main(String[] args) {
		CostcoSales store = new CostcoSales();
		store.storeSalesPage();
		store.executeStore();
	}
	@SuppressWarnings("unchecked")
	public String getJsonObj() {
		JSONObject jsonObj = new JSONObject();
		if (jsonlist.size() == 0) bldJsonList();
		jsonObj.put("costco",JSONArray.toJSONString(jsonlist));
		return jsonObj.toJSONString();
	}
}
