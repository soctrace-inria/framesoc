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
package fr.inria.soctrace.tools.importer.otf2.core;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

import fr.inria.soctrace.framesoc.core.tools.importers.AbstractTraceMetadataManager;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.SystemDBObject;

/**
 * Class to manage Otf2 Trace metadata.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
class Otf2TraceMetadata extends AbstractTraceMetadataManager {

	private Trace buffer = new Trace(0);

	@Override
	public String getTraceTypeName() {
		return Otf2Constants.TRACE_TYPE;
	}

	public Otf2TraceMetadata(SystemDBObject sysDB, String dbName, String alias)
			throws SoCTraceException {
		super(sysDB);
		this.buffer.setDbName(dbName);
		this.buffer.setAlias(alias);
	}

	public void setNumberOfEvents(int events) {
		this.buffer.setNumberOfEvents(events);
	}

	public void setMinTimestamp(long min) {
		this.buffer.setMinTimestamp(min);
	}

	public void setMaxTimestamp(long max) {
		this.buffer.setMaxTimestamp(max);
	}

	public void setTimeUnit(int timeUnit) {
		this.buffer.setTimeUnit(timeUnit);
	}

	@Override
	public void setTraceFields(Trace trace) {

		// fields read by the buffer
		trace.setAlias(buffer.getAlias());
		trace.setDbName(buffer.getDbName());

		trace.setNumberOfEvents(buffer.getNumberOfEvents());
		trace.setMinTimestamp(buffer.getMinTimestamp());
		trace.setMaxTimestamp(buffer.getMaxTimestamp());

		trace.setTimeUnit(buffer.getTimeUnit());
		trace.setTracedApplication(buffer.getTracedApplication());
		trace.setOperatingSystem(buffer.getOperatingSystem());

		// standard fields
		trace.setDescription("otf2 trace imported " + getCurrentDate());
		trace.setOutputDevice("otf2-print");
		trace.setProcessed(false);

		// not used fields
		trace.setNumberOfCpus(1);
		trace.setBoard("none");

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

	/**
	 * Get the exponent of the time unit
	 * 
	 * @return a string with the current date
	 */
	public int getTimeUnit(long ticksPerSecond) {
		int exp = 0;
		while (ticksPerSecond >= 10) {
			ticksPerSecond = ticksPerSecond / 10;
			exp++;
		}
		return -1 * exp;
	}

}
