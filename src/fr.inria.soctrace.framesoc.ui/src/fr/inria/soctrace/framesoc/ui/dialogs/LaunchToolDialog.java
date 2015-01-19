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

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import fr.inria.soctrace.framesoc.ui.input.AbstractToolInputComposite;
import fr.inria.soctrace.framesoc.ui.input.EmptyToolInputComposite;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Eclipse Dialog to launch a Framesoc tool.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class LaunchToolDialog extends AbstractLaunchToolDialog {

	private static final String DIALOG_TITLE = "Launch tool";

	public LaunchToolDialog(Shell parentShell, List<Tool> tools) throws SoCTraceException {
		super(parentShell, tools);
	}

	@Override
	protected AbstractToolInputComposite getDefaultToolInputComposite(Composite parent, int style) {
		return new EmptyToolInputComposite(parent, style);
	}

	@Override
	protected String getDialogTitle() {
		return DIALOG_TITLE;
	}

	@Override
	protected String getDialogText() {
		return DIALOG_TITLE;
	}

}
