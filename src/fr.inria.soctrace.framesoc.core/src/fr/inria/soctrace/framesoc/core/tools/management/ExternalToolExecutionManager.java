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
package fr.inria.soctrace.framesoc.core.tools.management;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Manager able to launch an external application capturing <i>stdout</i> and
 * <i>stderr</i>.
 * 
 * <p>
 * The external process is launched within an Eclipse {@link Job} that the user
 * can cancel.
 * 
 * <p>
 * After the execution of the process, a post execute method is called inside
 * the {@link Job}.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class ExternalToolExecutionManager {

	private String name;
	private String command;

	/**
	 * Constructor
	 * 
	 * @param name
	 *            external tool name
	 * @param command
	 *            external tool command
	 */
	public ExternalToolExecutionManager(String name, String command) {
		this.name = name;
		this.command = command;
	}

	/**
	 * Execute the tool within an Eclipse {@link Job}.
	 * 
	 * @throws SoCTraceException
	 */
	public void execute() throws SoCTraceException {
		ToolJob job = new ToolJob(name, command);
		job.setUser(true);
		job.schedule();
	}

	/**
	 * Optional operation to be performed before the process execution, still
	 * inside the Eclipse job.
	 * 
	 */
	public void preExecute() {
		// do nothing
	}

	/**
	 * Optional operation to be performed at the end of the process execution,
	 * still inside the Eclipse job.
	 * 
	 * @param status
	 *            the status that will be returned by the Eclipse job.
	 */
	public void postExecute(IStatus status) {
		// do nothing
	}

	/**
	 * Eclipse {@link Job} able to launch a command inside a {@link Process}.
	 * 
	 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
	 */
	class ToolJob extends Job {

		private String command;

		public ToolJob(String name, String command) {
			super(name);
			this.command = command;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {

			monitor.setTaskName(name);
			monitor.beginTask(name, IProgressMonitor.UNKNOWN);
			
			// before execution
			preExecute();

			try {
				Process p = Runtime.getRuntime().exec(command);

				boolean notExited = true;
				while (!monitor.isCanceled() && notExited) {
					try {
						p.exitValue();
						notExited = false;
					} catch (IllegalThreadStateException e) {
						Thread.sleep(500);
					}
				}

				if (notExited) {
					p.destroy();
				}

				// debug code
				else {
					String line;
					BufferedReader bri = new BufferedReader(new InputStreamReader(
							p.getInputStream()));
					BufferedReader bre = new BufferedReader(new InputStreamReader(
							p.getErrorStream()));

					while ((line = bri.readLine()) != null) {
						System.out.println(line);
					}
					bri.close();
					while ((line = bre.readLine()) != null) {
						System.err.println(line);
					}
					bre.close();
				}

			} catch (Exception e) {
				System.err.println(e.getMessage());
				postExecute(Status.CANCEL_STATUS);
				return Status.CANCEL_STATUS;
			}
			monitor.done();

			postExecute(Status.OK_STATUS);

			return Status.OK_STATUS;
		}
	}
}
