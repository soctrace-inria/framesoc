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

import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Class representing events of VARIABLE category.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class Variable extends Event {

	public Variable(int id) {
		super(id);
		try {
			setCategory(EventCategory.VARIABLE);
		} catch (SoCTraceException e) {
			e.printStackTrace();
		}
		setVariableId(0);
	}

	/**
	 * Warning: the event ID and the variable ID are different things.
	 * Different event ID may correspond to a given variable ID.
	 * The rationale behind the variable ID is to have the possibility
	 * to have several variable for a given variable type.
	 * Its utilization is optional.
	 * 
	 * @return the variable id
	 */
	public long getVariableId() {
		return getLongPar();
	}

	/**
	 * @param variableId the variable id to set
	 */
	public void setVariableId(long variableId) {
		setLongPar(variableId);
	}

	/**
	 * @return the variable value
	 */
	public double getValue() {
		return getDoublePar();
	}

	/**
	 * @param value the variable value to set
	 */
	public void setValue(double value) {
		setDoublePar(value);
	}

}
