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
package fr.inria.soctrace.framesoc.ui.piechart.model;

import fr.inria.soctrace.framesoc.ui.model.ITableColumn;

/**
 * Column of the table beside the pie chart.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public enum StatisticsTableColumn implements ITableColumn {

	NAME("Name", 250),
	PERCENTAGE("Percentage", 120),
	OCCURRENCES("Occurrences", 120);
	
	private String name;
	private int width;
	
	private StatisticsTableColumn(String name, int width){
		this.name = name;
		this.width = width;
	}

	@Override
	public String getHeader() {
		return name;
	}

	@Override
	public int getWidth() {
		return width;
	}
}
