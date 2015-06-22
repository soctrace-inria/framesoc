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
package fr.inria.soctrace.lib.storage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import fr.inria.soctrace.lib.model.TraceParamType;
import fr.inria.soctrace.lib.model.TraceType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.utils.ModelElementCache;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;
import fr.inria.soctrace.lib.storage.visitors.SystemDBDeleteVisitor;
import fr.inria.soctrace.lib.storage.visitors.SystemDBSaveVisitor;
import fr.inria.soctrace.lib.storage.visitors.SystemDBUpdateVisitor;
import fr.inria.soctrace.lib.utils.Configuration;
import fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty;

/**
 * Object used to deal with a SoC-Trace System DB.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class SystemDBObject extends DBObject {
	
	/**
	 * Trace format (type and param types) cache
	 */
	private ModelElementCache traceTypeCache = null;
	
	/**
	 * The constructor. 
	 */
	public SystemDBObject(String name, DBMode mode)
			throws SoCTraceException {
		super(name, mode);
	}
	
	/**
	 * Static builder. Create a new SystemDBObject instance
	 * opening the (existing) database.
	 *  
	 * @return a new system db object  
	 * @throws SoCTraceException
	 */
	public static SystemDBObject openNewInstance() throws SoCTraceException { // TODO: check this name
		return new SystemDBObject(Configuration.getInstance().get(SoCTraceProperty.soctrace_db_name), 
				DBMode.DB_OPEN);
	}

	@Override
	protected void createDB() throws SoCTraceException {	

		if (dbManager.isDBExisting())
			throw new SoCTraceException("Database " + dbManager.getDBName()
					+ " already present");

		// create the DB and the tables
		dbManager.createDB();
		
		dbManager.createTableStatement();
		dbManager.initTrace();
		dbManager.initTraceType();
		dbManager.initTraceParam();
		dbManager.initTraceParamType();
		dbManager.initTool();
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
		if (traceTypeCache != null)
			traceTypeCache.clear();
		traceTypeCache = null;
	}
	
	/**
	 * Check if a given trace type format (TraceType, TraceParamType(s))
	 * is already stored in the DB.
	 * Note that the type attribute of TRACE_TYPE table is UNIQUE.
	 * 
	 * @param type trace type 
	 * @return true if the type is already present, false otherwise
	 */
	public synchronized boolean isTraceTypePresent(String type) throws SoCTraceException {
		try {
			Statement stm = dbManager.getConnection().createStatement();
			ResultSet rs = stm.executeQuery("SELECT * FROM " + FramesocTable.TRACE_TYPE + " WHERE NAME='"+type+"'");
			if ( rs.next() ) {
				stm.close();
			    return true;
			}
			stm.close();
			return false;
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	/**
	 * Return a TraceType object corresponding to a given type.
	 * @param type trace type name
	 * @return the TraceType object or null if not found
	 */
	public TraceType getTraceType(String type) throws SoCTraceException {
		TraceType traceType = null;
		try {
			Statement stm = dbManager.getConnection().createStatement();
			ResultSet rs = stm.executeQuery("SELECT ID FROM " + FramesocTable.TRACE_TYPE + " WHERE NAME='"+type+"'");
			if (rs.next()) {
				int id = rs.getInt("ID");
				traceType = getTraceTypeCache().get(TraceType.class, id);
			}
			stm.close();
			return traceType;
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}		
	}
	
	/**
	 * Types cache getter.
	 * The cache is lazy initialized.
	 * 
	 * @return the types cache
	 * @throws SoCTraceException
	 */
	public ModelElementCache getTraceTypeCache() throws SoCTraceException {
		if (traceTypeCache == null) 
			loadTraceTypeCache();
		return traceTypeCache;
	}

	/**
	 * Load the trace types from the DB.
	 */
	private synchronized void loadTraceTypeCache() throws SoCTraceException {

		// double check in synchronized environment
		if (traceTypeCache != null)
			return;
		
		traceTypeCache = new ModelElementCache();
		traceTypeCache.addElementMap(TraceType.class);
		traceTypeCache.addElementMap(TraceParamType.class);
		
		Statement stm;
		ResultSet rs;
	
		try {
			stm = dbManager.getConnection().createStatement();
			
			// load TraceType
			rs = stm.executeQuery("SELECT * FROM " + FramesocTable.TRACE_TYPE);
			while (rs.next()) {
				TraceType tt = new TraceType(rs.getInt("ID"));
				tt.setName(rs.getString("NAME"));
				traceTypeCache.put(tt);
			}

			// load TraceParamType
			rs = stm.executeQuery("SELECT * FROM " + FramesocTable.TRACE_PARAM_TYPE);
			while (rs.next()) {
				TraceParamType tpt = new TraceParamType(rs.getInt("ID"));
				tpt.setName(rs.getString("NAME"));
				tpt.setType(rs.getString("TYPE"));
				tpt.setTraceType(traceTypeCache.get(TraceType.class, rs.getInt("TRACE_TYPE_ID")));
				traceTypeCache.put(tpt);
			}
			stm.close();		
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	@Override
	protected void initializeVisitors() throws SoCTraceException {
		saveVisitor = new SystemDBSaveVisitor(this);
		deleteVisitor = new SystemDBDeleteVisitor(this);
		updateVisitor = new SystemDBUpdateVisitor(this);
	}
		
}
