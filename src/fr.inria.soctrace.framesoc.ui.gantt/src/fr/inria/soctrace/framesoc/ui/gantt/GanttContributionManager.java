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
package fr.inria.soctrace.framesoc.ui.gantt;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import fr.inria.soctrace.framesoc.ui.gantt.loaders.EventLoader;
import fr.inria.soctrace.framesoc.ui.gantt.loaders.NoCpuEventDrawer;
import fr.inria.soctrace.framesoc.ui.gantt.model.IEventDrawer;
import fr.inria.soctrace.framesoc.ui.gantt.model.IEventLoader;
import fr.inria.soctrace.lib.model.IModelElement;
import fr.inria.soctrace.lib.model.TraceType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.SystemDBObject;

/**
 * Manager class for Framesoc gantt contribution.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class GanttContributionManager {

	// Constants
	private static final String POINT_ID = "fr.inria.soctrace.framesoc.ui.gantt.adapter"; //$NON-NLS-1$
	private static final String TRACE_TYPE_NAME = "traceTypeName"; //$NON-NLS-1$
	private static final String EVENT_LOADER = "eventLoader"; //$NON-NLS-1$
	private static final String EVENT_DRAWER = "eventDrawer"; //$NON-NLS-1$

	private static Map<Integer, IConfigurationElement> extensions = new HashMap<Integer, IConfigurationElement>();

	static {
		SystemDBObject sysDB = null;
		try {
			// get trace types
			sysDB = SystemDBObject.openNewInstance();
			Map<Integer, IModelElement> types = sysDB.getTraceTypeCache().getElementMap(
					TraceType.class);
			Map<String, Integer> name2id = new HashMap<String, Integer>();
			for (IModelElement element : types.values()) {
				TraceType type = (TraceType) element;
				name2id.put(type.getName(), type.getId());
			}
			sysDB.close();

			// link extensions to type id
			IExtensionRegistry reg = Platform.getExtensionRegistry();
			IConfigurationElement[] config = reg.getConfigurationElementsFor(POINT_ID);
			for (IConfigurationElement elem : config) {
				String name = elem.getAttribute(TRACE_TYPE_NAME);
				extensions.put(name2id.get(name), elem);
			}
		} catch (SoCTraceException e1) {
			e1.printStackTrace();
		} finally {
			DBObject.finalClose(sysDB);
		}
	}

	public static IEventLoader getEventLoader(int typeId) {
		if (extensions.containsKey(typeId)) {
			final Object o = getObject(typeId, EVENT_LOADER);
			if (o instanceof IEventLoader) {
				return (IEventLoader) o;
			}
		}
		return new EventLoader();
	}

	public static IEventDrawer getEventDrawer(int typeId) {
		if (extensions.containsKey(typeId)) {
			final Object o = getObject(typeId, EVENT_DRAWER);
			if (o instanceof IEventDrawer) {
				return (IEventDrawer) o;
			}
		}
		return new NoCpuEventDrawer();
	}

	private static Object getObject(int typeId, String field) {
		try {
			if (extensions.get(typeId).getAttribute(field) != null)
				return extensions.get(typeId).createExecutableExtension(field);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

}
