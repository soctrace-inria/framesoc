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
package fr.inria.soctrace.framesoc.core.tools.management;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;

import fr.inria.soctrace.framesoc.core.tools.model.FramesocTool;
import fr.inria.soctrace.framesoc.core.tools.model.IFramesocToolInput;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.utils.IdManager;

/**
 * Manager class for Framesoc Tool Plugin contribution.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class ToolContributionManager {
	
	// Constants
	private static final String POINT_ID = "fr.inria.soctrace.framesoc.core.tool"; //$NON-NLS-1$
	private static final String EP_TOOL_NAME = "name"; //$NON-NLS-1$
	private static final String EP_TOOL_TYPE = "type"; //$NON-NLS-1$
	private static final String EP_TOOL_DOC = "doc"; //$NON-NLS-1$
	private static final String EP_TOOL_CLASS = "class"; //$NON-NLS-1$
	
	/**
	 * Get a list containing all the new plugin tool, not already present
	 * in the passed list of old tools.
	 * 
	 * @param oldTools old tools
	 * @param reverseIdManager id manager for newly created tools
	 * @return the list of newly created tools (may be empty)
	 */
	public static List<Tool> getNewPluginTools(Collection<Tool> oldTools, IdManager reverseIdManager) {

		// load tools from extension point
		List<Tool> newTools = getPluginTools(reverseIdManager);

		// remove from the list the old tools
		Set<String> oldNames = new HashSet<String>();
		for (Tool t: oldTools) {
			oldNames.add(t.getName());
		}
		Iterator<Tool> iterator = newTools.iterator();
		while(iterator.hasNext()) {
			Tool t = iterator.next();
			if (oldNames.contains(t.getName()))
				iterator.remove();		
		}

		// return the difference		
		return newTools;
	}
	
	/**
	 * Load all the tools from the extension point.
	 * @param reverseIdManager an id manager for newly created tools
	 * @return a list of all plugin tools
	 */
	public static List<Tool> getPluginTools(IdManager reverseIdManager) {
		List<Tool> tools = new LinkedList<Tool>();
		
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IConfigurationElement[] config = reg.getConfigurationElementsFor(POINT_ID);

		for (IConfigurationElement e : config) {
			Tool tmp = new Tool(reverseIdManager.getNextId());
			tmp.setPlugin(true);
			tmp.setCommand("plugin:"+e.getNamespaceIdentifier());
			tmp.setName(e.getAttribute(EP_TOOL_NAME));
			tmp.setType(e.getAttribute(EP_TOOL_TYPE));
			if (e.getAttribute(EP_TOOL_DOC)!=null)
				tmp.setDoc(e.getAttribute(EP_TOOL_DOC));
			tools.add(tmp);
		}
		
		return tools;
	}
			
	/**
	 * Execute a plugin tool. If it is not a plugin tool, nothing happens.
	 *  
	 * WARNING: the tool is executed INSIDE the UI Thread, so if long
	 * operations have to be done, the launch method must create its 
	 * own Job (for example using the predefined class {@link PluginToolJob} 
	 * or a class extending it).
	 * 
	 * @param tool the plugin tool
	 * @param input arguments list
	 */
	public static void executePluginTool(final Tool tool, final IFramesocToolInput input) {
		
		if (!tool.isPlugin())
			return;
		
		ISafeRunnable runnable = new ISafeRunnable() {
			@Override
			public void handleException(Throwable e) {
				System.out.println("Exception in client plugin");
			}
			@Override
			public void run() throws Exception {
				FramesocTool ftool = getToolLauncher(tool);
				if (ftool!=null) 
					ftool.launch(input);
			}
		};
		SafeRunner.run(runnable);
	}	
		
	/**
	 * Get the launcher class for a given plugin tool.
	 * 
	 * @param tool the tool
	 * @return the launcher class, or null if not found or the tool is not a plugin.
	 */
	public static FramesocTool getToolLauncher(Tool tool) {
		
		if (!tool.isPlugin())
			return null;
			
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IConfigurationElement[] config = reg.getConfigurationElementsFor(POINT_ID);
		try {
			for (IConfigurationElement e : config) {
				if (e.getAttribute(EP_TOOL_NAME).equals(tool.getName())) {
					final Object o = e.createExecutableExtension(EP_TOOL_CLASS);
					if (o instanceof FramesocTool) {
						FramesocTool ft = (FramesocTool) o;
						ft.setTool(tool);
						return ft;
					}			
				}
			}
		} catch (CoreException ex) {
			System.out.println(ex.getMessage());
		}
		
		return null;
	}

} 
