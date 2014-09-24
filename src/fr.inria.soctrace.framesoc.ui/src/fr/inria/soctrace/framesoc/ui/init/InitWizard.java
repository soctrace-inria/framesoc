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

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import fr.inria.soctrace.lib.utils.Configuration;
import fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty;
import fr.inria.soctrace.lib.utils.DBMS;

/**
 * Wizard to initialize the System.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class InitWizard extends Wizard {

	private WelcomePage welcomePage;
	private DbmsPage dbmsPage;
	private MySqlPage mysqlPage;
	private SQLitePage sqlitePage;
	private InitProperties properties;

	/**
	 * Constructor 
	 */
	public InitWizard(boolean firstime) {

		// set default properties here
		properties = new InitProperties();
		properties.setDbms(Configuration.getInstance().getDefault(SoCTraceProperty.soctrace_dbms));
		properties.setMysqlUser(Configuration.getInstance().getDefault(SoCTraceProperty.mysql_db_user));
		properties.setMysqlUrl(Configuration.getInstance().getDefault(SoCTraceProperty.mysql_base_db_jdbc_url));
		
		// first time page
		if (firstime) {
			welcomePage = new WelcomePage();
			addPage(welcomePage);
		} else {
			welcomePage = null;
		}

		// create pages
		dbmsPage = new DbmsPage("DBMS Selection", properties);
		mysqlPage = new MySqlPage("MySQL configuration", properties);
		sqlitePage = new SQLitePage("SQLite configuration", properties);
		addPage(dbmsPage);
		addPage(mysqlPage);
		addPage(sqlitePage);
		
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	@Override
	public IWizardPage getNextPage(IWizardPage currentPage) {
		
		if ( welcomePage != null && currentPage == welcomePage )
			return dbmsPage;
		
		if (currentPage == dbmsPage) {
			if (properties.getDbms().equalsIgnoreCase(DBMS.MYSQL.toString()))
				return mysqlPage;
			if (properties.getDbms().equalsIgnoreCase(DBMS.SQLITE.toString()))
				return sqlitePage;
		} 

		return null;
	}
	
	/**
	 * Get the properties.
	 */
	public InitProperties getInitProperties() {
		return properties;
	}
	
	@Override
	public boolean canFinish() {
		
		return dbmsPage.isPageComplete() 
				&& 
			  (((mysqlPage.isCurrentPage() && properties.getDbms().equalsIgnoreCase(DBMS.MYSQL.toString()) && mysqlPage.isPageComplete()) 
					  || 
			  ((sqlitePage.isCurrentPage() && properties.getDbms().equalsIgnoreCase(DBMS.SQLITE.toString()) && sqlitePage.isPageComplete()))));
	}
	
}
