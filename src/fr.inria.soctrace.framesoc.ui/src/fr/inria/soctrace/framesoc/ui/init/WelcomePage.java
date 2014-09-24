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
/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.init;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * Initialization wizard welcome page.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class WelcomePage extends WizardPage {

	public WelcomePage() {
		super("Welcome to Framesoc");
		setTitle("Welcome to Framesoc");
		setMessage("This wizard will help you to initialize your installation");
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		setControl(container);
		container.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Text text = new Text(container, SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
		text.setEnabled(false);
		text.setText("You can reinitialize your installation at any time by selecting the menu item:\n"
				   + "Framesoc > Management > Initialize System.");
	}
}
