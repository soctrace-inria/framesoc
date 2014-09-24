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
package fr.inria.soctrace.framesoc.ui.utils;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Spinner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;

/**
 * Class to wrap a SWT spinner in a ControlContribution.
 * 
 * <p>
 * The spinner is used to change the visualized page of events.
 * The operation to do when the page is changed is defined by 
 * the user, who must subclass this abstract class and provide
 * a Job to be executed at page change.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
@Deprecated
public abstract class PageSelector extends ControlContribution {
	
	/**
	 * Logger
	 */
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final int MIN_DELTA = 10;
	private Spinner spinner;
	private int min;
	private int max;
	private int loadedPage;
	private int lastEventTime;
	private String tooltip;
	
	/**
	 * Constructor
	 * @param tooltip tooltip message
	 */
	public PageSelector(String tooltip) {
		super(tooltip);
		this.min = 0;
		this.max = Integer.MAX_VALUE;
		this.loadedPage = 0;
		this.lastEventTime = 0;
		this.tooltip = tooltip;
		
	}
	
	/**
	 * Return the Job to run when a new valid page is set.
	 * The Job may assume that all the PageSelector fields are updated to 
	 * the new page.
	 * The Job run method must execute UI related methods only using 
	 * syncExec() or asyncExec().
	 * 
	 * @return a Job to be executed at page change
	 */
	protected abstract Job getModifyTextJob();

	
	/**
	 * Small workaround for spinner width
	 */
	@Override 
	protected int computeWidth(Control c) {
		if (c!=spinner)
			return super.computeWidth(c);
		else 
			return c.computeSize(50, SWT.DEFAULT, true).x;
	}
	
	@Override
	protected Control createControl(Composite parent)
	{
		spinner = new Spinner(parent, SWT.BORDER);
		spinner.setToolTipText(tooltip);
		spinner.setMinimum(min);
		spinner.setMaximum(max);
		spinner.setSelection(min);
		spinner.setIncrement(1);
		spinner.setPageIncrement(1);
		spinner.setEnabled(false);
		
		spinner.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				
				// fix for several events fired at the same time (probably a SWT bug)
				if (!deltaIsEnough(event.time,lastEventTime)) {
					spinner.setSelection(loadedPage);
					return;
				}
				else 
					lastEventTime = event.time;
				
				int page = 0;
				
				// check if it is a numbers
				try {
					page = Integer.valueOf(spinner.getText());
				} catch (NumberFormatException e) {
					logger.debug("Page not a number");
					spinner.setSelection(loadedPage);
					return;
				}

				//  check for limits
				if (page < min)
					page = min;
				else if (page > max)
					page = max;

				// check for page already loaded
				if (page == loadedPage) {
					logger.debug("Page already loaded: " + loadedPage);
					return;
				}
				
				// OK, we have to load the page.
				loadedPage = page;
				spinner.setSelection(loadedPage);
				
				/*
				 * Create the user provided job
				 */
				Job job = getModifyTextJob();
				
				job.setUser(true);
				job.schedule();
			}

			private boolean deltaIsEnough(int time, int lastEventTime) {
				return Math.abs( time - lastEventTime ) > MIN_DELTA; 
			}
		});

		return spinner;
	}	
	
	/*
	 * Methods not touching the UI: they can be executed in whatever thread. 
	 */
	
	/**
	 * @return the current trace smallest page
	 */
	public int getMinPage() {
		return min;
	}
	
	/**
	 * @return the current trace biggest page
	 */
	public int getMaxPage() {
		return max;
	}
	
	/**
	 * @return the current trace loaded page
	 */
	public int getLoadedPage() {
		return loadedPage;
	}

	/**
	 * Load min and max page values from the trace DB.
	 * It does not use the UI.
	 * @param trace trace to work with
	 */
	public void loadTraceData(Trace trace) {
		TraceDBObject traceDB = null;
		try {
			// get min/max page from DB
			traceDB = new TraceDBObject(trace.getDbName(), DBMode.DB_OPEN);
			min = (int)traceDB.getMinPage();
			max = (int)traceDB.getMaxPage();
		} catch (SoCTraceException e) {
			e.printStackTrace(); 
		} finally {
			DBObject.finalClose(traceDB);
		}
	}
	
	/*
	 * Methods to be executed in the UI thread 
	 */
	
	/**
	 * Reset page selector UI elements to present the smallest page.
	 */
	public void resetUi() {
		// load the first page
		loadedPage = min;
		spinner.setMinimum(min);
		spinner.setMaximum(max);
		spinner.setSelection(loadedPage);
		spinner.setEnabled(true);			
	}
	
	/**
	 * Reset page selector UI elements to present a given page.
	 * @param page page to present
	 */
	public void setUi(int page) {
		loadedPage = page;
		spinner.setMinimum(min);
		spinner.setMaximum(max);
		spinner.setSelection(loadedPage);
		spinner.setEnabled(true);						
	}
		
}
