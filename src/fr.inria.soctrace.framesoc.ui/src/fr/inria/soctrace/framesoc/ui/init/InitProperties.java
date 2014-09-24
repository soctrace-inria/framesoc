/*******************************************************************************
 * Copyright (c) 2012-2014 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Generoso Pagano - initial API and implementation
 ******************************************************************************/
package fr.inria.soctrace.framesoc.ui.init;

/**
 * Properties to be initialized with the initialization wizard.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class InitProperties {

	private String dbms = "";
	private String sqliteDirectory = "";
	private String mysqlUser = "";
	private String mysqlPassword = "";
	private String mysqlUrl = "";
	
	/**
	 * @return the dbms
	 */
	public String getDbms() {
		return dbms;
	}
	/**
	 * @param dbms the dbms to set
	 */
	public void setDbms(String dbms) {
		this.dbms = dbms;
	}
	/**
	 * @return the sqliteDirectory
	 */
	public String getSqliteDirectory() {
		return sqliteDirectory;
	}
	/**
	 * @param sqliteDirectory the sqliteDirectory to set
	 */
	public void setSqliteDirectory(String sqliteDirectory) {
		this.sqliteDirectory = sqliteDirectory;
	}
	/**
	 * @return the mysqlUser
	 */
	public String getMysqlUser() {
		return mysqlUser;
	}
	/**
	 * @param mysqlUser the mysqlUser to set
	 */
	public void setMysqlUser(String mysqlUser) {
		this.mysqlUser = mysqlUser;
	}
	/**
	 * @return the mysqlPassword
	 */
	public String getMysqlPassword() {
		return mysqlPassword;
	}
	/**
	 * @param mysqlPassword the mysqlPassword to set
	 */
	public void setMysqlPassword(String mysqlPassword) {
		this.mysqlPassword = mysqlPassword;
	}
	/**
	 * @return the mysqlUrl
	 */
	public String getMysqlUrl() {
		return mysqlUrl;
	}
	/**
	 * @param mysqlUrl the mysqlUrl to set
	 */
	public void setMysqlUrl(String mysqlUrl) {
		this.mysqlUrl = mysqlUrl;
	}
	
	@Override
	public String toString() {
		return "DBMS: " + dbms + "\n" + 
	           "SQLite dir: " + sqliteDirectory + "\n" +
	           "MySQL user: " + mysqlUser + "\n" +
	           "MySQL passwd: " + mysqlPassword + "\n" +
	           "MySQL url: " + mysqlUrl;
	}
	
}
