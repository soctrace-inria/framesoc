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
/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.utils;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog for {@link TimeBar} parameters.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TimeBarSettingsDialog extends Dialog {

	private static final String TIME_BAR_SETTINGS_DIALOG_TITLE = "Settings";
	private long startL;
	private long endL;
	private long windowL;
	private boolean customWindow;
	private Text startText;
	private Text endText;
	private Text windowText;

	protected TimeBarSettingsDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		this.getShell().setText(TIME_BAR_SETTINGS_DIALOG_TITLE);
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(1, false));

		// Interval
		Group interval = new Group(composite, SWT.BORDER);
		interval.setText("Time Interval");
		interval.setLayout(new GridLayout(2, false));
		interval.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblStartTimestamp = new Label(interval, SWT.NONE);
		lblStartTimestamp.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblStartTimestamp.setText("Start timestamp");

		startText = new Text(interval, SWT.BORDER);
		startText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		startText.addVerifyListener(new TimeVerifyListener());
		startText.setText(String.valueOf(startL));

		Label lblEndTimestamp = new Label(interval, SWT.NONE);
		lblEndTimestamp.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblEndTimestamp.setText("End timestamp");

		endText = new Text(interval, SWT.BORDER);
		endText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		endText.addVerifyListener(new TimeVerifyListener());
		endText.setText(String.valueOf(endL));
		endText.setText(String.valueOf(endL));

		// Settings
		Group settings = new Group(composite, SWT.BORDER);
		settings.setText("Settings");
		settings.setLayout(new GridLayout(2, false));
		settings.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		final Button btnWindowSize = new Button(settings, SWT.CHECK);
		btnWindowSize.setText("Custom window size");
		btnWindowSize.setSelection(customWindow);;
	
		windowText = new Text(settings, SWT.BORDER);
		GridData gd_windowText = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_windowText.widthHint = 180;
		windowText.setLayoutData(gd_windowText);
		windowText.addVerifyListener(new TimeVerifyListener());
		windowText.setText(String.valueOf(windowL));
		windowText.setEnabled(customWindow);
		btnWindowSize.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				customWindow = btnWindowSize.getSelection();
				windowText.setEnabled(customWindow);
			}
		});

		return parent;
	}	

	@Override
	protected void okPressed() {
		startL = check(startText, Long.MIN_VALUE);
		endL = check(endText, Long.MAX_VALUE);
		windowL = check(windowText, Long.MAX_VALUE);
		super.okPressed();
	}

	private long check(Text text, long replacement) {
		try {
			return Long.valueOf(text.getText());
		} catch (NumberFormatException e) {
			return replacement;
		}
	}
	
	public long getStartTimestamp() {
		return startL;
	}

	public long getEndTimestamp() {
		return endL;
	}

	public long getWindowsSize() {
		return windowL;
	}

	public boolean getCustomWindow() {
		return customWindow;
	}
	
	private class TimeVerifyListener implements VerifyListener {

		@Override
		public void verifyText(VerifyEvent e) {
			if (!e.text.equals("") && !checkTime(e.text))
				e.doit = false;
		}

		private boolean checkTime(String txt) {
			try {
				Long.valueOf(txt);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		}
	}

	public void setStartTimestamp(long lowerValue) {
		this.startL = lowerValue;
	}

	public void setEndTimestamp(long upperValue) {
		this.endL = upperValue;
	}

	public void setWindowSize(long windowSize) {
		this.windowL = windowSize;
	}
	
	public void setCustomWindow(boolean customWindow) {
		this.customWindow = customWindow;
	}
}
