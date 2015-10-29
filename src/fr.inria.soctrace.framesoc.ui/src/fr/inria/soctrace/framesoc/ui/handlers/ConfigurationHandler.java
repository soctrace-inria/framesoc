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
package fr.inria.soctrace.framesoc.ui.handlers;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import fr.inria.soctrace.framesoc.core.FramesocManager;
import fr.inria.soctrace.framesoc.ui.dialogs.ConfigurationDialog;
import fr.inria.soctrace.framesoc.ui.init.Initializer;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;

/**
 * Handler for configuration command.
 * 
 * @author youenn
 *
 */
public class ConfigurationHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil
				.getActiveWorkbenchWindowChecked(event);

		boolean systemDBOK = true;
		if (!HandlerCommons.checkSystemDB(event))
			systemDBOK = false;

		if (systemDBOK) {
			ConfigurationDialog dialog = new ConfigurationDialog(
					window.getShell());
			SystemDBObject sysDB = null;

			if (dialog.open() != Window.OK)
				return null;

			// update TOOL table
			try {
				sysDB = SystemDBObject.openNewInstance();
				
				Map<Long, Tool> newTools = dialog.getManageToolsComposite()
						.getNewTools();
				Map<Long, Tool> oldTools = dialog.getOldTools();
				for (Long id : oldTools.keySet()) {
					if (newTools.containsKey(id)) {
						// Ignore plugin tools since they can not be modified
						if (!newTools.get(id).isPlugin())
							// updated
							sysDB.update(newTools.get(id));

						newTools.remove(id);
					} else {
						// deleted
						FramesocManager.getInstance().removeTool(
								oldTools.get(id));
					}
				}

				// commit to avoid conflicts on UNIQUE name when removing, then
				// adding a tool with a given name
				sysDB.commit();

				// in newTools there are only added tools
				Long baseNewId = sysDB.getMaxId(FramesocTable.TOOL.toString(),
						"ID");
				Iterator<Entry<Long, Tool>> iterator = newTools.entrySet()
						.iterator();
				while (iterator.hasNext()) {
					Tool tmp = iterator.next().getValue();
					Tool newTool = new Tool(++baseNewId);
					newTool.setCommand(tmp.getCommand());
					newTool.setName(tmp.getName());
					newTool.setType(tmp.getType());
					newTool.setPlugin(tmp.isPlugin());
					newTool.setDoc(tmp.getDoc());
					sysDB.save(newTool);
				}
			} catch (SoCTraceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				DBObject.finalClose(sysDB);
			}
		} else // If the system db is not set
		{
			// Launch the DBMS wizard 
			if (Initializer.INSTANCE.initializeSystem(window.getShell(), false)) {
				Initializer.INSTANCE.manageTools(window.getShell());
			}
		}
		return null;
	}

}
