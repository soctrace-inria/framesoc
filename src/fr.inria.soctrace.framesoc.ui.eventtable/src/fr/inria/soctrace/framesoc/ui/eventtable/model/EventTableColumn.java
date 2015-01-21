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
package fr.inria.soctrace.framesoc.ui.eventtable.model;

import fr.inria.soctrace.framesoc.ui.model.ITableColumn;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public enum EventTableColumn implements ITableColumn {

	TIMESTAMP("Timestamp", 150),
	CPU("CPU", 100), 
	PRODUCER_NAME("Event Producer", 200),
	CATEGORY("Category", 120), 
	TYPE_NAME("Event Type", 200),
	PARAMS("Parameters", 400);
	
	private String name;
	private int width;
	
	private EventTableColumn(String name, int width){
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
