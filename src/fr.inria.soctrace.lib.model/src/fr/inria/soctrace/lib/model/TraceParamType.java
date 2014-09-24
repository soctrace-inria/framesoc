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
 * Class representing the TRACE_PARAM_TYPE entity of the data model.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TraceParamType implements IModelElement, Serializable {
	
	/**
	 * Generated UID for serialization 
	 */
	private static final long serialVersionUID = -2352376564185018059L;
	
	private final int id;
	private TraceType traceType;
	private String name;
	private String type;
	
	/**
	 * Constructor 
	 * @param id the entity unique id
	 */
	public TraceParamType(int id) {
		this.id = id;
	}
	
	/**
	 * @return the traceType
	 */
	public TraceType getTraceType() {
		return traceType;
	}
	
	/**
	 * Note: the TraceParamType is added to the TraceType as well.
	 * @param traceType the traceType to set
	 */
	public void setTraceType(TraceType traceType) {
		this.traceType = traceType;
		this.traceType.addTraceParamType(this);
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return "TraceParamType[(id:" + getId() + ")," +
				"(trace_type.name:\"" + getTraceType().getName() + "\")," +
				"(type:\"" + getType() + "\"),(name:\"" + getName() + "\")]";
	}

	@Override
	public void accept(IModelVisitor visitor) throws SoCTraceException {
		visitor.visit(this);
	}

	/* Note to equals and hashCode.
	 * 
	 * Only ID, NAME and TYPE are compared.
	 */
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		if (!(obj instanceof TraceParamType))
			return false;
		TraceParamType other = (TraceParamType) obj;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
}
