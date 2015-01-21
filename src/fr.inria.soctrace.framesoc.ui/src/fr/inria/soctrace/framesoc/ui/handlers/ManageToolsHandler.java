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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import fr.inria.soctrace.framesoc.core.FramesocManager;
import fr.inria.soctrace.framesoc.ui.dialogs.ManageToolsDialog;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.search.ITraceSearch;
import fr.inria.soctrace.lib.search.TraceSearch;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;

/**
 * Handler for manage tools command.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class ManageToolsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		
		if (!HandlerCommons.checkSystemDB(event))
			return null;
		
		SystemDBObject sysDB = null;
		try {			
			
			Map<Integer, Tool> oldTools = loadTools(window);
			ManageToolsDialog dialog = new ManageToolsDialog(window.getShell(), oldTools);
			if (dialog.open() != Window.OK)
				return null;
			
			// update TOOL table
			sysDB = SystemDBObject.openNewIstance();
			Map<Integer, Tool> newTools = dialog.getNewTools();
			for (Integer id: oldTools.keySet()) {
				if (newTools.containsKey(id)) {
					// updated
					sysDB.update(newTools.get(id));
					newTools.remove(id);
				} else {
					// deleted
					FramesocManager.getInstance().removeTool(oldTools.get(id));
				}
			}	
			
			// commit to avoid conflicts on UNIQUE name when removing, then adding
			// a tool with a given name
			sysDB.commit(); 
			
			// in newTools there are only added tools
			int baseNewId = sysDB.getMaxId(FramesocTable.TOOL.toString(), "ID");
			Iterator<Entry<Integer, Tool>> iterator = newTools.entrySet().iterator();
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
			MessageDialog.openError(window.getShell(), "Error registering the tool", e.getMessage());
		} finally {
			DBObject.finalClose(sysDB);
		}
		return null;
	}
	
    private Map<Integer, Tool> loadTools(IWorkbenchWindow window) {
		Map<Integer, Tool> toolsMap = new HashMap<Integer, Tool>();
		ITraceSearch searchInterface = null;
    	try {
			searchInterface = new TraceSearch().initialize();
			List<Tool> tools = searchInterface.getTools();
			for (Tool t: tools) {
				toolsMap.put(t.getId(), t);
			}
			searchInterface.uninitialize();
		} catch (SoCTraceException e) {
			MessageDialog.openError(window.getShell(), "Exception", e.getMessage());
		} finally {
			TraceSearch.finalUninitialize(searchInterface);
		}
    	return toolsMap;
    }

}
