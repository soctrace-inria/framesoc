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
package fr.inria.soctrace.lib.model.utils;

/**
 * Exception class to be used in the SoC-Trace Infrastructure.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class SoCTraceException extends Exception {

	private static final long serialVersionUID = 1L;

	public SoCTraceException() {
		super();
	}

	public SoCTraceException(String message, Throwable cause) {
		super(message, cause);
	}

	public SoCTraceException(String message) {
		super(message);
	}

	public SoCTraceException(Throwable cause) {
		super(cause);
	}
	
}
