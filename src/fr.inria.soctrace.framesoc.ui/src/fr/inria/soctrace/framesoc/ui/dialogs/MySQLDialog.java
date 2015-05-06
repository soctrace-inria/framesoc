/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Generoso Pagano - initial API and implementation
 ******************************************************************************/
package fr.inria.soctrace.framesoc.ui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty;

/**
 * Show Parameters for MySQL DMBS
 * @author youenn
 *
 */
public class MySQLDialog extends DBMSDialog {

	private Composite compositeDB;

	public MySQLDialog(Composite parent, ConfigurationDialog parentDialog) {
		super(parentDialog);

		createPartControl(parent);
	}

	public void createPartControl(Composite parent) {
		compositeDB = parent;
		GridLayout gl_compositeTable = new GridLayout(2, false);
		compositeDB.setLayout(gl_compositeTable);

		final GridData gd_MiscDir = new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1);
		gd_MiscDir.widthHint = 100;

		final Label lblMySqlUser = new Label(compositeDB, SWT.NONE);
		lblMySqlUser.setText("User name:");

		final Label mySqlUser = new Label(compositeDB, SWT.NONE);
		mySqlUser.setText(config.get(SoCTraceProperty.mysql_db_user));
		mySqlUser.setToolTipText(config.get(SoCTraceProperty.mysql_db_user));

		final Label lblSqlURL = new Label(compositeDB, SWT.NONE);
		lblSqlURL.setText("SQL URL:");

		final Label mySqlURL = new Label(compositeDB, SWT.NONE);
		mySqlURL.setText(config.get(SoCTraceProperty.mysql_base_db_jdbc_url));
		mySqlURL.setToolTipText(config
				.get(SoCTraceProperty.mysql_base_db_jdbc_url));
	}

}
