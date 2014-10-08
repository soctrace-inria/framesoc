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
package fr.inria.soctrace.framesoc.ui.tracetable;

import fr.inria.soctrace.framesoc.ui.model.TableRow;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.TraceParam;
import fr.inria.soctrace.lib.model.utils.ModelConstants.TimeUnit;

/**
 * Model element for a row in the Trace table
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TraceTableRow extends TableRow {

	private Trace fTrace;

	/**
	 * Constructor used to create a table row related to a given trace.
	 * 
	 * @param trace
	 *            the trace
	 */
	public TraceTableRow(Trace trace) {
		
		fTrace = trace;
		
		fields.put(TraceTableColumn.ALIAS, String.valueOf(trace.getAlias()));
		fields.put(TraceTableColumn.TRACING_DATE, String.valueOf(trace.getTracingDate()));
		fields.put(TraceTableColumn.TRACED_APPLICATION, trace.getTracedApplication());
		fields.put(TraceTableColumn.BOARD, trace.getBoard());
		fields.put(TraceTableColumn.OPERATING_SYSTEM, trace.getOperatingSystem());
		fields.put(TraceTableColumn.NUMBER_OF_CPUS, String.valueOf(trace.getNumberOfCpus()));
		fields.put(TraceTableColumn.NUMBER_OF_EVENTS, String.valueOf(trace.getNumberOfEvents()));
		fields.put(TraceTableColumn.OUTPUT_DEVICE, trace.getOutputDevice());
		fields.put(TraceTableColumn.DESCRIPTION, trace.getDescription());
		fields.put(TraceTableColumn.DBNAME, trace.getDbName());
		fields.put(TraceTableColumn.MIN_TIMESTAMP, String.valueOf(trace.getMinTimestamp()));
		fields.put(TraceTableColumn.MAX_TIMESTAMP, String.valueOf(trace.getMaxTimestamp()));
		fields.put(TraceTableColumn.TIMEUNIT, TimeUnit.getLabel(trace.getTimeUnit()));
		
		StringBuilder tmp = new StringBuilder();
		boolean first = true;

		for (TraceParam tp : trace.getParams()) {
			if (first) {
				first = false;
			} else {
				tmp.append(", ");
			}
			tmp.append(tp.getTraceParamType().getName() + "='" + tp.getValue() + "'");
		}
		fields.put(TraceTableColumn.PARAMS, tmp.toString());
	}

	/**
	 * Empty table row, to be used for filters.
	 */
	public TraceTableRow() {
		for (TraceTableColumn col : TraceTableColumn.values()) {
			fields.put(col, "");
		}
	}

	/**
	 * 
	 * @return the trace corresponding to this row
	 */
	public Trace getTrace() {
		return fTrace;
	}
	
}
