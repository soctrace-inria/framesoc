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
	public TraceTableRow(Trace trace, TraceTableCache cache) {
		fTrace = trace;
		
		fields.put(cache.getTableColumns().get(TraceTableColumnEnum.ALIAS.getHeader()), String.valueOf(trace.getAlias()));
		fields.put(cache.getTableColumns().get(TraceTableColumnEnum.TRACING_DATE.getHeader()), String.valueOf(trace.getTracingDate()));
		fields.put(cache.getTableColumns().get(TraceTableColumnEnum.TRACED_APPLICATION.getHeader()), trace.getTracedApplication());
		fields.put(cache.getTableColumns().get(TraceTableColumnEnum.BOARD.getHeader()), trace.getBoard());
		fields.put(cache.getTableColumns().get(TraceTableColumnEnum.OPERATING_SYSTEM.getHeader()), trace.getOperatingSystem());
		fields.put(cache.getTableColumns().get(TraceTableColumnEnum.NUMBER_OF_CPUS.getHeader()), String.valueOf(trace.getNumberOfCpus()));
		fields.put(cache.getTableColumns().get(TraceTableColumnEnum.NUMBER_OF_EVENTS.getHeader()), String.valueOf(trace.getNumberOfEvents()));
		fields.put(cache.getTableColumns().get(TraceTableColumnEnum.OUTPUT_DEVICE.getHeader()), trace.getOutputDevice());
		fields.put(cache.getTableColumns().get(TraceTableColumnEnum.DESCRIPTION.getHeader()), trace.getDescription());
		fields.put(cache.getTableColumns().get(TraceTableColumnEnum.DBNAME.getHeader()), trace.getDbName());
		fields.put(cache.getTableColumns().get(TraceTableColumnEnum.MIN_TIMESTAMP.getHeader()), String.valueOf(trace.getMinTimestamp()));
		fields.put(cache.getTableColumns().get(TraceTableColumnEnum.MAX_TIMESTAMP.getHeader()), String.valueOf(trace.getMaxTimestamp()));
		fields.put(cache.getTableColumns().get(TraceTableColumnEnum.TIMEUNIT.getHeader()), TimeUnit.getLabel(trace.getTimeUnit()));

		for (TraceParam tp : trace.getParams()) {
			if (!cache.getTableColumns().containsKey(
					tp.getTraceParamType().getName())) {
				cache.getTableColumns().put(
						tp.getTraceParamType().getName(),
						new TraceTableColumn(tp.getTraceParamType().getName(),
								tp.getTraceParamType().getName(), 100));
			}

			fields.put(cache.getTableColumns().get(tp.getTraceParamType().getName()), tp.getValue());
		}
	}

	/**
	 * Empty table row, to be used for filters.
	 */
	public TraceTableRow() {
		for (TraceTableColumnEnum col : TraceTableColumnEnum.values()) {
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
	
	/**
	 * Since the custom parameters of the trace are added dynamically, some
	 * fields are not initialized. This method initialiazed them with empty
	 * strings
	 * 
	 * @param cache
	 *            the cache providing the comumns in the table
	 */
	public void initValues(TraceTableCache cache) {
		for (TraceTableColumn col : cache.getTableColumns().values())
			if (!fields.containsKey(col))
				fields.put(col, "");
	}
}
