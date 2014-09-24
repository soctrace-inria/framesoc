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
package fr.inria.soctrace.tools.framesoc.exporter.dbexporter;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import fr.inria.soctrace.framesoc.core.tools.model.FramesocTool;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class FramesocDBExporter extends FramesocTool {

	@Override
	public void launch(String[] args) {

		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		ExportDBDialog dlg = new ExportDBDialog(window.getShell());
		if (dlg.open() != Window.OK)
			return;

		ExporterInput input = dlg.getExporterInput();
		if (input!=null) {
			ExporterJob ejob = new ExporterJob("Exporter", input);
			ejob.setUser(true);
			ejob.schedule(); 
		} else {
			// enable button
			MessageDialog.openError(window.getShell(), "Error", 
					"Error while getting the exporter parameters!");
		}
	}
	
	@Override 
	public boolean canLaunch(String[] args) {
		return true;
	}

}
