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
package fr.inria.soctrace.lib.query;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;


/**
 * Utility class used to build SQL compliant value lists.
 * Format: ( 1, 2, 3, 4 )
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class ValueListString {
	
	private int size = 0;
	private boolean first = true;
	private boolean quotes = false;
	private StringBuilder buffer = null;
	
	public ValueListString() {
		clear();
	}
	
	public void setQuotes(boolean quotes) {
		this.quotes = quotes;
	}
	
	public void addValue(String value) {
		size++;
		if (!first) {
			buffer.append(", ");
		} else {
			first = false;
		}
		if (quotes) {
			buffer.append("'");
		}
		buffer.append(value);
		if (quotes) {
			buffer.append("'");
		}
	}
	
	public String getValueString() throws SoCTraceException {
		if (size==0)
			throw new SoCTraceException("Error: trying to get an empty list of values!");
		return "( " + buffer.toString() + " )";
	}
	
	public void clear() {
		buffer = null; 
		buffer = new StringBuilder();
		size = 0;
		first = true;
	}
	
	public int size() {
		return size;
	}
}
