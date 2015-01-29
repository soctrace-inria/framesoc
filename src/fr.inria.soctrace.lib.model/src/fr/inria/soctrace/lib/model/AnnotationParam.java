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
 * Class representing the ANNOTATION_PARAM entity of the data model.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class AnnotationParam {

	private final long id;
	private Annotation annotation;
	private AnnotationParamType annotationParamType;
	private String value;
	
	/**
	 * Constructor 
	 * @param id the entity unique id
	 */
	public AnnotationParam(long id) {
		super();
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}
	
	/**
	 * @return the annotation
	 */
	public Annotation getAnnotation() {
		return annotation;
	}

	/**
	 * Note: the AnnotationParam is added to the Annotation params list.
	 * @param annotation the annotation to set
	 */
	public void setAnnotation(Annotation annotation) {
		this.annotation = annotation;
		this.annotation.addAnnotationParam(this);
	}

	/**
	 * @return the annotationParamType
	 */
	public AnnotationParamType getAnnotationParamType() {
		return annotationParamType;
	}

	/**
	 * @param annotationParamType the annotationParamType to set
	 */
	public void setAnnotationParamType(AnnotationParamType annotationParamType) {
		this.annotationParamType = annotationParamType;
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

	@Override
	public String toString() {
		return "AnnotationParam [id=" + id + ", annotation.name=" + annotation.getName()
				+ ", annotationParamType.name=" + annotationParamType.getName() + ", value="
				+ value + "]";
	}

	/* Note to equals and hashCode.
	 * 
	 * Compare only ID, VALUE to avoid recursive check.
	 */

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
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
		if (getClass() != obj.getClass())
			return false;
		AnnotationParam other = (AnnotationParam) obj;
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
