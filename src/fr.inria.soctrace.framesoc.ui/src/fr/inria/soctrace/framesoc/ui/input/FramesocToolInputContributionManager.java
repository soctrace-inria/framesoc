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
package fr.inria.soctrace.framesoc.ui.input;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * Manager class for Framesoc Tool Input contributions. It has only package visibility.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class FramesocToolInputContributionManager {

	// Constants
	private static final String POINT_ID = "fr.inria.soctrace.framesoc.ui.input.toolInput"; //$NON-NLS-1$
	private static final String EP_TOOL_ID = "toolId"; //$NON-NLS-1$
	private static final String EP_COMPOSITE_FACTORY = "compositeFactory"; //$NON-NLS-1$

	/**
	 * Get the custom tool composites.
	 * 
	 * @return a map between tool extension ids and composites.
	 */
	public static Map<String, AbstractToolInputCompositeFactory> getToolInputComposites() {

		Map<String, AbstractToolInputCompositeFactory> map = new HashMap<>();
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IConfigurationElement[] config = reg.getConfigurationElementsFor(POINT_ID);
		try {
			for (IConfigurationElement e : config) {
				AbstractToolInputCompositeFactory factory = null;
				Object o = e.createExecutableExtension(EP_COMPOSITE_FACTORY);
				if (o instanceof AbstractToolInputCompositeFactory) {
					factory = (AbstractToolInputCompositeFactory) o;
				}
				if (factory != null) {
					map.put(e.getAttribute(EP_TOOL_ID), factory);
				}
			}
		} catch (CoreException ex) {
			System.out.println(ex.getMessage());
		}

		return map;
	}

}
