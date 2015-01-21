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
package fr.inria.soctrace.tools.framesoc.exporter.dbimporter;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import fr.inria.soctrace.lib.model.AnalysisResult;

/**
 * Eclipse Dialog to select analysis result to import.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class ImportDBResultDialog extends Dialog {

	private GridData gd_grpAnalysisResultTo;
	private Label lblInTheDatabase;
	private CheckboxTableViewer checkboxTableViewer;
	private List<AnalysisResult> arToShow;
	private Object[] arToKeep;

	public ImportDBResultDialog(Shell parentShell, List<AnalysisResult> arToShow) {
		super(parentShell);
		this.arToShow = arToShow; 
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		Composite composite = (Composite) super.createDialogArea(parent);

		lblInTheDatabase = new Label(composite, SWT.READ_ONLY);
		lblInTheDatabase.setText("In the database you want to import there are some analysis results you can keep.");

		Label lblPleaseChoseFrom = new Label(composite, SWT.NONE);
		lblPleaseChoseFrom.setText("Please chose from the following list");

		Group grpAnalysisResultTo = new Group(composite, SWT.NONE);
		grpAnalysisResultTo.setText("Analysis Result to keep");
		GridLayout gl_grpAnalysisResultTo = new GridLayout(1,false);
		grpAnalysisResultTo.setLayout(gl_grpAnalysisResultTo);
		gd_grpAnalysisResultTo = new GridData(GridData.FILL_BOTH);
		grpAnalysisResultTo.setLayoutData(gd_grpAnalysisResultTo);

		checkboxTableViewer = CheckboxTableViewer.newCheckList(grpAnalysisResultTo, SWT.BORDER | SWT.FULL_SELECTION);
		Table table = checkboxTableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		checkboxTableViewer.setContentProvider(new ResultsContentProvider());
		checkboxTableViewer.setLabelProvider(new ResultsLabelProvider());
		checkboxTableViewer.setInput(arToShow);
		checkboxTableViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				arToKeep = checkboxTableViewer.getCheckedElements();			
			}
		});

		return composite;
	}	

	protected Point getInitialSize() {
		return new Point(556, 423);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button ok = createButton(parent, IDialogConstants.OK_ID, "Import", true);
		ok.setText("Import");
		setButtonLayoutData(ok);
	}

	class ResultsContentProvider extends ArrayContentProvider {
		public Object[] getElements(Object arg0) {
			@SuppressWarnings("unchecked")
			List<AnalysisResult> srl = (List<AnalysisResult>) arg0;
			return srl.toArray();
		}
	}

	class ResultsLabelProvider extends LabelProvider {
		public String getText(Object arg0) {
			AnalysisResult ar = (AnalysisResult) arg0;
			return ar.getTool().getName() + ": " + ar.getDescription();
		}
	}

	public Object[] getResultsToKeep() {
		return arToKeep;
	}
}
