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

/**
 * Interface implemented by all the object that can be part 
 * of a search analysis result.
 * @see fr.inria.soctrace.lib.model.AnalysisResultSearchData
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public interface ISearchable {
	
	/**
	 * Get the searchable element ID.
	 * 
	 * @return the ID of the searchable object
	 */
	int getId();
}
