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
package fr.inria.soctrace.lib.model;

import java.io.Serializable;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Class representing the TRACE_PARAM entity of the data model.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class TraceParam implements IModelElement, Serializable {
	
	/**
	 * Generated UID for serialization 
	 */
	private static final long serialVersionUID = 1307516067871932226L;
	
	private final int id;
	private String value;
	private Trace trace;
	private TraceParamType traceParamType;
	
	/**
	 * Constructor 
	 * @param id the entity unique id
	 */
	public TraceParam(int id) {
		this.id = id;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the trace
	 */
	public Trace getTrace() {
		return trace;
	}

	/**
	 * Note: the TraceParam is added to the Trace params list.
	 * @param trace the trace to set
	 */
	public void setTrace(Trace trace) {
		this.trace = trace;
		this.trace.addTraceParam(this);
	}

	/**
	 * @return the traceParamType
	 */
	public TraceParamType getTraceParamType() {
		return traceParamType;
	}

	/**
	 * @param traceParamType the traceParamType to set
	 */
	public void setTraceParamType(TraceParamType traceParamType) {
		this.traceParamType = traceParamType;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return "TraceParam[(id:" + getId() + ")," +
				"(trace.id:" + getTrace().getId() + ")," +
				"(value:\"" + getValue() + "\")," +
				"(trace_param_type.name:\"" + getTraceParamType().getName() + "\"," +
				"trace_param_type.type:\"" + getTraceParamType().getType() + "\")]";
	}
	
	@Override
	public void accept(IModelVisitor visitor) throws SoCTraceException {
		visitor.visit(this);
	}

	/* Note to equals and hashCode.
	 * 
	 * Only ID and VALUE are compared, to avoid recursive check. 
	 */
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		if (!(obj instanceof TraceParam))
			return false;
		TraceParam other = (TraceParam) obj;
		if (id != other.id)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
}
