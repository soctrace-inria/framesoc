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
package fr.inria.soctrace.framesoc.core;

import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.core.FramesocConstants.FramesocToolType;
import fr.inria.soctrace.framesoc.core.bus.FramesocBus;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.core.tools.management.ExternalImporterExecutionManager;
import fr.inria.soctrace.framesoc.core.tools.management.ExternalToolExecutionManager;
import fr.inria.soctrace.framesoc.core.tools.management.ToolContributionManager;
import fr.inria.soctrace.framesoc.core.tools.model.IFramesocToolInput;
import fr.inria.soctrace.lib.model.AnalysisResult;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.TraceParam;
import fr.inria.soctrace.lib.model.TraceParamType;
import fr.inria.soctrace.lib.model.TraceType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.AnalysisResultQuery;
import fr.inria.soctrace.lib.query.ToolQuery;
import fr.inria.soctrace.lib.query.TraceParamTypeQuery;
import fr.inria.soctrace.lib.query.TraceQuery;
import fr.inria.soctrace.lib.query.TraceTypeQuery;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;
import fr.inria.soctrace.lib.utils.Configuration;
import fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty;
import fr.inria.soctrace.lib.utils.IdManager;

/**
 * Singleton to access management functionalities.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public final class FramesocManager {

	/**
	 * Single instance of the manager
	 */
	private static FramesocManager instance = null;

	/**
	 * Logger
	 */
	private static Logger logger = LoggerFactory.getLogger(FramesocManager.class);

	/**
	 * Instance getter
	 * 
	 * @return the manager instance
	 */
	public static FramesocManager getInstance() {
		if (instance == null)
			instance = new FramesocManager();
		return instance;
	}

	/*
	 * Shortcuts
	 */

	private final String sysDbName = Configuration.getInstance().get(
			SoCTraceProperty.soctrace_db_name);

	/*
	 * Management methods
	 */

	/**
	 * Create the SoC-Trace System DB
	 * 
	 * @throws SoCTraceException
	 *             if the System DB already exists or if the check for its existence produces a DBMS
	 *             dependent exception.
	 */
	public void createSystemDB() throws SoCTraceException {
		if (isSystemDBExisting()) {
			throw new SoCTraceException("System DB (" + sysDbName + ") already present");
		}
		new SystemDBObject(sysDbName, DBMode.DB_CREATE).close();
		logger.debug("SystemDB created");
	}

	/**
	 * Register a new tool to the System DB. The System DB must exist.
	 * 
	 * @param toolName
	 *            tool name
	 * @param toolCommand
	 *            tool command
	 * @param toolType
	 *            tool type
	 * @param isPlugin
	 *            boolean stating whether the tool is a plugin or not
	 * @param doc
	 *            documentation text to be presented to the user
	 * @param extensionId
	 *            tool extension id
	 * @throws SoCTraceException
	 */
	public void registerTool(String toolName, String toolCommand, String toolType,
			boolean isPlugin, String doc, String extensionId) throws SoCTraceException {

		if (toolName.equals("") || toolCommand.equals(""))
			throw new SoCTraceException("Wrong parameters: ( '" + toolName + "', '" + toolCommand
					+ "' )");

		SystemDBObject db = getSystemDB();
		int id = db.getNewId(FramesocTable.TOOL.toString(), "ID");
		Tool tool = new Tool(id);
		tool.setName(toolName);
		tool.setCommand(toolCommand);
		tool.setType(toolType);
		tool.setPlugin(isPlugin);
		tool.setDoc(doc);
		tool.setExtensionId(extensionId);

		db.save(tool);
		db.commit();
		db.close();
		logger.debug("Tool " + tool.getName() + " registered");
	}

	/**
	 * Remove a tool from the system. If there are analysis results produced by this tool, they are
	 * removed too.
	 * 
	 * @param tool
	 *            the tool to remove
	 * @throws SoCTraceException
	 */
	public void removeTool(Tool tool) throws SoCTraceException {

		SystemDBObject sysDB = null;

		try {
			sysDB = getSystemDB();

			// remove results
			TraceQuery tq = new TraceQuery(sysDB);
			List<Trace> traces = tq.getList();
			for (Trace t : traces) {
				TraceDBObject traceDB = null;
				try {
					traceDB = TraceDBObject.openNewInstance(t.getDbName());
					AnalysisResultQuery arq = new AnalysisResultQuery(traceDB);
					arq.setElementWhere(new SimpleCondition("TOOL_ID", ComparisonOperation.EQ,
							String.valueOf(tool.getId())));
					List<AnalysisResult> arl = arq.getList();
					for (AnalysisResult ar : arl) {
						traceDB.delete(ar);
					}
				} catch (SoCTraceException e) {
					e.printStackTrace();
				} finally {
					DBObject.finalClose(traceDB);
				}
			}

			// remove the tool
			sysDB.delete(tool);

		} finally {
			DBObject.finalClose(sysDB);
		}
	}

	/**
	 * Launch a tool as if using a simple command line interface: $ toolCommand arg1 arg2 ...
	 * 
	 * @param tool
	 *            tool to launch
	 * @param args
	 *            arguments
	 * @throws SoCTraceException
	 */
	public void launchTool(Tool tool, IFramesocToolInput input) throws SoCTraceException {

		// plugin tools
		if (tool.isPlugin()) {
			logger.debug("Launcing plugin " + tool.getName());
			ToolContributionManager.executePluginTool(tool, input);
			return;
		}

		// external tools
		if (tool.getType().equals(FramesocToolType.IMPORT.toString()))
			new ExternalImporterExecutionManager(tool.getName(), input.getCommand()).execute();
		else
			new ExternalToolExecutionManager(tool.getName(), input.getCommand()).execute();
	}

	/**
	 * Remove the trace from the system, removing its metadata from the SystemDB and erasing the
	 * TraceDB.
	 * 
	 * @param trace
	 *            trace to remove
	 * @throws SoCTraceException
	 */
	public void deleteTrace(Trace trace) throws SoCTraceException {
		SystemDBObject sysDB = null;

		try {
			sysDB = getSystemDB();

			// check if last trace of this type
			TraceQuery tq = new TraceQuery(sysDB);
			tq.setElementWhere(new SimpleCondition("TRACE_TYPE_ID", ComparisonOperation.EQ, String
					.valueOf(trace.getType().getId())));
			List<Trace> tl = tq.getList();
			if (tl.size() == 1) {
				sysDB.delete(trace.getType());
				for (TraceParamType tpt : trace.getType().getTraceParamTypes()) {
					sysDB.delete(tpt);
				}
			}
			tq.clear();

			sysDB.delete(trace);
			for (TraceParam tp : trace.getParams()) {
				sysDB.delete(tp);
			}

			sysDB.close();

			// Check if the trace was already deleted on disk or in the DB ?
			if (DBObject.isDBExisting(trace.getDbName())) {
				// delete the trace db
				TraceDBObject traceDB = TraceDBObject.openNewInstance(trace
						.getDbName());
				traceDB.dropDatabase();
			}
			
			// notify the bus
			FramesocBus.getInstance().send(FramesocBusTopic.TOPIC_UI_SYNCH_TRACES_NEEDED, true);

			logger.debug("Trace " + trace.getAlias() + " deleted");

		} finally {
			DBObject.finalClose(sysDB);
		}

		// delete the garbage after closing the connection with the system DB
		// to have the DB up to date
		deleteGarbageTraceType();
	}

	/**
	 * Create the Trace DB name with the predefined format: <BASE_NAME>_<yyyyMMdd_HHmmss_z>
	 * 
	 * @param baseName
	 *            the base name for the database
	 * @return The standard DB name, computed from a base name
	 */
	public String getTraceDBName(String baseName) {

		// if empty string, use default name
		if (baseName.equals("")) {
			baseName = "TRACE_DB";
		}

		// remove space from name and replace with '_'
		baseName = baseName.replaceAll("\\s+", "_");
		// remove - from name and replace with '_'
		baseName = baseName.replaceAll("-", "_");
		// remove . from name and replace with '_'
		baseName = baseName.replaceAll("\\.", "_");
		// if first character is a digit, add a prefix
		if (Character.isDigit(baseName.charAt(0)))
			baseName = "DB_" + baseName;

		// get current date
		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.setTimeZone(SimpleTimeZone.getDefault());
		sdf.applyPattern("yyyyMMdd_HHmmss_z");
		String date = sdf.format(new Date()).toString();

		return baseName + "_" + date;
	}

	/*
	 * Convenience methods to check DB existence
	 */

	/**
	 * Check whether the System DB is existing or not.
	 * 
	 * @return true if the System DB exists, false otherwise
	 * @throws SoCTraceException
	 */
	public boolean isSystemDBExisting() throws SoCTraceException {
		return DBObject.isDBExisting(sysDbName);
	}
	
	/**
	 * Check whether the System DB are correct
	 * 
	 * @return true if the settings are correct, false otherwise
	 * @throws SoCTraceException
	 */
	public boolean isSystemDBParameterCorrect() throws SoCTraceException {
		return DBObject.checkSettings(sysDbName);
	}

	/**
	 * Get a new System DB object. WARNING: The client of this method MUST close the connection.
	 * 
	 * @return the System DB object
	 * @throws SoCTraceException
	 *             if the System DB does not exist or there is an error while checking its existence
	 *             or creating it.
	 */
	public SystemDBObject getSystemDB() throws SoCTraceException {

		if (!isSystemDBExisting()) {
			throw new SoCTraceException("System DB (" + sysDbName + ") not existing");
		}

		return new SystemDBObject(sysDbName, DBMode.DB_OPEN);
	}

	/**
	 * Check whether a given DB is existing or not.
	 * 
	 * @param dbName
	 *            DB name
	 * @return true if the DB exists, false otherwise
	 * @throws SoCTraceException
	 */
	public boolean isDBExisting(String dbName) throws SoCTraceException {
		return DBObject.isDBExisting(dbName);
	}

	/**
	 * Get the tool object from the DB given the tool name. If no tool is found, null is returned.
	 * 
	 * @param name
	 *            tool name
	 * @return the tool, or null if not found
	 * @throws SoCTraceException
	 */
	public Tool getTool(String name) throws SoCTraceException {

		SystemDBObject sysDB = getSystemDB();
		ToolQuery tq = new ToolQuery(sysDB);
		tq.setElementWhere(new SimpleCondition("NAME", ComparisonOperation.EQ, name));
		List<Tool> tl = tq.getList();
		Tool ret = null;
		for (Tool t : tl) {
			ret = t;
			break;
		}
		sysDB.close();
		return ret;
	}

	/*
	 * Private methods
	 */

	/**
	 * Private constructor. Prevents instantiation.
	 */
	private FramesocManager() {
	}

	/**
	 * Add the new trace param to the passed traces.
	 * 
	 * If the param type is not present, the type is created and saved along with the new param. If
	 * the param type is already present and there is no param for such type, the param is saved for
	 * that type. If the param type is already present and there is already a param for such type,
	 * the new param is not saved.
	 * 
	 * @param traces
	 *            traces where the new param should be added
	 * @param name
	 *            new param name
	 * @param type
	 *            new param type name
	 * @param value
	 *            new param value
	 * @throws SoCTraceException
	 */
	public void saveParam(List<Trace> traces, String name, String type, String value)
			throws SoCTraceException {

		SystemDBObject sysDB = null;

		try {
			sysDB = getSystemDB();

			// trace type id -> (trace param type name -> trace param type)
			Map<Long, Map<String, TraceParamType>> typeMap = new HashMap<>();

			// init all trace types
			TraceTypeQuery ttQuery = new TraceTypeQuery(sysDB);
			List<TraceType> ttList = ttQuery.getList();
			for (TraceType tt : ttList) {
				typeMap.put(tt.getId(), new HashMap<String, TraceParamType>());
			}

			// init all trace param types
			TraceParamTypeQuery query = new TraceParamTypeQuery(sysDB);
			List<TraceParamType> types = query.getList();
			for (TraceParamType tpt : types) {
				typeMap.get(tpt.getTraceType().getId()).put(tpt.getName(), tpt);
			}

			IdManager tptId = new IdManager();
			IdManager tpId = new IdManager();
			tptId.setNextId(1 + sysDB.getMaxId(FramesocTable.TRACE_PARAM_TYPE.toString(), "ID"));
			tpId.setNextId(1 + sysDB.getMaxId(FramesocTable.TRACE_PARAM.toString(), "ID"));

			for (Trace t : traces) {
				Map<String, TraceParamType> tptMap = typeMap.get(t.getType().getId());
				if (!tptMap.containsKey(name)) {
					// the trace param type is not present for this trace type: create it
					TraceParamType tpt = new TraceParamType(tptId.getNextId());
					tpt.setName(name);
					tpt.setType(type);
					tpt.setTraceType(t.getType());
					tptMap.put(name, tpt);
					sysDB.save(tpt);
				}
				if (!t.getParamMap().containsKey(name)) {
					// the trace param is not present for this trace param type: create it
					TraceParam tp = new TraceParam(tpId.getNextId());
					tp.setTrace(t);
					tp.setValue(value);
					tp.setTraceParamType(tptMap.get(name));
					sysDB.save(tp);
				}
			}

			sysDB.close();

		} finally {
			DBObject.finalClose(sysDB);
		}

	}

	/**
	 * Delete the trace params from the passed traces.
	 * 
	 * @param traces
	 *            traces where the params should be removed
	 * @param params
	 *            params to remove
	 * @throws SoCTraceException
	 */
	public void deleteParams(List<Trace> traces, List<String> params) throws SoCTraceException {

		SystemDBObject sysDB = null;

		try {
			sysDB = getSystemDB();

			for (Trace t : traces) {
				Map<String, TraceParam> paramMap = t.getParamMap();
				for (String p : params) {
					if (paramMap.containsKey(p)) {
						TraceParam tp = paramMap.get(p);
						paramMap.remove(p);
						t.getParams().remove(tp);
						sysDB.delete(tp);
					}
				}
			}

			sysDB.close();

		} finally {
			DBObject.finalClose(sysDB);
		}

		// delete the garbage after closing the connection with the system DB
		// to have the DB up to date
		deleteGarbageTraceParamType();

	};

	/**
	 * Remove all trace type for which there is no corresponding trace
	 * 
	 * @throws SoCTraceException
	 */
	private void deleteGarbageTraceType() throws SoCTraceException {
		SystemDBObject sysDB = null;
		try {
			sysDB = getSystemDB();
			Statement stm = sysDB.getConnection().createStatement();
			stm.execute("DELETE FROM " + FramesocTable.TRACE_TYPE.toString()
					+ " WHERE ID NOT IN (SELECT DISTINCT TRACE_TYPE_ID FROM "
					+ FramesocTable.TRACE.toString() + ")");
			stm.close();
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		} finally {
			DBObject.finalClose(sysDB);
		}
	}

	/**
	 * Remove all trace param types for which there is no corresponding trace param.
	 * 
	 * @throws SoCTraceException
	 */
	private void deleteGarbageTraceParamType() throws SoCTraceException {
		SystemDBObject sysDB = null;
		try {
			sysDB = getSystemDB();
			Statement stm = sysDB.getConnection().createStatement();
			stm.execute("DELETE FROM " + FramesocTable.TRACE_PARAM_TYPE.toString()
					+ " WHERE ID NOT IN (SELECT DISTINCT TRACE_PARAM_TYPE_ID FROM "
					+ FramesocTable.TRACE_PARAM.toString() + ")");
			stm.close();
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		} finally {
			DBObject.finalClose(sysDB);
		}
	}

}
