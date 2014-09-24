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
package fr.inria.soctrace.tools.importer.gstreamer.core;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SimpleTimeZone;

import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.TraceParam;
import fr.inria.soctrace.lib.model.TraceParamType;
import fr.inria.soctrace.lib.model.TraceType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.model.utils.ModelConstants.TimeUnit;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;
import fr.inria.soctrace.lib.utils.IdManager;
import fr.inria.soctrace.tools.importer.gstreamer.core.GStreamerConstants.GStreamerTraceParamType;

/**
 * Class to manage GStreamer Trace metadata.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class GStreamerTraceMetadata {

	private SystemDBObject sysDB;
	private String dbName;
	private boolean traceTypeExisting;
	private TraceType traceType;
	private Trace trace;
	private Map<String, TraceParamType> tptMap;

	/**
	 * Constructor
	 * 
	 * @param sysDB
	 *            system DB object
	 * @param dbName
	 *            trace DB name
	 * @throws SoCTraceException
	 */
	public GStreamerTraceMetadata(SystemDBObject sysDB, String dbName) throws SoCTraceException {
		this.sysDB = sysDB;
		this.dbName = dbName;
		this.traceTypeExisting = (sysDB.isTraceTypePresent(GStreamerConstants.TRACE_TYPE));
		this.tptMap = new HashMap<String, TraceParamType>();
	}

	/**
	 * Build trace information
	 * 
	 * @param startFrameEventTypeId
	 * @param numberOfEvents
	 */
	public void computeMetadata(int startFrameEventTypeId, int numberOfFrames, int numberOfEvents,
			long minTimestamp, long maxTimestamp) throws SoCTraceException {

		// Trace Type
		buildTraceType();

		// Trace
		buildTrace(startFrameEventTypeId, numberOfFrames, numberOfEvents, minTimestamp, maxTimestamp);

	}

	/**
	 * Save the trace general information into the System DB. If the trace type
	 * was already present, is not saved again (avoid redundancy). Note: the
	 * method does not commit.
	 * 
	 * @throws SoCTraceException
	 */
	public void saveMetadata() throws SoCTraceException {

		if (!traceTypeExisting) {
			sysDB.save(traceType);
			for (TraceParamType tpt : traceType.getTraceParamTypes()) {
				sysDB.save(tpt);
			}
		}

		sysDB.save(trace);
		for (TraceParam tp : trace.getParams()) {
			sysDB.save(tp);
		}
	}

	/**
	 * If the trace type is already saved in the DB, the TraceType object is
	 * loaded from the DB. Otherwise it is created.
	 * 
	 * @throws SoCTraceException
	 */
	private void buildTraceType() throws SoCTraceException {

		if (traceTypeExisting) {
			traceType = sysDB.getTraceType(GStreamerConstants.TRACE_TYPE);
			for (TraceParamType tpt : traceType.getTraceParamTypes()) {
				tptMap.put(tpt.getName(), tpt);
			}
		} else {
			traceType = new TraceType(sysDB.getNewId(FramesocTable.TRACE_TYPE.toString(), "ID"));
			traceType.setName(GStreamerConstants.TRACE_TYPE);
			IdManager tptIdManager = new IdManager();
			tptIdManager
					.setNextId(sysDB.getMaxId(FramesocTable.TRACE_PARAM_TYPE.toString(), "ID") + 1);
			TraceParamType tpt;
			for (GStreamerTraceParamType p : GStreamerTraceParamType.values()) {
				tpt = new TraceParamType(tptIdManager.getNextId());
				tpt.setTraceType(traceType);
				tpt.setName(p.getName());
				tpt.setType(p.getType());
				tptMap.put(p.toString(), tpt);
			}
		}
	}

	/**
	 * Builds the Trace object
	 * 
	 * @param startFrameEventTypeId
	 * @param numberOfFrames
	 * @param numberOfEvents
	 * @param maxTimestamp 
	 * @param minTimestamp 
	 * 
	 * @param br
	 *            trace file buffered reader
	 * @throws SoCTraceException
	 */
	private void buildTrace(int startFrameEventTypeId, int numberOfFrames, int numberOfEvents, long minTimestamp, long maxTimestamp)
			throws SoCTraceException {

		trace = new Trace(sysDB.getNewId(FramesocTable.TRACE.toString(), "ID"));
		trace.setDescription("GStreamer trace imported on " + getCurrentDate());
		trace.setDbName(dbName);
		trace.setType(traceType);
		trace.setNumberOfEvents(numberOfEvents);
		trace.setNumberOfCpus(1); // XXX only single cpu now
		trace.setMinTimestamp(minTimestamp);
		trace.setMaxTimestamp(maxTimestamp);
		trace.setTimeUnit(TimeUnit.NANOSECONDS.getInt());

		IdManager tpIdManager = new IdManager();
		tpIdManager.setNextId(sysDB.getMaxId(FramesocTable.TRACE_PARAM.toString(), "ID") + 1);
		TraceParam tp;

		tp = new TraceParam(tpIdManager.getNextId());
		tp.setTrace(trace);
		tp.setTraceParamType(tptMap.get(GStreamerTraceParamType.FRAME_START_EVENT_TYPE_ID.getName()));
		tp.setValue(String.valueOf(startFrameEventTypeId));

		tp = new TraceParam(tpIdManager.getNextId());
		tp.setTrace(trace);
		tp.setTraceParamType(tptMap.get(GStreamerTraceParamType.NUMBER_OF_FRAMES.getName()));
		tp.setValue(String.valueOf(numberOfFrames));

	}

	/**
	 * Get the current date.
	 * 
	 * @return a string with the current date
	 */
	private String getCurrentDate() {
		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.setTimeZone(new SimpleTimeZone(0, "GMT"));
		sdf.applyPattern("dd MMM yyyy HH:mm:ss z");
		return sdf.format(new Date()).toString();
	}

}
