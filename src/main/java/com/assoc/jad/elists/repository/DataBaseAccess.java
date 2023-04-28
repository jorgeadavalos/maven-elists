package com.assoc.jad.elists.repository;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.assoc.jad.elists.tools.ShopListStatic;

/**
 * this class implements the database access. The name of the 'schema' variable is fixed because
 * there will be only one DB resource per VM but there will be more than one VM housing the same DB resource name.
 * the constructor will be invoked using two parameters: the class of the instance and the instance itself.
 * reflection will be used to build and invoke the methods from the meta data of the sql request.
 * In case of a failure due to connection timeout it will try to reconnect 4-times before throwing and exception.
 * It implements image insertion (Blob in Mysql jargon) by accessing from a file and if the file size is larger than 64k
 * it will enter multiple rows per one image.
 * Errors will be report in the TomCat server log.
 */

public class DataBaseAccess {

	public static final String schema = "shoplistDB";
	
	private ResultSet rst=null;
	private Connection conn = null;
	private Statement stmt = null;
	private String sqlColName = "";
	private String sqlColValue = "";
	private Object retObj = null;
	private Object retType = null;
	private Object updater = null;

	private Class<?> caller = null;
	private Object instance = null;
	private String currvalFormat = null;
	private HashMap<String,String> dataBaseNames = new HashMap<String,String>(10);
	private String errorMsg;
	
