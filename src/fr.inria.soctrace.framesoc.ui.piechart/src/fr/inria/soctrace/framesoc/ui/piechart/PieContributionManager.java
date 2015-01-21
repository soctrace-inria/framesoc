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
package fr.inria.soctrace.framesoc.ui.piechart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import fr.inria.soctrace.framesoc.ui.piechart.model.IPieChartLoader;

/**
 * Manager class for Framesoc pie chart loader contribution.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class PieContributionManager {

	// Constants
	private static final String POINT_ID = "fr.inria.soctrace.framesoc.ui.piechart.loader"; //$NON-NLS-1$
	private static final String LOADER_CLASS = "loaderClass"; //$NON-NLS-1$

	/**
	 * Get a list of loader instances, sorted by name.
	 * 
	 * @return a list of pie chart loader instances
	 */
	public static List<IPieChartLoader> getLoaders() {

		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IConfigurationElement[] config = reg.getConfigurationElementsFor(POINT_ID);
		List<IPieChartLoader> loaders = new ArrayList<>(config.length);

		for (IConfigurationElement elem : config) {
			IPieChartLoader loader = (IPieChartLoader) getObject(elem, LOADER_CLASS);
			if (loader != null) {
				loaders.add(loader);
			}
		}
		
		Collections.sort(loaders, new Comparator<IPieChartLoader>() {
			@Override
			public int compare(IPieChartLoader o1, IPieChartLoader o2) {
				return o1.getStatName().compareTo(o2.getStatName());
			}
		});
		
		return loaders;
	}

	private static Object getObject(IConfigurationElement elem, String field) {
		try {
			if (elem.getAttribute(field) != null)
				return elem.createExecutableExtension(field);				
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

}
