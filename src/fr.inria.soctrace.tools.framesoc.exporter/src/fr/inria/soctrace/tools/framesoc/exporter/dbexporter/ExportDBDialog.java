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
package fr.inria.soctrace.tools.framesoc.exporter.dbexporter;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class ExportDBDialog extends Dialog {

	private Text textDirectory;
	private TraceComboManager traceComboManager;
	protected ExporterInput input = new ExporterInput();

	protected ExportDBDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		GridLayout gl_parent = new GridLayout(1, false);
		parent.setLayout(gl_parent);
		
		Group grpExportSettings = new Group(parent, SWT.NONE);
		grpExportSettings.setLayout(new GridLayout(1, false));
		grpExportSettings.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpExportSettings.setText("Export Settings");
		
		// Trace
		Composite compositeTrace = new Composite(grpExportSettings, SWT.NONE);
		compositeTrace.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		compositeTrace.setSize(584, 41);
		compositeTrace.setLayout(new GridLayout(2, false));
		
		Label lblTrace = new Label(compositeTrace, SWT.NONE);
		lblTrace.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTrace.setText("Trace");

		Combo comboTraces = new Combo(compositeTrace, SWT.READ_ONLY);
		comboTraces.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		traceComboManager = new TraceComboManager(comboTraces, true);
		traceComboManager.loadAll();
		comboTraces.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateExporterInput();
			}
		});

		// Directory	
		Composite compositeDirectory = new Composite(grpExportSettings, SWT.NONE);
		compositeDirectory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		compositeDirectory.setLayout(new GridLayout(3, false));
	
		Label lblDirectory = new Label(compositeDirectory, SWT.NONE);
		lblDirectory.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDirectory.setText("Export Directory");
		
		textDirectory = new Text(compositeDirectory, SWT.BORDER);
		textDirectory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textDirectory.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String dir = textDirectory.getText();
				File d = new File(dir);
				updateExporterInput();
				updateOk(d.isDirectory());
			}
		});
		
		Button btnBrowse = new Button(compositeDirectory, SWT.NONE);
		btnBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				textDirectory.setText(""); 
        		DirectoryDialog dlg = new DirectoryDialog(getShell(), SWT.SINGLE);
        		textDirectory.setText(dlg.open());
			}
		});
		btnBrowse.setText("Browse");
		
		return parent;
	}

	public ExporterInput getExporterInput() {
		if (input.check())
			return input;
		return null;
	}
	
	private void updateExporterInput() {
		// trace
		input.trace = traceComboManager.getSelectedTrace();
		// Directory
		input.directory = textDirectory.getText(); 
	}
	
	protected Point getInitialSize() {
		return new Point(657, 244);
	}

    @Override
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
		// OK disabled at the beginning
		getButton(IDialogConstants.OK_ID).setEnabled(false);
	}
    
	public void updateOk(boolean canLaunch) {
		Button ok = getButton(IDialogConstants.OK_ID);
		if (ok == null)
			return;
		ok.setEnabled(canLaunch);
	}

}
