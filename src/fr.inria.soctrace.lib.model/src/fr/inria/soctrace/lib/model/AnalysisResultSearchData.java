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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.model.utils.ModelConstants.ModelEntity;

/**
 * Data for an analysis result of type search.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class AnalysisResultSearchData extends AnalysisResultData {

	/**
	 * Map: searchable class -> entity name
	 */
	private static Map<Class<? extends ISearchable>, String> searchableClassToName = initClassToNameMap();

	private final String targetEntity;
	private final Class<? extends ISearchable> targetClass;
	private String searchCommand;
	private List<ISearchable> elements;
	
	/**
	 * The constructor. Set the correct type.
	 * @throws SoCTraceException 
	 */
	public AnalysisResultSearchData(Class<? extends ISearchable> targetClass) 
			throws SoCTraceException {
		super();
		this.type = AnalysisResultType.TYPE_SEARCH;
		this.searchCommand = "";
		this.targetEntity = getTargetEntityName(targetClass);
		this.targetClass = targetClass;
		this.elements = new LinkedList<ISearchable>();
	}

	/**
	 * @return the searchCommand
	 */
	public String getSearchCommand() {
		return searchCommand;
	}

	/**
	 * @param searchCommand the searchCommand to set
	 */
	public void setSearchCommand(String searchCommand) {
		this.searchCommand = searchCommand;
	}

	/**
	 * @return the targetEntity
	 */
	public String getTargetEntity() {
		return targetEntity;
	}

	/**
	 * @return the elements
	 */
	public List<ISearchable> getElements() {
		return elements;
	}

	/**
	 * @param elements the elements to set
	 * @throws SoCTraceException 
	 */
	@SuppressWarnings("unchecked")
	public void setElements(List<? extends ISearchable> elements) throws SoCTraceException {
		for (ISearchable e: elements){
			checkElementClass(e);
		}
		this.elements = (List<ISearchable>) elements;
	}
	
	/**
	 * Add an element to the search list.
	 * @param e
	 * @throws SoCTraceException 
	 */
	public void addElement(ISearchable e) throws SoCTraceException {
		checkElementClass(e);
		elements.add(e);
	}

	@Override
	public void print() {
		System.out.println("Search command: " + searchCommand);
		for (ISearchable e: elements) {
			System.out.println(e.toString());
		}
	}

	/**
	 * Initialize the searchable-object classes-names map.
	 * 
	 * @return a constant map
	 */
    private static Map<Class<? extends ISearchable>, String> initClassToNameMap() {
        Map<Class<? extends ISearchable>, String> result = new HashMap<Class<? extends ISearchable>, String>();
        result.put(Event.class, ModelEntity.EVENT.name());
        result.put(EventProducer.class, ModelEntity.EVENT_PRODUCER.name());
        return Collections.unmodifiableMap(result);
    }

    /**
     * Check that the passed element class is the same as the target class.
     * 
     * @param e searchable element
     * @throws SoCTraceException
     */
    private void checkElementClass(ISearchable e) throws SoCTraceException {
    	// for events the condition is more complex since there may be categorized events
    	if ((targetClass.equals(Event.class) && (e instanceof Event)))
    		return;
		if (!e.getClass().equals(targetClass)) {
			throw new SoCTraceException("Illegal element. " +
					"The element class ("+e.getClass().toString()+") " +
					"differs from the target one ("+targetClass.toString()+")");
		}
    }

    /**
     * Check the target class and return the corresponding entity name.
     * 
     * @param targetClass target class
     * @return the corresponding entity name
     * @throws SoCTraceException
     */
    private String getTargetEntityName(Class<? extends ISearchable> targetClass) 
    		throws SoCTraceException {
    	String name = searchableClassToName.get(targetClass);
    	if (name == null)
    		throw new SoCTraceException("Target class ("+targetClass.toString()+") not found among the searchable ones");
    	return name;
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((elements == null) ? 0 : elements.hashCode());
		result = prime * result + ((searchCommand == null) ? 0 : searchCommand.hashCode());
		result = prime * result + ((targetClass == null) ? 0 : targetClass.hashCode());
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
		if (!(obj instanceof AnalysisResultSearchData))
			return false;
		AnalysisResultSearchData other = (AnalysisResultSearchData) obj;
		if (elements == null) {
			if (other.elements != null)
				return false;
		} else if (!elements.equals(other.elements))
			return false;
		if (searchCommand == null) {
			if (other.searchCommand != null)
				return false;
		} else if (!searchCommand.equals(other.searchCommand))
			return false;
		if (targetClass == null) {
			if (other.targetClass != null)
				return false;
		} else if (!targetClass.equals(other.targetClass))
			return false;
		if (targetEntity == null) {
			if (other.targetEntity != null)
				return false;
		} else if (!targetEntity.equals(other.targetEntity))
			return false;
		return true;
	}   
    
}
