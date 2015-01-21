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
package fr.inria.soctrace.framesoc.ui.input;

import org.eclipse.swt.widgets.Composite;

/**
 * Base abstract class for tool input composite factories.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class AbstractToolInputCompositeFactory {

	/**
	 * Create a composite.
	 * 
	 * @param parent composite parent
	 * @param style composite style
	 * @return the Framesoc tool input composite
	 */
	public abstract AbstractToolInputComposite getComposite(Composite parent,
			int style);

}
