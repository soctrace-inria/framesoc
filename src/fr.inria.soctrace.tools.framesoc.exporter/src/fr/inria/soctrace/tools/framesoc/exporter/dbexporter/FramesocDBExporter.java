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
package fr.inria.soctrace.tools.framesoc.exporter.dbexporter;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import fr.inria.soctrace.framesoc.core.tools.model.FramesocTool;
import fr.inria.soctrace.framesoc.core.tools.model.IFramesocToolInput;
import fr.inria.soctrace.tools.framesoc.exporter.input.ExporterInput;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class FramesocDBExporter extends FramesocTool {

	@Override
	public void launch(IFramesocToolInput input) {
		ExporterInput exporterInput = (ExporterInput) input;
		if (exporterInput != null) {
			ExporterJob ejob = new ExporterJob("Exporter", exporterInput);
			ejob.setUser(true);
			ejob.schedule();
		} else {
			// enable button
			MessageDialog.openError(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error",
					"Error while getting the exporter parameters!");
		}
	}

	@Override
	public ParameterCheckStatus canLaunch(IFramesocToolInput input) {
		ExporterInput exporterInput = (ExporterInput) input;
		ParameterCheckStatus status = new ParameterCheckStatus(true, "");
		if (exporterInput.directory == null) {
			status.valid = false;
			status.message = "Specify a directory";
		} else if (exporterInput.traces == null
				|| exporterInput.traces.isEmpty()) {
			status.valid = false;
			status.message = "Select a trace";
		} else {
			File dir = new File(exporterInput.directory);
			if (!dir.exists()) {
				status.valid = false;
				status.message = "Directory " + exporterInput.directory + " does not exist";
			} else if (!dir.isDirectory()) {
				status.valid = false;
				status.message = exporterInput.directory + " is not a directory";
			} else if (!dir.canWrite()) {
				status.valid = false;
				status.message = "Does not have write permission for "
						+ exporterInput.directory + " directory";
			}
		}
		return status;
	}

}
