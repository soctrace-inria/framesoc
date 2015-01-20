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

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;

import fr.inria.soctrace.framesoc.core.bus.FramesocBus;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.core.tools.importers.TraceChecker;
import fr.inria.soctrace.framesoc.core.tools.model.IPluginToolJobBody;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;

/**
 * Subclass of {@link PluginToolJob} for Plugin Importer Tools.
 * 
 * <p>
 * In the {@link PluginToolJob#postExecute(IStatus)} method implementation, a message is sent on the
 * {@link FramesocBus} for the topic {@link FramesocBusTopic#TOPIC_UI_SYNCH_TRACES_NEEDED}. The
 * associated data is true or false, depending on whether the Job returned status is
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

	/**
	 * Static utility methods to print user message if import fails.
	 * 
	 * It should be used to generate the exception message for exceptions launched in
	 * {@link IPluginToolJobBody#run(IProgressMonitor)}. The alternative is to call
	 * {@link #catchImporterException(Exception, SystemDBObject, TraceDBObject)}, which manages the
	 * exception and calls this method internally.
	 * 
	 * @param e
	 *            Exception
	 * @param sysDbRollback
	 *            true if the System DB modifications have been rollbacked, false otherwise
	 * @param traceDbDrop
	 *            true if the Trace DB has been deleted, false otherwise
	 * @return the complete user message
	 */
	public static String getExceptionMessage(Exception e, boolean sysDbRollback, boolean traceDbDrop) {
		String base = "";
		if (e != null) {
			if (e.getMessage() != null) {
				base = e.getMessage();
				int i = base.indexOf(":");
				if (i != -1) {
					base = base.substring(base.indexOf(":") + 2);
				}
			}
		}
		StringBuilder sb = new StringBuilder("Import failed.\n");
		sb.append(base);
		sb.append("\n");
		sb.append("System DB rollback " + ((sysDbRollback) ? "done." : "not done."));
		sb.append("\n");
		sb.append("Trace DB " + ((traceDbDrop) ? "" : "not ") + "deleted.");
		return sb.toString();
	}

	/**
	 * Method to manage an exception that occurs in importers.
	 * 
	 * The method should be called in the catch clause of the
	 * {@link IPluginToolJobBody#run(IProgressMonitor)}. The method tries to rollback the
	 * modifications on the System DB and to drop the Trace DB. At the end an exception is thrown to
	 * the user code.
	 * 
	 * @param e
	 *            exception thrown in the importer
	 * @param sysDB
	 *            system DB
	 * @param traceDB
	 *            trace DB
	 * @throws SoCTraceException
	 *             Exception always thrown to the user. The message is generated with
	 *             {@link #getExceptionMessage(String, boolean, boolean)}
	 */
	public static void catchImporterException(Exception e, SystemDBObject sysDB,
			TraceDBObject traceDB) throws SoCTraceException {
		System.err.println("Import failure. Trying to rollback modifications in DB.");		
		boolean rollback = rollbackSystemDB(sysDB);
		boolean drop = dropTraceDB(traceDB);
		throw new SoCTraceException(getExceptionMessage(e, rollback, drop));
	}

	/**
	 * Method to manage an exception that occurs in importers.
	 * 
	 * The method should be called in the catch clause of the
	 * {@link IPluginToolJobBody#run(IProgressMonitor)}. The method tries to rollback the
	 * modifications on the System DB and to drop the Trace DB. At the end an exception is thrown to
	 * the user code.
	 * 
	 * @param e
	 *            exception thrown in the importer
	 * @param sysDB
	 *            system DB
	 * @param tdbs
	 *            list of trace DBs
	 * @throws SoCTraceException
	 *             Exception always thrown to the user. The message is generated with
	 *             {@link #getExceptionMessage(String, boolean, boolean)}
	 */
	public static void catchImporterException(Exception e, SystemDBObject sysDB,
			List<TraceDBObject> tdbs) throws SoCTraceException {
		System.err.println("Import failure. Trying to rollback modifications in DB.");
		boolean rollback = rollbackSystemDB(sysDB);
		boolean drop = true;
		for (TraceDBObject traceDB : tdbs) {
			drop = drop && dropTraceDB(traceDB);
		}
		throw new SoCTraceException(getExceptionMessage(e, rollback, drop));		
	}
	
	private static boolean rollbackSystemDB(SystemDBObject sysDB) {
		boolean rollback = false;
		if (sysDB != null) {
			try {
				sysDB.rollback();
				rollback = true;
			} catch (SoCTraceException ex) {
				System.err.println("Exception trying to rollback System DB.");
				System.err.println(ex.getMessage());
			}
		}
		return rollback;
	}
	
	private static boolean dropTraceDB(TraceDBObject traceDB) {
		boolean drop = false;
		if (traceDB != null) {
			try {
				traceDB.dropDatabase();
				drop = true;
			} catch (SoCTraceException ex) {
				System.err.println("Exception trying to drop Trace DB.");
				System.err.println(ex.getMessage());
			}
		}		
		return drop;
	}

}
