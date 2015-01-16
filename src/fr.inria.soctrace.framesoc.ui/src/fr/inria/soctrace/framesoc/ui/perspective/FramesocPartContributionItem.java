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
/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.perspective;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

import fr.inria.soctrace.framesoc.ui.perspective.FramesocPartContributionManager.PartContributionDescriptor;

/**
 * Dynamically creates the Traces view context menu items
 * related to the Framesoc part contribution extension point.
 * 
 * <p>
 * For each contribution, a menu entry is created.
 * Menu entries are sorted by position and, within each position, by priority.
 * Position and priority corresponds to the fields of the Framesoc part
 * extension point.
 * 
 * Note: For a given position, a priority must be unique.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class FramesocPartContributionItem extends CompoundContributionItem {

	@Override
	protected IContributionItem[] getContributionItems() {
		List<PartContributionDescriptor> parts = FramesocPartContributionManager.getInstance().getPartContributionDescriptors();
	    	    
		Map<String, Map<Integer, PartContributionDescriptor>> sortedIds = new HashMap<String, Map<Integer, PartContributionDescriptor>>();
		for (PartContributionDescriptor des: parts) {
			if (!sortedIds.containsKey(des.position)) {
				sortedIds.put(des.position, new TreeMap<Integer, PartContributionDescriptor>());
			}
			sortedIds.get(des.position).put(des.priority, des);
		}
		
		boolean separator = (sortedIds.containsKey(FramesocPerspective.TOP_RIGHT) && sortedIds.containsKey(FramesocPerspective.BOTTOM_RIGHT));
		
	    int i = 0;
	    
		IContributionItem[] list = new IContributionItem[parts.size() + ( separator ? 1 : 0 )];
		
	    if (sortedIds.containsKey(FramesocPerspective.TOP_RIGHT)) {
			for (PartContributionDescriptor des: sortedIds.get(FramesocPerspective.TOP_RIGHT).values()) {
				CommandContributionItemParameter param = new CommandContributionItemParameter(PlatformUI.getWorkbench(), null, des.commandId, CommandContributionItem.STYLE_PUSH);
			    param.icon = des.icon;
			    list[i++] = new CommandContributionItem(param);	
			}
		}
	    
	    if (separator) 
	    	list[i++] = new Separator();
	    
	    if (sortedIds.containsKey(FramesocPerspective.BOTTOM_RIGHT)) {
			for (PartContributionDescriptor des: sortedIds.get(FramesocPerspective.BOTTOM_RIGHT).values()) {
				CommandContributionItemParameter param = new CommandContributionItemParameter(PlatformUI.getWorkbench(), null, des.commandId, CommandContributionItem.STYLE_PUSH);
			    param.icon = des.icon;
			    list[i++] = new CommandContributionItem(param);	
			}
		}

	    return list;
	}

}
