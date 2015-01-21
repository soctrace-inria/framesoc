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
package fr.inria.soctrace.framesoc.core.tools.importers;

/**
 * Descriptor for an entity parameter (self-defining-pattern).
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class ParameterDescriptor {
	
	/** Name of the parameters in the DB */
	private String name;
	/** Type of the parameters in the DB */
	private String type;
	/** Value of the parameter */
	private String value;
	
	/**
	 * Constructor.
	 * 
	 * @param name parameter name 
	 * @param type parameter type 
	 * @param value parameter value
	 */
	private ParameterDescriptor(String name, String type, String value) {
		this.name = name;
		this.type = type;
		this.value = value;	
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * 
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

}
