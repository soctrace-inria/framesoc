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
 * Show Parameters for SQLite DMBS
 * 
 * @author youenn
 *
 */
public class SQLiteDialog extends DBMSDialog {

	private Composite compositeDB;

	public SQLiteDialog(Composite parent, ConfigurationDialog parentDialog) {
		super(parentDialog);

		createPartControl(parent);
	}

	public void createPartControl(Composite parent) {
		compositeDB = parent;
		GridLayout gl_compositeTable = new GridLayout(3, false);
		compositeDB.setLayout(gl_compositeTable);

		final Label lblSqlDirectory = new Label(compositeDB, SWT.NONE);
		lblSqlDirectory.setText("SQLite database directory:");

		final GridData gd_MiscDir = new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1);
		gd_MiscDir.widthHint = 100;

		final Label sqlDBDirectory = new Label(compositeDB, SWT.NONE);
		sqlDBDirectory
				.setText(config.get(SoCTraceProperty.sqlite_db_directory));
		sqlDBDirectory.setToolTipText(config
				.get(SoCTraceProperty.sqlite_db_directory));
	}
}
