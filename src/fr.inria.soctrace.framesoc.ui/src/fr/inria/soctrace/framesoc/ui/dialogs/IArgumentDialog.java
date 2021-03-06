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
package fr.inria.soctrace.framesoc.ui.dialogs;

import fr.inria.soctrace.framesoc.core.tools.model.IFramesocToolInput;

/**
 * Interface for dialogs taking parameters that need validation.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public interface IArgumentDialog {

	/**
	 * Method to update the OK button status. It is called inside widget listeners when some
	 * argument is changed.
	 */
	public void updateOk();

	/**
	 * Get the tool input.
	 * 
	 * @return the tool input
	 */
	public IFramesocToolInput getInput();

}
