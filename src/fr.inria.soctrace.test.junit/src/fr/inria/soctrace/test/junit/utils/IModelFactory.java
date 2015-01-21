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
/**
 * 
 */
package fr.inria.soctrace.test.junit.utils;

import java.util.List;

import fr.inria.soctrace.lib.model.AnalysisResult;
import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.utils.IdManager;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public interface IModelFactory {

	/**
	 * Create the only instance from the package private class
	 * ModelFactory.
	 */
	public static IModelFactory INSTANCE = new ModelFactory();

	/**
	 * Create a complete event of the given category
	 * @param category event category
	 * @return an event of the given category
	 */
	Event createEvent(int category);

	/**
	 * Create a complete event
	 * @return an event
	 */
	Event createEvent();

	/**
	 * Create a complete event
	 * @param eId event id manager
	 * @param epId event param id manager
	 * @return an event
	 */
	Event createEvent(IdManager eId, IdManager epId);

	/**
	 * Create a list of complete events of the same type and producer
	 * @param n number of events
	 * @return a list of events
	 */
	List<Event> createEvents(int n);
	
	/**
	 * Create a list of complete events of the same type and producer
	 * of different categories.
	 * @param n number of events for each category
	 * @return a list of events
	 */
	List<Event> createCategorizedEvents(int n);

	/**
	 * Create a complete trace
	 * @return a trace
	 */
	Trace createTrace();

	/**
	 * Create a complete trace
	 * @param tId trace id manager
	 * @param tpId trace param id manager
	 * @return a trace
	 */
	Trace createTrace(IdManager tId, IdManager tpId);

	/**
	 * Create a list of complete traces of the same type
	 * @param n number of traces
	 * @return a list of traces
	 */
	List<Trace> createTraces(int n);
	
	/**
	 * Create a new analysis Tool
	 */
	Tool createAnalysisTool();

	/**
	 * Create a trace analysis result using the two traces
	 * registered in the system db: virtual and junit dummy.
	 */
	AnalysisResult createTraceResult(IdManager aid, String desc);

	/**
	 * Create a search analysis result
	 */
	AnalysisResult createSearchResult(IdManager aid, String desc);
	
	/**
	 * Create a group analysis result
	 */
	AnalysisResult createGroupResult(IdManager aid, String desc);
	
	/**
	 * Create an annotation analysis result
	 */
	AnalysisResult createAnnotationResult(IdManager aid, String desc);
	
}
