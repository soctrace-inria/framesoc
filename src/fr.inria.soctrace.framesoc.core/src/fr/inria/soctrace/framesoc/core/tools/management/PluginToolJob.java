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
package fr.inria.soctrace.framesoc.core.tools.management;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import fr.inria.soctrace.framesoc.core.tools.model.IPluginToolJobBody;

/**
 * Eclipse Job for a generic Plugin Tool.
 * 
 * <p>
 * If the {@link IPluginToolJobBody#run(IProgressMonitor)} method of the tool
 * launches an exception the Job returns Status.CANCEL_STATUS.
 * 
 * <p>
 * It launches the {@link IPluginToolJobBody#run(IProgressMonitor)} method then
 * executes a {@link #postExecute(IStatus)}. The base class, provides an empty
 * {@link #postExecute(IStatus)}.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class PluginToolJob extends Job {

	protected IPluginToolJobBody body;

	public PluginToolJob(String name, IPluginToolJobBody body) {
		super(name);
		this.body = body;
	}

	/**
	 * Method executed in the job body, before the run body.
	 * 
	 * @param monitor
	 *            The progress monitor. When overriding this method, it is up to
	 *            the implementation to call beginTask() and done().
	 */
	public void preExecute(IProgressMonitor monitor) {
		// do nothing
	}

	/**
	 * Method executed in the job body, after the run body.
	 * 
	 * @param monitor
	 *            The progress monitor. When overriding this method, it is up to
	 *            the implementation to call beginTask() and done().
	 * @param status
	 *            The final execution status of the IPluginToolJobBody
	 */
	public void postExecute(IProgressMonitor monitor, IStatus status) {
		// do nothing
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {

		try {
			// before execution
			preExecute(monitor);

			// body
			body.run(monitor);

			// post execution
			postExecute(monitor, Status.OK_STATUS);

		} catch (Exception e) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Error", e.getMessage());
			e.printStackTrace();
			return Status.CANCEL_STATUS;
		}

		return Status.OK_STATUS;
	}

}
