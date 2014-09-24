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
/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.model;

/**
 * Descriptor to describe the return of a load operation
 * on a trace interval.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class LoadDescriptor {

	/**
	 * Enumeration for load operation result.
	 */
	public enum LoadStatus {
		/** The requested interval was already loaded */
		LOAD_UNCHANGED,
		/** The whole interval has been loaded */
		LOAD_COMPLETE,
		/** Only a part of the interval has been loaded */
		LOAD_PARTIAL,
		/** The load operation has been cancelled */
		LOAD_CANCELLED;
	}
	
	private long actualStartTimestamp;
	private long actualEndTimestamp;
	private String message;
	private LoadStatus status;
	
	/**
	 * @return the actualStartTimestamp
	 */
	public long getActualStartTimestamp() {
		return actualStartTimestamp;
	}
	
	/**
	 * @param actualStartTimestamp the actualStartTimestamp to set
	 */
	public void setActualStartTimestamp(long actualStartTimestamp) {
		this.actualStartTimestamp = actualStartTimestamp;
	}
	
	/**
	 * @return the actualEndTimestamp
	 */
	public long getActualEndTimestamp() {
		return actualEndTimestamp;
	}
	
	/**
	 * @param actualEndTimestamp the actualEndTimestamp to set
	 */
	public void setActualEndTimestamp(long actualEndTimestamp) {
		this.actualEndTimestamp = actualEndTimestamp;
	}
	
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	
	/**
	 * @return the status
	 */
	public LoadStatus getStatus() {
		return status;
	}
	
	/**
	 * @param status the status to set
	 */
	public void setStatus(LoadStatus status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "LoadDescriptor [actualStartTimestamp=" + actualStartTimestamp + ", actualEndTimestamp=" + actualEndTimestamp + ", message=" + message + ", status=" + status + "]";
	}
	
}
