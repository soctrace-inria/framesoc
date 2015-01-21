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
 * Generic interface for a node wrapping a IModelElement.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public interface IModelElementNode {

	/**
	 * Get the model element id
	 * 
	 * @return the id
	 */
	int getId();

}
