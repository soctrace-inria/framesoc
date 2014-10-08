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
package fr.inria.soctrace.framesoc.ui.tracetable;

import fr.inria.soctrace.framesoc.ui.model.ITableColumn;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public enum TraceTableColumn implements ITableColumn {

	ALIAS("Alias", "Alias", 150),
	TRACING_DATE("Tracing date", "Date", 100),
	TRACED_APPLICATION("Traced application", "Application", 100),
	BOARD("Board", "Board", 100),
	OPERATING_SYSTEM("Operating System", "OS", 100),
	NUMBER_OF_CPUS("Number of CPUs", "CPUs", 70),
	NUMBER_OF_EVENTS("Number of events", "Events", 100),
	OUTPUT_DEVICE("Output device", "Out device", 100),
	DESCRIPTION("Description", "Description", 100),
	DBNAME("DB name", "DB", 100),
	MIN_TIMESTAMP("Min Timestamp", "Min Ts", 100),
	MAX_TIMESTAMP("Max Timestamp", "Max Ts", 100),
	TIMEUNIT("Time-unit", "Ts unit", 70),
	PARAMS("Custom Parameters", "Parameters", 200);
	
	private String shortName;
	private String name;
	private int width;
	
	private TraceTableColumn(String name, String shortName, int width){
		this.name = name;
		this.shortName = shortName;
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
	
	public String getShortName() {
		return shortName;
	}
}
