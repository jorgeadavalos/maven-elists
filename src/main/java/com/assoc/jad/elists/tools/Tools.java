package com.assoc.jad.elists.tools;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
/**
 * modified from common's project on 8/31/2017 trimmed version
 * @author jorge
 *
 */
public class Tools {
	public static String endOfRepTag       = "";
	public boolean addSpace = true;
	public boolean cmdExecuted = false;
	private long JVMHalffreeMemory = -1;
	private char[] hexvalue = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
	private StringBuffer sb = new StringBuffer();

	public String ParseQuery(String query, String token) {
		String result="";
		if (query == null) return result;
		query = query.trim();
		int ndx2=-1;
		int ndx1 = query.indexOf(token);
		if (ndx1 != -1 ) ndx2 = query.indexOf("=",ndx1);

		if (ndx2 != -1 ) ndx1 = query.indexOf("&",ndx2);
		if (ndx1 == -1 ) ndx1 = query.length();
		if (ndx2 != -1 ) result = query.substring(ndx2+1,ndx1);
		return result;
	}
	public String toHexString(byte[] bytes,int cnt) {
		byte wrkbyte;
		int hex;
		StringBuffer sb = new StringBuffer();
		 try {
		 	for (int ii=0;ii<cnt;ii++) {
			 	wrkbyte = bytes[ii];
			 	wrkbyte=(byte)(wrkbyte >>4);
			 	wrkbyte = (byte)(wrkbyte & 0x0f);
		        hex = wrkbyte;
		        sb.append(hexvalue[hex]);
			 	wrkbyte = bytes[ii];
			 	wrkbyte = (byte)(wrkbyte & 0x0f);
		        hex = wrkbyte;
		        sb.append(hexvalue[hex]);
		        if (addSpace) sb.append(" ");
		 	}
		 } catch (Exception cnse) {
		 	System.out.println(cnse);
		 }
		 return (sb.toString());
	}
	public String toHexString(String parm) {
		if (parm == null) return "";
		if (parm.length() == 0) return parm;
		addSpace = false;
		return toHexString(parm.getBytes(),parm.length());
	}
	private boolean isHexString(String parm) {
 		boolean ishex = false;
	 	for (int ii=0;ii<parm.length();ii++) {
	 		ishex = false;
	 		for (int jj = 0;jj<hexvalue.length;jj++) {
	 			if (parm.charAt(ii) == hexvalue[jj]) {
	 				ishex = true;
	 				break;
	 			}
	 		}
	 		if (!ishex) return ishex;
	 	}
		 return ishex;
	}
	private String fromHexString(String parm,int cnt) {
		int hex;
		StringBuffer sb = new StringBuffer();
		for (int i=0;i<parm.length();i++) {
			hex = 0;
	 		for (int j=0;j<hexvalue.length;j++) {
	 			if (parm.charAt(i) != hexvalue[j]) continue;
			 	hex = j*16;
			 	break;
	 		}
 			i++;
	 		for (int j=0;j<hexvalue.length;j++) {
	 			if (parm.charAt(i) != hexvalue[j]) continue;
			 	hex += j;
			 	break;
	 		}
			if (hex > 127) sb.append("&#"+hex);
			else           sb.append((char)(hex));
 		}
		return (sb.toString());
	}
	
