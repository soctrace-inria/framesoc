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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventProducerQuery;
import fr.inria.soctrace.lib.storage.TraceDBObject;

/**
 * Data structure the help the usage of Event Producer hierarchy.
 * 
 * For a typical usage example see {@link TypicalUsage} source code.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class EPHierarchyDescMap {

	/**
	 * Map of descriptors, accessible by EP id.
	 */
	private Map<Integer, EPHierarchyDesc> descriptors = null;
	
	/**
	 * Tree virtual root, to navigate the hierarchy as a tree.
	 */
	private EPHierarchyDesc root = null;
	
	/**
	 * Constructor
	 */
	public EPHierarchyDescMap() {
		root = new EPHierarchyDesc();
		root.setEventProducer(null);
		root.setRank(-1);
	}
	/**
	 * Get the descriptors map.
	 * Call one of the load methods before.
	 * 
	 * @return the descriptor map
	 * @throws SoCTraceException 
	 */
	public Map<Integer, EPHierarchyDesc> getHierarchyDescMap() throws SoCTraceException {
		if (descriptors == null)
			throw new SoCTraceException("Map not loaded!");
		return descriptors;
	} 
	
	/**
	 * Get the tree root.
	 * Note that this root is NOT an actual event producer,
	 * but a virtual root node (rank == -1)
	 * 
	 * @return the root
	 * @throws SoCTraceException
	 */
	public EPHierarchyDesc getRoot() throws SoCTraceException {
		if (descriptors == null)
			throw new SoCTraceException("Map not loaded!");
		return root;
	}
	
	/**
	 * Load the map with all the producers of the trace.
	 * 
	 * @param traceDB trace DB
	 * @throws SoCTraceException 
	 */
	public void load(TraceDBObject traceDB) throws SoCTraceException {
		EventProducerQuery epq = new EventProducerQuery(traceDB);
		List<EventProducer> eps = epq.getList();
		load(eps);
		epq.clear();
	}
	
	/**
	 * Load the map with the passed producers
	 * 
	 * @param eps producers
	 */
	private void load(List<EventProducer> eps) {
		descriptors = new HashMap<Integer, EPHierarchyDesc>();
		
		// first put in the map, filling only the parent
		for (EventProducer ep: eps) {
			EPHierarchyDesc desc = new EPHierarchyDesc();
			desc.setEventProducer(ep);
			descriptors.put(ep.getId(), desc);			
		}	
		
		// fill the direct sons to have the tree
		for (EventProducer ep: eps) {
			EPHierarchyDesc desc = descriptors.get(ep.getId());
			if (ep.getParentId() == EventProducer.NO_PARENT_ID) {
				root.getDirectSons().add(desc);
			} else {
				EPHierarchyDesc parentDesc = descriptors.get(ep.getParentId());
				parentDesc.getDirectSons().add(desc);					
			}
		}	
		
		setRank(root, -1);
		setDescendants(root);
	}

	/**
	 * Father fist visit to set rank
	 */
	private void setRank(EPHierarchyDesc desc, int rank) {
		desc.setRank(rank);
		for (EPHierarchyDesc d: desc.getDirectSons()) {
			setRank(d, rank+1);
		}
	}
	
	/**
	 * Leaves first visit to set descendants
	 */
	private void setDescendants(EPHierarchyDesc desc) {
		// descendants are: my direct sons...
		desc.getDescendants().addAll(desc.getDirectSons());
		
		for (EPHierarchyDesc d: desc.getDirectSons()) {
			setDescendants(d);
			// ... and my sons' descendants
			desc.getDescendants().addAll(d.getDescendants());
		}
	}
	
	/**
	 * Print the map
	 * @throws SoCTraceException
	 */
	public void print() throws SoCTraceException {
		getRoot().print("");
	}
	
}
