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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Initialization wizard MySQL configuration page.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class MySqlPage extends InitPage {
	
	private Text user;
	private Text password;
	private Text url;
	
	/**
	 * @param pageName
	 * @param properties 
	 */
	public MySqlPage(String pageName, InitProperties properties) {
		super(pageName, properties);
		setTitle("MySQL configuration");
		setDescription("Configure MySQL DBMS parameters");
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		setControl(container);
		container.setLayout(new GridLayout(2, false));
		
		Label lblNewLabel = new Label(container, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("User");
		
		user = new Text(container, SWT.BORDER);
		user.setText(properties.getMysqlUser());
		user.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		user.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				properties.setMysqlUser(user.getText());
				updatePageComplete();
			}
		});
		
		Label lblNewLabel_1 = new Label(container, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText("Password");
		
		password = new Text(container, SWT.BORDER | SWT.PASSWORD);
		password.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		password.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				properties.setMysqlPassword(password.getText());
				updatePageComplete();
			}
		});
		
		Label lblNewLabel_2 = new Label(container, SWT.NONE);
		lblNewLabel_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_2.setText("Url");
		
		url = new Text(container, SWT.BORDER);
		url.setText(properties.getMysqlUrl());
		url.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		url.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				properties.setMysqlUrl(url.getText());
				updatePageComplete();
			}
		});
		
		updatePageComplete();
	}
	
	private void updatePageComplete() {
		// Note: password is optional
		setPageComplete(!user.getText().isEmpty() && !url.getText().isEmpty()); 
	}
	
}
