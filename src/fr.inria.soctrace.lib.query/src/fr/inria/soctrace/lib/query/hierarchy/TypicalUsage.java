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
package fr.inria.soctrace.lib.query.hierarchy;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.DeltaManager;

/**
 * Example of typical usage of hierarchy classes.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TypicalUsage {

	private static Logger logger = LoggerFactory.getLogger(TypicalUsage.class);
	
	/**
	 * Note: the following code is only an example
	 * @param args
	 * @throws SoCTraceException 
	 */
	public static void main(String[] args) throws SoCTraceException {
		
		DeltaManager dm = new DeltaManager();
		dm.start();
		if (args.length<1)
			throw new IllegalArgumentException("missing db name");
		String dbname = args[0];
		
		// -----------------------------------------------------------------------------------
		
		// 1. open the trace DB
		TraceDBObject traceDB = TraceDBObject.openNewInstance(dbname);
		
		// 2. create the map
		EPHierarchyDescMap hmap = new EPHierarchyDescMap();
		
		// 3. load it
		hmap.load(traceDB);
		
		// 4. play with it
		logger.debug("All");
		hmap.print();
		
		logger.debug("Direct sons of root");
		List<EPHierarchyDesc> descs = hmap.getRoot().getDirectSons();
		for (EPHierarchyDesc desc: descs) {
			logger.debug(desc.toString());
		}
		
		int r = 2;
		logger.debug("Descendants of the first EP of rank " + r);
		for (EPHierarchyDesc desc: hmap.getHierarchyDescMap().values()) {
			if (desc.getRank() == r) {
				logger.debug(desc.toString());
				for (EPHierarchyDesc d: desc.getDescendants()) {
					logger.debug(d.toString());
				}
				break;
			}
		}
		
		// -----------------------------------------------------------------------------------
		
		logger.debug(dm.endMessage("End"));
	}

}
