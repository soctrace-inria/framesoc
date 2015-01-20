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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
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
import org.eclipse.swt.widgets.Text;

import fr.inria.soctrace.framesoc.core.tools.management.ToolContributionManager;
import fr.inria.soctrace.framesoc.core.tools.model.IFramesocTool;
import fr.inria.soctrace.framesoc.core.tools.model.IFramesocTool.ParameterCheckStatus;
import fr.inria.soctrace.framesoc.core.tools.model.IFramesocToolInput;
import fr.inria.soctrace.framesoc.ui.input.AbstractToolInputComposite;
import fr.inria.soctrace.framesoc.ui.input.AbstractToolInputCompositeFactory;
import fr.inria.soctrace.framesoc.ui.input.FramesocToolInputContributionManager;
import fr.inria.soctrace.framesoc.ui.listeners.ComboListener;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Base Eclipse Dialog to launch tools
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class AbstractLaunchToolDialog extends Dialog implements IArgumentDialog {

	// Constants
	public final static int MIN_HEIGHT = 380;
	public final static int MIN_WIDTH = 610;
	public final static int MIN_TOOL_INPUT_COMPOSITE_WIDTH = MIN_WIDTH - 45;

	// tool name -> tool object
	private Map<String, Tool> toolMap;
	// tool name -> framesoc tool
	private Map<String, IFramesocTool> fsToolMap; // the tool is null for non plugin tools
	// tool extension id -> composite factory
	private Map<String, AbstractToolInputCompositeFactory> factoryMap;
	// sorted tools
	private String[] sortedToolNames;

	// Tool Selection
	private ComboListener toolNameListener;
	private Label toolNameLabel;
	private Combo toolNameCombo;

	// Tool Message
	private Group groupMessage;
	private Text message;

	// Tool input composite. It must have a Grid Layout and Grid Data.
	private Composite inputComposite;
	// Custom tool input
	private AbstractToolInputComposite toolInputComposite;
	// Current selected tool, whose input composite is already displayed
	private Tool currentTool = null;

	// Dialog parent composite
	private Composite dialogParentComposite;

	public AbstractLaunchToolDialog(Shell parentShell, List<Tool> tools) throws SoCTraceException {
		super(parentShell);
		toolMap = new HashMap<String, Tool>();
		fsToolMap = new HashMap<String, IFramesocTool>();
		sortedToolNames = new String[tools.size()];
		int i = 0;
		for (Tool t : tools) {
			sortedToolNames[i++] = t.getName();
			toolMap.put(t.getName(), t);
			fsToolMap.put(t.getName(), ToolContributionManager.getToolLauncher(t));
		}
		Arrays.sort(sortedToolNames);
		if (tools.size() > 0) {
			toolNameListener = new ComboListener(sortedToolNames[0]);
		} else {
			toolNameListener = new ComboListener("");
		}
		factoryMap = FramesocToolInputContributionManager.getToolInputComposites();
	}

	/**
	 * Get the default input composite when there is no extension point for a tool.
	 * 
	 * @param parent
	 *            composite parent
	 * @param style
	 *            style
	 * @return the default composite
	 */
	protected abstract AbstractToolInputComposite getDefaultToolInputComposite(Composite parent,
			int style);

	/**
	 * @return the dialog title
	 */
	protected abstract String getDialogTitle();

	/**
	 * @return the dialog text
	 */
	protected abstract String getDialogText();

	@Override
	protected Control createContents(Composite parent) {
		dialogParentComposite = parent;
		Control c = super.createContents(parent);
		layoutDialog();
		return c;
	}

	@Override
	protected Control createDialogArea(final Composite parent) {

		this.getShell().setText(getDialogTitle());
		Composite composite = (Composite) super.createDialogArea(parent);

		// *** Tool group ***

		Group toolGroup = new Group(composite, SWT.NONE);
		toolGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		toolGroup.setText(getDialogText());
		toolGroup.setLayout(new GridLayout(1, true));

		// Tool
		Composite importerComposite = new Composite(toolGroup, SWT.NONE);
		importerComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		importerComposite.setLayout(new GridLayout(2, false));

		inputComposite = new Composite(toolGroup, SWT.NONE);
		inputComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
		GridLayout gl_inputComposite = new GridLayout(1, false);
		gl_inputComposite.marginWidth = 0;
		inputComposite.setLayout(gl_inputComposite);

		toolNameLabel = new Label(importerComposite, SWT.NONE);
		toolNameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolNameLabel.setText("Tool");
		toolNameCombo = new Combo(importerComposite, SWT.BORDER | SWT.READ_ONLY);
		toolNameCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		for (String s : sortedToolNames) {
			toolNameCombo.add(s);
		}
		toolNameCombo.select(0);
		toolNameCombo.addSelectionListener(toolNameListener);
		toolNameCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateInputComposite();
				updateOk();
				layoutDialog();
			}
		});

		// Tool input
		updateInputComposite();

		// *** Message group ***

		groupMessage = new Group(composite, SWT.V_SCROLL);
		groupMessage.setText("Tool message");
		groupMessage.setLayout(new GridLayout(1, false));
		groupMessage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		message = new Text(groupMessage, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
		GridData messageData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		messageData.minimumHeight = 80;
		message.setLayoutData(messageData);

		return composite;
	}

	private void updateInputComposite() {
		// aliasing check
		Tool t = getTool();
		if (t.equals(currentTool)) {
			return;
		}
		currentTool = t;

		// clean all the composites in the input parent
		Control[] controls = inputComposite.getChildren();
		for (Control c : controls) {
			c.dispose();
		}

		if (factoryMap.containsKey(currentTool.getExtensionId())) {
			toolInputComposite = factoryMap.get(currentTool.getExtensionId()).getComposite(
					inputComposite, SWT.NONE);
		} else {
			toolInputComposite = getDefaultToolInputComposite(inputComposite, SWT.NONE);
		}
		toolInputComposite.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true, 1, 1));
		toolInputComposite.setArgumentDialog(this);
		toolInputComposite.layout(true);
	}

	private void layoutDialog() {
		Assert.isNotNull(inputComposite);
		GridData data = (GridData) inputComposite.getLayoutData();
		/*
		 * We set the min width to 0, we pack, then we set the correct min width and we pack again,
		 * to force a redraw of the whole dialog even if nothing has changed.
		 * 
		 * This fixes the following bug: if we change the tool, but the input composite used is an
		 * instance of the same class as the previous, the new composite is not painted, since the
		 * dialog thinks it is the same as before.
		 */
		data.minimumWidth = 0;
		dialogParentComposite.pack();
		data.minimumWidth = MIN_TOOL_INPUT_COMPOSITE_WIDTH;
		dialogParentComposite.pack();
	}

	@Override
	protected Point getInitialSize() {
		return new Point(MIN_WIDTH, MIN_HEIGHT);
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
		return toolMap.get(toolNameListener.getText());
	}

	private ParameterCheckStatus canLaunch() {
		ParameterCheckStatus status = new ParameterCheckStatus(false, "");
		Tool t = getTool();
		if (t == null) {
			status.message = "Importer not existing";
			return status;
		}
		IFramesocTool tool = fsToolMap.get(t.getName());
		if (tool == null) {
			status.message = "Importer not existing";
			return status;
		}
		return tool.canLaunch(getInput());
	}

}
