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
/**
 * 
 */
package fr.inria.soctrace.framesoc.core.tools.importers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.core.FramesocManager;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.TraceQuery;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.Configuration;
import fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty;

/**
 * Checks traces after import.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TraceChecker {

	private final static Logger logger = LoggerFactory.getLogger(TraceChecker.class);

	/**
	 * List containing all SystemDB traces at a given time.
	 */
	private Set<Trace> traces;

	/**
	 * The installed trace checkers
	 */
	private List<IChecker> checkers;

	/**
	 * Constructor. Initializes the service, loading the existing trace
	 * metadata.
	 */
	public TraceChecker() {

		// load checkers
		checkers = new ArrayList<>();
		checkers.add(new MinMaxChecker());
		checkers.add(new IndexChecker());

		// load the traces, if a SystemDB exists
		traces = new HashSet<Trace>();
		SystemDBObject sysDB = null;
		try {
			if (!FramesocManager.getInstance().isSystemDBExisting())
				return;
			sysDB = SystemDBObject.openNewIstance();
			TraceQuery tq = new TraceQuery(sysDB);
			List<Trace> tmp = tq.getList();
			for (Trace t : tmp) {
				traces.add(t);
			}
		} catch (SoCTraceException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			DBObject.finalClose(sysDB);
		}
	}

	/**
	 * Applies all the checking to the newly imported traces.
	 * 
	 * @param monitor
	 *            Progress monitor. This method use it *only* to specify sub
	 *            tasks. If it is null, a NullProgressMonitor is used.
	 */
	public void checkTraces(IProgressMonitor monitor) {

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		SystemDBObject sysDB = null;
		try {
			if (!FramesocManager.getInstance().isSystemDBExisting())
				return;
			sysDB = SystemDBObject.openNewIstance();
			TraceQuery tq = new TraceQuery(sysDB);
			List<Trace> tmp = tq.getList();
			for (Trace t : tmp) {
				if (!traces.contains(t)) {
					logger.debug("Check trace: {}", t.getAlias());
					for (IChecker checker : checkers) {
						checker.checkTrace(t, sysDB, monitor);
					}
				}
			}
		} catch (SoCTraceException e) {
			e.printStackTrace();
		} finally {
			DBObject.finalClose(sysDB);
		}
	}

	/*
	 * Trace checkers
	 */

	/**
	 * Interface for all trace checker.
	 */
	private interface IChecker {
		void checkTrace(Trace t, SystemDBObject sysDB, IProgressMonitor monitor);
	}

	/**
	 * Trace checker for min/max trace metadata.
	 */
	private class MinMaxChecker implements IChecker {

		@Override
		public void checkTrace(Trace t, SystemDBObject sysDB, IProgressMonitor monitor) {

			monitor.subTask("Min/Max check on trace:  " + t.getAlias());

			if (t.getMinTimestamp() != Trace.UNKNOWN_INT
					&& t.getMaxTimestamp() != Trace.UNKNOWN_INT)
				return;

			if (!isDBExisting(t.getDbName()))
				return;

			TraceDBObject traceDB = null;
			try {
				traceDB = TraceDBObject.openNewIstance(t.getDbName());
				if (t.getMinTimestamp() == Trace.UNKNOWN_INT) {
					t.setMinTimestamp(traceDB.getMinTimestamp());
					sysDB.update(t);
				}
				if (t.getMaxTimestamp() == Trace.UNKNOWN_INT) {
					t.setMaxTimestamp(traceDB.getMaxTimestamp());
					sysDB.update(t);
				}
			} catch (SoCTraceException e) {
				e.printStackTrace();
			} finally {
				DBObject.finalClose(traceDB);
			}
		}

	}

	/**
	 * Trace checker for timestamp index.
	 */
	private class IndexChecker implements IChecker {

		private boolean enabled = true;
		
		public IndexChecker() {
			enabled = Configuration.getInstance().get(SoCTraceProperty.trace_db_indexing).equals("true");
		}
		
		@Override
		public void checkTrace(Trace t, SystemDBObject sysDB, IProgressMonitor monitor) {

			if (!enabled)
				return;
				
			monitor.subTask("Indexing trace: " + t.getAlias());

			if (!isDBExisting(t.getDbName()))
				return;

			TraceDBObject traceDB = null;
			try {
				traceDB = TraceDBObject.openNewIstance(t.getDbName());
				traceDB.createTimestampIndex();
			} catch (SoCTraceException e) {
				e.printStackTrace();
			} finally {
				DBObject.finalClose(traceDB);
			}
		}

	}

	private boolean isDBExisting(String dbName) {
		try {
			if (FramesocManager.getInstance().isDBExisting(dbName))
				return true;
			return false;
		} catch (SoCTraceException e) {
			e.printStackTrace();
			return false;
		}
	}

}
