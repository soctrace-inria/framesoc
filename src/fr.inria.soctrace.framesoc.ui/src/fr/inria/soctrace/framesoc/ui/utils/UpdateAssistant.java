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
package fr.inria.soctrace.framesoc.ui.utils;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import fr.inria.soctrace.framesoc.core.FramesocManager;
import fr.inria.soctrace.framesoc.core.tools.importers.TraceChecker;
import fr.inria.soctrace.framesoc.ui.dialogs.UpdaterDialog;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.utils.DBModelChecker;

/**
 * This class implements method to check that the current DB  to the
 * current DB model
 * 
 * @author "Youenn Corre <youenn.corre@inria.fr>"
 */
public class UpdateAssistant {

	private DBModelChecker dbModelChecker;
	private ProgressMonitorDialog progressDialog;

	/**
	 * Check if the system database has the same version that the current model
	 */
	public static void checkDB() {
		SystemDBObject sysDB = null;
		try {
			if (!FramesocManager.getInstance().isSystemDBExisting())
				return;

			sysDB = SystemDBObject.openNewInstance();

			// If getting sysdb failed
			if (sysDB == null)
				return;

			// If the model version does not match
			if (!sysDB.checkDBVersion()) {
				// Update the DB
				UpdateAssistant updateAssistant = new UpdateAssistant();
				updateAssistant.updateDBModel();
			}

		} catch (SoCTraceException e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					"Checking database version",
					"Database version checking failed with the following error: "
							+ e.getMessage());
		} finally {
			DBObject.finalClose(sysDB);
		}
	}
	
	/**
	 * Perform the update of the database
	 */
	public void updateDBModel() {
		final Shell shell = Display.getCurrent().getActiveShell();

		// Ask the user if she wants to update the DB
		UpdaterDialog updaterDialog = new UpdaterDialog(shell);
		if (updaterDialog.open() == Window.OK) {
			try {
				progressDialog = new ProgressMonitorDialog(
						shell);
				progressDialog.setCancelable(false);
				progressDialog.getProgressMonitor().setTaskName("Updating Database");
				progressDialog.run(false, false, new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor)
							throws InvocationTargetException,
							InterruptedException {
						monitor.beginTask("Updating database", 2);
						try {
							runUpdate(monitor);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						monitor.done();
					}
				});
				MessageDialog.openInformation(shell, "Updating Database",
						"Update successful!");

			} catch (InvocationTargetException | InterruptedException e) {
				MessageDialog.openError(shell, "Error Updating Database",
						"Database updating failed with the folllowing error: "
								+ e.getMessage());
			}
		}
	}
	
	/**
	 * Run the update of the database, and check the current traces
	 * 
	 * @param monitor
	 *            show the progress of the update to the user
	 */
	private void runUpdate(IProgressMonitor monitor) {
		monitor.beginTask("Updating Database", 2);
		
		monitor.subTask("Copying Database");
		dbModelChecker = new DBModelChecker();
		try {
			dbModelChecker.updateDB();
		} catch (final SoCTraceException e) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openError(Display.getDefault()
							.getActiveShell(), "Error Updating Database",
							"Database updating failed with the following error: "
									+ e.getMessage());
				}
			});
		}
		monitor.worked(1);

		monitor.subTask("Checking Traces");
		TraceChecker traceChecker = new TraceChecker();
		traceChecker.checkAllTraces(monitor);
		monitor.worked(1);
	}

}
