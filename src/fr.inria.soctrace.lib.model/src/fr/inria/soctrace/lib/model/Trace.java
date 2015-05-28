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
package fr.inria.soctrace.lib.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.lib.model.utils.ModelConstants.TimeUnit;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Class representing the TRACE entity of the data model.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class Trace implements IModelElement, Serializable {

	/**
	 * Generated UID for serialization 
	 */
	private static final long serialVersionUID = 5330672371654939983L;
	
	public static final String UNKNOWN_STRING = "UNKNOWN";
	public static final int UNKNOWN_INT = -1;
	
	private final int id;
	private TraceType type;
	private Timestamp tracingDate;
	private String tracedApplication;
	private String board;
	private String operatingSystem;
	private int numberOfCpus;
	private int numberOfEvents;
	private String outputDevice;
	private String description;
	private boolean processed;
	private String dbName;
	private String alias;
	private int timeUnit;
	private long minTimestamp;
	private long maxTimestamp;
	private int numberOfProducers;
	private List<TraceParam> params;
	
	/**
	 * Constructor.
	 * By default the trace is not a "processed trace".
	 * 
	 * @param id the trace unique id
	 */
	public Trace(int id) {
		this.id = id;
		this.params = new ArrayList<TraceParam>();
		// default values
		this.processed = false;
		this.numberOfCpus = UNKNOWN_INT;
		this.numberOfEvents = UNKNOWN_INT;
		this.tracingDate = new Timestamp(new Date().getTime());
		this.tracedApplication = UNKNOWN_STRING;
		this.board = UNKNOWN_STRING;
		this.operatingSystem = UNKNOWN_STRING;
		this.outputDevice = UNKNOWN_STRING;
		this.description = UNKNOWN_STRING;
		this.dbName = UNKNOWN_STRING;
		this.alias = UNKNOWN_STRING;
		this.minTimestamp = UNKNOWN_INT;
		this.maxTimestamp = UNKNOWN_INT;
		this.timeUnit = TimeUnit.UNKNOWN.getInt();
		this.numberOfProducers = UNKNOWN_INT;
	}
		
	/**
	 * @return the type
	 */
	public TraceType getType() {
		return type;
	}
	
	/**
	 * @param type the type to set
	 */
	public void setType(TraceType type) {
		this.type = type;
	}
	
	/**
	 * @return the tracingDate
	 */
	public Timestamp getTracingDate() {
		return tracingDate;
	}

	/**
	 * @param tracingDate the tracingDate to set
	 */
	public void setTracingDate(Timestamp tracingDate) {
		this.tracingDate = tracingDate;
	}

	/**
	 * @return the tracedApplication
	 */
	public String getTracedApplication() {
		return tracedApplication;
	}

	/**
	 * @param tracedApplication the tracedApplication to set
	 */
	public void setTracedApplication(String tracedApplication) {
		this.tracedApplication = tracedApplication;
	}

	/**
	 * @return the board
	 */
	public String getBoard() {
		return board;
	}

	/**
	 * @param board the board to set
	 */
	public void setBoard(String board) {
		this.board = board;
	}

	/**
	 * @return the operatingSystem
	 */
	public String getOperatingSystem() {
		return operatingSystem;
	}

	/**
	 * @param operatingSystem the operatingSystem to set
	 */
	public void setOperatingSystem(String operatingSystem) {
		this.operatingSystem = operatingSystem;
	}

	/**
	 * @return the numberOfCpus
	 */
	public int getNumberOfCpus() {
		return numberOfCpus;
	}

	/**
	 * @param numberOfCpus the numberOfCpus to set
	 */
	public void setNumberOfCpus(int numberOfCpus) {
		this.numberOfCpus = numberOfCpus;
	}

	/**
	 * @return the outputDevice
	 */
	public String getOutputDevice() {
		return outputDevice;
	}

	/**
	 * @param outputDevice the outputDevice to set
	 */
	public void setOutputDevice(String outputDevice) {
		this.outputDevice = outputDevice;
	}

	/**
	 * @param params the params to set
	 */
	public void setParams(List<TraceParam> params) {
		this.params = params;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * @return the processed
	 */
	public boolean isProcessed() {
		return processed;
	}

	/**
	 * @param processed the processed to set
	 */
	public void setProcessed(boolean processed) {
		this.processed = processed;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * @return the dbName
	 */
	public String getDbName() {
		return dbName;
	}
	
	/**
	 * @param dbName the dbName to set
	 */
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	/**
	 * @return the alias
	 */
	public String getAlias() {
		if (alias.equals(UNKNOWN_STRING))
			return dbName;
		return alias;
	}
	
	/**
	 * @param alias the alias to set
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}

	
	/**
	 * @return the timeUnit
	 */
	public int getTimeUnit() {
		return timeUnit;
	}

	/**
	 * @param timeUnit the timeUnit to set
	 */
	public void setTimeUnit(int timeUnit) {
		this.timeUnit = timeUnit;
	}

	/**
	 * 
	 * @return the number of producers
	 */
	public int getNumberOfProducers() {
		return numberOfProducers;
	}

	/**
	 * 
	 * @param numberOfProducers number of producers to set
	 */
	public void setNumberOfProducers(int numberOfProducers) {
		this.numberOfProducers = numberOfProducers;
	}

	/**
	 * @return the number of events
	 */
	public int getNumberOfEvents() {
		return numberOfEvents;
	}

	/**
	 * @param numberOfEvents The number of events to set
	 */
	public void setNumberOfEvents(int numberOfEvents) {
		this.numberOfEvents = numberOfEvents;
	}

	/**
	 * @return the params
	 */
	public List<TraceParam> getParams() {
		return params;
	}
	
	/**
	 * This method has protected visibility in order to 
	 * prevent clients to call it. This method should be 
	 * called only by {@link TraceParam#setTrace()}.
	 *  
	 * @param traceParam
	 */
	protected void addTraceParam(TraceParam traceParam) {
		params.add(traceParam);
	}
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
			
//	@Override
//	public String toString() {
//		return "Trace [id=" + id + ", type=" + type + ", tracingDate="
//				+ tracingDate + ", tracedApplication=" + tracedApplication
//				+ ", board=" + board + ", operatingSystem=" + operatingSystem
//				+ ", numberOfCpus=" + numberOfCpus + ", outputDevice="
//				+ outputDevice + ", description=" + description
//				+ ", processed=" + processed + ", dbName=" + dbName
//				+ ", alias=" + alias + ", timeUnit=" + TimeUnit.getLabel(timeUnit)
//				+ ", numberOfEvents=" + numberOfEvents + ", params=" + params
//				+ "]";
//	}

	@Override
	public String toString() { return getAlias(); } // XXX

	@Override
	public void accept(IModelVisitor visitor) throws SoCTraceException {
		visitor.visit(this);
	}

	/**
	 * Debug methods
	 */
	
	public void print() {
		print(false);
	}
	
	public void print(boolean verbose) {
		System.out.println(toString());
		if (!verbose)
			return;
		for (TraceParam tp: params) {
			System.out.println("  " + tp.toString());
		}
	}
	
	/**
	 * Get a Map : trace param name <-> trace param reference.
	 * The map is built on the fly.
	 * @return the map of trace parameters
	 */
	public Map<String, TraceParam> getParamMap() {
		Map<String, TraceParam> map = new HashMap<String, TraceParam>();
		for (TraceParam param : params) {
			map.put(param.getTraceParamType().getName(), param);
		}
		return map;
	}

	/**
	 * Copy trace metadata from another trace object.
	 * TraceType and TraceParams are not touched.
	 * 
	 * @param t trace to copy
	 */
	public void copyMetadata(Trace t) {
		this.processed = t.isProcessed();
		this.numberOfCpus = t.getNumberOfCpus();
		this.numberOfEvents = t.getNumberOfEvents();
		this.tracingDate = t.getTracingDate();
		this.tracedApplication = t.getTracedApplication();
		this.board = t.getBoard();
		this.operatingSystem = t.getOperatingSystem();
		this.outputDevice = t.getOutputDevice();
		this.description = t.getDescription();
		this.dbName = t.getDbName();
		this.alias = t.getAlias();
		this.timeUnit = t.getTimeUnit();
		this.numberOfProducers = t.getNumberOfProducers();
		this.minTimestamp = t.getMinTimestamp();
		this.maxTimestamp = t.getMaxTimestamp();
	}

	/**
	 * Copy the values of fixed fields and trace parameters.
	 * @param other source trace
	 */
	public void synchWith(Trace other) {
		copyMetadata(other);
		Map<String, TraceParam> map = other.getParamMap();
		for (TraceParam tp: this.params) {
			tp.setValue(map.get(tp.getTraceParamType().getName()).getValue());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alias == null) ? 0 : alias.hashCode());
		result = prime * result + ((board == null) ? 0 : board.hashCode());
		result = prime * result + ((dbName == null) ? 0 : dbName.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + id;
		result = prime * result + (int) (maxTimestamp ^ (maxTimestamp >>> 32));
		result = prime * result + (int) (minTimestamp ^ (minTimestamp >>> 32));
		result = prime * result + numberOfCpus;
		result = prime * result + numberOfEvents;
		result = prime * result + numberOfProducers;
		result = prime * result + ((operatingSystem == null) ? 0 : operatingSystem.hashCode());
		result = prime * result + ((outputDevice == null) ? 0 : outputDevice.hashCode());
		result = prime * result + ((params == null) ? 0 : params.hashCode());
		result = prime * result + (processed ? 1231 : 1237);
		result = prime * result + timeUnit;
		result = prime * result + ((tracedApplication == null) ? 0 : tracedApplication.hashCode());
		result = prime * result + ((tracingDate == null) ? 0 : tracingDate.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Trace other = (Trace) obj;
		if (alias == null) {
			if (other.alias != null)
				return false;
		} else if (!alias.equals(other.alias))
			return false;
		if (board == null) {
			if (other.board != null)
				return false;
		} else if (!board.equals(other.board))
			return false;
		if (dbName == null) {
			if (other.dbName != null)
				return false;
		} else if (!dbName.equals(other.dbName))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (id != other.id)
			return false;
		if (maxTimestamp != other.maxTimestamp)
			return false;
		if (minTimestamp != other.minTimestamp)
			return false;
		if (numberOfCpus != other.numberOfCpus)
			return false;
		if (numberOfEvents != other.numberOfEvents)
			return false;
		if (numberOfProducers != other.numberOfProducers)
			return false;
		if (operatingSystem == null) {
			if (other.operatingSystem != null)
				return false;
		} else if (!operatingSystem.equals(other.operatingSystem))
			return false;
		if (outputDevice == null) {
			if (other.outputDevice != null)
				return false;
		} else if (!outputDevice.equals(other.outputDevice))
			return false;
		if (params == null) {
			if (other.params != null)
				return false;
		} else if (!params.equals(other.params))
			return false;
		if (processed != other.processed)
			return false;
		if (timeUnit != other.timeUnit)
			return false;
		if (tracedApplication == null) {
			if (other.tracedApplication != null)
				return false;
		} else if (!tracedApplication.equals(other.tracedApplication))
			return false;
		if (tracingDate == null) {
			if (other.tracingDate != null)
				return false;
		} else if (!tracingDate.equals(other.tracingDate))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	/**
	 * @return the minTimestamp
	 */
	public long getMinTimestamp() {
		return minTimestamp;
	}

	/**
	 * @param minTimestamp the minTimestamp to set
	 */
	public void setMinTimestamp(long minTimestamp) {
		this.minTimestamp = minTimestamp;
	}

	/**
	 * Getter for the max timestamp, as defined in {@link #setMaxTimestamp(long)}.
	 * 
	 * @return the maxTimestamp
	 */
	public long getMaxTimestamp() {
		return maxTimestamp;
	}

	/**
	 * Setter for the max timestamp. Note that the max timestamp is defined
	 * as the max among all punctual event timestamps *and* all state/links 
	 * end timestamps.
	 * 
	 * @param maxTimestamp the maxTimestamp to set
	 */
	public void setMaxTimestamp(long maxTimestamp) {
		this.maxTimestamp = maxTimestamp;
	}

}
