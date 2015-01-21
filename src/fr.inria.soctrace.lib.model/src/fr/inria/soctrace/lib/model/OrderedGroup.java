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
package fr.inria.soctrace.lib.model;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Class representing an ordered group of groupable entities,
 * i.e., entities implementing {@link IGroupable}.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class OrderedGroup extends Group {

	/**
	 * Ordered map containing all the son of this group:
	 * the key of the map is the sequence number of the son.
	 */
	private Map<Integer, IGroupable> sons = new TreeMap<Integer, IGroupable>();
	
	/**
	 * Ordered map containing only the references to actual sub-group.
	 */
	private Map<Integer, Group> sonGroups = new TreeMap<Integer, Group>();
	
	/**
	 * Ordered map containing only the references to actual leaf entities.
	 */
	private Map<Integer, LeafMapping> sonLeaves = new TreeMap<Integer, LeafMapping>();

	/**
	 * Constructor.
	 * 
	 * @param id group unique id 
	 * @param targetClass leaves class
	 * @throws SoCTraceException 
	 */
	public OrderedGroup(int id, Class<? extends IGroupable> targetClass)
			throws SoCTraceException {
		super(id, targetClass);
		this.ordered = true;
	}

	/**
	 * @return the sons
	 */
	public Map<Integer, IGroupable> getSons() {
		return sons;
	}
	
	/**
	 * Add a groupable son in a specific position (used by the user).
	 * 
	 * @param son groupable son
	 * @param position position where the son should be put
	 * @throws SoCTraceException 
	 */
	public void addSon(IGroupable son, Integer position) throws SoCTraceException {
		
		if (son instanceof Group) {
			Group g = (Group) son;
			g.setParentId(id);
			g.setSequenceNumber(position.intValue());
			sonGroups.put(position, g);
		}
		else {
			checkElementClass(son);
			sonLeaves.put(position, new LeafMapping(son, -1));
		}
		
		sons.put(position, son);
	}

	/**
	 * Add a groupable son in a specific position (used by the query object).
	 * 
	 * @param son groupable son
	 * @param position position where the son should be put
	 * @param mappingId mapping id
	 * @throws SoCTraceException 
	 */
	public void addSon(IGroupable son, Integer position, Integer mappingId) throws SoCTraceException {
		
		if (son instanceof Group) {
			Group g = (Group) son;
			g.setParentId(id);
			g.setSequenceNumber(position.intValue());
			sonGroups.put(position, g);
		}
		else {
			checkElementClass(son);
			sonLeaves.put(position, new LeafMapping(son, mappingId));
		}
		
		sons.put(position, son);
	}
	
	/**
	 * Get a son in a specific position.
	 * 
	 * @param position position where the son is expected to be
	 * @return the son, or null if not found
	 */
	public IGroupable getSon(Integer position) {
		return sons.get(position);
	}
	
	/**
	 * Get only the sub-groups.
	 * 
	 * @return the sub-groups ordered map.
	 */
	public Map<Integer, Group> getSonGroups() {
		return sonGroups;
	}
	
	/**
	 * Get only the leaves.
	 * 
	 * @return the leaf ordered map.
	 */
	public Map<Integer, LeafMapping> getSonLeaves() {
		return sonLeaves;
	}

	@Override
	protected void checkNode(Map<Integer, Boolean> visited) throws SoCTraceException {
		
		if ( visited.get(this.getId()) != null ) {
			throw new SoCTraceException(structureErrorMessage());
		}
		visited.put(this.getId(), true);

		for (IGroupable s: sons.values()) {
			if (s instanceof Group) {
				Group g = (Group) s;
				g.checkNode(visited);
			}
		}		
	}

	@Override
	protected void realPrint(int indentation) {
		String spaces = "";
		if (indentation > 0) {
			char[] sp = new char[indentation*2];
			Arrays.fill(sp, ' ');
			spaces = new String(sp);
		}
		
		if (sequenceNumber>=0)
			System.out.println(spaces + String.valueOf(sequenceNumber) + "." + this.toString());
		else 
			System.out.println(spaces + "#." + this.toString());
		
	    Iterator<Map.Entry<Integer, IGroupable>> it = 
	    		(Iterator<Map.Entry<Integer, IGroupable>>) sons.entrySet().iterator();
	    
	    while (it.hasNext()) {
			Map.Entry<Integer, IGroupable> pairs = (Map.Entry<Integer, IGroupable>)it.next();
	        IGroupable elem = pairs.getValue();
			if ( elem instanceof Group ) {
				Group g = (Group) elem;
				g.realPrint(indentation+1);
			}
			else {
				System.out.println("  " + spaces + pairs.getKey() + "." + elem.toString());
			}
	    }		
	}

	/* Note to equals and hashCode().
	 * 
	 * Compare only SONS, since SONGROUPS and SONLEAVES
	 * are only convenience replications.
	 */
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((sons == null) ? 0 : sons.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof OrderedGroup))
			return false;
		OrderedGroup other = (OrderedGroup) obj;
		if (sons == null) {
			if (other.sons != null)
				return false;
		} else if (!sons.equals(other.sons))
			return false;
		return true;
	}
	
}
