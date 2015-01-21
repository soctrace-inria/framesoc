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
package fr.inria.soctrace.lib.query;


import fr.inria.soctrace.lib.model.AnalysisResultData;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.TraceDBObject;

/**
 * Base abstract class to load an AnalysisResultData object
 * from the DB, given the analysis result ID.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public abstract class AnalysisResultDataQuery {
	
	/**
	 * Flag to enable debug messages printing
	 */
	private boolean debug = false;
	
	/**
	 * Trace DB object
	 */
	protected TraceDBObject traceDB;
	
	/**
	 * The constructor
	 * 
	 * @param traceDB trace DB object containing the analysis result
	 */
	public AnalysisResultDataQuery(TraceDBObject traceDB) {
		this.traceDB = traceDB;
	}
	
	/**
	 * Get the result data.
	 * 
	 * @param analysisResultId analysis result id
	 * @return the result data object (concrete class instance)
	 * @throws SoCTraceException 
	 */
	public abstract AnalysisResultData getAnalysisResultData(int analysisResultId) throws SoCTraceException;
	
	/**
	 * Print a debug message if the debug flag is activated.
	 * 
	 * @param msg debug message
	 */
	protected void debug(String msg) {
		if (debug) 
			System.out.println(msg);
	}
}
