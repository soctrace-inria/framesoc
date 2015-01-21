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
package fr.inria.soctrace.framesoc.ui.init;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Initialization wizard SQLite configuration page.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class SQLitePage extends InitPage {

	private Text dbDirectory;
	private Composite container;
	private Button btnBrowse;
	
	/**
	 * @param pageName
	 * @param properties 
	 */
	public SQLitePage(String pageName, InitProperties properties) {
		super(pageName, properties);
		setTitle("SQLite configuration");
		setDescription("Configure SQLite DBMS parameters");
	}

	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NULL);
		setControl(container);
		
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		Label label = new Label(container, SWT.NULL);
		label.setText("Select DBs directory");

		dbDirectory = new Text(container, SWT.BORDER | SWT.SINGLE);
		dbDirectory.setText("");
		dbDirectory.addModifyListener( new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				properties.setSqliteDirectory(dbDirectory.getText());
    			if (new File(dbDirectory.getText()).exists())
    				setPageComplete(true);
    			else
    				setPageComplete(false);
			}				
		});
		GridData gd_text1 = new GridData(GridData.FILL_HORIZONTAL);
		gd_text1.widthHint = 331;
		dbDirectory.setLayoutData(gd_text1);
		
		btnBrowse = new Button(container, SWT.NONE);
		btnBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				dbDirectory.setText("");
				DirectoryDialog dlg = new DirectoryDialog(container.getShell(), SWT.SINGLE);
        		String dir = dlg.open();
        		if (dir != null) {
        			if (new File(dir).exists()) {
        				dbDirectory.setText(dir);
        				properties.setSqliteDirectory(dbDirectory.getText());
        				setPageComplete(true);
        			} else {
        				setPageComplete(false);
        			}
        		}
			}
		});
		btnBrowse.setText("Browse");
		setPageComplete(false);
	}

	public String getText() {
		return dbDirectory.getText();
	}

}
