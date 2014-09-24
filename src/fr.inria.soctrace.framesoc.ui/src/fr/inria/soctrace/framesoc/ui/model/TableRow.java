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
package fr.inria.soctrace.framesoc.ui.model;

import java.util.HashMap;
import java.util.Map;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Row of a table, storing a map Column - Value.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class TableRow implements ITableRow {
	
	/**
	 * Map: column - value
	 */
	protected Map<ITableColumn, String> fields = new HashMap<ITableColumn, String>();
	
	@Override
	public void add(ITableColumn col, String value) {
		fields.put(col, value);
	}
	
	@Override
	public String get(ITableColumn col) throws SoCTraceException {
		if (!fields.containsKey(col))
			throw new SoCTraceException("Column " + col.toString() + " not found in table row " + this.toString());
		return fields.get(col);
	}
	
	@Override
	public void set(ITableColumn col, String value) {
		fields.put(col, value);
	}
	
}
