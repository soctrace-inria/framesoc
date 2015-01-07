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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import fr.inria.soctrace.framesoc.core.tools.management.ToolContributionManager;
import fr.inria.soctrace.framesoc.core.tools.model.IFramesocTool;
import fr.inria.soctrace.framesoc.core.tools.model.IFramesocTool.ParameterCheckStatus;
import fr.inria.soctrace.framesoc.core.tools.model.IFramesocToolInput;
import fr.inria.soctrace.framesoc.ui.input.AbstractToolInputComposite;
import fr.inria.soctrace.framesoc.ui.input.AbstractToolInputCompositeFactory;
import fr.inria.soctrace.framesoc.ui.input.DefaultImporterInputComposite;
import fr.inria.soctrace.framesoc.ui.input.FramesocToolInputContributionManager;
import fr.inria.soctrace.framesoc.ui.listeners.ComboListener;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Eclipse Dialog to import a trace into the infrastructure.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class ImportTraceDialog extends Dialog implements IArgumentDialog {

	private static final String IMPORT_TRACE_DIALOG_TITLE = "Import a new trace";

	// tool name -> tool object
	private Map<String, Tool> toolMap;
	// tool name -> framesoc tool TODO use extension id
	private Map<String, IFramesocTool> fsToolMap; // the tool is null for non plugin tools
	// ext id -> composite factory
	private Map<String, AbstractToolInputCompositeFactory> factoryMap;
	// ext id -> composite
	private Map<String, AbstractToolInputComposite> compositeCache;

	// Importer
	private ComboListener importerNameListener;
	private Label importerNameLabel;
	private Combo importerNameCombo;

	// Message
	private Group groupMessage;
	private Label message;

	// Custom tool input
	private AbstractToolInputComposite toolInputComposite;

	public ImportTraceDialog(Shell parentShell, List<Tool> tools) throws SoCTraceException {
		super(parentShell);
		compositeCache = new HashMap<>();
		toolMap = new HashMap<String, Tool>();
		fsToolMap = new HashMap<String, IFramesocTool>();
		for (Tool t : tools) {
			toolMap.put(t.getName(), t);
			fsToolMap.put(t.getName(), ToolContributionManager.getToolLauncher(t));
		}
		importerNameListener = new ComboListener(toolMap.keySet().iterator().next());
		factoryMap = FramesocToolInputContributionManager.getToolInputComposites();
	}

	@Override
	protected Control createDialogArea(final Composite parent) {

		this.getShell().setText(IMPORT_TRACE_DIALOG_TITLE);
		Composite composite = (Composite) super.createDialogArea(parent);

		// Import group

		final Group importGroup = new Group(composite, SWT.NONE);
		importGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		importGroup.setText("Import new trace");
		importGroup.setLayout(new GridLayout(1, true));

		// Importer
		Composite importerComposite = new Composite(importGroup, SWT.NONE);
		importerComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		importerComposite.setLayout(new GridLayout(2, false));

		importerNameLabel = new Label(importerComposite, SWT.NONE);
		importerNameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		importerNameLabel.setText("Importer");
		importerNameCombo = new Combo(importerComposite, SWT.BORDER | SWT.READ_ONLY);
		importerNameCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		for (String s : toolMap.keySet()) {
			importerNameCombo.add(s);
		}
		importerNameCombo.select(0);
		importerNameCombo.addSelectionListener(importerNameListener);
		importerNameCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateOk();
				updateComposite(importGroup);
				parent.layout(true); // if changing form XXX
			}
		});

		// Importer input
		updateComposite(importGroup);

		// Message group

		groupMessage = new Group(composite, SWT.V_SCROLL);
		groupMessage.setText("Error message");
		groupMessage.setLayout(new GridLayout(1, false));
		groupMessage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		message = new Label(groupMessage, SWT.WRAP);
		message.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		return composite;
	}

	private void updateComposite(Composite parent) {
		Tool t = getTool(); 
		
		// look in the cache first
		if (compositeCache.containsKey(t.getExtensionId())) {
			toolInputComposite = compositeCache.get(t.getExtensionId());
			return;
		}
		
		// create it if not found
		if (factoryMap.containsKey(t.getExtensionId())) {
			toolInputComposite = factoryMap.get(t.getExtensionId()).getComposite(parent, SWT.NONE);
		} else {
			toolInputComposite = new DefaultImporterInputComposite(parent, SWT.NONE);
		}
		toolInputComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		toolInputComposite.setArgumentDialog(this);
		compositeCache.put(t.getExtensionId(), toolInputComposite);
		
	}
	
    @Override
	protected Point getInitialSize() {
		return new Point(610, 380);
	}
    
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		// OK enabled only if the Framesoc tool says so
		updateOk();
	}

	@Override
	public IFramesocToolInput getInput() {
		return toolInputComposite.getToolInput();
	}

	@Override
	public void updateOk() {
		Button ok = getButton(IDialogConstants.OK_ID);
		if (ok == null)
			return;
		ParameterCheckStatus status = canLaunch();
		message.setText(status.message);
		message.setToolTipText(status.message);
		ok.setEnabled(status.valid);
	}

	public Tool getTool() {
		return toolMap.get(importerNameCombo.getText());
	}

	private ParameterCheckStatus canLaunch() {
		ParameterCheckStatus status = new ParameterCheckStatus(false, "");
		Tool t = getTool();
		if (t == null) {
			status.message = "Importer not existing";
			return status;
		}
		IFramesocTool tool = getToolLauncher();
		if (tool == null) {
			status.message = "Importer not existing";
			return status;
		}
		return tool.canLaunch(getInput());
	}

	private IFramesocTool getToolLauncher() {
		return fsToolMap.get(importerNameCombo.getText());
	}

}
