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
package fr.inria.soctrace.framesoc.ui.toolbar;

import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;

import fr.inria.soctrace.framesoc.core.FramesocConstants.FramesocToolType;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.search.ITraceSearch;
import fr.inria.soctrace.lib.search.TraceSearch;

/**
 * Base abstract class for extension contribution factories
 * creating drop down menu commands to launch tools.
 * 
 * <p>
 * The tools of a given type are dynamically loaded from the 
 * System DB.
 * 
 * <p>
 * Concrete classes override the createContributionItems() method of the 
 * ExtensionContributionFactory, passing the correct {@link FramesocToolType} 
 * to the createToolsItems() method of this class.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class AbstractMenuContribution extends ExtensionContributionFactory {

	/**
	 * Given a tool type, create a menu contribution for each tool of this type.
	 * Such contribution is added to the passed contribution root.
	 * @param serviceLocator the service locator
	 * @param additions the contribution root
	 * @param type the Framesoc tool type
	 */
	@SuppressWarnings("unchecked")
	protected void createToolsItems(IServiceLocator serviceLocator, IContributionRoot additions, FramesocToolType type) {
		List<Tool> tools = getTools(type);		
		for (Tool tool: tools) {
			CommandContributionItemParameter p = new CommandContributionItemParameter(
					serviceLocator, "", "fr.inria.soctrace.framesoc.ui.commands.dropdown", SWT.PUSH);
			p.parameters = new HashMap<String, String>();
			p.parameters.put("fr.inria.soctrace.framesoc.ui.commands.dropdown.toolName", tool.getName());
	        p.label = tool.getName();
			CommandContributionItem item = new CommandContributionItem(p);
			item.setVisible(true);
			additions.addContributionItem(item, null);			
		}		
	}
	
	/**
	 * Get the list of registered tool of a given type.
	 * @param type Framesoc tool type
	 * @return a list of tool of a given type
	 */
	private List<Tool> getTools(FramesocToolType type) {
		ITraceSearch traceSearch = null;
		try {
			traceSearch = new TraceSearch().initialize();
			List<Tool> tools = traceSearch.getToolByType(type.toString());
			traceSearch.uninitialize();
			return tools;
		} catch (SoCTraceException e) {
			e.printStackTrace();
		} finally {
			TraceSearch.finalUninitialize(traceSearch);
		}
		return null;
	}

}
