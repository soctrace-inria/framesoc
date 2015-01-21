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
/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.perspective;


/**
 * Descriptor for the result of opening a Framesoc 
 * analysis view.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class OpenFramesocPartStatus {

	/**
	 * The loaded part if not null.
	 * If it is null, an error occurred and the 
	 * above message should be displayed.
	 */
	public FramesocPart part;
	
	/**
	 * Error message, if part is null.
	 */
	public String message;
	
}
