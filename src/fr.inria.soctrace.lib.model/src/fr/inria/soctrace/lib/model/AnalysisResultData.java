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
 * Base class for all analysis result type of data.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class AnalysisResultData {
	
	/**
	 * SoC-Trace Analysis Result Types 
	 */
	public static enum AnalysisResultType {		

		TYPE_SEARCH("SEARCH"),
		TYPE_GROUP("GROUP"),
		TYPE_ANNOTATION("ANNOTATION"),
		TYPE_PROCESSED_TRACE("PROCESSED_TRACE");
				
		private String name;
		
		private AnalysisResultType(String name){
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

	}
	
	/**
	 * AnalysisResult object this data refers to.
	 */
	protected AnalysisResult analysisResult;
	
	/**
	 * Analysis result type: set by concrete classes constructors.
	 */
	protected AnalysisResultType type;
	
	/**
	 * @return the analysis result type
	 */
	public AnalysisResultType getType() {
		return type;
	}

	/**
	 * @return the analysisResult
	 */
	public AnalysisResult getAnalysisResult() {
		return analysisResult;
	}

	/**
	 * @param analysisResult the analysisResult to set
	 */
	public void setAnalysisResult(AnalysisResult analysisResult) {
		this.analysisResult = analysisResult;
	}

	/**
	 * Debug method
	 * @throws SoCTraceException 
	 */
	public abstract void print() throws SoCTraceException;
	
	@Override
	public abstract int hashCode();

	@Override
	public abstract boolean equals(Object obj);

}
