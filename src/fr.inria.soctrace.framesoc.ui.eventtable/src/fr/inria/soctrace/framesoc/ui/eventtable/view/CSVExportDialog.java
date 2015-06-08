/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Youenn Corre - initial API and implementation
 ******************************************************************************/
package fr.inria.soctrace.framesoc.ui.eventtable.view;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.ResourceManager;

import fr.inria.soctrace.framesoc.ui.eventtable.model.EventTableColumn;

/**
 * This class implement a configuration dialog for the export in csv of the
 * values of the event table It allows to select which columns of the table are
 * exported and specify the destination file.
 * 
 * @author "Youenn Corre <youenn.corre@inria.fr>"
 */
public class CSVExportDialog extends Dialog {

	private Map<EventTableColumn, Button> selectedColumns = new HashMap<EventTableColumn, Button>();
	private Map<EventTableColumn, Boolean> columnSelection;
	private Text exportDirectory;
	private Button btnChangeExportDirectory;
	private String exportFileName;
	private EventTableView tableView;
	
	protected CSVExportDialog(Shell parentShell, EventTableView tableView) {
		super(parentShell);

		this.tableView = tableView;
		this.exportFileName = tableView.getExportFileName();
		this.columnSelection = tableView.getColumnSelection();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite c = (Composite) super.createDialogArea(parent);
		c.setLayout(new GridLayout(2, false));
		
		Composite composite = new Composite(c, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite.setLayout(new GridLayout(2, false));
		
		final Group groupExportSettings = new Group(composite, SWT.NONE);
		groupExportSettings.setText("Export Settings");
		groupExportSettings.setLayout(new GridLayout(3, false));

		final Label lblExportDirectory = new Label(groupExportSettings, SWT.NONE);
		lblExportDirectory.setText("Export Directory:");

		final GridData gd_MiscDir = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_MiscDir.widthHint = 100;

		exportDirectory = new Text(groupExportSettings, SWT.BORDER);
		exportDirectory.setLayoutData(gd_MiscDir);
		exportDirectory.setText(System.getProperty("user.home") + "/"
				+ exportFileName);
		//exportDirectory.addModifyListener(new CheckDirectoryListener());

		btnChangeExportDirectory = new Button(groupExportSettings, SWT.PUSH);
		btnChangeExportDirectory.setLayoutData(new GridData(SWT.CENTER,
				SWT.CENTER, false, false, 1, 1));
		btnChangeExportDirectory.setToolTipText("Change export directory");
		btnChangeExportDirectory.setImage(ResourceManager.getPluginImage(
				"fr.inria.soctrace.framesoc.ui",
				"icons/fldr_obj.gif"));
		btnChangeExportDirectory.addSelectionListener(new ModifyExportDirectory());
		
		Composite compositeColumnSelection = new Composite(groupExportSettings, SWT.NONE);
		compositeColumnSelection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		compositeColumnSelection.setLayout(new GridLayout(1, false));
		
		final Label lblSelectedColumns = new Label(compositeColumnSelection, SWT.NONE);
		lblSelectedColumns.setText("Exported Columns:");
		
		for(EventTableColumn column: EventTableColumn.values()){
			Button aColumnBox = new Button(compositeColumnSelection, SWT.CHECK);
			aColumnBox.setSelection(true);
			aColumnBox.setText(column.toString());
			selectedColumns.put(column, aColumnBox);
		}

		return composite;
	}
	
	
	@Override
	protected void okPressed() {
		for (EventTableColumn column : EventTableColumn.values()) {
			columnSelection.put(column, selectedColumns.get(column)
					.getSelection());
		}
		
		tableView.setColumnSelection(columnSelection);
		tableView.setExportFileName(exportDirectory.getText());

		super.okPressed();
	}
	
	/**
	 * Set a customize title for the setting window
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Export CSV");
	}
	
	
	private class ModifyExportDirectory extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
			// Display a warning if the selected file already exists
			dialog.setOverwrite(true);

			// Set a default file name
			dialog.setFileName(exportFileName);

			String csvFilename = dialog.open();

			// Did the user cancel?
			if (csvFilename != null) {
				// Update the displayed path
				exportDirectory.setText(csvFilename);
			}
		}
	}
}
