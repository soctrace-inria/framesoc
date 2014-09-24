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
package fr.inria.soctrace.lib.search.utils;

/**
 * Utility class used to represent a [name,value] couple,
 * to be used for self-defined-element parameters.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class ParamDesc {
	
	public String name;
	public String value;
	
	/**
	 * @param name parameter name
	 * @param value parameter value
	 */
	public ParamDesc(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
}
