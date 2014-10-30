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
package fr.inria.soctrace.lib.query;

import fr.inria.soctrace.lib.utils.TagList;


/**
 * Utility class used to build SQL compliant value lists.
 * Format: (1, 2, 3, 4)
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class ValueListString extends TagList {

	private static final String START = "(";
	private static final String END = ")";
	private static final String SEPARATOR = ", ";
	
	@Override
	protected String getSeparator() {
		return SEPARATOR;
	}
	
	@Override
	protected String getStart() {
		return START;
	}
	
	@Override
	protected String getEnd() {
		return END;
	}
	
}