	public String fromHexString(String parm) {
		if (!isHexString(parm)) return null;
		return fromHexString(parm,parm.length());
	}
	public byte[] fromHexStringToByteArray(String parm) {
		byte[] bytes = null;
		int k = 0;
		
		if (!isHexString(parm)) return null;
		bytes = new byte[parm.length()/2]; // a character is a nibble.(4bits)
		
		int hex;
		for (int i=0;i<parm.length();i++) {
			hex = 0;
	 		for (int j=0;j<hexvalue.length;j++) {
	 			if (parm.charAt(i) != hexvalue[j]) continue;
			 	hex = j*16;
			 	break;
	 		}
 			i++;
	 		for (int j=0;j<hexvalue.length;j++) {
	 			if (parm.charAt(i) != hexvalue[j]) continue;
			 	hex += j;
			 	break;
	 		}
	 		bytes[k++] = (byte)hex;
 		}
		return bytes;
	}
	public void decimalToHexConvert(int num) {
		int divisor = num / 16;
		int remainder = num % 16;
		if (divisor > 16) decimalToHexConvert(divisor);
		else sb.append(hexvalue[divisor]);
		
		sb.append(hexvalue[remainder]);

	}
	public byte[] bldIntToArray(int parm) {
/*		decimalToHexConvert(parm);
		StringBuffer fourbytes = new StringBuffer(8);
		for (int i=0;i<8-sb.length();i++) fourbytes.append(hexvalue[0]);
		fourbytes.append(sb);
		return fromHexStringToByteArray(fourbytes.toString());*/
		
		byte[] wrkbuf = new byte[4];
		wrkbuf[0] = (byte)(0xff & (parm >> 24));
		wrkbuf[1] = (byte)(0xff & (parm >> 16));
		wrkbuf[2] = (byte)(0xff & (parm >>  8));
		wrkbuf[3] = (byte)(0xff & parm);

		return wrkbuf;
	}
	public byte[] testInt(int parm) {
		byte[] wrkbuf = new byte[4];
		wrkbuf[0] = (byte)(0xff & (parm >> 24));
		wrkbuf[1] = (byte)(0xff & (parm >> 16));
		wrkbuf[2] = (byte)(0xff & (parm >>  8));
		wrkbuf[3] = (byte)(0xff & parm);

		return wrkbuf;
	}
	public int bldIntFromArray(byte[] arrin,int offset) {
		 int wrkint = (int)(((arrin[offset] & 0xff) << 24) | ((arrin[offset+1] & 0xff) << 16) |
		 		  ((arrin[offset+2] & 0xff) << 8) | (arrin[offset+3] & 0xff));

		return wrkint;
	}
	public String WebSpecialChar(String parm) {
		String key = "";
		parm = parm.replaceAll("\\+", " ");
		Map<String, String> webHex2Val      = new HashMap<String, String>();
		webHex2Val.put(new String("%2C"), new String(","));
		webHex2Val.put(new String("%2B"), new String("+"));
		webHex2Val.put(new String("%0A"), new String(" "));
		webHex2Val.put(new String("%0D"), new String(""));
		int len = parm.length();
		int ndx1 = 0;

		for (;ndx1<len;) {
			ndx1 = parm.indexOf("%", ndx1);
			if (ndx1 == -1) break;
			key = parm.substring(ndx1, ndx1+3);
			parm = parm.replaceAll(key,(String)webHex2Val.get(key));
			len = parm.length();
		}
		return parm.trim();
	}
	public String CmdProcessor(String dir,String command,boolean logOutput) {
		File file = new File(dir);
		file.mkdirs();
		String time = new java.sql.Time(new Date().getTime()).toString()
				.replaceAll(":", "_");
		String log = dir + File.separator + "batchRepOutput"
				+ time + ".txt";
		String cmd = command;
		if (logOutput) cmd = command + " >" + log;	// + " 2>&1";
		JVMHalffreeMemory = Runtime.getRuntime().freeMemory()/2;

		StringBuffer cmdOutPut = new StringBuffer(cmd);
		ProcessBuilder pb = new ProcessBuilder(new String[] { "cmd.exe", "/C",cmd});
		int left = 1;
		Runtime r = Runtime.getRuntime();
		StringBuffer sb = new StringBuffer();
		byte[] bytes = new byte[4096];
		try {
			Process p0 = pb.start();
			p0 = r.exec(cmd);
			p0.getOutputStream();
			int tmp = 0;
			BufferedInputStream bis = new BufferedInputStream(p0.getInputStream());
			BufferedInputStream berrs = new BufferedInputStream(p0.getErrorStream());
			Thread.sleep(1000);
			while (left > 0) {
				if (bis.available() > 0)
					tmp = bis.read(bytes);
				if (tmp > 0)
					sb.append(new String(bytes, 0, tmp));
				if (berrs.available() > 0)
					tmp = berrs.read(bytes);
				if (tmp > 0)
					sb.append(new String(bytes, 0, tmp));
				left = bis.available() + berrs.available();
			}
			cmdOutPut.append(waitForRepToEnd(log));
			p0.destroy();
		} catch (Exception err) {
			cmdOutPut.setLength(0);
			cmdOutPut.append("Tools::WrkStaAccess " + err.toString());
		}
		return dumpFile(log);
	}
	private String waitForRepToEnd(String log) {
		String outp = "";
		int eof = 0;
		try {
			while ((eof = outp.indexOf(endOfRepTag.trim())) == -1 && outp.length() < JVMHalffreeMemory) {
				outp = dumpFile(log);
				if (eof == -1)
					Thread.sleep(1000 * 2);
			}
		} catch (Exception e) {
			return ("Tools::dumpFile " + e.toString());
		}
		int len = outp.lastIndexOf(endOfRepTag);
		if (len == -1)
			len = outp.length();
		outp = outp.substring(0, len);
		return outp;
	}
	private String dumpFile(String fulldir) {
		StringBuffer sb = new StringBuffer();
		File inputFile = new File(fulldir);
		String wrkstr;
		try {
			FileReader in = new FileReader(inputFile);
			BufferedReader ind = new BufferedReader(in);
			while ((wrkstr = ind.readLine()) != null && sb.length() < JVMHalffreeMemory) {
				sb.append(wrkstr).append("\n");
			}
			ind.close();
		} catch (Exception e) {
			return ("Tools::dumpFile " + e.toString());
		}
		return sb.toString();
	}
	/*
	 * read file from server
	 * it overwrites the endOfRepTag to the new tag from server file.
	 */
	public String readServerFile(String codeBase,String filename) {
		String command="";
		String msg="no msg";
		int len = 0;
		int ndx1 = 0;
		int ndx2 = 0;
		byte[] bytes = new byte[16384];
		HttpURLConnection urlconn = null;
		String parm = "AdminCmd.jsp?CMD=GET";
		parm       += "&DIR="+filename;
		cmdExecuted = true;
		try {
			URL url = new URL(codeBase+parm);
			urlconn = (HttpURLConnection)url.openConnection();
	        urlconn.setRequestProperty("Content-type","multipart");
			urlconn.setDoOutput(true);
			urlconn.setDoInput(true);
	        InputStream urlin = urlconn.getInputStream();
	        while ((len = urlin.read(bytes)) != -1) {
	        	command +=  new String(bytes,0,len);
	        }
	        ndx1 = command.toLowerCase().lastIndexOf("echo");
	        ndx2 = command.lastIndexOf("\r");
	        if (ndx2 == -1) ndx2 = command.lastIndexOf("\n");
	        if (ndx2 == -1 || ndx2 <= ndx1) ndx2 = command.length();
	        if (ndx1 != -1) endOfRepTag = command.substring(ndx1+5,ndx2);
			msg = urlconn.getResponseCode()+ " "+urlconn.getResponseMessage();
		} catch (IOException err) {
			System.out.println(msg);
			err.printStackTrace();
			cmdExecuted = false;
			return "Tools::readServerFile "+msg+"\n" + err.toString();
		}
		System.out.println("JADTEST readServerFile command="+command);
		return command;
	}
	public boolean moveModuleFromServer(String codeBase,String serverfile,String localfile) {
		int len = 0;
		byte[] bytes = new byte[16384];
		HttpURLConnection urlconn = null;
		String parm = "AdminCmd.jsp?CMD=GET";
		parm       += "&DIR="+serverfile;
		cmdExecuted = true;
		FileOutputStream outs = null;
		try {
			URL url = new URL(codeBase+parm);
			urlconn = (HttpURLConnection)url.openConnection();
	        urlconn.setRequestProperty("Content-type","multipart");
			urlconn.setDoOutput(true);
			urlconn.setDoInput(true);
	        InputStream urlin = urlconn.getInputStream();

			File taskf = new File(localfile);
			outs = new FileOutputStream(taskf);

	        while ((len = urlin.read(bytes)) != -1) {
	        	outs.write(bytes, 0, len);
	        }
			outs.flush();
			outs.close();
		} catch (IOException err) {
			err.printStackTrace();
			cmdExecuted = false;
			if (outs != null)
				try {
					outs.close();
				} catch (IOException e) {}
		}
		return cmdExecuted;
	}
	public void writeLocalFile(String filename, String text) {
		try {
			System.out.println("JADTEST2 writeLocalFile filename="+filename);
			File taskf = new File(filename);
			FileOutputStream outs = new FileOutputStream(taskf);
			OutputStreamWriter outw = new OutputStreamWriter(outs);
			outw.write(text);
			outw.flush();
			outw.close();
		} catch (IOException e) {
			System.out.println("Tools::writeLocalFile " + e.toString());
		}
	}
}

