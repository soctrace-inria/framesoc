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
package fr.inria.soctrace.framesoc.ui.perspective;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.wb.swt.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager class for Framesoc Part contributions.
 * It has only package visibility.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
class FramesocPartContributionManager {

	private final static Logger logger = LoggerFactory.getLogger(FramesocPartContributionManager.class);

	// Constants
	private static final String POINT_ID = "fr.inria.soctrace.framesoc.ui.perspective.part"; //$NON-NLS-1$
	private static final String VIEW_ID = "viewId"; //$NON-NLS-1$
	private static final String ICON = "icon"; //$NON-NLS-1$
	private static final String COMMAND = "launchCommand"; //$NON-NLS-1$
	private static final String POSITION = "position"; //$NON-NLS-1$
	private static final String PRIORITY = "priority"; //$NON-NLS-1$
	private static final String SHOW_DEFAULT = "show_default"; //$NON-NLS-1$

	private List<PartContributionDescriptor> parts = new LinkedList<PartContributionDescriptor>();

	private final static FramesocPartContributionManager instance = new FramesocPartContributionManager();

	private FramesocPartContributionManager() {
		// read all the extensions for our point from the registry
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IConfigurationElement[] config = reg.getConfigurationElementsFor(POINT_ID);

		// for each extension create a PartContributionDescriptor
		for (IConfigurationElement elem : config) {
			PartContributionDescriptor des = new PartContributionDescriptor();
			des.id = elem.getAttribute(VIEW_ID);
			if (!viewExists(des.id))
				continue;
			des.commandId = elem.getAttribute(COMMAND);				
			if (!commandExists(des.commandId)) 
				continue;
			String iconPath = elem.getAttribute(ICON);
			des.icon = ResourceManager.getPluginImageDescriptor(elem.getContributor().getName(), iconPath);
			des.position = elem.getAttribute(POSITION);
			try {
				des.priority = Integer.valueOf(elem.getAttribute(PRIORITY));
			} catch (NumberFormatException e) {
				logger.debug(e.getMessage());
				e.printStackTrace();
				des.priority = Integer.MAX_VALUE;
			}
			des.showDefault = Boolean.valueOf(elem.getAttribute(SHOW_DEFAULT));
			logger.debug("descriptor: {}", des);
			parts.add(des);
		}		
	}

	/**
	 * Verify that the view with this ID actually exists.
	 * 
	 * @param id view ID
	 * @return true if the view exits, false otherwise
	 */
	private boolean viewExists(String id) {
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IConfigurationElement[] config = reg.getConfigurationElementsFor("org.eclipse.ui.views");
		for (IConfigurationElement elem : config) {
			if (elem.getAttribute("id").equals(id)) {
				return true;
				
				// XXX We don't check that that's actually a FramesocPart
				// since sometimes there are CoreException even for 
				// FramesocPart instances. To be investigated.
//				try {
//					Object o = elem.createExecutableExtension("class");
//					if (o instanceof FramesocPart)
//						return true;
//					logger.debug(o.getClass().getName() + " is not a FramesocPart.");
//					return false;
//				} catch (CoreException e) {
//					e.printStackTrace();
//					return false;
//				}
				
			}
		}
		logger.debug("View corresponding to ID '" + id + "' not found in current runtime.");
		return false;
	}

	/**
	 * Check if a command exists.
	 * @param id command id
	 * @return true if the command exists, false otherwise
	 */
	private boolean commandExists(String id) {
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IConfigurationElement[] config = reg.getConfigurationElementsFor("org.eclipse.ui.commands");
		for (IConfigurationElement elem : config) {
			if (elem.getAttribute("id").equals(id)) {
				return true;
			}
		}
		logger.debug("Command corresponding to ID '" + id + "' not found in current runtime.");
		return false;
	}

	/**
	 * Get the singleton instance
	 * @return the singleton instance
	 */
	public static FramesocPartContributionManager getInstance() {
		return instance;
	}

	/**
	 * Return the list of all part contribution descriptors.
	 * This list contains only the descriptors for the existing
	 * views extending the FramesocPart.
	 *  
	 * @return the complete list of part contribution descriptors
	 */
	public List<PartContributionDescriptor> getPartContributionDescriptors() {
		return parts;
	}

	/**
	 * Utility class for storing part contribution meta information.
	 */
	protected class PartContributionDescriptor {
		public String id;
		public ImageDescriptor icon;
		public String commandId;
		public String position;
		public int priority;
		public boolean showDefault;
		@Override
		public String toString() {
			return "PartContributionDescriptor [id=" + id + ", icon=" + icon + ", commandId="
					+ commandId + ", position=" + position + ", priority=" + priority
					+ ", showDefault=" + showDefault + "]";
		}		
	}

} 
