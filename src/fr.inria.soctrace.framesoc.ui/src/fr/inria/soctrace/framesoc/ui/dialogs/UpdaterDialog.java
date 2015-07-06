/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Youenn Corre - initial API and implementation
 ******************************************************************************/
package fr.inria.soctrace.framesoc.ui.dialogs;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog for allowing the user to update the database
 * 
 * @author "Youenn Corre <youenn.corre@inria.fr>"
 */
public class UpdaterDialog extends MessageDialog {

	private static final String MESSAGE = "Framesoc has detected that the model of the trace database is different "
			+ "from the current model: that can lead to error while reading database. Framesoc can try to automatically "
			+ "update the database. It is highly recommended to do so.\n \n Would you like to try to update the database ?";

	// Customize button labels
	private static final  String[] ButtonLabels = {"Yes", "No"};
	
	public UpdaterDialog(Shell parentShell) {
		super(parentShell, "Update Database", null, MESSAGE,
				MessageDialog.CONFIRM, ButtonLabels, 0);
	}

}
