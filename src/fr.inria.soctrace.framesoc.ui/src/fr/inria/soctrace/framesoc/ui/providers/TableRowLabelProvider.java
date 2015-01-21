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
package fr.inria.soctrace.framesoc.ui.providers;

import org.eclipse.jface.viewers.ColumnLabelProvider;


import fr.inria.soctrace.framesoc.ui.model.ITableColumn;
import fr.inria.soctrace.framesoc.ui.model.ITableRow;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Label provider for ITableRow objects.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TableRowLabelProvider extends ColumnLabelProvider {
	
	protected ITableColumn col;
	
	/**
	 * Constructor
	 * @param col ITableColumn the provider is related to.
	 */
	public TableRowLabelProvider(ITableColumn col) {
		this.col = col;
	}
	
	@Override
	public String getText(Object element) {
		try {
			return ((ITableRow) element).get(col);
		} catch (SoCTraceException e) {
			e.printStackTrace();
		}
		return "";
	}
	
}
