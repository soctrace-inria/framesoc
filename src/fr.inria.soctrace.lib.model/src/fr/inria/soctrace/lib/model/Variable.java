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
		setEndTimestamp(0);
	}

	/**
	 * @return the variable end timestamp
	 */
	public long getEndTimestamp() {
		return getLongPar();
	}

	/**
	 * @param endTimestamp the end timestamp to set
	 */
	public void setEndTimestamp(long endTimestamp) {
		setLongPar(endTimestamp);
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
