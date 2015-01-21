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
package fr.inria.soctrace.test.junit.lib.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import fr.inria.soctrace.lib.model.AnalysisResult;
import fr.inria.soctrace.lib.model.AnalysisResultAnnotationData;
import fr.inria.soctrace.lib.model.Annotation;
import fr.inria.soctrace.lib.model.AnnotationParam;
import fr.inria.soctrace.lib.utils.IdManager;
import fr.inria.soctrace.test.junit.utils.IModelFactory;

public class AnnotationTest {

	@Test
	public void testEqualsAndHashCode() {
		Annotation a1 = getNewAnnotation();
		Annotation a2 = getNewAnnotation();
		assertTrue(a1.equals(a2));
		assertTrue(a2.equals(a1));
		assertTrue(a1.hashCode() == a2.hashCode());
	}

	@Test
	public void testSetAnnotationType() {
		Annotation base = getNewAnnotation();
		Annotation a = new Annotation(10);
		a.setAnnotationType(base.getAnnotationType());
		assertEquals(base.getAnnotationType(), a.getAnnotationType());
	}

	@Test
	public void testGetParams() {
		Annotation a1 = getNewAnnotation();
		Annotation a2 = getNewAnnotation();
		assertEquals(a1.getParams(), a2.getParams());
	}

	@Test
	public void testGetParamMap() {
		Annotation a1 = getNewAnnotation();
		Annotation a2 = getNewAnnotation();
		Map<String, AnnotationParam> epm = a1.getParamMap();
		List<AnnotationParam> epl = a2.getParams();
		for (AnnotationParam ep: epl) {
			assertTrue(epm.containsKey(ep.getAnnotationParamType().getName()));
			assertEquals(ep, epm.get(ep.getAnnotationParamType().getName()));
		}		
	}
	
	// all the annotation returned by this method are equals
	private Annotation getNewAnnotation() {
		AnalysisResult ar = IModelFactory.INSTANCE.createAnnotationResult(new IdManager(), "desc");
		AnalysisResultAnnotationData data = (AnalysisResultAnnotationData)ar.getData();
		if (!data.getAnnotations().isEmpty())
			return data.getAnnotations().get(0);
		return null;
	}

}
