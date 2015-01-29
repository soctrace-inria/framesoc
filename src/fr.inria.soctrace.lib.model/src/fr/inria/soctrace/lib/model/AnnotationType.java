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

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing the ANNOTATION_TYPE entity of the data model.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class AnnotationType {

	private final long id;
	private String name;
	private List<AnnotationParamType> paramTypes;

	/**
	 * Constructor 
	 * @param id the entity unique id
	 */
	public AnnotationType(long id) {
		this.id = id;
		paramTypes = new ArrayList<AnnotationParamType>();
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
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
	 * @return the paramTypes
	 */
	public List<AnnotationParamType> getParamTypes() {
		return paramTypes;
	}

	/**
	 * @param paramTypes the paramTypes to set
	 */
	public void setParamTypes(List<AnnotationParamType> paramTypes) {
		this.paramTypes = paramTypes;
	}
	
	/**
	 * This method has protected visibility in order to 
	 * prevent clients to call it. This method should be 
	 * called only by {@link AnnotationParamType#setAnnotationType()}.
	 *  
	 * @param annotationParamType the parameter type to add
	 */
	protected void addAnnotationParamType(AnnotationParamType annotationParamType) {
		paramTypes.add(annotationParamType);
	}

	@Override
	public String toString() {
		return "AnnotationType [id=" + id + ", name=" + name + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((paramTypes == null) ? 0 : paramTypes.hashCode());
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
		AnnotationType other = (AnnotationType) obj;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (paramTypes == null) {
			if (other.paramTypes != null)
				return false;
		} else if (!paramTypes.equals(other.paramTypes))
			return false;
		return true;
	}
	
}
