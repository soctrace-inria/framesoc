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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Data for an analysis result of type group.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class AnalysisResultGroupData extends AnalysisResultData {

	/**
	 * Root of the groups hierarchy.
	 * Note: even for flat grouping with several groups
	 *     A, B, C
	 * a root is needed, to save the grouping as a hierarchy.
	 * E.g. 
	 *     ROOT_GROUP
	 *         | A, B, C  
	 */
	private Group root;
	
	/**
	 * The constructor. Set the correct type.
	 */
	public AnalysisResultGroupData(Group root) {
		super();
		this.type = AnalysisResultType.TYPE_GROUP;
		this.root = root;
	}

	@Override
	public void print() throws SoCTraceException {
		root.print(0);
	}

	/**
	 * @return the root
	 */
	public Group getRoot() {
		return root;
	}

	/**
	 * @param root the root to set
	 */
	public void setRoot(Group root) {
		this.root = root;
	}
	
	/**
	 * Depth first iterator for the group tree.
	 * Get an instance of a sub-group iterator.
	 * Each time an iterator is requested, the tree consistency
	 * is checked (loop detection).
	 * 
	 * @return the sub-groups iterator
	 * @throws SoCTraceException 
	 */
	public DepthFirstIterator getDepthFirstIterator() throws SoCTraceException {
		this.root.checkTree();
		return new DepthFirstIterator();
	}
	
	/**
	 * It navigates only through the sub groups.
	 * It is used in the save visitor.
	 */
	public class DepthFirstIterator implements Iterator<Group> {

	    private Stack<Group> groupStack = new Stack<Group>();

	    public DepthFirstIterator() {
	        if (AnalysisResultGroupData.this.root != null) {
	        	groupStack.push (AnalysisResultGroupData.this.root);
	        }
	    }

	    public boolean hasNext() {
	        return !groupStack.empty ( );
	    }

	    public Group next () {
	        if (!hasNext()) {
	            return null;
	        }
	        Group node = groupStack.pop();
	        
	        Stack<Group> tmp = new Stack<Group>();
	        if(node instanceof OrderedGroup) {
	        	Map<Integer, Group> groups = ((OrderedGroup) node).getSonGroups();
	        	for (Group g: groups.values()) {
	        		tmp.push(g);
	        	}
	        } else {
	        	List<Group> groups = ((UnorderedGroup) node).getSonGroups();
	        	for (Group g: groups) {
	        		tmp.push(g);
	        	}
	        }
	        int size = tmp.size();
	        for (int i=0; i<size; ++i) {
	        	groupStack.push(tmp.pop());
	        }
	        
	        return node;
	    }

	    public void remove() {
	        throw new UnsupportedOperationException();
	    }
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((root == null) ? 0 : root.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof AnalysisResultGroupData))
			return false;
		AnalysisResultGroupData other = (AnalysisResultGroupData) obj;
		if (root == null) {
			if (other.root != null)
				return false;
		} else if (!root.equals(other.root))
			return false;
		return true;
	}
	
}
