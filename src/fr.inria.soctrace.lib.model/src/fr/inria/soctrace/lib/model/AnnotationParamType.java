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

/**
 * Class representing the ANNOTATION_PARAM_TYPE entity of the data model.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class AnnotationParamType {

	private final int id;
	private AnnotationType annotationType;
	private String name;
	private String type;
	
	/**
	 * Constructor 
	 * @param id the entity unique id
	 */
	public AnnotationParamType(int id) {
		super();
		this.id = id;
	}
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @return the annotationType
	 */
	public AnnotationType getAnnotationType() {
		return annotationType;
	}
	
	/**
	 * Note: the AnnotationParamType is added to the AnnotationType as well.
	 * @param annotationType the annotationType to set
	 */
	public void setAnnotationType(AnnotationType annotationType) {
		this.annotationType = annotationType;
		this.annotationType.addAnnotationParamType(this);
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

	@Override
	public String toString() {
		return "AnnotationParamType [id=" + id + ", annotationType.id="
				+ annotationType.getId() + ", name=" + name + ", type=" + type + "]";
	}

	/* Note to equals and hashCode.
	 * 
	 * Compare only ID, NAME, TYPE to avoid recursive check.
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
		if (!(obj instanceof AnnotationParamType))
			return false;
		AnnotationParamType other = (AnnotationParamType) obj;
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
