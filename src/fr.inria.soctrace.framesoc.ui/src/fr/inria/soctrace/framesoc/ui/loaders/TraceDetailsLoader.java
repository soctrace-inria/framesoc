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
package fr.inria.soctrace.framesoc.ui.loaders;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.inria.soctrace.framesoc.core.bus.FramesocBus;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.ui.model.DetailsTableRow;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.TraceParam;
import fr.inria.soctrace.lib.model.utils.ModelConstants.TimeUnit;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.utils.DBModelConstants.TraceTableModel;

/**
 * Loader for the trace details view.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TraceDetailsLoader {

	private List<DetailsTableRow> properties = new ArrayList<DetailsTableRow>();

	/**
	 * Create table rows using Trace data
	 * 
	 * @param trace
	 *            trace to read
	 */
	public void load(Trace trace) {
		properties.clear();

		// default params
		properties.add(new DetailsTableRow(TraceTableModel.ALIAS.getName(), trace.getAlias()));
		properties.add(new DetailsTableRow(TraceTableModel.TRACING_DATE.getName(), trace
				.getTracingDate().toString()));
		properties.add(new DetailsTableRow(TraceTableModel.TRACED_APPLICATION.getName(), trace
				.getTracedApplication()));
		properties.add(new DetailsTableRow(TraceTableModel.BOARD.getName(), trace.getBoard()));
		properties.add(new DetailsTableRow(TraceTableModel.OPERATING_SYSTEM.getName(), trace
				.getOperatingSystem()));
		properties.add(new DetailsTableRow(TraceTableModel.NUMBER_OF_CPUS.getName(), String
				.valueOf(trace.getNumberOfCpus())));
		properties.add(new DetailsTableRow(TraceTableModel.NUMBER_OF_EVENTS.getName(), String
				.valueOf(trace.getNumberOfEvents())));
		properties.add(new DetailsTableRow(TraceTableModel.NUMBER_OF_PRODUCERS.getName(), String
				.valueOf(trace.getNumberOfProducers())));
		properties.add(new DetailsTableRow(TraceTableModel.OUTPUT_DEVICE.getName(), trace
				.getOutputDevice()));
		properties.add(new DetailsTableRow(TraceTableModel.DESCRIPTION.getName(), trace
				.getDescription()));
		properties.add(new DetailsTableRow(TraceTableModel.MIN_TIMESTAMP.getName(), String
				.valueOf(trace.getMinTimestamp()), false, true));
		properties.add(new DetailsTableRow(TraceTableModel.MAX_TIMESTAMP.getName(), String
				.valueOf(trace.getMaxTimestamp()), false, true));
		properties.add(new DetailsTableRow(TraceTableModel.TIMEUNIT.getName(), TimeUnit.getLabel(trace
				.getTimeUnit()), false, false));
		properties.add(new DetailsTableRow(TraceTableModel.TRACE_DB_NAME.getName(), trace.getDbName(), false,
				true));

		// custom params
		List<TraceParam> params = trace.getParams();
		sortParams(params);
		for (TraceParam tp : params) {
			properties.add(new DetailsTableRow(tp.getTraceParamType().getName(), tp.getValue(),
					true, false));
		}
	}

	/**
	 * Create table rows using the data from all the passed traces. Only parameters having common
	 * values are shown.
	 * 
	 * @param traces
	 *            trace to read
	 */
	public void load(List<Trace> traces) {

		properties.clear();

		if (traces.size() < 1)
			return;

		Trace first = traces.iterator().next();

		// Alias
		boolean	show = true;
		for (Trace t : traces) {
			if (!first.getAlias().equals(t.getAlias())) {
				show = false;
				break;
			}
		}
		if (show)
			properties.add(new DetailsTableRow(TraceTableModel.ALIAS.getName(), first.getAlias()));

		// Tracing date
		show = true;
		for (Trace t : traces) {
			if (!first.getTracingDate().equals(t.getTracingDate())) {
				show = false;
				break;
			}
		}
		if (show)
			properties.add(new DetailsTableRow(TraceTableModel.TRACING_DATE.getName(), first
					.getTracingDate().toString()));

		// Traced application
		show = true;
		for (Trace t : traces) {
			if (!first.getTracedApplication().equals(t.getTracedApplication())) {
				show = false;
				break;
			}
		}
		if (show)
			properties.add(new DetailsTableRow(TraceTableModel.TRACED_APPLICATION.getName(), first
					.getTracedApplication().toString()));

		// Board
		show = true;
		for (Trace t : traces) {
			if (!first.getBoard().equals(t.getBoard())) {
				show = false;
				break;
			}
		}
		if (show)
			properties.add(new DetailsTableRow(TraceTableModel.BOARD.getName(), first.getBoard()));

		// Operating system
		show = true;
		for (Trace t : traces) {
			if (!first.getOperatingSystem().equals(t.getOperatingSystem())) {
				show = false;
				break;
			}
		}
		if (show)
			properties.add(new DetailsTableRow(TraceTableModel.OPERATING_SYSTEM.getName(), first
					.getOperatingSystem()));

		// Number of CPU
		show = true;
		for (Trace t : traces) {
			if (first.getNumberOfCpus() != t.getNumberOfCpus()) {
				show = false;
				break;
			}
		}
		if (show)
			properties.add(new DetailsTableRow(TraceTableModel.NUMBER_OF_CPUS.getName(), String
					.valueOf(first.getNumberOfCpus())));

		// Number of events
		show = true;
		for (Trace t : traces) {
			if (first.getNumberOfEvents() != t.getNumberOfEvents()) {
				show = false;
				break;
			}
		}
		if (show)
			properties.add(new DetailsTableRow(TraceTableModel.NUMBER_OF_EVENTS.getName(), String
					.valueOf(first.getNumberOfEvents())));
		
		// Number of producers
		show = true;
		for (Trace t : traces) {
			if (first.getNumberOfProducers() != t.getNumberOfProducers()) {
				show = false;
				break;
			}
		}
		if (show)
			properties.add(new DetailsTableRow(TraceTableModel.NUMBER_OF_PRODUCERS.getName(), String
					.valueOf(first.getNumberOfProducers())));

		// Traced application
		show = true;
		for (Trace t : traces) {
			if (!first.getOutputDevice().equals(t.getOutputDevice())) {
				show = false;
				break;
			}
		}
		if (show)
			properties.add(new DetailsTableRow(TraceTableModel.OUTPUT_DEVICE.getName(), first
					.getOutputDevice()));

		// Description
		show = true;
		for (Trace t : traces) {
			if (!first.getDescription().equals(t.getDescription())) {
				show = false;
				break;
			}
		}
		if (show)
			properties.add(new DetailsTableRow(TraceTableModel.DESCRIPTION.getName(), first
					.getDescription()));

		// Min Timestamp
		show = true;
		for (Trace t : traces) {
			if (first.getMinTimestamp() != t.getMinTimestamp()) {
				show = false;
				break;
			}
		}
		if (show)
			properties.add(new DetailsTableRow(TraceTableModel.MIN_TIMESTAMP
					.getName(), String.valueOf(first.getMinTimestamp()),
					false, true));

		// Max Timestamp
		show = true;
		for (Trace t : traces) {
			if (first.getMaxTimestamp() != t.getMaxTimestamp()) {
				show = false;
				break;
			}
		}
		if (show)
			properties.add(new DetailsTableRow(TraceTableModel.MAX_TIMESTAMP
					.getName(), String.valueOf(first.getMaxTimestamp()),
					false, true));

		// Time unit
		show = true;
		for (Trace t : traces) {
			if (first.getTimeUnit() != t.getTimeUnit()) {
				show = false;
				break;
			}
		}
		if (show)
			properties.add(new DetailsTableRow(TraceTableModel.TIMEUNIT.getName(), TimeUnit
					.getLabel(first.getTimeUnit()), false, false));
		
		// Trace DB name
		show = true;
		for (Trace t : traces) {
			if (!first.getDbName().equals(t.getDbName())) {
				show = false;
				break;
			}
		}
		if (show)
			properties.add(new DetailsTableRow(TraceTableModel.TRACE_DB_NAME.getName(), first.getDbName(),
					false, true));

		// Show a row for each parameter having the same name and the same value
		List<Map<String, TraceParam>> plist = new LinkedList<Map<String, TraceParam>>();
		for (Trace t : traces) {
			plist.add(t.getParamMap());
		}
		Map<String, TraceParam> firstMap = first.getParamMap();
		List<TraceParam> params = new ArrayList<>();
		for (TraceParam tp : firstMap.values()) {
			show = true;
			for (Map<String, TraceParam> tm : plist) {
				if (!tm.containsKey(tp.getTraceParamType().getName())) {
					show = false;
					break;
				} else {
					if (!tm.get(tp.getTraceParamType().getName()).getValue().equals(tp.getValue())) {
						show = false;
						break;
					}
				}
			}
			if (show) {
				params.add(tp);
			}
		}
		sortParams(params);
		for (TraceParam tp : params) {
			properties.add(new DetailsTableRow(tp.getTraceParamType().getName(), tp.getValue(),
					true, false));
		}
	}

	/**
	 * Get the list of details table row.
	 * 
	 * @return a list of details table row
	 */
	public List<DetailsTableRow> getProperties() {
		return properties;
	}

	/**
	 * Save the modification done to this list of traces.
	 * 
	 * @param currentTraces
	 *            list of traces to modify
	 * @throws SoCTraceException
	 */
	public void store(List<Trace> currentTraces) throws SoCTraceException {

		boolean argError = false;

		SystemDBObject sysDB = null;

		try {
			sysDB = SystemDBObject.openNewInstance();

			for (Trace trace : currentTraces) {
				argError |= updateTraceObject(trace);
				sysDB.update(trace);
				for (TraceParam p : trace.getParams()) {
					sysDB.update(p);
				}
			}
		} finally {
			DBObject.finalClose(sysDB);
		}

		// notify the bus to refresh trace view
		FramesocBus.getInstance().send(FramesocBusTopic.TOPIC_UI_SYNCH_TRACES_NEEDED, true);

		if (argError) {
			throw new SoCTraceException("Illegal format for some parameters. Skipped.");
		}
	}

	/**
	 * Update the trace object using the value actually present in the properties. Read Only fields
	 * (DBNAME and TIMEUNIT) are skipped.
	 * 
	 * @param trace
	 *            the trace to update
	 * @return true if an error occurs, false otherwise
	 */
	private boolean updateTraceObject(Trace trace) {

		// only custom param
		Map<String, String> params = new HashMap<String, String>();

		// only fixed field
		Map<String, String> fixed = new HashMap<String, String>();

		// put all the properties (params and fixed) in params...
		for (DetailsTableRow r : properties) {
			params.put(r.getName(), r.getValue());
		}
		// ...then remove the fixed fields from param
		for (TraceTableModel f : TraceTableModel.values()) {
			if (!params.containsKey(f.getName()))
				continue;
			fixed.put(f.getName(), params.get(f.getName()));
			params.remove(f.getName());
		}

		// Trace fixed fields
		boolean argError = false;
		try {
			if (fixed.containsKey(TraceTableModel.TRACING_DATE.getName()))
				trace.setTracingDate(Timestamp.valueOf(fixed.get(TraceTableModel.TRACING_DATE.getName())));
		} catch (IllegalArgumentException e) {
			argError = true;
		}
		try {
			if (fixed.containsKey(TraceTableModel.NUMBER_OF_CPUS.getName()))
				trace.setNumberOfCpus(Integer.valueOf(fixed.get(TraceTableModel.NUMBER_OF_CPUS
						.getName())));
			if (fixed.containsKey(TraceTableModel.NUMBER_OF_EVENTS.getName()))
				trace.setNumberOfEvents(Integer.valueOf(fixed.get(TraceTableModel.NUMBER_OF_EVENTS
						.getName())));	
			if (fixed.containsKey(TraceTableModel.NUMBER_OF_PRODUCERS.getName()))
				trace.setNumberOfProducers(Integer.valueOf(fixed.get(TraceTableModel.NUMBER_OF_PRODUCERS
						.getName())));
		} catch (NumberFormatException e) {
			argError = true;
		}

		if (fixed.containsKey(TraceTableModel.TIMEUNIT.getName()))
			trace.setTimeUnit(TimeUnit.getInt(fixed.get(TraceTableModel.TIMEUNIT.getName())));
		if (fixed.containsKey(TraceTableModel.TRACED_APPLICATION.getName()))
			trace.setTracedApplication(fixed.get(TraceTableModel.TRACED_APPLICATION.getName()));
		if (fixed.containsKey(TraceTableModel.BOARD.getName()))
			trace.setBoard(fixed.get(TraceTableModel.BOARD.getName()));
		if (fixed.containsKey(TraceTableModel.OPERATING_SYSTEM.getName()))
			trace.setOperatingSystem(fixed.get(TraceTableModel.OPERATING_SYSTEM.getName()));
		if (fixed.containsKey(TraceTableModel.OUTPUT_DEVICE.getName()))
			trace.setOutputDevice(fixed.get(TraceTableModel.OUTPUT_DEVICE.getName()));
		if (fixed.containsKey(TraceTableModel.DESCRIPTION.getName()))
			trace.setDescription(fixed.get(TraceTableModel.DESCRIPTION.getName()));
		if (fixed.containsKey(TraceTableModel.ALIAS.getName()))
			trace.setAlias(fixed.get(TraceTableModel.ALIAS.getName()));

		// DBNAME, MIN_TIMESTAMP, MAX_TIMESTAMP and TIMEUNIT are Read only, so not modified here

		// Trace Param
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		Map<String, TraceParam> pmap = trace.getParamMap();
		while (iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			pmap.get(entry.getKey()).setValue(entry.getValue());
		}

		return argError;
	}

	/**
	 * Sort the passed trace param list by trace param type name.
	 * 
	 * @param params
	 *            list to sort
	 */
	private void sortParams(List<TraceParam> params) {
		Collections.sort(params, new Comparator<TraceParam>() {
			@Override
			public int compare(TraceParam o1, TraceParam o2) {
				return o1.getTraceParamType().getName().compareTo(o2.getTraceParamType().getName());
			}
		});
	}

}
