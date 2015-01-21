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

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.EventProducer;

/**
 * Hierarchy descriptor for an event producer (EP).
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class EPHierarchyDesc {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(EPHierarchyDesc.class);

	/**
	 * EP object
	 */
	private EventProducer ep;
	
	/**
	 * EP rank in the hierarchy
	 */
	private int rank;
	
	/**
	 * EP direct sons
	 */
	private List<EPHierarchyDesc> directSons = new LinkedList<EPHierarchyDesc>();
	
	/**
	 * EP descendants (contains direct sons)
	 */
	private List<EPHierarchyDesc> descendants = new LinkedList<EPHierarchyDesc>();
	
	/**
	 * @return the rank
	 */
	public int getRank() {
		return rank;
	}
	
	/**
	 * @param rank the rank to set
	 */
	public void setRank(int rank) {
		this.rank = rank;
	}
	
	/**
	 * @return the event producer
	 */
	public EventProducer getEventProducer() {
		return ep;
	}
	
	/**
	 * @param ep the event producer to set
	 */
	public void setEventProducer(EventProducer ep) {
		this.ep = ep;
	}
	
	/**
	 * @return the directSons
	 */
	public List<EPHierarchyDesc> getDirectSons() {
		return directSons;
	}
	
	/**
	 * @return the descendants
	 */
	public List<EPHierarchyDesc> getDescendants() {
		return descendants;
	}
	
	/**
	 * Print the node and the tree following.
	 */
	public void print(String start) {
		logger.debug(start + this.toString());
		for (EPHierarchyDesc desc: directSons) {
			desc.print(start + " ");
		}
	}

	@Override
	public String toString() {
		return "EPHierarchyDesc [ep=" + getEpString() + ", rank=" + rank + ", directSons=" + directSons.size() + ", descendants=" + descendants.size() + "]";
	}
	
	private String getEpString() {
		return ((ep==null)?"RootDesc":ep.getName());
	}
	
}
