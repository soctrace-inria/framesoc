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

import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import fr.inria.soctrace.framesoc.core.FramesocConstants.FramesocToolType;
import fr.inria.soctrace.framesoc.ui.listeners.ComboListener;
import fr.inria.soctrace.framesoc.ui.listeners.TextListener;

/**
 * Eclipse Dialog to register a new tool to the infrastructure.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class EditToolDialog extends Dialog {

	private static final String EDIT_TOOL_DIALOG_TITLE = "Edit tool parameters";
	
	// Data Listener
	private TextListener nameListener;
	private TextListener commandListener;
	private TextListener docListener;
	private ComboListener typeListener;
	
	// name
    private Label toolNameLabel;
    private Text toolNameText;
    // command
    private Label toolCommandLabel;
    // type
    private Label toolTypeLabel;
    private Combo toolTypeCombo;
    private Composite composite_1;
    private Text toolCommandText;
    private Button btnBrowse;
    // doc
    private Label lblDoc;
    private Text toolDocText;
 	
	public EditToolDialog(Shell parentShell) {
		super(parentShell);
		nameListener = new TextListener("");
		commandListener = new TextListener("");
		docListener = new TextListener("");
		typeListener = new ComboListener(FramesocToolType.values()[0].toString());
	}

	public EditToolDialog(Shell parentShell, String name, String command, String type, String doc) {
		super(parentShell);
		nameListener = new TextListener(name);
		commandListener = new TextListener(command);
		docListener = new TextListener(doc);
		typeListener = new ComboListener(type);
	}

    @Override
    protected Control createDialogArea(Composite parent) {
        
    	this.getShell().setText(EDIT_TOOL_DIALOG_TITLE);
        Composite composite = (Composite) super.createDialogArea(parent);
       
        GridData data;
      
        Group c = new Group(composite, SWT.NONE);
        c.setText("New tool data");
        GridLayout layout = new GridLayout(2,false);
        c.setLayout(layout);
        data = new GridData(GridData.FILL_BOTH);
        c.setLayoutData(data);
        
        data = new GridData(SWT.FILL, GridData.CENTER, true, false);
        data.minimumWidth = 400;
        
        // name
        toolNameLabel = new Label(c, SWT.NONE);
        toolNameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        toolNameLabel.setText("Tool name");
        toolNameText = new Text(c, SWT.BORDER);
        toolNameText.setText(nameListener.getText());
        toolNameText.addModifyListener(nameListener);
        toolNameText.setLayoutData(data);
        
        // command
        toolCommandLabel = new Label(c, SWT.NONE);
        toolCommandLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        toolCommandLabel.setText("Tool command");
        
        composite_1 = new Composite(c, SWT.NONE);
        GridLayout gl_composite_1 = new GridLayout(2, false);
        gl_composite_1.marginWidth = 0;
        gl_composite_1.marginHeight = 0;
        composite_1.setLayout(gl_composite_1);
        composite_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        
        toolCommandText = new Text(composite_1, SWT.BORDER);
        GridData gd_toolCommandText = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
        gd_toolCommandText.widthHint = 385;
        toolCommandText.setLayoutData(gd_toolCommandText);
        toolCommandText.setText(commandListener.getText());
        toolCommandText.addModifyListener(commandListener);
        
        btnBrowse = new Button(composite_1, SWT.NONE);
        btnBrowse.addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent e) {
        		FileDialog dlg = new FileDialog(composite_1.getShell(), SWT.SINGLE);
        		String fn = dlg.open();
        		if (fn != null) {
        			toolCommandText.append(fn);
        		}        		
        	}
        });
        btnBrowse.setText("Browse");
        
        // type
        toolTypeLabel = new Label(c, SWT.NONE);
        toolTypeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        toolTypeLabel.setText("Tool type");
        toolTypeCombo = new Combo(c, SWT.BORDER | SWT.READ_ONLY);
        int currentIndex = 0;
        int selectedIndex = currentIndex;
        for (FramesocToolType t: FramesocToolType.values()) {
        	if (t.toString().equals(typeListener.getText()))
        		selectedIndex = currentIndex;
        	toolTypeCombo.add(t.toString());
        	currentIndex++;
        }
        toolTypeCombo.select(selectedIndex);
        toolTypeCombo.addSelectionListener(typeListener);
        
        // doc
        lblDoc = new Label(c, SWT.NONE);
        lblDoc.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
        lblDoc.setText("Doc");
        
        toolDocText = new Text(c, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
        GridData gd_text = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
        gd_text.heightHint = 112;
        toolDocText.setLayoutData(gd_text);
        toolDocText.setText(docListener.getText());
        toolDocText.addModifyListener(docListener);
              
        return composite;
    }	
       
    public String getToolCommand() {
    	return commandListener.getText();
    }

    public String getToolName() {
    	return nameListener.getText();
    }
    
    public String getToolDoc() {
    	return docListener.getText();
    }

    public String getToolType() {
    	return typeListener.getText();
    }

	protected Point getInitialSize() {
		return new Point(602, 403);
	}
}
