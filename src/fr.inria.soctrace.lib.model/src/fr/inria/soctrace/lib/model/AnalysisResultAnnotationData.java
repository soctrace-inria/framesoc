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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Data for an analysis result of type annotation.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class AnalysisResultAnnotationData extends AnalysisResultData {

	private List<Annotation> annotations;
	private Map<Long, AnnotationType> annotationTypes;
	
	/**
	 * The constructor. Set the correct type.
	 */
	public AnalysisResultAnnotationData() {
		super();
		this.type = AnalysisResultType.TYPE_ANNOTATION;
		this.annotations = new LinkedList<Annotation>();
		this.annotationTypes = new HashMap<>();
	}

	@Override
	public void print() {
		System.out.println("AnnotationTypes");
		for (AnnotationType a: annotationTypes.values()) {
			System.out.println(a.toString());
			for (AnnotationParamType p: a.getParamTypes()) {
				System.out.println("  " + p.toString());
			}
		}
		System.out.println("Annotations");
		for (Annotation a: annotations) {
			System.out.println(a.toString());
			for (AnnotationParam p: a.getParams()) {
				System.out.println("  " + p.toString());
			}
		}
		System.out.println("");
	}
	
	/**
	 * @param annotation the annotation to add
	 */
	public void addAnnotation(Annotation annotation) {
		annotations.add(annotation);
		long typeId = annotation.getAnnotationType().getId();
		if ( ! annotationTypes.containsKey(typeId) ) {
			annotationTypes.put(Long.valueOf(typeId), annotation.getAnnotationType());
		}
	}
	
	/**
	 * @return a collection containing all the different annotation types
	 */
	public Collection<AnnotationType> getAnnotationTypes() {
		return annotationTypes.values();
	}

	/**
	 * @return the annotations
	 */
	public List<Annotation> getAnnotations() {
		return annotations;
	}

	/**
	 * @param annotations the annotations to set
	 */
	public void setAnnotations(List<Annotation> annotations) {
		this.annotations = annotations;
		this.annotationTypes.clear();
		for (Annotation a: annotations) {
			long typeId = a.getAnnotationType().getId();
			if ( ! annotationTypes.containsKey(typeId) ) {
				annotationTypes.put(Long.valueOf(typeId), a.getAnnotationType());
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((annotationTypes == null) ? 0 : annotationTypes.hashCode());
		result = prime * result + ((annotations == null) ? 0 : annotations.hashCode());
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
		if (!(obj instanceof AnalysisResultAnnotationData))
			return false;
		AnalysisResultAnnotationData other = (AnalysisResultAnnotationData) obj;
		if (annotationTypes == null) {
			if (other.annotationTypes != null)
				return false;
		} else if (!annotationTypes.equals(other.annotationTypes))
			return false;
		if (annotations == null) {
			if (other.annotations != null)
				return false;
		} else if (!annotations.equals(other.annotations))
			return false;
		return true;
	}

}
