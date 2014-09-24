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
 * Class representing events of PUNCTUAL_EVENT category.
 * Only provides the name.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class PunctualEvent extends Event {

	public PunctualEvent(int id) {
		super(id);
		try {
			setCategory(EventCategory.PUNCTUAL_EVENT);
		} catch (SoCTraceException e) {
			e.printStackTrace();
		}
	}

}
