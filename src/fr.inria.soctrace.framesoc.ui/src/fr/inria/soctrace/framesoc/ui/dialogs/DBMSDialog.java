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

import fr.inria.soctrace.lib.utils.Configuration;

/**
 * Class for showing different parameters of the DBMS
 * 
 * @author youenn
 *
 */
public abstract class DBMSDialog {

	protected ConfigurationDialog parentDialog;
	protected Configuration config;

	public DBMSDialog(ConfigurationDialog parentdialog) {
		config = Configuration.getInstance();
		parentDialog = parentdialog;
	}

}
