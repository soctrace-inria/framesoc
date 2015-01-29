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
package fr.inria.soctrace.framesoc.ui.gantt.model;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Reduced event class.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class ReducedEvent {
	
	// SQL constants
	public final static String SELECT_COLUMNS = " CPU, CATEGORY, TIMESTAMP, LPAR, EVENT_TYPE_ID, EVENT_PRODUCER_ID, DPAR ";
	
	public static final int CPU = 1;
	public static final int CATEGORY = 2;
	public static final int TIMESTAMP = 3;
	public static final int END_TIMESTAMP = 4;
	public static final int TYPE_ID = 5;
	public static final int PRODUCER_ID = 6;
	public static final int END_PRODUCER_ID = 7;
	
	// public fields
	public int cpu;
	public int category;
	public long timestamp;
	public long endTimestamp;
	public long typeId;
	public long producerId;
	public long endProducerId;
	
	/**
	 * Builds a reduced event from the result set element obtained from a 
	 * query done using the <code>SELECT_COLUMNS</code> query string.
	 *  
	 * @param res result set element
	 * @throws SQLException 
	 */
	public ReducedEvent(ResultSet res) throws SQLException {
		cpu = res.getInt(ReducedEvent.CPU);
		category = res.getInt(ReducedEvent.CATEGORY);
		timestamp = res.getLong(ReducedEvent.TIMESTAMP);
		endTimestamp =res.getLong(ReducedEvent.END_TIMESTAMP);
		typeId =res.getLong(ReducedEvent.TYPE_ID);
		producerId = res.getLong(ReducedEvent.PRODUCER_ID);
		endProducerId = res.getLong(ReducedEvent.END_PRODUCER_ID); 
	}

	@Override
	public String toString() {
		return "ReducedEvent [cpu=" + cpu + ", category=" + category + ", timestamp=" + timestamp
				+ ", endTimestamp=" + endTimestamp + ", typeId=" + typeId + ", producerId="
				+ producerId + ", endProducerId=" + endProducerId + "]";
	}

}
