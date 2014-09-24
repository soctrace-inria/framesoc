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
import org.eclipse.swt.widgets.Display;

import fr.inria.soctrace.framesoc.core.bus.FramesocBus;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.core.tools.importers.TraceChecker;
import fr.inria.soctrace.framesoc.core.tools.model.IPluginToolJobBody;

/**
 * Subclass of {@link PluginToolJob} for Plugin Importer Tools.
 * 
 * <p>
 * In the {@link PluginToolJob#postExecute(IStatus)} method implementation, a
 * message is sent on the {@link FramesocBus} for the topic
 * {@link FramesocBusTopic#TOPIC_UI_SYNCH_TRACES_NEEDED}. The associated data is
 * true or false, depending on whether the Job returned status is
 * {@link Status#OK_STATUS} or {@link Status#CANCEL_STATUS} respectively.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class PluginImporterJob extends PluginToolJob {

	private TraceChecker checker;

	public PluginImporterJob(String name, IPluginToolJobBody body) {
		super(name, body);
	}

	@Override
	public void preExecute(IProgressMonitor monitor) {
		checker = new TraceChecker();
	}
	
	@Override
	public void postExecute(IProgressMonitor monitor, IStatus status) {

		try {
			monitor.beginTask("Post-processing imported traces", IProgressMonitor.UNKNOWN);

			// check trace metadata
			checker.checkTraces(monitor);

			// we want to provide UI feedback, so we post the event using
			// Display sync exec
			final IStatus jobStatus = status;
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					if (!jobStatus.equals(Status.OK_STATUS)) {
						FramesocBus.getInstance().send(
								FramesocBusTopic.TOPIC_UI_SYNCH_TRACES_NEEDED, false);
					} else {
						FramesocBus.getInstance().send(
								FramesocBusTopic.TOPIC_UI_SYNCH_TRACES_NEEDED, true);
					}
				}
			});
		} finally {
			monitor.done();
		}
	}

}
