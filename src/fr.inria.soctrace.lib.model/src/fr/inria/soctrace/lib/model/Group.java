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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.model.utils.ModelConstants.ModelEntity;

/**
 * Base class for the GROUP entity of the data model.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public abstract class Group implements IGroupable {

	/**
	 * Utility class to manage mapping id
	 */
	public class LeafMapping {
		private IGroupable son;
		private long mappingId;
		public LeafMapping(IGroupable son, long mappingId) {
			this.son = son;
			this.mappingId = mappingId;
		}
		/**
		 * @return the son
		 */
		public IGroupable getSon() {
			return son;
		}
		/**
		 * @param son the son to set
		 */
		public void setSon(IGroupable son) {
			this.son = son;
		}
		/**
		 * @return the mappingId
		 */
		public long getMappingId() {
			return mappingId;
		}
		/**
		 * @param mappingId the mappingId to set
		 */
		public void setMappingId(long mappingId) {
			this.mappingId = mappingId;
		}
		
		/**
		 * Compare only ID and SON. 
		 * The outer class MUST not be compared.
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			LeafMapping other = (LeafMapping) obj;
			if (mappingId != other.mappingId)
				return false;
			if (son == null) {
				if (other.son != null)
					return false;
			} else if (!son.equals(other.son))
				return false;
			return true;
		}
				
	}
	
	/**
	 * Logical operators used to define the relations
	 * among grouped entities.
	 */
	public enum GROUPING_OPERATOR {
		AND, OR;
	}
	
	/**
	 * Map: groupable class -> entity name
	 */
	protected static Map<Class<? extends IGroupable>, String> groupableClassToName = initClassToNameMap();

	/**
	 * Group ID: it is unique within an AnalysisResultGroupData
	 */
	protected final long id;
	
	/**
	 * ID of the parent of this group (in a hierarchy).
	 */
	protected long parentId;
	
	/**
	 * Group name
	 */
	protected String name;
	
	/**
	 * Leaf entity being grouped (e.g. EVENT, EVENT_TYPE, EVENT_PARAM_TYPE)
	 */
	protected final String targetEntity;
	
	/**
	 * Class corresponding to the leaf entity.
	 */
	protected final Class<? extends IGroupable> targetClass;
	
	/**
	 * Grouping logical operator.
	 */
	protected String groupingOperator;
	
	/**
	 * Flag stating if the sons of this group are ordered or not.
	 */
	protected boolean ordered;
	
	/**
	 * Sequence number of the group, with respect to the (ordered)
	 * parent group.
	 */
	protected int sequenceNumber;
		
	/**
	 * Constructor. Create a group whose direct leaves are objects of the
	 * passed class. If the passed class is null, the group cannot have direct
	 * entity leaves, but only other groups as sons.
	 * By default, the grouping operator is AND, the group name is GROUP_{id}
	 * and the ordered flag is set to false.
	 * The ordered flag is set to the correct value in concrete subclasses 
	 * constructors.
	 * The parent id is automatically set when a sub group is added to a parent
	 * group.
	 * The sequence number is automatically set when a sub group is added to
	 * an ordered parent group.
	 * 
	 * @param id group unique id 
	 * @param targetClass leaves class (if null, the group can only have other groups as sons)
	 * @throws SoCTraceException 
	 */
	public Group(long id, Class<? extends IGroupable> targetClass) throws SoCTraceException {
		super();
		this.id = id;
		this.targetClass = targetClass;
		this.targetEntity = getTargetEntityName(targetClass);
		
		this.groupingOperator = GROUPING_OPERATOR.AND.name();
		this.name = "GROUP_"+id;
		this.ordered = false;
		this.parentId = -1;
		this.sequenceNumber = -1;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}
	
	/**
	 * @return the parentId
	 */
	public long getParentId() {
		return parentId;
	}
	
	/**
	 * @param parentId the parentId to set
	 */
	public void setParentId(long parentId) {
		this.parentId = parentId;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the groupingOperator
	 */
	public String getGroupingOperator() {
		return groupingOperator;
	}
	
	/**
	 * @param groupingOperator the groupingOperator to set
	 */
	public void setGroupingOperator(String groupingOperator) {
		this.groupingOperator = groupingOperator;
	}
	
	/**
	 * @return the ordered
	 */
	public boolean isOrdered() {
		return ordered;
	}
	
	/**
	 * @return the sequenceNumber
	 */
	public int getSequenceNumber() {
		return sequenceNumber;
	}
	
	/**
	 * @param sequenceNumber the sequenceNumber to set
	 */
	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
	
	/**
	 * @return the targetEntity
	 */
	public String getTargetEntity() {
		return targetEntity;
	}
	
	/**
	 * @return the targetClass (may be null if the group cannot have direct entity leaves)
	 */
	public Class<? extends IGroupable> getTargetClass() {
		return targetClass;
	}
	
	/*
	 *     T r e e     c o n s i s t e n c y
	 */
	
	/**
	 * Check for loop in the tree.
	 * 
	 * @throws SoCTraceException
	 */
	public void checkTree() throws SoCTraceException {
		Map<Long, Boolean> visited = new HashMap<Long, Boolean>();
		checkNode(visited);
	}
	
	/**
	 * Concrete loop finding function: recursively explores the tree
	 * looking for loops.
	 * 
	 * @param visited Map of visited nodes.
	 * @throws SoCTraceException
	 */
	protected abstract void checkNode(Map<Long, Boolean> visited) throws SoCTraceException;
	
	/**
	 * Structure error message function.
	 * 
	 * @return the structure error message.
	 */
	protected String structureErrorMessage() {
		return "Structural error detected in the tree!! " +
			   "More than one path lead to group '" + this.getName() + "' " +
			   "(id:" + this.getId() + ").";
	}

	/*
	 *     O u t p u t 
	 */
	
	/**
	 * Debug method. Print the group.
	 * 
	 * @param indentation number of indentation spaces
	 * @throws SoCTraceException 
	 */
	public void print(int indentation) throws SoCTraceException {
		checkTree();
		realPrint(indentation);
	}

	/**
	 * Concrete printing function: recursively explores the tree, printing it.
	 * 
	 * @param indentation number of indentation spaces
	 */
	protected abstract void realPrint(int indentation);
	
	@Override
	public String toString() {
		return "Group [id=" + id + ", parentId=" + parentId + ", name=" + name
				+ ", targetEntity=" + targetEntity + ", targetClass="
				+ targetClass + ", groupingOperator=" + groupingOperator
				+ ", ordered=" + ordered + ", sequenceNumber=" + sequenceNumber
				+ "]";
	}

	/*
	 *     E n t i t y     c l a s s     m a n a g e m e n t 
	 */
	
	/**
	 * Initialize the groupable-object classes-names map.
	 * 
	 * @return a constant map
	 */
    private static Map<Class<? extends IGroupable>, String> initClassToNameMap() {
        Map<Class<? extends IGroupable>, String> result = new HashMap<Class<? extends IGroupable>, String>();
        result.put(Event.class, ModelEntity.EVENT.name());
        result.put(EventType.class, ModelEntity.EVENT_TYPE.name());
        result.put(EventParamType.class, ModelEntity.EVENT_PARAM_TYPE.name());
        return Collections.unmodifiableMap(result);
    }

    /**
     * Check the target class and return the corresponding entity name.
     * 
     * @param targetClass target class
     * @return the corresponding entity name
     * @throws SoCTraceException
     */
    private String getTargetEntityName(Class<? extends IGroupable> targetClass) 
    		throws SoCTraceException {
    	if (targetClass == null)
    		return ModelEntity.NO_ENTITY.name();
    	String name = groupableClassToName.get(targetClass);
    	if (name == null)
    		throw new SoCTraceException("Target class ("+targetClass.toString()+") not found among the groupable ones");
    	return name;
    }

    /**
     * Check that the group has actually a target class and that 
     * the passed element class is the same as the target class.
     * 
     * @param e groupable element
     * @throws SoCTraceException
     */
    protected void checkElementClass(IGroupable e) throws SoCTraceException {
    	if (targetClass == null)
			throw new SoCTraceException("Error. Trying to add a leaf entity to a " +
					"group without target entity");
    	// for events the condition is more complex since there may be categorized events
    	if ((targetClass.equals(Event.class) && (e instanceof Event)))
    		return;
		if (!e.getClass().equals(targetClass)) {
			throw new SoCTraceException("Illegal element. " +
					"The element class ("+e.getClass().toString()+") " +
					"differs from the target one ("+targetClass.toString()+")");
		}
		
    }

    /*
     * Note to equals and hashCode.
     * 
     * Generated: targetClass not considered, since Class does not
	 * implement equals and hashCode. Moreover there is already
	 * targetEntity.
     */
    
    /* (non-Javadoc) 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((groupingOperator == null) ? 0 : groupingOperator.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (ordered ? 1231 : 1237);
		result = prime * result + (int) (parentId ^ (parentId >>> 32));
		result = prime * result + sequenceNumber;
		result = prime * result + ((targetEntity == null) ? 0 : targetEntity.hashCode());
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
		if (!(obj instanceof Group))
			return false;
		Group other = (Group) obj;
		if (groupingOperator == null) {
			if (other.groupingOperator != null)
				return false;
		} else if (!groupingOperator.equals(other.groupingOperator))
			return false;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (ordered != other.ordered)
			return false;
		if (parentId != other.parentId)
			return false;
		if (sequenceNumber != other.sequenceNumber)
			return false;
		if (targetEntity == null) {
			if (other.targetEntity != null)
				return false;
		} else if (!targetEntity.equals(other.targetEntity))
			return false;
		return true;
	}
    
}