	public DataBaseAccess(Class<?> caller, Object instance) {
		this.caller = caller;
		this.instance = instance;
		dataBaseNames.put("POSTGRESQL", "select currval('%s_%s_seq')");
		dataBaseNames.put("MYSQL", "SELECT currval(pg_get_serial_sequence('%s','%s')");
	}
	private void wrapup() {
		 try {
				if(rst != null)  rst.close();
				if(stmt != null) stmt.close();
				if(conn != null) conn.close();
				rst = null;
				stmt = null;
				conn = null;
			}catch (SQLException e) {e.printStackTrace();}
		
	}
	private synchronized boolean reConnect(String schema, String sql) {
		
		try {
			wait(3000);
			Context initContext = new InitialContext();
			Context envContext  = (Context)initContext.lookup("java:/comp/env");
			DataSource ds = (DataSource)envContext.lookup("jdbc/"+schema);
			conn = ds.getConnection();
	        stmt = conn.createStatement();
	        stmt.setMaxRows(0); //no limit
	      	if (stmt.execute(sql,Statement.RETURN_GENERATED_KEYS)) rst = stmt.getResultSet();
	      	if (rst != null ) return true;
	      	return false;
		} catch (Exception e) {
			System.out.println("DataBaseAccess::reConnect "+" "+sql+"\n"+e);
			return false;
		}
	}
	private void setDataBaseAttributes(Connection conn) {
		DatabaseMetaData databaseMetaData;
		try {
			databaseMetaData = conn.getMetaData();
			String keydatabase = databaseMetaData.getDatabaseProductName();
			currvalFormat = dataBaseNames.get(keydatabase.toUpperCase());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	private boolean SQLExec(String schema, String sql) {
		errorMsg = "";
		try {
			if (conn == null) {
				Context initContext = new InitialContext();
				Context envContext  = (Context)initContext.lookup("java:/comp/env");
				DataSource ds = (DataSource)envContext.lookup("jdbc/"+schema);
				conn = ds.getConnection();
				setDataBaseAttributes(conn);
				stmt = conn.createStatement();
				stmt.setMaxRows(0); //no limit
			}
//	      	if (stmt.execute(sql,Statement.RETURN_GENERATED_KEYS)) rst = stmt.getResultSet();
	      	if (stmt.execute(sql)) rst = stmt.getResultSet();
	      	return true;
		} catch (SQLException e) {
			errorMsg = e.toString();
            String sqlState = e.getSQLState();

            // staled connection or deadlock. retry it.
            if ("08S01".equals(sqlState) || "41000".equals(sqlState)) {
            	for (int i=0;i<4;i++) {
                	if (reConnect( schema,  sql) ) return true;
            	}
            }
			System.out.println("DataBaseAccess::SQLExec sqlState="+sqlState+" "+sql+"\n"+e);
			return false;
		} catch (Exception e) {
			System.out.println("DataBaseAccess::SQLExec "+" "+sql+"\n"+e);
			e.printStackTrace();
			return false;
		}
	}
	private String bldAModelName(String prefix,String colName) {
		StringBuilder name = new StringBuilder(colName);
		byte cap = (byte) name.charAt(0);
		cap = (byte)(0xdf & cap);
		name.setCharAt(0, (char) cap);
		return prefix+name;
	}
	private List<Object> bldSetterName() throws Exception {
        if (rst == null || caller == null) return null;

    	List<Object> callerInstances = new ArrayList<Object>();
		ResultSetMetaData meta = rst.getMetaData();
     	int col = meta.getColumnCount();
     	while (rst.next()) { 
    		Object instance = caller.getDeclaredConstructor().newInstance();
        	for (int i=1;i <=col;i++) {
        		Object value = rst.getObject(i);
        		if (value != null && value.getClass().getName().equals("java.lang.String"))
        			value = ShopListStatic.undoSpecialChars(value.toString());

        		executeMethods(bldAModelName("set",meta.getColumnName(i)),value,instance);
        	}
        	callerInstances.add(instance);
     	}
     	return callerInstances;
	}
	private void bldGetterName() throws Exception {
        if (rst == null || caller == null) return;

		Object instance = this.instance;
		ResultSetMetaData meta = rst.getMetaData();
     	int col = meta.getColumnCount();
    	for (int i=1;i <=col;i++) {
			if (!meta.isAutoIncrement(i)) sqlColName += meta.getColumnName(i)+",";
    		executeMethods(bldAModelName("get",meta.getColumnName(i)),null,instance);
    		if (retObj == null ) {
    			if (retType.toString().equals("class [B")) sqlColValue += "?,";
    			else if (retType.toString().indexOf("java.sql.Blob") != -1) sqlColValue += "?,";
    			else sqlColValue += "' ',";
    		}
    		else {
    			String type = retObj.getClass().getName();
    			if (type.equals("[B")) sqlColValue += "?,";
    			else if (type.equals("java.lang.String")||type.equals("java.sql.Timestamp")) {
    				 if (retObj.equals("java.sql.Blob"))
        	    			sqlColValue += "?,";
    				 else sqlColValue += "'"+ShopListStatic.specialChars(retObj.toString())+"',";  //html special chars representation
    			} else if (!meta.isAutoIncrement(i)) sqlColValue += retObj.toString()+",";
    		}
    	}
    	int len = sqlColValue.length()-1;
    	if (sqlColValue.endsWith(",")) sqlColValue = sqlColValue.substring(0, len);
    	len = sqlColName.length()-1;
    	if (sqlColName.endsWith(",")) sqlColName = sqlColName.substring(0, len);
	}
	private void executeMethods(String methnam,Object obj,Object instance) throws Exception {
		Object[] arguments = new Object[] {obj};
		if (obj == null && methnam.startsWith("get")) arguments = null;

		Method[] methods = caller.getMethods();
		Method method = null;
		for (int i=0;i<methods.length;i++) {
			if (methods[i].getName().equals(methnam)) {
				if (obj == null) {
					method = methods[i];
					break;
				}
				Object types[] = methods[i].getParameterTypes();
//				Object methodArg = types[0];
				Object argObj = arguments[0].getClass();
//				argObj.toString();
				if (	types[0].toString().equalsIgnoreCase(argObj.toString()) 
					) {
						method = methods[i];
						break;
					}
					continue;
			} 
		} 
		if (method == null) {
			System.out.println("DataBaseAccess::executeMethods: method not found "+methnam+" class="+caller.getName());
			return;
		}
		try {
			retType = method.getReturnType();
			retObj = method.invoke(instance,arguments);
//			if (retObj == null) retObj = method.getReturnType().getName();
		} catch (Exception e) {
			System.out.println("DataBaseAccess::executeMethods:"+methnam+" "+e);
			e.printStackTrace();
		} 
	}
	private boolean foundMethod(String methnam) {

		Method[] methods = caller.getMethods();
		for (int i=0;i<methods.length;i++) {
			if (methods[i].getName().equals(methnam)) {
				return true;
				}
		}
		return false;
	}

	/**
     * executes sql 
     * @param SQL command (String)
     * @return    list of Objects or null
     */
	public List<Object> readSql( String sql) {
		try {
			List<Object> callerInstances = null;
			
			if (SQLExec(schema,sql))
				callerInstances = bldSetterName();
			return callerInstances ;

		} catch (Exception e) {
			System.out.println("DataBaseAccess::readSql " +" "+sql+"\n"+ e.toString());
			e.printStackTrace();
			return null;
		} finally {
			wrapup();
		}
	}
	/**
	 * insertSql: 
	 * 	execute input sql string to retrieve meta data for the table to update.
	 * 	builds getter to retrieve data from instance.
     * @param SQL command (String)
     * @param table table name(String)
     * @return boolean   true=OK false=failed
	 */
	public boolean insertSql( String sql,String table) {
		sqlColName = "insert into "+table+" (";
		sqlColValue = "values (";
		try {
			boolean flag = SQLExec(schema,sql);
			if (flag) {
				bldGetterName();
				sql = sqlColName += ") " + sqlColValue + ")";
				flag = SQLExec(schema,sql);
			}
			if (flag) getCurrValue(table, "id");
			wrapup();
			return flag ;

		} catch (Exception e) {
			System.out.println("DataBaseAccess::readSql " +" "+sql+"\n"+e.toString());
			e.printStackTrace();
			return false;
		}
	}
	private void getCurrValue(String table, String colName) throws Exception {
		String modelName = bldAModelName("set",colName);
		if (!foundMethod(modelName)) return;
		
		String sql = String.format(currvalFormat, table,colName);
		Boolean gotId = SQLExec(schema,sql);
		if (!gotId) return;
		ResultSetMetaData meta = rst.getMetaData();
		if (meta == null) return;
     	rst.next();
		executeMethods(modelName,rst.getObject(1),instance);
	}
	private String bldUpdateSqlCmd(String table) throws Exception {
		String sql = "update "+table+" set ";
		boolean changes = false;
		bldGetterName();
		String sqlColValueOld = sqlColValue;
		sqlColName = "";
		sqlColValue = "";
		instance = updater;
		bldGetterName();
		String[] oldVals  = sqlColValueOld.split(",");
		String[] newVals  = sqlColValue.split(",");
		String[] colNames = sqlColName.split(",");
		if (oldVals.length != colNames.length) throw new Exception("number of vals diff from names "+colNames+""+oldVals);
		for (int i=0;i<oldVals.length;i++) {
			if (newVals[i].equals(oldVals[i])) continue;
			sql += colNames[i]+"="+newVals[i]+",";
			changes = true;
		}
		if (!changes) return "";
		int ndx = sql.length()-1;
		sql = sql.substring(0,ndx);
		return sql;
	}
	/**
	 * updateSql: 
	 * 	execute input sql string to retrieve meta data for the table to update.
	 * 	builds setter to update only columns that have changed .
     * @param SQL command (String)
     * @param table table name(String)
     * @return boolean   true=OK false=failed
	 */
	public boolean updateSql( String sql,String table) {
		if (this.instance == null) return false;
		updater = instance;
		sqlColName = "";
		sqlColValue = "";
		try {
			List<Object> callerInstances = null;
			if (SQLExec(schema,sql))
				callerInstances = bldSetterName();
			else return false;
			if (callerInstances.size() == 0) return false;
			
			instance =callerInstances.get(0);
			int ndx = sql.toLowerCase().indexOf("where");
			if (ndx == -1) throw new Exception("no where clause in sql statement");
			String whereClause = sql.substring(ndx);
			sql = bldUpdateSqlCmd(table);
			if (sql.length() == 0) return true; //no changes are necesssary all fields are the same;
			sql += " "+whereClause;
			boolean flag = SQLExec(schema,sql);
			return flag ;

		} catch (Exception e) {
			System.out.println("DataBaseAccess::readSql " +" "+sql+"\n"+e.toString());
			e.printStackTrace();
			return false;
		} finally {
			wrapup();
		}
	}
	public boolean deleteSql( String sql) {
		try {
			SQLExec(schema,sql);
			wrapup();
			return true;
		} catch (Exception e) {
			System.out.println("DataBaseAccess::deleteSql " + sql+" "+e.toString());
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * insertSqlBlob: 
	 * 	read image from input file.
	 * 	builds getter to help create a new row with max blob-size. 
	 * 	If file size is larger than max blob size then create multiple rows.
     * @param File command (File)
     * @param sql sql command(String)
     * @param table table name(String)
     * @return boolean   true=OK false=failed
	 */
	public boolean insertSqlBlob(InputStream ins, String sql,String table) {
		sqlColName = "insert into "+table+" (";
		sqlColValue = "values (";
		byte[] bytes = new byte[65535];
		int len = 0;
		try {
			boolean flag = SQLExec(schema,sql);
			if (flag) {
				bldGetterName();
				sql = sqlColName += ") " + sqlColValue + ")";
			    java.sql.PreparedStatement prep = conn.prepareStatement(sql);
				while ((len = ins.read(bytes)) != -1) {
				    ByteArrayInputStream bais = new ByteArrayInputStream(bytes,0,len);
				    prep.setBinaryStream(1, bais, len);
				    prep.execute();
				}
				getCurrValue(table, "id");
				ins.close();
				if (prep != null) {prep.close();}
			}
		} catch (Exception e) {
			System.out.println("DataBaseAccess::insertSqlBlob "+" "+sql+"\n"+e);
			e.printStackTrace();
			return false;
		} finally {
			wrapup();
		}
		return true;
	}
	public boolean insertSqlBlob(File tmpFile, String sql,String table) {
		try {
			InputStream is = new FileInputStream(tmpFile);
			return insertSqlBlob(is,  sql, table);
		} catch (Exception e) {
			System.out.println("DataBaseAccess::insertSqlBlob "+" "+sql+"\n"+e);
			e.printStackTrace();
			return false;
		} finally {
			wrapup();
		}
	}
	public void selectSqlBlob(String sql,File image) {
	    java.sql.Blob blob = null;
		int len = 0;
		try {
			if (!SQLExec(schema,sql)) {
				wrapup();
				return;
			}
			FileOutputStream outs = new FileOutputStream(image);
			 while (rst.next()) {
				blob = rst.getBlob(1);
				len = (int)blob.length();
				outs.write(blob.getBytes(1, len));
			}
			outs.close();
			wrapup();
		} catch (Exception e) {
			System.out.println("DataBaseAccess::selectSqlBlob " +" "+sql+"\n"+ e.toString());
		}
	}
	public boolean insertByteArray(byte[] bytes, String sql,String table) {
		sqlColName = "insert into "+table+" (";
		sqlColValue = "values (";
		try {
			boolean flag = SQLExec(schema,sql);
			if (!flag) return flag;
			bldGetterName();
			sql = sqlColName += ") " + sqlColValue + ")";
		    java.sql.PreparedStatement prep = conn.prepareStatement(sql);
		    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		    prep.setBinaryStream(1, bais);
		    prep.execute();
			getCurrValue(table, "id");
			if (prep != null) {prep.close();}
		} catch (Exception e) {
			System.out.println("DataBaseAccess::insertSqlBlob "+" "+sql+"\n"+e);
			e.printStackTrace();
			return false;
		} finally {
			wrapup();
		}
		return true;
	}

/*
 * getters and setters 
 */
	public String getErrorMsg() {
		return errorMsg;
	}
}