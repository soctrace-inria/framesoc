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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import fr.inria.soctrace.lib.utils.DBMS;

/**
 * Initialization wizard DBMS selection page.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class DbmsPage extends InitPage {

	private Combo combo;
	private Composite container;

	/**
	 * @param pageName
	 */
	public DbmsPage(String pageName, InitProperties properties) {
		super(pageName, properties);
		setTitle("DBMS Selection");
		setDescription("Choose the DBMS to be used in Framesoc (SQLite recommended)");
	}

	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NULL);
		setControl(container);
		
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;
		Label label = new Label(container, SWT.NULL);
		label.setText("Choose the DBMS");

		combo = new Combo(container, SWT.READ_ONLY);
		combo.add(DBMS.SQLITE.toString());
		combo.add(DBMS.MYSQL.toString());
		combo.setText(properties.getDbms());
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		combo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				properties.setDbms(combo.getText());
				setPageComplete(true); // to force update buttons
			}
		});
		setPageComplete(true);
	}	
}
