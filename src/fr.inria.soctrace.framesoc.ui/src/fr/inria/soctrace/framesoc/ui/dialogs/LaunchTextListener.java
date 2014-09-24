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
package fr.inria.soctrace.framesoc.ui.dialogs;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;

/**
 * Text field listener.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class LaunchTextListener extends StringListener implements ModifyListener {

	IArgumentDialog dialog;
	
	public LaunchTextListener(String initialValue, IArgumentDialog dialog) {
		super(initialValue);
		this.dialog = dialog;
	}

	@Override
	public void modifyText(ModifyEvent e) {
		text = ((Text)e.getSource()).getText();
		dialog.updateOk();
	}

}
