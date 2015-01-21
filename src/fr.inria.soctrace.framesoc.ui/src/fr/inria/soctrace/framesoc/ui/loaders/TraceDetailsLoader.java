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

/**
 * Loader for the trace details view.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TraceDetailsLoader {

	/**
	 * Labels for fixed Trace attributes
	 */
	public static enum TraceField {

		ALIAS("Alias"), 
		TRACING_DATE("Tracing date"), 
		TRACED_APPLICATION("Traced application"), 
		BOARD("Board"), 
		OPERATING_SYSTEM("Operating System"), 
		NUMBER_OF_CPUS("Number of CPUs"), 
		NUMBER_OF_EVENTS("Number of events"),
		OUTPUT_DEVICE("Output device"),
		DESCRIPTION("Description"), 
		MIN_TIMESTAMP("Min Timestamp"),
		MAX_TIMESTAMP("Max Timestamp"),
		TIMEUNIT("Time-unit"), 
		DBNAME("DB name");

		private String name;

		private TraceField(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

	}

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
		properties.add(new DetailsTableRow(TraceField.ALIAS.toString(), trace.getAlias()));
		properties.add(new DetailsTableRow(TraceField.TRACING_DATE.toString(), trace
				.getTracingDate().toString()));
		properties.add(new DetailsTableRow(TraceField.TRACED_APPLICATION.toString(), trace
				.getTracedApplication()));
		properties.add(new DetailsTableRow(TraceField.BOARD.toString(), trace.getBoard()));
		properties.add(new DetailsTableRow(TraceField.OPERATING_SYSTEM.toString(), trace
				.getOperatingSystem()));
		properties.add(new DetailsTableRow(TraceField.NUMBER_OF_CPUS.toString(), String
				.valueOf(trace.getNumberOfCpus())));
		properties.add(new DetailsTableRow(TraceField.NUMBER_OF_EVENTS.toString(), String
				.valueOf(trace.getNumberOfEvents())));
		properties.add(new DetailsTableRow(TraceField.OUTPUT_DEVICE.toString(), trace
				.getOutputDevice()));
		properties.add(new DetailsTableRow(TraceField.DESCRIPTION.toString(), trace
				.getDescription()));
		properties.add(new DetailsTableRow(TraceField.MIN_TIMESTAMP.toString(), String
				.valueOf(trace.getMinTimestamp()), false, true));
		properties.add(new DetailsTableRow(TraceField.MAX_TIMESTAMP.toString(), String
				.valueOf(trace.getMaxTimestamp()), false, true));
		properties.add(new DetailsTableRow(TraceField.TIMEUNIT.toString(), TimeUnit.getLabel(trace
				.getTimeUnit()), false, true));
		properties.add(new DetailsTableRow(TraceField.DBNAME.toString(), trace.getDbName(), false,
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

		// Tracing date
		boolean show = true;
		for (Trace t : traces) {
			if (!first.getTracingDate().equals(t.getTracingDate())) {
				show = false;
				break;
			}
		}
		if (show)
			properties.add(new DetailsTableRow(TraceField.TRACING_DATE.toString(), first
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
			properties.add(new DetailsTableRow(TraceField.TRACED_APPLICATION.toString(), first
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
			properties.add(new DetailsTableRow(TraceField.BOARD.toString(), first.getBoard()));

		// Operating system
		show = true;
		for (Trace t : traces) {
			if (!first.getOperatingSystem().equals(t.getOperatingSystem())) {
				show = false;
				break;
			}
		}
		if (show)
			properties.add(new DetailsTableRow(TraceField.OPERATING_SYSTEM.toString(), first
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
			properties.add(new DetailsTableRow(TraceField.NUMBER_OF_CPUS.toString(), String
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
			properties.add(new DetailsTableRow(TraceField.NUMBER_OF_EVENTS.toString(), String
					.valueOf(first.getNumberOfEvents())));

		// Traced application
		show = true;
		for (Trace t : traces) {
			if (!first.getOutputDevice().equals(t.getOutputDevice())) {
				show = false;
				break;
			}
		}
		if (show)
			properties.add(new DetailsTableRow(TraceField.OUTPUT_DEVICE.toString(), first
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
			properties.add(new DetailsTableRow(TraceField.DESCRIPTION.toString(), first
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
			properties.add(new DetailsTableRow(TraceField.MIN_TIMESTAMP.toString(), String
					.valueOf(first.getMinTimestamp())));

		// Max Timestamp
		show = true;
		for (Trace t : traces) {
			if (first.getMaxTimestamp() != t.getMaxTimestamp()) {
				show = false;
				break;
			}
		}
		if (show)
			properties.add(new DetailsTableRow(TraceField.MAX_TIMESTAMP.toString(), String
					.valueOf(first.getMaxTimestamp())));

		// Alias
		show = true;
		for (Trace t : traces) {
			if (!first.getAlias().equals(t.getAlias())) {
				show = false;
				break;
			}
		}
		if (show)
			properties.add(new DetailsTableRow(TraceField.ALIAS.toString(), first.getAlias()));

		// Trace DB name
		show = true;
		for (Trace t : traces) {
			if (!first.getDbName().equals(t.getDbName())) {
				show = false;
				break;
			}
		}
		if (show)
			properties.add(new DetailsTableRow(TraceField.DBNAME.toString(), first.getDbName(),
					false, true));

		// Traced application
		show = true;
		for (Trace t : traces) {
			if (first.getTimeUnit() != t.getTimeUnit()) {
				show = false;
				break;
			}
		}
		if (show)
			properties.add(new DetailsTableRow(TraceField.TIMEUNIT.toString(), TimeUnit
					.getLabel(first.getTimeUnit()), false, true));

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
			sysDB = SystemDBObject.openNewIstance();

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
		for (TraceField f : TraceField.values()) {
			if (!params.containsKey(f.toString()))
				continue;
			fixed.put(f.toString(), params.get(f.toString()));
			params.remove(f.toString());
		}

		// Trace fixed fields
		boolean argError = false;
		try {
			if (fixed.containsKey(TraceField.TRACING_DATE.toString()))
				trace.setTracingDate(Timestamp.valueOf(fixed.get(TraceField.TRACING_DATE.toString())));
		} catch (IllegalArgumentException e) {
			argError = true;
		}
		try {
			if (fixed.containsKey(TraceField.NUMBER_OF_CPUS.toString()))
				trace.setNumberOfCpus(Integer.valueOf(fixed.get(TraceField.NUMBER_OF_CPUS
						.toString())));
			if (fixed.containsKey(TraceField.NUMBER_OF_EVENTS.toString()))
				trace.setNumberOfEvents(Integer.valueOf(fixed.get(TraceField.NUMBER_OF_EVENTS
						.toString())));
		} catch (NumberFormatException e) {
			argError = true;
		}

		if (fixed.containsKey(TraceField.TRACED_APPLICATION.toString()))
			trace.setTracedApplication(fixed.get(TraceField.TRACED_APPLICATION.toString()));
		if (fixed.containsKey(TraceField.BOARD.toString()))
			trace.setBoard(fixed.get(TraceField.BOARD.toString()));
		if (fixed.containsKey(TraceField.OPERATING_SYSTEM.toString()))
			trace.setOperatingSystem(fixed.get(TraceField.OPERATING_SYSTEM.toString()));
		if (fixed.containsKey(TraceField.OUTPUT_DEVICE.toString()))
			trace.setOutputDevice(fixed.get(TraceField.OUTPUT_DEVICE.toString()));
		if (fixed.containsKey(TraceField.DESCRIPTION.toString()))
			trace.setDescription(fixed.get(TraceField.DESCRIPTION.toString()));
		if (fixed.containsKey(TraceField.ALIAS.toString()))
			trace.setAlias(fixed.get(TraceField.ALIAS.toString()));

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
