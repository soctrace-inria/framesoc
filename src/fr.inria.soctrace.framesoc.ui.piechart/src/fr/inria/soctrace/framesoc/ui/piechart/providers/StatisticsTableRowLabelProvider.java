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
package fr.inria.soctrace.framesoc.ui.piechart.providers;

import org.eclipse.swt.graphics.Color;

import fr.inria.soctrace.framesoc.ui.model.ITableColumn;
import fr.inria.soctrace.framesoc.ui.model.ITableRow;
import fr.inria.soctrace.framesoc.ui.piechart.model.StatisticsTableRow;
import fr.inria.soctrace.framesoc.ui.providers.SquareIconLabelProvider;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Label provider for StatisticsTableRow objects.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class StatisticsTableRowLabelProvider extends SquareIconLabelProvider {

	/**
	 * Managed column
	 */
	protected ITableColumn col;

	/**
	 * Constructor
	 * 
	 * @param col
	 *            ITableColumn the provider is related to.
	 */
	public StatisticsTableRowLabelProvider(ITableColumn col) {
		super();
		this.col = col;
	}

	@Override
	protected String getText(Object element) {
		String text = "";
		try {
			text = ((ITableRow) element).get(col);
		} catch (SoCTraceException e) {
			e.printStackTrace();
		}
		return text;
	}

	@Override
	protected Color getColor(Object element) {
		if (element instanceof StatisticsTableRow) {
			StatisticsTableRow row = (StatisticsTableRow) element;
			return row.getColor();
		}
		return null;
	}

}
