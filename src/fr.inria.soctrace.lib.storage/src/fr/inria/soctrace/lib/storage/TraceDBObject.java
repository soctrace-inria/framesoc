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
package fr.inria.soctrace.lib.storage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import fr.inria.soctrace.lib.model.EventParamType;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.utils.ModelElementCache;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;
import fr.inria.soctrace.lib.storage.visitors.TraceDBDeleteVisitor;
import fr.inria.soctrace.lib.storage.visitors.TraceDBSaveVisitor;
import fr.inria.soctrace.lib.storage.visitors.TraceDBUpdateVisitor;

/**
 * Object used to deal with a SoC-Trace Trace DB.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TraceDBObject extends DBObject {
    
	/**
	 * Element format (type and param types) cache
	 */
	private ModelElementCache eventTypeCache = null; 	
	
	/**
	 * The constructor. 
	 */
	public TraceDBObject(String name, DBMode mode)
			throws SoCTraceException {
		super(name, mode);		
	}

	/**
	 * Static builder. Create a new TraceDBObject instance
	 * opening the (existing) database.
	 *  
	 * @return a new trace db object  
	 * @throws SoCTraceException
	 */
	public static TraceDBObject openNewIstance(String dbName) throws SoCTraceException {
		return new TraceDBObject(dbName, DBMode.DB_OPEN);
	}

	@Override
	protected void createDB() throws SoCTraceException {
		
		if ( dbManager.isDBExisting() )
			throw new SoCTraceException("Database "+dbManager.getDBName()+" already present");
		
		// create the DB and the tables
		dbManager.createDB();
		
		dbManager.createTableStatement();
		dbManager.initEvent();
		dbManager.initEventType();
		dbManager.initEventParam();
		dbManager.initEventParamType();
		dbManager.initEventProducer();
		dbManager.initFile();
		dbManager.initAnalysisResult();
		dbManager.initAnnotation();
		dbManager.initAnnotationType();
		dbManager.initAnnotationParam();
		dbManager.initAnnotationParamType();
		dbManager.initGroup();
		dbManager.initGroupMapping();
		dbManager.initSearch();
		dbManager.initSearchMapping();
		dbManager.initProcessedTrace();
		dbManager.closeTableStatement();
	    
		// commit
		commit();
	}

	@Override
	protected void openDB() throws SoCTraceException {
		dbManager.openConnection();
	}
	
	@Override
	protected void cleanCache() {
		if (eventTypeCache != null)
			eventTypeCache.clear();
		eventTypeCache = null;
	}

	/**
	 * Structure cache getter.
	 * The cache is lazy initialized.
	 * 
	 * @return the structure cache
	 * @throws SoCTraceException
	 */
	public ModelElementCache getEventTypeCache() throws SoCTraceException {
		if (eventTypeCache == null) 
			loadEventStructureCache();
		return eventTypeCache;
	}
	
	/**
	 * Load the event structure from the DB.
	 * The DB should so contain a valid structure.
	 */
	private synchronized void loadEventStructureCache() throws SoCTraceException {

		// double check in synchronized environment
		if (eventTypeCache != null) 
			return;
		
		eventTypeCache = new ModelElementCache();
		eventTypeCache.addElementMap(EventType.class);
		eventTypeCache.addElementMap(EventParamType.class);
		
		Statement stm;
		ResultSet rs;
	
		try {
			stm = dbManager.getConnection().createStatement();
			
			// load EventType
			rs = stm.executeQuery("SELECT * FROM " + FramesocTable.EVENT_TYPE);
			while (rs.next()) {
				EventType et = new EventType(rs.getInt("ID"), rs.getInt("CATEGORY"));
				et.setName(rs.getString("NAME"));
				eventTypeCache.put(et);
			}

			// load EventTypeParam
			rs = stm.executeQuery("SELECT * FROM " + FramesocTable.EVENT_PARAM_TYPE);
			while (rs.next()) {
				EventParamType ept = new EventParamType(rs.getInt("ID"));
				ept.setName(rs.getString("NAME"));
				ept.setType(rs.getString("TYPE"));
				ept.setEventType(eventTypeCache.get(EventType.class, rs.getInt("EVENT_TYPE_ID")));
				eventTypeCache.put(ept);
			}			
			stm.close();			
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	@Override
	protected void initializeVisitors() throws SoCTraceException {
		saveVisitor = new TraceDBSaveVisitor(this);
		deleteVisitor = new TraceDBDeleteVisitor(this);
		updateVisitor = new TraceDBUpdateVisitor(this);
	}

	public long getMinPage() throws SoCTraceException {
		return getValue("MIN", "PAGE", FramesocTable.EVENT);
	}
	
	public long getMaxPage() throws SoCTraceException {
		return getValue("MAX", "PAGE", FramesocTable.EVENT);
	}

	public long getMinTimestamp() throws SoCTraceException {
		return getValue("MIN", "TIMESTAMP", FramesocTable.EVENT);
	}
	
	public long getMaxTimestamp() throws SoCTraceException {

		/*
		 * Single query implementation of this request:
		 * 
		 * select max(REALMAX) from
		 * (select
		 *  case
		 *     when TIMESTAMP>=LPAR then TIMESTAMP
		 *     else LPAR
		 *     end
		 *     as REALMAX
		 *  from EVENT where CATEGORY IN (1,2));
		 */

		long maxTs = getValue("MAX", "TIMESTAMP", FramesocTable.EVENT);
		long maxEndTs = getMaxEndTimestamp();
		return Math.max(maxTs, maxEndTs);
	}
	
	/**
	 * Create an index on the timestamp column of EVENT table.
	 * 
	 * @throws SoCTraceException
	 */
	public void createTimestampIndex() throws SoCTraceException {
		dbManager.createIndex(FramesocTable.EVENT.toString(), "TIMESTAMP", "ts_index");
	}

	/**
	 * Drop the index on the timestamp column of EVENT table.
	 * 
	 * @throws SoCTraceException
	 */
	public void dropTimestampIndex() throws SoCTraceException {
		dbManager.dropIndex(FramesocTable.EVENT.toString(), "ts_index");
	}

	private long getValue(String operation, String column, FramesocTable table) throws SoCTraceException {
		Statement stm;
		ResultSet rs;
		long value = 0;
		try {
			stm = dbManager.getConnection().createStatement();
			rs = stm.executeQuery("SELECT "+operation+"("+column+") FROM " + table);
			while (rs.next()) {
				value = rs.getLong(1);
			}
			stm.close();
			return value;
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}
	
	private long getMaxEndTimestamp() throws SoCTraceException {
		Statement stm;
		ResultSet rs;
		long value = 0;
		try {
			stm = dbManager.getConnection().createStatement();
			rs = stm.executeQuery("SELECT MAX(LPAR) FROM EVENT "
					+ "WHERE CATEGORY IN ("+EventCategory.LINK+", "+EventCategory.STATE+")");
			while (rs.next()) {
				value = rs.getLong(1);
			}
			stm.close();
			return value;
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

}
