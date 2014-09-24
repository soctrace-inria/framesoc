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
package fr.inria.soctrace.lib.model;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Class representing an unordered group of groupable entities,
 * i.e., entities implementing {@link IGroupable}.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class UnorderedGroup extends Group {

	/**
	 * List containing all the sons.
	 */
	private List<IGroupable> sons = new LinkedList<IGroupable>();
	
	/**
	 * List containing only the references to actual sub-group.
	 */
	private List<Group> sonGroups = new LinkedList<Group>();
	
	/**
	 * List containing only the references to actual leaf entities.
	 */
	private List<LeafMapping> sonLeaves = new LinkedList<LeafMapping>();

	/**
	 * Constructor.
	 * 
	 * @param id group unique id 
	 * @param targetClass leaves class
	 * @throws SoCTraceException 
	 */
	public UnorderedGroup(int id, Class<? extends IGroupable> targetClass)
			throws SoCTraceException {
		super(id, targetClass);
		this.ordered = false;
	}

	/**
	 * @return the sons
	 */
	public List<IGroupable> getSons() {
		return sons;
	}
	
	/**
	 * Add a groupable son (used by the user).
	 * 
	 * @param son groupable son to add
	 * @throws SoCTraceException 
	 */
	public void addSon(IGroupable son) throws SoCTraceException {
		
		if (son instanceof Group) {
			// set parent id and add to sub groups
			Group g = (Group) son;
			g.setParentId(id);
			sonGroups.add(g);
		} else {
			// check target class and add to leaves
			checkElementClass(son);
			sonLeaves.add(new LeafMapping(son, -1));
		}
		
		sons.add(son);
	}
	
	/**
	 * Add a groupable son (used by the query object)
	 * 
	 * @param son groupable son to add
	 * @throws SoCTraceException 
	 */
	public void addSon(IGroupable son, int mappingId) throws SoCTraceException {
		
		if (son instanceof Group) {
			// set parent id and add to sub groups
			Group g = (Group) son;
			g.setParentId(id);
			sonGroups.add(g);
		} else {
			// check target class and add to leaves
			checkElementClass(son);
			sonLeaves.add(new LeafMapping(son, mappingId));
		}
		
		sons.add(son);
	}

	
	/**
	 * Get only the sub-groups.
	 * 
	 * @return the sub-groups ordered map.
	 */
	public List<Group> getSonGroups() {
		return sonGroups;
	}
	
	/**
	 * Get only the leaves.
	 * 
	 * @return the leaf ordered map.
	 */
	public List<LeafMapping> getSonLeaves() {
		return sonLeaves;
	}	
		
	@Override
	protected void checkNode(Map<Integer, Boolean> visited) throws SoCTraceException {

		if ( visited.get(this.getId()) != null ) {
			throw new SoCTraceException(structureErrorMessage());
		}
		visited.put(this.getId(), true);

		for (IGroupable s: sons) {
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
		
		for (IGroupable s: sons) {
			if (s instanceof Group) {
				Group g = (Group) s;
				g.realPrint(indentation+1);
			}
			else {
				System.out.println("  " + spaces + "#." + s.toString());
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
		if (!(obj instanceof UnorderedGroup))
			return false;
		UnorderedGroup other = (UnorderedGroup) obj;
		if (sons == null) {
			if (other.sons != null)
				return false;
		} else if (!sons.equals(other.sons))
			return false;
		return true;
	}

}
