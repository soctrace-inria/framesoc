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

import fr.inria.soctrace.lib.model.utils.SoCTraceException;


/**
 * Interface for all model visitors.
 *  
 * This interface contains all the visit method for all the data model entities,
 * as defined in {@link fr.inria.soctrace.lib.model.utils.ModelConstants.ModelEntity}.
 * This way the object of the model do not need to know where they are saved.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public interface IModelVisitor {

	void executeBatches() throws SoCTraceException;
	
	void clearBatches() throws SoCTraceException;
	
	void close() throws SoCTraceException;
	
	void visit(Trace trace) throws SoCTraceException;

	void visit(TraceType traceType) throws SoCTraceException;

	void visit(TraceParam traceParam) throws SoCTraceException;

	void visit(TraceParamType traceParamType) throws SoCTraceException;
	
	void visit(Tool tool) throws SoCTraceException;

	void visit(Event event) throws SoCTraceException;

	void visit(EventParam eventParam) throws SoCTraceException;

	void visit(EventType eventType) throws SoCTraceException;

	void visit(EventParamType eventParamType) throws SoCTraceException;

	void visit(EventProducer eventProducer) throws SoCTraceException;

	void visit(File file) throws SoCTraceException;
	
	void visit(AnalysisResult analysisResult) throws SoCTraceException;

}
