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
/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.init;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.core.FramesocManager;
import fr.inria.soctrace.framesoc.core.bus.FramesocBus;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusVariable;
import fr.inria.soctrace.framesoc.core.tools.management.ToolContributionManager;
import fr.inria.soctrace.framesoc.ui.utils.UpdateAssistant;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.ToolQuery;
import fr.inria.soctrace.lib.query.TraceQuery;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.utils.Configuration;
import fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty;
import fr.inria.soctrace.lib.utils.DBMS;
import fr.inria.soctrace.lib.utils.IdManager;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public enum Initializer {

	INSTANCE;

	private final static Logger logger = LoggerFactory.getLogger(Initializer.class);

	public boolean initializeSystem(Shell shell, boolean firstime) {

		boolean done = false;

		try {

			InitWizard wizard = new InitWizard(firstime);
			WizardDialog dialog = new WizardDialog(shell, wizard);
			if (dialog.open() != Window.OK)
				return false;

			done = true;

			InitProperties properties = wizard.getInitProperties();

			// DBMS info
			Configuration.getInstance().set(SoCTraceProperty.soctrace_dbms, properties.getDbms());
			if (properties.getDbms().equalsIgnoreCase(DBMS.SQLITE.toString())) {
				Configuration.getInstance().set(SoCTraceProperty.sqlite_db_directory,
						properties.getSqliteDirectory());
			} else if (properties.getDbms().equalsIgnoreCase(DBMS.MYSQL.toString())) {
				Configuration.getInstance().set(SoCTraceProperty.mysql_base_db_jdbc_url,
						properties.getMysqlUrl());
				Configuration.getInstance().set(SoCTraceProperty.mysql_db_user,
						properties.getMysqlUser());
				Configuration.getInstance().set(SoCTraceProperty.mysql_db_password,
						properties.getMysqlPassword());
			}

			Configuration.getInstance().saveOnFile();

			StringBuilder sb = new StringBuilder();
			sb.append("Configuration saved. ");

			if (FramesocManager.getInstance().isSystemDBExisting()) {
				sb.append("A System DB instance is already existing.");
				manageDatabases();
			} else {
				FramesocManager.getInstance().createSystemDB();
				sb.append("A System DB instance has been created.");
			}

			MessageDialog.openInformation(shell, "SoC-Trace Manager", sb.toString());

			return true;
		} catch (SoCTraceException e) {
			MessageDialog.openError(shell, "Error creating the system DB", e.getMessage());
			return false;
		} finally {
			if (done) {
				// refresh Traces view and Trace Details
				FramesocBus.getInstance().setVariable(
						FramesocBusVariable.TRACE_VIEW_SELECTED_TRACE, null);
				FramesocBus.getInstance().setVariable(
						FramesocBusVariable.TRACE_VIEW_CURRENT_TRACE_SELECTION, null);
				FramesocBus.getInstance().send(FramesocBusTopic.TOPIC_UI_SYSTEM_INITIALIZED, null);
			}
		}
	}

	/**
	 * Manage Framesoc tools registering all the plugin tools in the runtime not yet registered, and
	 * removing all the plugin tools registered but not present in the runtime.
	 */
	public void manageTools(Shell shell) {

		SystemDBObject sysDB = null;
		try {
			sysDB = SystemDBObject.openNewInstance();
			ToolQuery tq = new ToolQuery(sysDB);
			List<Tool> registeredTools = tq.getList();
			List<Tool> runtimeTools = ToolContributionManager.getPluginTools(new IdManager());
			sysDB.close();

			Map<String, Tool> regMap = new HashMap<String, Tool>();
			Map<String, Tool> runMap = new HashMap<String, Tool>();
			for (Tool t : registeredTools)
				regMap.put(t.getName(), t);
			for (Tool t : runtimeTools)
				runMap.put(t.getName(), t);

			// registered no longer present: unregister
			for (Tool t : registeredTools) {
				if (!t.isPlugin())
					continue;
				if (!runMap.containsKey(t.getName())) {
					boolean remove = true;
					boolean ask = Configuration.getInstance()
							.get(SoCTraceProperty.ask_for_tool_removal).equals("true");
					if (ask) {
						remove = MessageDialog.openQuestion(shell, "Missing tool",
								"The tool " + t.getName()
										+ " is no longer present in the current Eclipse runtime.\n"
										+ "Do you want to remove it and all its results (if any)?");
					}
					if (remove) {
						// unregister
						logger.debug("Unregister tool " + t.getName()
								+ " because no longer present");
						FramesocManager.getInstance().removeTool(t);
					}
				}
			}

			// present but unregistered: register
			for (Tool t : runtimeTools) {
				if (!regMap.containsKey(t.getName())) {
					// register
					logger.debug("Register tool " + t.getName());
					FramesocManager.getInstance().registerTool(t.getName(), t.getCommand(),
							t.getType(), t.isPlugin(), t.getDoc(), t.getExtensionId());
				}
			}

		} catch (SoCTraceException e) {
			e.printStackTrace();
		} finally {
			DBObject.finalClose(sysDB);
		}
	}

	/**
	 * Check if the existing system DB is similar and if all the registered
	 * TraceDBs are still existing, removing them if it is not the case.
	 */
	public void manageDatabases() {

		SystemDBObject sysDB = null;
		try {
			sysDB = SystemDBObject.openNewInstance();
			
			// Check that the db version is correct
			UpdateAssistant.checkDB();
				
			TraceQuery tq = new TraceQuery(sysDB);
			List<Trace> registeredTraces = tq.getList();

			// registered no longer present: unregister
			for (Trace t : registeredTraces) {
				if (!DBObject.isDBExisting(t.getDbName())) {
					logger.warn(
							"The trace database {} corresponding to the trace {} is no longer present. "
									+ "The trace will be removed.", t.getDbName(), t.getAlias());
					sysDB.delete(t);
				}
			}
			sysDB.close();

		} catch (SoCTraceException e) {
			e.printStackTrace();
		} finally {
			DBObject.finalClose(sysDB);
		}

	}

}
