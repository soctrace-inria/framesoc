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

import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Class representing events of STATE category.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class State extends Event {

	public State(int id) {
		super(id);
		try {
			setCategory(EventCategory.STATE);
		} catch (SoCTraceException e) {
			e.printStackTrace();
		}
		setImbricationLevel(0);
	}
	
	/**
	 * @return the end timestamp
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
	 * @return the <i>imbrication</i> level, which is defined as the
	 * nesting level for states containing other states (e.g., the root
	 * state has imbrication 0, his direct sons have imbrication 1, etc.)
	 */
	public int getImbricationLevel() {
		return ((Double)getDoublePar()).intValue();
	}

	/**
	 * @param imbricationLevel the <i>imbrication</i> level to set.
	 * The imbrication level is defined in {@link #getImbricationLevel()}
	 * documentation.
	 */
	public void setImbricationLevel(int imbricationLevel) {
		setDoublePar(((Integer)imbricationLevel).doubleValue());
	}

}
