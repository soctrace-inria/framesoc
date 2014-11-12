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
/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.utils;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.ui.Activator;
import fr.inria.soctrace.framesoc.ui.model.TimeInterval;

/**
 * Time Bar widget, including a {@link RangeSlider}.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TimeBar {

	/**
	 * Logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(TimeBar.class);

	private boolean customWindowSize = false;
	private long windowSize;

	private Composite parent;
	private Button prev;
	private Button next;
	private Button all;
	private Button settings;
	private Button synch;
	private Button load;
	private RangeSlider range;

	public TimeBar(Composite parent, int style, boolean hasSynch, boolean hasLoad) {

		this.parent = parent;

		// Time slider bar
		Composite sliderBar = new Composite(parent, style);
		GridLayout gl_sliderBar = new GridLayout(7, false);
		gl_sliderBar.horizontalSpacing = 1;
		gl_sliderBar.marginHeight = 0;
		gl_sliderBar.verticalSpacing = 0;
		gl_sliderBar.marginWidth = 0;
		sliderBar.setLayout(gl_sliderBar);
		sliderBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		prev = new Button(sliderBar, SWT.NONE);
		GridData gd_prev = new GridData(SWT.RIGHT, SWT.BOTTOM, false, false, 1, 1);
		gd_prev.heightHint = 28;
		prev.setLayoutData(gd_prev);
		prev.setText("<");
		prev.setToolTipText("Previous time window");
		prev.addSelectionListener(new PreviousWindowListener());
		range = new RangeSlider(sliderBar, SWT.HORIZONTAL);
		range.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		range.setMinimum(0);
		range.setMaximum(1000000);
		range.setLowerValue(0);
		range.setUpperValue(0);
		range.setShowGrads(true);
		next = new Button(sliderBar, SWT.NONE);
		GridData gd_next = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_next.heightHint = 28;
		next.setLayoutData(gd_next);
		next.setToolTipText("Next time window");
		next.setText(">");
		next.addSelectionListener(new NextWindowListener());
		all = new Button(sliderBar, SWT.NONE);
		all.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		all.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				range.setSelection(range.getMinimum(), range.getMaximum(), true);
			}
		});
		all.setToolTipText("Select whole time interval");
		all.setImage(ResourceManager.getPluginImage(Activator.PLUGIN_ID, "icons/all_timebar.png"));
		settings = new Button(sliderBar, SWT.NONE);
		settings.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		settings.addSelectionListener(new EditListener(parent.getShell()));
		settings.setToolTipText("Manual editing");
		settings.setImage(ResourceManager.getPluginImage(Activator.PLUGIN_ID, "icons/edit2.png"));

		if (hasSynch) {
			synch = new Button(sliderBar, SWT.NONE);
			synch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			synch.setImage(ResourceManager.getPluginImage(Activator.PLUGIN_ID, "icons/load.png"));
		}

		if (hasLoad) {
			load = new Button(sliderBar, SWT.NONE);
			load.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			load.setToolTipText("Draw current selection");
			load.setImage(ResourceManager.getPluginImage(Activator.PLUGIN_ID, "icons/play.png"));
		}
	}

	/**
	 * Get the load button. It may be null.
	 * 
	 * @return the load button.
	 */
	public Button getLoadButton() {
		return load;
	}

	/**
	 * Get the synch button. It may be null.
	 * 
	 * @return the synch button
	 */
	public Button getSynchButton() {
		return synch;
	}

	class NextWindowListener extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			long actualWindowSize = windowSize;
			if (!customWindowSize)
				actualWindowSize = range.getUpperValue() - range.getLowerValue();
			long newEnd = Math.min(range.getUpperValue() + actualWindowSize, range.getMaximum());
			long newStart = Math.max(newEnd - actualWindowSize, range.getMinimum());
			range.setSelection(newStart, newEnd, true);
		}
	}

	class PreviousWindowListener extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			long actualWindowSize = windowSize;
			if (!customWindowSize)
				actualWindowSize = range.getUpperValue() - range.getLowerValue();
			long newStart = Math.max(range.getLowerValue() - actualWindowSize, range.getMinimum());
			long newEnd = Math.min(newStart + actualWindowSize, range.getMaximum());
			range.setSelection(newStart, newEnd, true);
		}
	}

	class EditListener extends SelectionAdapter {
		private Shell shell;

		public EditListener(Shell parent) {
			this.shell = parent;
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			TimeBarSettingsDialog dlg = new TimeBarSettingsDialog(shell);
			dlg.setStartTimestamp(range.getLowerValue());
			dlg.setEndTimestamp(range.getUpperValue());
			dlg.setCustomWindow(customWindowSize);

			if (customWindowSize)
				dlg.setWindowSize(windowSize);
			else
				dlg.setWindowSize(range.getUpperValue() - range.getLowerValue());

			if (dlg.open() == Window.OK) {
				// get
				long start = dlg.getStartTimestamp();
				long end = dlg.getEndTimestamp();
				long window = dlg.getWindowsSize();
				// check
				if (start < range.getMinimum())
					start = range.getMinimum();
				if (end > range.getMaximum())
					end = range.getMaximum();
				if (window > range.getMaximum() - range.getMinimum())
					window = range.getMaximum() - range.getMinimum();
				// set
				windowSize = window;
				range.setSelection(start, end, true);
				customWindowSize = dlg.getCustomWindow();
			}
		}
	}

	/**
	 * Add a selection listener to the range slider
	 * 
	 * @param listener
	 */
	public void addSelectionListener(SelectionListener listener) {
		range.addSelectionListener(listener);
	}

	/**
	 * @return the startTimestamp
	 */
	public long getStartTimestamp() {
		return range.getLowerValue();
	}

	/**
	 * @return the endTimestamp
	 */
	public long getEndTimestamp() {
		return range.getUpperValue();
	}

	/**
	 * Get a new time interval corresponding to the current selection
	 * 
	 * @return the current selection
	 */
	public TimeInterval getSelection() {
		return new TimeInterval(range.getLowerValue(), range.getUpperValue());
	}

	/**
	 * @return the windowSize
	 */
	public long getWindowSize() {
		return windowSize;
	}

	/**
	 * @param windowSize
	 *            the windowSize to set
	 */
	public void setWindowSize(long windowSize) {
		this.windowSize = windowSize;
	}

	public void setExtrema(long min, long max) {
		logger.debug("extrema:" + min + ", " + max);
		this.range.setExtrema(min, max);
	}

	public void setMaxTimestamp(long max) {
		logger.debug("set max:" + max);
		this.range.setMaximum(max);
	}

	public void setMinTimestamp(long min) {
		logger.debug("set min:" + min);
		this.range.setMinimum(min);
	}

	public void setEnabled(boolean enabled) {
		prev.setEnabled(enabled);
		next.setEnabled(enabled);
		all.setEnabled(enabled);
		settings.setEnabled(enabled);
		range.setEnabled(enabled);
		if (synch != null) {
			synch.setEnabled(enabled);
		}
		if (load != null) {
			load.setEnabled(enabled);
		}
	}

	/**
	 * Set the selection without notifying listeners
	 * 
	 * @param startTimestamp
	 *            start timestamp
	 * @param endTimestamp
	 *            end timestamp
	 */
	public void setSelection(long startTimestamp, long endTimestamp) {
		range.setSelection(startTimestamp, endTimestamp, false);
	}

	/**
	 * Set the selection copying the time interval start and end timestamps without notifying
	 * listeners
	 * 
	 * @param timeInterval
	 *            the time interval to select
	 */
	public void setSelection(TimeInterval timeInterval) {
		setSelection(timeInterval.startTimestamp, timeInterval.endTimestamp);
	}

	/**
	 * Explicitly dispose the parent, since this class does not extend composite.
	 */
	public void dispose() {
		parent.dispose();
	}

	/**
	 * Returns true if any of the graphical objects has been disposed
	 * 
	 * @return true if disposed
	 */
	public boolean isDisposed() {
		return parent.isDisposed() || prev.isDisposed() || next.isDisposed()
				|| settings.isDisposed() || range.isDisposed();
	}

	@Override
	public String toString() {
		return "TimeBar [start=" + range.getLowerValue() + ", end=" + range.getUpperValue() + ", "
				+ "min=" + range.getMinimum() + ", max=" + range.getMaximum() + "]";
	}

	public void setStatusLineManager(IStatusLineManager manager) {
		range.setStatusLineManager(manager);
	}

}
