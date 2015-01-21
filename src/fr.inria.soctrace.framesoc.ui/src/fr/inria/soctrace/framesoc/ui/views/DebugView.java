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
package fr.inria.soctrace.framesoc.ui.views;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import fr.inria.soctrace.framesoc.ui.perspective.FramesocPart;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocPartManager;
import fr.inria.soctrace.framesoc.ui.perspective.OpenFramesocPartStatus;
import fr.inria.soctrace.framesoc.ui.utils.TraceSelection;
import fr.inria.soctrace.lib.model.Trace;

/**
 * Test view class.
 * 
 * <p>
 * IMPORTANT: to use it, uncomment its declaration in the plugin.xml file.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class DebugView extends FramesocPart {

	public static final String ID = "fr.inria.soctrace.framesoc.ui.views.DebugView"; //$NON-NLS-1$
	private Label label;

	@Override
	public void createFramesocPartControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		Button btnNewButton = new Button(composite, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Trace t = TraceSelection.getCurrentSelectedTrace();
				if (t == null) {
					System.out.println("Select a trace!");
					return;
				}
				OpenFramesocPartStatus status = FramesocPartManager.getInstance().getPartInstance(
						ID, null);
				if (status.part != null)
					status.part.showTrace(TraceSelection.getCurrentSelectedTrace(), null);
				else
					MessageDialog.openError(getSite().getShell(), "Error", status.message);
			}
		});
		btnNewButton.setText("Fake load");

		label = new Label(composite, SWT.NONE);
		label.setText("                                                                       ");

		setContentDescription("View secondary ID: " + getViewSite().getSecondaryId());
	}

	@Override
	public void setFocus() {
		super.setFocus();
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void showTrace(Trace trace, Object data) {
		currentShownTrace = trace;
		label.setText(currentShownTrace.getAlias());
		setContentDescription("View secondary ID: " + getViewSite().getSecondaryId());
	}

}
