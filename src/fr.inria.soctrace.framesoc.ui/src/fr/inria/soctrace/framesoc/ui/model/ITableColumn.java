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
package fr.inria.soctrace.framesoc.ui.model;

/**
 * Interface for table columns.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public interface ITableColumn {
	
	/**
	 * Header for the column
	 * @return a text to be used as column header
	 */
	public String getHeader();
	
	/**
	 * Column width
	 * @return the column width
	 */
	public int getWidth();
	
}
