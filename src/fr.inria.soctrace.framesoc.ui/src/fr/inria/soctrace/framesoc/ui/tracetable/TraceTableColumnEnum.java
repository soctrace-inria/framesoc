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
package fr.inria.soctrace.framesoc.ui.tracetable;

import fr.inria.soctrace.framesoc.ui.model.ITableColumn;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public enum TraceTableColumnEnum implements ITableColumn {

	ALIAS("Alias", "Alias", 200),
	TRACING_DATE("Tracing date", "Date", 150),
	TRACED_APPLICATION("Traced application", "Application", 90),
	BOARD("Board", "Board", 90),
	OPERATING_SYSTEM("Operating System", "OS", 90),
	NUMBER_OF_CPUS("Number of CPUs", "CPUs", 60),
	NUMBER_OF_EVENTS("Number of events", "Events", 70),
	OUTPUT_DEVICE("Output device", "Out device", 90),
	DESCRIPTION("Description", "Description", 100),
	MIN_TIMESTAMP("Min Timestamp", "Min Ts", 100),
	MAX_TIMESTAMP("Max Timestamp", "Max Ts", 100),
	TIMEUNIT("Time-unit", "Ts unit", 60),
	DBNAME("DB name", "DB", 100);
	
	private String shortName;
	private String name;
	private int width;
	
	private TraceTableColumnEnum(String name, String shortName, int width){
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
