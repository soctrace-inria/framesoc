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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class representing the ANNOTATION entity of the data model.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class Annotation {
	
	private final int id;
	private AnnotationType annotationType;
	private String name;
	private List<AnnotationParam> params;

	/**
	 * Constructor 
	 * @param id the entity unique id
	 */
	public Annotation(int id) {
		this.id = id;
		params = new ArrayList<AnnotationParam>();
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
	 * @param annotationType the annotationType to set
	 */
	public void setAnnotationType(AnnotationType annotationType) {
		this.annotationType = annotationType;
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
	 * @return the params
	 */
	public List<AnnotationParam> getParams() {
		return params;
	}

	/**
	 * @param params the params to set
	 */
	public void setParams(List<AnnotationParam> params) {
		this.params = params;
	}
	
	/**
	 * This method has protected visibility in order to 
	 * prevent clients to call it. This method should be 
	 * called only by {@link AnnotationParam#setAnnotation()}.
	 * 
	 * @param annotationParam annotation parameter to add
	 */
	protected void addAnnotationParam(AnnotationParam annotationParam) {
		params.add(annotationParam);
	}

	@Override
	public String toString() {
		return "Annotation [id=" + id + ", annotationType.name=" + 
				annotationType.getName() + ", name=" + name + "]";
	}
	
	/**
	 * Get a Map : annotation param name <-> annotation param reference.
	 * The map is built on the fly.
	 * @return the map of annotation parameters
	 */
	public Map<String, AnnotationParam> getParamMap() {
		Map<String, AnnotationParam> map = new HashMap<String, AnnotationParam>();
		for (AnnotationParam param : params) {
			map.put(param.getAnnotationParamType().getName(), param);
		}
		return map;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((annotationType == null) ? 0 : annotationType.hashCode());
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((params == null) ? 0 : params.hashCode());
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
		if (!(obj instanceof Annotation))
			return false;
		Annotation other = (Annotation) obj;
		if (annotationType == null) {
			if (other.annotationType != null)
				return false;
		} else if (!annotationType.equals(other.annotationType))
			return false;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (params == null) {
			if (other.params != null)
				return false;
		} else if (!params.equals(other.params))
			return false;
		return true;
	}
	
}
