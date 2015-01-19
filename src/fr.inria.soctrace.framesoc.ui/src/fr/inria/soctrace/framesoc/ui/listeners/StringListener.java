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
package fr.inria.soctrace.framesoc.ui.listeners;

/**
 * Base class for listeners managing a string.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class StringListener {

	protected String text;
	
	public StringListener(String initialValue) {
		text = initialValue;
	}
	
	public String getText() {
		return text;
	}
	
}
