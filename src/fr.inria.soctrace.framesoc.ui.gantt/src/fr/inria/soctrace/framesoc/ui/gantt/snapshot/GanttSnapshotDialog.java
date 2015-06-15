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
package fr.inria.soctrace.framesoc.ui.gantt.snapshot;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.ResourceManager;

import fr.inria.soctrace.framesoc.ui.gantt.view.GanttView;

/**
 * Class that implements the options for taking snapshot of the Gantt chart
 * 
 * @author "Youenn Corre <youenn.corre@inria.fr>"
 */
public class GanttSnapshotDialog extends Dialog {
	private GanttView ganttView;

	private Text snapshotDirectory;
	private Button btnChangeSnapshotDirectory;
	private Button fullHeigtSelection, includeHeader;
	private Spinner snapshotWidth;
	private Spinner snapshotHeight;
	private Integer width;
	private Integer height;

	public GanttSnapshotDialog(Shell parentShell, GanttView ganttView,
			int defaultWidth, int defaultHeight) {
		super(parentShell);
		this.ganttView = ganttView;
		width = defaultWidth;
		height = defaultHeight;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite c = (Composite) super.createDialogArea(parent);
		c.setLayout(new GridLayout(2, false));

		Composite composite = new Composite(c, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));
		composite.setLayout(new GridLayout(2, false));

		final Group groupSnapshotSettings = new Group(composite, SWT.NONE);
		groupSnapshotSettings.setText("Snapshot Settings");
		groupSnapshotSettings.setLayout(new GridLayout(3, false));

		final Label lblSnapshotDirectory = new Label(groupSnapshotSettings,
				SWT.NONE);
		lblSnapshotDirectory.setText("Snapshot Directory:");

		final GridData gd_MiscDir = new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1);
		gd_MiscDir.widthHint = 300;

		snapshotDirectory = new Text(groupSnapshotSettings, SWT.BORDER);
		snapshotDirectory.setLayoutData(gd_MiscDir);
		snapshotDirectory.setText(System.getProperty("user.home"));
		snapshotDirectory.setToolTipText(snapshotDirectory.getText());
		snapshotDirectory.addModifyListener(new CheckDirectoryListener());

		btnChangeSnapshotDirectory = new Button(groupSnapshotSettings, SWT.PUSH);
		btnChangeSnapshotDirectory.setLayoutData(new GridData(SWT.CENTER,
				SWT.CENTER, false, false, 1, 1));
		btnChangeSnapshotDirectory.setToolTipText("Change snapshot directory");
		btnChangeSnapshotDirectory.setImage(ResourceManager
				.getPluginImage("fr.inria.soctrace.framesoc.ui.histogram",
						"icons/fldr_obj.gif"));
		btnChangeSnapshotDirectory
				.addSelectionListener(new ModifySnapshotDirectory());

		Label lblsnapshotWidth = new Label(groupSnapshotSettings, SWT.NONE);
		lblsnapshotWidth.setText("Snapshot Width:");

		snapshotWidth = new Spinner(groupSnapshotSettings, SWT.BORDER);
		final GridData spinnerGridData = new GridData(SWT.LEFT, SWT.CENTER,
				true, false, 1, 1);
		spinnerGridData.widthHint = 75;
		snapshotWidth.setLayoutData(spinnerGridData);
		snapshotWidth.setIncrement(1);
		snapshotWidth.setMaximum(100000);
		snapshotWidth.setMinimum(10);
		snapshotWidth.setSelection(width);
		snapshotWidth
				.setToolTipText("Width of the generated image in pixels (10 - 100000)");
		new Label(groupSnapshotSettings, SWT.NONE);

		Label lblsnapshotHeight = new Label(groupSnapshotSettings, SWT.NONE);
		lblsnapshotHeight.setText("Snapshot Height:");

		snapshotHeight = new Spinner(groupSnapshotSettings, SWT.BORDER);
		snapshotHeight.setLayoutData(spinnerGridData);
		snapshotHeight.setIncrement(1);
		snapshotHeight.setMaximum(100000);
		snapshotHeight.setMinimum(10);
		snapshotHeight.setSelection(height);
		snapshotHeight
				.setToolTipText("Height of the generated image in pixels  (10 - 100000)");
		new Label(groupSnapshotSettings, SWT.NONE);

		Composite optionComposite = new Composite(groupSnapshotSettings,
				SWT.NONE);
		optionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 1, 1));
		optionComposite.setLayout(new GridLayout(1, false));

		fullHeigtSelection = new Button(optionComposite, SWT.CHECK);
		fullHeigtSelection.setSelection(false);
		fullHeigtSelection.setText("Snapshot full height");
		fullHeigtSelection
				.setToolTipText("Snapshot the full height of the Gannt.");
		fullHeigtSelection.addSelectionListener(new FullHeightistener());

		includeHeader = new Button(optionComposite, SWT.CHECK);
		includeHeader.setSelection(false);
		includeHeader.setText("Include time scale");
		includeHeader.setToolTipText("Snapshot the time scale.");
		includeHeader.addSelectionListener(new FullHeightistener());

		return composite;
	}

	@Override
	protected void okPressed() {
		GanttSnapshot snapshot = new GanttSnapshot(snapshotDirectory.getText(),
				ganttView);
		snapshot.setHeight(snapshotHeight.getSelection());
		snapshot.setWidth(snapshotWidth.getSelection());
		snapshot.setWidth(snapshotWidth.getSelection());
		snapshot.setFullHeight(fullHeigtSelection.getSelection());
		snapshot.setIncludeHeader(includeHeader.getSelection());
		snapshot.takeSnapShot();

		super.okPressed();
	}

	/**
	 * Set a customize title for the setting window
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Snapshot");
	}

	private class CheckDirectoryListener implements ModifyListener {

		@Override
		public void modifyText(ModifyEvent e) {
			getButton(IDialogConstants.OK_ID).setEnabled(okEnabled());
		}

		private boolean okEnabled() {
			snapshotDirectory.setToolTipText(snapshotDirectory.getText());
			File dirPath = new File(snapshotDirectory.getText());

			if (dirPath.exists() && dirPath.isDirectory() && dirPath.canWrite())
				return true;

			return false;
		}
	}

	private class ModifySnapshotDirectory extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			DirectoryDialog dialog = new DirectoryDialog(getShell());
			String newSnapDir = dialog.open();
			// Did the user cancel?
			if (newSnapDir != null) {
				// Update the displayed path
				snapshotDirectory.setText(newSnapDir);
			}
		}
	}

	private class FullHeightistener extends SelectionAdapter {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			// Disable the height field if the full height option is selected
			snapshotHeight.setEnabled(!fullHeigtSelection.getSelection());
		}
	}
}
