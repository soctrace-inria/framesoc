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
package fr.inria.soctrace.lib.storage.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import fr.inria.soctrace.lib.model.IModelElement;

/**
 * This class is a cache for {@link IModelElement} objects.
 * For each {@link IModelElement} class we want to cache we have to add 
 * a new element map, which maps the object IDs with the objects.
 * 
 * The class is currently used to store *Type objects inside database 
 * objects.
 * The access to a given object is obtained specifying the object
 * class and id. See the {@link #get(Class, int)} method. 
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class ModelElementCache {

	/**
	 * Map of element maps.
	 */
	private final HashMap<Class<? extends IModelElement>, Map<Integer, IModelElement>> map;
	
	/**
	 * The constructor
	 */
	public ModelElementCache() {
		map = new HashMap<Class<? extends IModelElement>, Map<Integer, IModelElement>>();
	}
	
	/**
	 * Add a map for the specified element class, if not existing yet.
	 * @param elementClass the element class.
	 */
	public void addElementMap(Class<? extends IModelElement> elementClass) {
		if ( map.containsKey(elementClass) )
			return;
		map.put(elementClass, new HashMap<Integer, IModelElement>());
	}

	/**
	 * Get the map for the specified element class, if existing.
	 * @param elementClass the element class.
	 * @return the map or null is not present
	 */
	public Map<Integer, IModelElement> getElementMap(Class<? extends IModelElement> elementClass) {
		if ( map.containsKey(elementClass) )
			return map.get(elementClass);
		return null;
	}

	/**
	 * Put an object in the element cache.
	 * @param obj ITraceElement object.
	 */
	public <T extends IModelElement> void put(T obj) {
		Class<? extends IModelElement> elementClass = obj.getClass();
		Map<Integer, IModelElement> objMap = map.get(elementClass);
		if(objMap != null){
			objMap.put(obj.getId(), obj);
		}
	}

	/**
	 * Get an object from the element cache.
	 * @param elementClass the element cache
	 * @param id the element id
	 * @return the object of the given class having the given id
	 */
	@SuppressWarnings("unchecked")
	public <T extends IModelElement> T get(Class<T> elementClass, int id){
		Map<Integer, IModelElement> objMap = map.get(elementClass);
		if (objMap != null) {
			IModelElement ref = objMap.get(id);
			return (T) ref;
		}
		return null;
	}

	/**
	 * Remove an object from the element cache.
	 * @param obj the object
	 */
	public <T extends IModelElement> void remove(T obj) {
		Class<? extends IModelElement> elementClass = obj.getClass();
		Map<Integer, IModelElement> objMap = map.get(elementClass);
		if (objMap != null) {
			objMap.remove(obj.getId());
		}
	}
	
	/**
	 * Clear the cache.
	 */
	public void clear() {
		map.clear();
	}
	
	/**
	 * Debug method
	 */
	public void print() {
	    for (Class<? extends IModelElement> c : map.keySet()) {
	        System.out.println("Class: " + c);
	    	Map<Integer, IModelElement> objMap = map.get(c);
	        Iterator<?> it = objMap.entrySet().iterator();
	        while (it.hasNext()) {
	            @SuppressWarnings("rawtypes")
				Map.Entry pairs = (Map.Entry)it.next();
	            System.out.println("   " + pairs.getKey() + " = " + pairs.getValue());
	        }
	    }
	}
		
}
