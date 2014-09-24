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
 * Data for an analysis result of type processed trace.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class AnalysisResultProcessedTraceData extends AnalysisResultData {
	
	private Trace sourceTrace = null;
	private Trace processedTrace = null;

	/**
	 * The constructor. Set the correct type.
	 */
	public AnalysisResultProcessedTraceData() {
		super();
		this.type = AnalysisResultType.TYPE_PROCESSED_TRACE;
	}
	
	/**
	 * @return the sourceTrace
	 */
	public Trace getSourceTrace() {
		return sourceTrace;
	}

	/**
	 * @param sourceTrace the sourceTrace to set
	 */
	public void setSourceTrace(Trace sourceTrace) {
		this.sourceTrace = sourceTrace;
	}

	/**
	 * @return the processedTrace
	 */
	public Trace getProcessedTrace() {
		return processedTrace;
	}

	/**
	 * @param processedTrace the processedTrace to set
	 */
	public void setProcessedTrace(Trace processedTrace) {
		this.processedTrace = processedTrace;
	}

	@Override
	public void print() throws SoCTraceException {
		System.out.println("Source trace: ");
		System.out.println(sourceTrace.toString());
		System.out.println("Processed trace: ");
		System.out.println(processedTrace.toString());		
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((processedTrace == null) ? 0 : processedTrace.hashCode());
		result = prime * result + ((sourceTrace == null) ? 0 : sourceTrace.hashCode());
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
		if (!(obj instanceof AnalysisResultProcessedTraceData))
			return false;
		AnalysisResultProcessedTraceData other = (AnalysisResultProcessedTraceData) obj;
		if (processedTrace == null) {
			if (other.processedTrace != null)
				return false;
		} else if (!processedTrace.equals(other.processedTrace))
			return false;
		if (sourceTrace == null) {
			if (other.sourceTrace != null)
				return false;
		} else if (!sourceTrace.equals(other.sourceTrace))
			return false;
		return true;
	}

}
