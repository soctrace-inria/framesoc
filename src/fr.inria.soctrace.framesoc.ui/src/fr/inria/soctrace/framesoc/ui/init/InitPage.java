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

import org.eclipse.jface.wizard.WizardPage;

/**
 * Initialization wizard abstract page class.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class InitPage extends WizardPage {

	protected InitProperties properties;
	
	public InitPage(String pageName, InitProperties properties) {
		super(pageName);
		this.properties = properties;
	}
	
	@Override
	public boolean isCurrentPage() {
		return super.isCurrentPage();
	}

}
