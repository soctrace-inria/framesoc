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
package fr.inria.soctrace.framesoc.ui.model;

/**
 * Model element for a Detail Table row, which is 
 * composed by two columns: NAME, VALUE.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class DetailsTableRow {

	private boolean isReadOnly;
	private boolean isCustomParam;
	private String name;
	private String value;
	
	/**
	 * Constructor.
	 * The parameter is supposed to be not custom and not readonly.
	 * 
	 * @param name parameter name
	 * @param value parameter value
	 */
	public DetailsTableRow(String name, String value) {
		this.name = name;
		this.value = value;
		this.isCustomParam = false;
		this.isReadOnly = false;
	}

	/**
	 * Constructor.
	 * 
	 * @param name parameter name
	 * @param value parameter value
	 * @param isCustomParam flag that should be true for custom parameters (self-defining-pattern)
	 * @param isReadOnly flag that should be true for read only parameters
	 */
	public DetailsTableRow(String name, String value, boolean isCustomParam, boolean isReadOnly) {
		this.name = name;
		this.value = value;
		this.isCustomParam = isCustomParam;
		this.isReadOnly = isReadOnly;
	}

	/**
	 * @return the isCustomParam
	 */
	public boolean isCustomParam() {
		return isCustomParam;
	}

	/**
	 * @param isCustomParam the isCustomParam to set
	 */
	public void setCustomParam(boolean isCustomParam) {
		this.isCustomParam = isCustomParam;
	}

	/**
	 * @return the isReadOnly
	 */
	public boolean isReadOnly() {
		return isReadOnly;
	}

	/**
	 * @param isReadOnly the isReadOnly to set
	 */
	public void setReadOnly(boolean isReadOnly) {
		this.isReadOnly = isReadOnly;
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
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "DetailsTableRow [isReadOnly=" + isReadOnly + ", isCustomParam=" + isCustomParam
				+ ", name=" + name + ", value=" + value + "]";
	}

}
