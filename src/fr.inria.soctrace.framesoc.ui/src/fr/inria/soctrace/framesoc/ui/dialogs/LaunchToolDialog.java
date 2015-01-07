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

import fr.inria.soctrace.framesoc.core.tools.management.ArgumentsManager;
import fr.inria.soctrace.framesoc.core.tools.management.ToolContributionManager;
import fr.inria.soctrace.framesoc.core.tools.model.IFramesocTool;
import fr.inria.soctrace.framesoc.core.tools.model.IFramesocTool.ParameterCheckStatus;
import fr.inria.soctrace.framesoc.core.tools.model.IFramesocToolInput;
import fr.inria.soctrace.framesoc.ui.input.CommandLineArgsInputComposite;
import fr.inria.soctrace.framesoc.ui.listeners.ComboListener;
import fr.inria.soctrace.framesoc.ui.listeners.LaunchTextListener;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Eclipse Dialog to launch a Framesoc tool.
 * 
 * TODO use new mechanism
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class LaunchToolDialog extends Dialog implements IArgumentDialog {

	private static final String DIALOG_TITLE = "Launch tool";

	Map<String, Tool> toolsMap;
	Map<String, IFramesocTool> fsToolsMap; // the tool is null for non plugin tools
	
	// listeners
	private ComboListener toolListener;
	private LaunchTextListener argsListener;
	
	// tool
    private Label toolLabel;
    private Combo toolCombo;
    
    // arguments
    private CommandLineArgsInputComposite argsComposite;
    private Group group;
    private Label message;

	public LaunchToolDialog(Shell parentShell, List<Tool> tools) throws SoCTraceException {
		super(parentShell);
		toolsMap = new HashMap<String, Tool>();
		fsToolsMap = new HashMap<String, IFramesocTool>();
		for(Tool t: tools) {
			toolsMap.put(t.getName(), t);
			fsToolsMap.put(t.getName(), ToolContributionManager.getToolLauncher(t));
		}
		toolListener = new ComboListener(toolsMap.keySet().iterator().next());
		argsListener = new LaunchTextListener("", this);
	}
	
    @Override
    protected Control createDialogArea(Composite parent) {
        
    	this.getShell().setText(DIALOG_TITLE);
        Composite composite = (Composite) super.createDialogArea(parent);
       
        Group analysisComposite = new Group(composite, SWT.NONE);
        analysisComposite.setLayout(new GridLayout(1,false));
        analysisComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        analysisComposite.setText("Launch analysis tool");
        
        Composite toolComposite = new Composite(analysisComposite, SWT.NONE);
        toolComposite.setLayout(new GridLayout(2, false));
        toolComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        // tool label
        toolLabel = new Label(toolComposite, SWT.NONE);
        toolLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        toolLabel.setText("Tool");
        // tool combo
        toolCombo = new Combo(toolComposite, SWT.BORDER | SWT.READ_ONLY);
        toolCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        for (String s: toolsMap.keySet()) {
        	toolCombo.add(s);
        }
        toolCombo.select(0);
        toolCombo.addSelectionListener(toolListener);
        toolCombo.addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent e) {
        		argsComposite.setDocText(toolsMap.get(toolCombo.getText()).getDoc());
        		updateOk();
        	}
		});
        
        // arguments and doc
        argsComposite = new CommandLineArgsInputComposite(analysisComposite, SWT.NONE, true);
        argsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        
        // message
        group = new Group(composite, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        group.setText("Error message");
        group.setLayout(new GridLayout(1, false));
        
        message = new Label(group, SWT.WRAP);
        message.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        message.setToolTipText("");
        message.setText("");

        return composite;
    }	
       
	public Tool getTool() {
		return toolsMap.get(toolListener.getText());
	}

	public String[] getArgs() {
		return ArgumentsManager.tokenize(argsListener.getText());
	}

	protected Point getInitialSize() {
		return new Point(625, 465);
	}
	
    @Override
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
		// OK enabled only if the Framesoc tool says so
		updateOk();
	}
	
    private ParameterCheckStatus canLaunch() {
    	ParameterCheckStatus status = new ParameterCheckStatus(false, "");
    	Tool t = getTool();
    	if (t == null) {
    		status.message = "Tool not existing";
    		return status;
    	}
    	IFramesocTool tool = getToolLauncher();
    	if (tool == null) {
    		status.message = "Tool not existing";
    		return status;
    	}
    	return tool.canLaunch(getInput()); 
    }
    	
	private IFramesocTool getToolLauncher() {
		return fsToolsMap.get(toolListener.getText());
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

	@Override
	public IFramesocToolInput getInput() {
		// TODO Auto-generated method stub
		// Do this
		throw new UnsupportedOperationException();
	}
}
