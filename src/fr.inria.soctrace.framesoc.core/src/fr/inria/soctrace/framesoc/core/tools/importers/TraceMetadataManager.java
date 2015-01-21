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
package fr.inria.soctrace.framesoc.core.tools.importers;

import java.util.List;

import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Interface for trace metadata managers.
 * 
 * Concrete classes typically do not implement this interface
 * directly, but extend the abstract skeleton implementation
 * {@link AbstractTraceMetadataManager}.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public interface TraceMetadataManager {
	
	/**
	 * Provide the trace type name
	 * @return the trace type name
	 */
	String getTraceTypeName();
	
	/**
	 * Provide the list of parameter descriptors for this trace
	 * @return a non null list of parameter descriptors
	 */
	List<ParameterDescriptor> getParameterDescriptors();

	/**
	 * Given a trace object, set the predefined fields.
	 * @param trace trace object to fill
	 */
	void setTraceFields(Trace trace);

	/**
	 * Build the complete trace object, with trace type and parameters.
	 * @throws SoCTraceException
	 */
	void createMetadata() throws SoCTraceException;

	/**
	 * Save the computed metadata in the DB.
	 * @throws SoCTraceException
	 */
	void saveMetadata() throws SoCTraceException;
	
}
