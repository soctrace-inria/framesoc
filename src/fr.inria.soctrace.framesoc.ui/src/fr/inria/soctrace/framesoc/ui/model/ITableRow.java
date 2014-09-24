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

import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Interface for table rows.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public interface ITableRow {

	/**
	 * Add a column to the table row.
	 * @param col column enumerate
	 * @param value column value
	 */
	public void add(ITableColumn col, String value);

	/**
	 * Get the value for the given column.
	 * @param col column enumerate
	 * @return the column value
	 * @throws SoCTraceException 
	 */
	public String get(ITableColumn col) throws SoCTraceException;
	
	/**
	 * Set the value for the given column.
	 * @param col column enumerate
	 * @param value column value
	 */
	public void set(ITableColumn col, String value);

}
