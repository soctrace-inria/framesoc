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
package fr.inria.soctrace.framesoc.ui.gantt.view;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.stage.WindowEvent;
import javafx.scene.control.MenuItem;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphContentProvider;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider2;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphRangeListener;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphSelectionListener;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphTimeListener;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.TimeGraphCombo;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.TimeGraphRangeUpdateEvent;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.TimeGraphSelectionEvent;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.TimeGraphTimeEvent;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.TimeGraphViewer;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.dialogs.TimeGraphFilterDialog;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.ILinkEvent;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.widgets.Utils.TimeFormat;
import fr.inria.soctrace.framesoc.ui.model.TimeInterval;
import fr.inria.soctrace.framesoc.ui.model.TraceIntervalDescriptor;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocPart;
import fr.inria.soctrace.framesoc.ui.providers.EventTypeTreeLabelProvider;
import fr.inria.soctrace.framesoc.ui.providers.TreeContentProvider;
import fr.inria.soctrace.framesoc.ui.utils.TimeBar;
import fr.inria.soctrace.lib.model.utils.ModelConstants.TimeUnit;

/**
 * An abstract view all time graph views can inherit
 * 
 * This view contains a time graph combo which is divided between a tree viewer on the left and a
 * time graph viewer on the right.
 * 
 */
public abstract class AbstractGanttView extends FramesocPart {

	private static final Logger logger = LoggerFactory.getLogger(AbstractGanttView.class);

	/**
	 * Redraw state enum
	 */
	private enum State {
		IDLE, BUSY, PENDING
	}

	/** Suffix text for link percentage label */
	private final static String LINK_PERCENTAGE = "Percentage of displayed links: ";

	// ------------------------------------------------------------------------
	// Fields
	// ------------------------------------------------------------------------

	/** The timegraph wrapper */
	private ITimeGraphWrapper fTimeGraphWrapper;

	/** The timegraph entry list */
	private List<TimeGraphEntry> fEntryList;

	/** The start time */
	private long fStartTime;

	/** The end time */
	private long fEndTime;

	/**
	 * Flag indicating if the user changed the selection (via the timebar or the viewer)
	 */
	private boolean fUserChangedTimeRange = false;

	/** Flag indicating if the user changed the time range (zooming or panning) */
	private boolean fUserChangedSelection = false;

	/** Flag stating if we were dragging */
	private boolean fDragging = false;

	/** The min time */
	private long fMinTime;

	/** The max time */
	private long fMaxTime;

	/** The display width */
	private final int fDisplayWidth;

	/** The zoom thread */
	private ZoomThread fZoomThread;

	/** The next resource action */
	private Action fNextResourceAction;

	/** The previous resource action */
	private Action fPreviousResourceAction;
	
    protected Action fVZoomOutAction;
    protected Action fVZoomInAction;
    protected Action fResetScaleAction;

	/** A comparator class */
	private Comparator<ITimeGraphEntry> fEntryComparator = null;

	/**
	 * The redraw state used to prevent unnecessary queuing of display runnables
	 */
	private State fRedrawState = State.IDLE;

	/** The redraw synchronization object */
	private final Object fSyncObj = new Object();

	/** The presentation provider for this view */
	private final TimeGraphPresentationProvider fPresentation;

	/** The tree label provider, or null if combo is not used */
	private TimeGraphTreeLabelProvider fLabelProvider = null;

	/** The relative weight of the sash, ignored if combo is not used */
	private double[] fWeight = { 0.2 };

	/** The filter column label array, or null if filter is not used */
	private String[] fFilterColumns;

	/** The filter label provider, or null if filter is not used */
	private TimeGraphTreeLabelProvider fFilterLabelProvider;

	/** The pack done flag */
	private boolean fPackDone = false;

	/** The time management bar */
	private TimeBar fTimeBar;

	/** Label displaying arrow percentage */
	private Label arrowPercentageLabel;

	/** Type filter dialog */
	private TimeGraphFilterDialog fTypeFilterDialog;

	// ------------------------------------------------------------------------
	// Classes
	// ------------------------------------------------------------------------

	private interface ITimeGraphWrapper {

		void setTimeGraphProvider(TimeGraphPresentationProvider fPresentation);

		TimeGraphViewer getTimeGraphViewer();

		void addSelectionListener(ITimeGraphSelectionListener iTimeGraphSelectionListener);

		ISelectionProvider getSelectionProvider();

		void setFocus();

		boolean isDisposed();

		void refresh();

		void setInput(Object input);

		Object getInput();

		void redraw();

		void update();

	}

	private class TimeGraphComboWrapper implements ITimeGraphWrapper {
		private TimeGraphCombo combo;

		private TimeGraphComboWrapper(Composite parent, int style) {
			combo = new TimeGraphCombo(parent, style, fWeight);
		}

		@Override
		public void setTimeGraphProvider(TimeGraphPresentationProvider timeGraphProvider) {
			combo.setTimeGraphProvider(timeGraphProvider);
		}

		@Override
		public TimeGraphViewer getTimeGraphViewer() {
			return combo.getTimeGraphViewer();
		}

		@Override
		public void addSelectionListener(ITimeGraphSelectionListener listener) {
			combo.addSelectionListener(listener);
		}

		@Override
		public ISelectionProvider getSelectionProvider() {
			return null;
			//return combo.getTreeViewer();
		}

		@Override
		public void setFocus() {
			combo.setFocus();
		}

		@Override
		public boolean isDisposed() {
			return combo.isDisposed();
		}

		@Override
		public void setInput(Object input) {
			combo.setInput(input);
		}

		@Override
		public Object getInput() {
			return combo.getInput();
		}

		@Override
		public void refresh() {
			combo.refresh();
		}

		@Override
		public void redraw() {
			combo.getTimeGraphViewer().hasChanged();
			combo.redraw();
		}

		@Override
		public void update() {
			combo.update();
		}

		TimeGraphCombo getTimeGraphCombo() {
			return combo;
		}
	}

	private class TimeGraphTreeContentProvider implements ITreeContentProvider {

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public ITimeGraphEntry[] getElements(Object inputElement) {
			if (inputElement != null) {
				try {
					return ((List<?>) inputElement).toArray(new ITimeGraphEntry[0]);
				} catch (ClassCastException e) {
				}
			}
			return new ITimeGraphEntry[0];
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			ITimeGraphEntry entry = (ITimeGraphEntry) parentElement;
			List<? extends ITimeGraphEntry> children = entry.getChildren();
			return children.toArray(new ITimeGraphEntry[children.size()]);
		}

		@Override
		public Object getParent(Object element) {
			ITimeGraphEntry entry = (ITimeGraphEntry) element;
			return entry.getParent();
		}

		@Override
		public boolean hasChildren(Object element) {
			ITimeGraphEntry entry = (ITimeGraphEntry) element;
			return entry.hasChildren();
		}

	}

	private class TimeGraphContentProvider implements ITimeGraphContentProvider {

		@Override
		public ITimeGraphEntry[] getElements(Object inputElement) {
			if (inputElement != null) {
				try {
					return ((List<?>) inputElement).toArray(new ITimeGraphEntry[0]);
				} catch (ClassCastException e) {
				}
			}
			return new ITimeGraphEntry[0];
		}

	}

	/**
	 * Base class to provide the labels for the tree viewer. Views extending this class typically
	 * need to override the getColumnText method if they have more than one column to display
	 */
	protected static class TimeGraphTreeLabelProvider implements ITableLabelProvider,
			ILabelProvider {

		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			TimeGraphEntry entry = (TimeGraphEntry) element;
			if (entry != null && columnIndex == 0) {
				return entry.getName();
			}
			return new String();
		}

		@Override
		public Image getImage(Object element) {
			return null;
		}

		@Override
		public String getText(Object element) {
			TimeGraphEntry entry = (TimeGraphEntry) element;
			return entry.getName();
		}

	}

	private class ZoomThread extends Thread {
		private final long fZoomStartTime;
		private final long fZoomEndTime;
		private final long fResolution;
		private final IProgressMonitor fMonitor;

		public ZoomThread(List<TimeGraphEntry> entryList, long startTime, long endTime) {
			super("Zoom Thread"); //$NON-NLS-1$
			fZoomStartTime = startTime;
			fZoomEndTime = endTime;
			fResolution = Math.max(1, (fZoomEndTime - fZoomStartTime) / fDisplayWidth);
			fMonitor = new NullProgressMonitor();
		}

		@Override
		public void run() {
			logger.debug("Zoom thread run");
			/* Refresh the arrows when zooming */
			List<ILinkEvent> events = getLinkList(fZoomStartTime, fZoomEndTime, fResolution,
					fMonitor);
			if (events != null) {
				fTimeGraphWrapper.getTimeGraphViewer().setLinks(events);
				redraw();
			}
		}

		public void cancel() {
			fMonitor.setCanceled(true);
		}
	}

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	/**
	 * Constructs a time graph view that contains a time graph combo.
	 * 
	 * The subclass constructor must call {@link #setTreeColumns(String[])} and
	 * {@link #setTreeLabelProvider(TimeGraphTreeLabelProvider)}.
	 * 
	 * @param id
	 *            The id of the view
	 * @param pres
	 *            The presentation provider
	 */
	public AbstractGanttView(String id, TimeGraphPresentationProvider pres) {
		fPresentation = pres;
		fDisplayWidth = Display.getDefault().getBounds().width;
	}

	// ------------------------------------------------------------------------
	// Getters and setters
	// ------------------------------------------------------------------------

	/**
	 * Getter for the time graph combo
	 * 
	 * @return The time graph combo, or null if combo is not used
	 */
	protected TimeGraphCombo getTimeGraphCombo() {
		if (fTimeGraphWrapper instanceof TimeGraphComboWrapper) {
			return ((TimeGraphComboWrapper) fTimeGraphWrapper).getTimeGraphCombo();
		}
		return null;
	}

	/**
	 * Getter for the time graph viewer
	 * 
	 * @return The time graph viewer
	 */
	protected TimeGraphViewer getTimeGraphViewer() {
		return fTimeGraphWrapper.getTimeGraphViewer();
	}

	/**
	 * Getter for the presentation provider
	 * 
	 * @return The time graph presentation provider
	 * @since 3.0
	 */
	protected ITimeGraphPresentationProvider2 getPresentationProvider() {
		return fPresentation;
	}

	/**
	 * Sets the tree label provider. This should be called from the constructor.
	 * 
	 * @param tlp
	 *            The tree label provider
	 */
	protected void setTreeLabelProvider(final TimeGraphTreeLabelProvider tlp) {
		fLabelProvider = tlp;
	}

	/**
	 * Sets the relative weight of each part of the time graph combo. This should be called from the
	 * constructor.
	 * 
	 * @param weights
	 *            The array (length 2) of relative weights of each part of the combo
	 */
	protected void setWeight(final double[] weights) {
		fWeight = weights;
	}

	/**
	 * Sets the filter column labels. This should be called from the constructor.
	 * 
	 * @param filterColumns
	 *            The array of filter column labels
	 */
	protected void setFilterColumns(final String[] filterColumns) {
		fFilterColumns = filterColumns;
	}

	/**
	 * Sets the filter label provider. This should be called from the constructor.
	 * 
	 * @param labelProvider
	 *            The filter label provider
	 * 
	 * @since 3.0
	 */
	protected void setFilterLabelProvider(final TimeGraphTreeLabelProvider labelProvider) {
		fFilterLabelProvider = labelProvider;
	}

	/**
	 * Gets the display width
	 * 
	 * @return the display width
	 */
	protected int getDisplayWidth() {
		return fDisplayWidth;
	}

	/**
	 * Gets the comparator for the entries
	 * 
	 * @return The entry comparator
	 */
	protected Comparator<ITimeGraphEntry> getEntryComparator() {
		return fEntryComparator;
	}

	/**
	 * Sets the comparator class for the entries
	 * 
	 * @param comparator
	 *            A comparator object
	 */
	protected void setEntryComparator(final Comparator<ITimeGraphEntry> comparator) {
		fEntryComparator = comparator;
	}

	/**
	 * Gets the start time
	 * 
	 * @return The start time
	 */
	protected long getStartTime() {
		return fStartTime;
	}

	/**
	 * Sets the start time
	 * 
	 * @param time
	 *            The start time
	 */
	protected void setMinTime(long time) {
		fMinTime = time;
	}

	/**
	 * Sets the start time
	 * 
	 * @param time
	 *            The start time
	 */
	protected void setMaxTime(long time) {
		fMaxTime = time;
	}

	/**
	 * Sets the start time
	 * 
	 * @param time
	 *            The start time
	 */
	protected void setStartTime(long time) {
		fStartTime = time;
	}

	/**
	 * Gets the end time
	 * 
	 * @return The end time
	 */
	protected long getEndTime() {
		return fEndTime;
	}

	/**
	 * Sets the end time
	 * 
	 * @param time
	 *            The end time
	 */
	protected void setEndTime(long time) {
		fEndTime = time;
	}

	/**
	 * Gets the entry list for a trace
	 * 
	 * @param trace
	 *            the trace
	 * 
	 * @return the entry list map
	 * @since 3.0
	 */
	protected List<TimeGraphEntry> getEntryList() {
		return fEntryList;
	}

	/**
	 * Adds a list of entries to the entry list
	 * 
	 * @param list
	 *            the list of time graph entries to add
	 */
	protected void addToEntryList(List<TimeGraphEntry> list) {
		if (fEntryList == null) {
			fEntryList = new CopyOnWriteArrayList<>(list);
		} else {
			fEntryList.addAll(list);
		}
	}

	/**
	 * Removes a list of entries from a trace's entry list
	 * 
	 * @param trace
	 *            the trace
	 * @param list
	 *            the list of time graph entries to remove
	 * @since 3.0
	 */
	protected void removeFromEntryList(List<TimeGraphEntry> list) {
		if (fEntryList != null) {
			fEntryList.removeAll(list);
		}
	}

	/**
	 * Text for the "next" button
	 * 
	 * @return The "next" button text
	 */
	protected String getNextText() {
		return "Next";
	}

	/**
	 * Tooltip for the "next" button
	 * 
	 * @return Tooltip for the "next" button
	 */
	protected String getNextTooltip() {
		return "Next";
	}

	/**
	 * Text for the "Previous" button
	 * 
	 * @return The "Previous" button text
	 */
	protected String getPrevText() {
		return "Previous";
	}

	/**
	 * Tooltip for the "previous" button
	 * 
	 * @return Tooltip for the "previous" button
	 */
	protected String getPrevTooltip() {
		return "Previous";
	}
	
	public boolean isfUserChangedTimeRange() {
		return fUserChangedTimeRange;
	}

	public void setfUserChangedTimeRange(boolean fUserChangedTimeRange) {
		this.fUserChangedTimeRange = fUserChangedTimeRange;
	}

	// ------------------------------------------------------------------------
	// ViewPart
	// ------------------------------------------------------------------------

	@Override
	public void createFramesocPartControl(Composite parent) {

		GridLayout layout = new GridLayout(1, false);
		parent.setLayout(layout);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		arrowPercentageLabel = new Label(parent, SWT.NONE);
		setArrowPercentage(0.0);
		
		// -------------------------------
		// TYPE FILTER DIALOG
		// -------------------------------
		fTypeFilterDialog = new TimeGraphFilterDialog(parent.getShell());
		fTypeFilterDialog.setColumnNames(new String[] { "Event Type" });
		fTypeFilterDialog.setContentProvider(new TreeContentProvider());
		fTypeFilterDialog.setLabelProvider(new EventTypeTreeLabelProvider());


		// -------------------------------
		// COMBO VIEWER
		// -------------------------------

		Composite comboComposite = new Composite(parent, SWT.BORDER);
		comboComposite.setLayout(new FillLayout());
		comboComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		TimeGraphComboWrapper wrapper = new TimeGraphComboWrapper(comboComposite, SWT.NONE);
		fTimeGraphWrapper = wrapper;
		final TimeGraphCombo combo = wrapper.getTimeGraphCombo();

		// Event Producer Tree
		combo.setTreeContentProvider(new TimeGraphTreeContentProvider());
		combo.setTreeLabelProvider(fLabelProvider);

		// Event Producer Context Menu
		final ContextMenu menu = new ContextMenu();
		MenuItem exp = new MenuItem("Dummy item");
		menu.getItems().add(exp);
		
		menu.setOnShown(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent e) {
				menu.getItems().clear();
				final ITimeGraphEntry node = (ITimeGraphEntry) combo
						.getTreeViewer().getSelectionModel().getSelectedItem().getValue();
				
				if(node == null)
					return;
				
				// expand/collapse
				if (node.hasChildren()) {
					MenuItem exp = new MenuItem();
					final boolean expanded = combo.getTreeItems().get(node)
							.isExpanded();
					exp.setText(expanded ? "Collapse" : "Expand All");
					exp.setOnAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent actionEvent) {
							if (expanded) {
								combo.setExpandedState(node, false);
							} else {
								expandFromNode(node);
							}
						}

						private void expandFromNode(ITimeGraphEntry nodeToExtand) {
							combo.setExpandedState(nodeToExtand, true);
							for (ITimeGraphEntry entry : nodeToExtand
									.getChildren()) {
								expandFromNode(entry);
							}
						}
					});
					menu.getItems().add(exp);
					// restore
					Set<ITimeGraphEntry> filteredSet = getFiltered();
					List<ITimeGraphEntry> filteredChildren = new ArrayList<>();
					getFilteredChildren(filteredChildren, filteredSet, node);
					if (filteredChildren.size() > 0) {
						for (ITimeGraphEntry entry : filteredChildren) {
							filteredSet.remove(entry);
						}
						final List<Object> newFiltered = new ArrayList<>();
						for (ITimeGraphEntry entry : filteredSet) {
							newFiltered.add(entry);
						}
						MenuItem restore = new MenuItem(
								"Restore hidden children");

						restore.setOnAction(new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent actionEvent) {
								combo.setFilteredEntries(newFiltered);
								combo.refresh();
							}
						});
						menu.getItems().add(restore);
					}
				}
				// hide
				MenuItem hide = new MenuItem("Hide");
				hide.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent actionEvent) {
						ITimeGraphEntry entryNode = (ITimeGraphEntry) combo
								.getTreeViewer().getSelectionModel()
								.getSelectedItem();
						combo.addFiltered(entryNode);
						combo.refresh();
					}
				});
				menu.getItems().add(hide);
			}

			private void getFilteredChildren(
					List<ITimeGraphEntry> filteredChildren,
					Set<ITimeGraphEntry> filteredSet, ITimeGraphEntry entry) {
				for (ITimeGraphEntry e : entry.getChildren()) {
					if (filteredSet.contains(e)) {
						filteredChildren.add(e);
					}
					getFilteredChildren(filteredChildren, filteredSet, e);
				}
			}

			private Set<ITimeGraphEntry> getFiltered() {
				List<Object> filtered = combo.getFilteredEntries();
				Set<ITimeGraphEntry> filteredSet = new HashSet<>();
				for (Object e : filtered) {
					filteredSet.add(((ITimeGraphEntry) e));
				}
				return filteredSet;
			}
		});

	    //combo.getTreeViewer().setContextMenu(menu);

		// Event Producer Filter
		combo.setFilterContentProvider(new TimeGraphTreeContentProvider());
		combo.setFilterLabelProvider(fFilterLabelProvider);
		combo.setFilterColumns(fFilterColumns);

		// Gantt Chart
		combo.setTimeGraphContentProvider(new TimeGraphContentProvider());

		getTimeGraphViewer().setTimeGraphProvider(fPresentation);

		getTimeGraphViewer().getTimeGraphControl().addDragSelectionListener(
				new ITimeGraphTimeListener() {
					public void timeSelected(TimeGraphTimeEvent event) {
						if (fTimeBar != null) {
							fTimeBar.setSelection(event.getBeginTime(), event.getEndTime());
							fUserChangedSelection = true;
							fDragging = true;
						}
					}
				});

		fTimeGraphWrapper.getTimeGraphViewer().addRangeListener(new ITimeGraphRangeListener() {
			@Override
			public void timeRangeUpdated(TimeGraphRangeUpdateEvent event) {
				// visible start and end time changed by the user
				// (range updated after zoom or pan)
				fUserChangedTimeRange = true;
				final long startTime = event.getStartTime();
				final long endTime = event.getEndTime();
				if (fZoomThread != null) {
					fZoomThread.cancel();
				}
				startZoomThread(startTime, endTime);
			}
		});

		fTimeGraphWrapper.getTimeGraphViewer().addTimeListener(new ITimeGraphTimeListener() {
			@Override
			public void timeSelected(TimeGraphTimeEvent event) {
				logger.debug("timeSelected");
				if (!fDragging && fTimeBar != null) {
					// resynch timebar
					fTimeBar.setSelection(fStartTime, fEndTime);
					// clean user selection change
					fUserChangedSelection = false;
				}
				fDragging = false;
			}
		});

		fTimeGraphWrapper.addSelectionListener(new ITimeGraphSelectionListener() {
			@Override
			public void selectionChanged(TimeGraphSelectionEvent event) {
				logger.debug("selectionChanged");
			}
		});

		fTimeGraphWrapper.getTimeGraphViewer().setTimeFormat(TimeFormat.NUMBER);

		IStatusLineManager statusLineManager = getViewSite().getActionBars().getStatusLineManager();
		fTimeGraphWrapper.getTimeGraphViewer().getTimeGraphControl()
				.setStatusLineManager(statusLineManager);

		// -------------------------------
		// TIME MANAGEMENT BAR
		// -------------------------------

		Composite timeComposite = new Composite(parent, SWT.BORDER);
		timeComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout gl_timeComposite = new GridLayout(1, false);
		gl_timeComposite.horizontalSpacing = 0;
		timeComposite.setLayout(gl_timeComposite);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		// time manager
		fTimeBar = new TimeBar(timeComposite, SWT.NONE, true, true);
		fTimeBar.setEnabled(false);
		fTimeBar.setStatusLineManager(statusLineManager);
		fTimeBar.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (combo != null && fTimeBar != null) {
					combo.getTimeGraphViewer().setSelectionRange(fTimeBar.getStartTimestamp(),
							fTimeBar.getEndTimestamp());
					fUserChangedSelection = true;
				}
			}
		});
		// button to synch the timebar with the gantt
		fTimeBar.getSynchButton().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (combo != null && fTimeBar != null) {
					// clean selection
					combo.getTimeGraphViewer().setSelectionRange(0, 0);
					// re-synch timebar
					fTimeBar.setSelection(fStartTime, fEndTime);
					// clean user selection change
					fUserChangedSelection = false;
				}
			}
		});
		// draw button
		fTimeBar.getLoadButton().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// reset selection
				fTimeGraphWrapper.getTimeGraphViewer().setSelectionRange(0, 0);
				// load new interval
				TraceIntervalDescriptor des = new TraceIntervalDescriptor();
				des.setTrace(currentShownTrace);
				des.setTimeInterval(fTimeBar.getSelection());
				showTrace(currentShownTrace, des);
			}
		});

		// -------------------------------
		// TOOL BAR
		// -------------------------------

		// View Action Handling
		makeActions();
		contributeToActionBars();
		enableActions(false);

	}

	@Override
	public void setFocus() {
		super.setFocus();
		fTimeGraphWrapper.setFocus();
	}

	// ------------------------------------------------------------------------
	// Internal
	// ------------------------------------------------------------------------

	/**
	 * Draw the percentage of shown arrows
	 * 
	 * @param percentage
	 *            percentage of shown arrows
	 */
	protected void setArrowPercentage(double percentage) {
		DecimalFormat decim = new DecimalFormat("##.#");
		DecimalFormatSymbols custom = new DecimalFormatSymbols();
		custom.setDecimalSeparator('.');
		decim.setDecimalFormatSymbols(custom);
		Double percent = Double.parseDouble(decim.format(percentage));
		StringBuilder sb = new StringBuilder();
		sb.append(LINK_PERCENTAGE);
		sb.append(percent);
		sb.append("%");
		arrowPercentageLabel.setText(sb.toString());
		arrowPercentageLabel.pack();
	}

	/**
	 * Gets the list of links (displayed as arrows) for a trace in a given time range. Default
	 * implementation returns an empty list.
	 * 
	 * @param startTime
	 *            Start of the time range
	 * @param endTime
	 *            End of the time range
	 * @param resolution
	 *            The resolution
	 * @param monitor
	 *            The progress monitor object
	 * @return The list of link events
	 * @since 2.1
	 */
	protected List<ILinkEvent> getLinkList(long startTime, long endTime, long resolution,
			IProgressMonitor monitor) {
		return new ArrayList<>();
	}

	/**
	 * Refresh the display
	 */
	protected void refresh() {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				if (fTimeGraphWrapper.isDisposed()) {
					return;
				}

				// update colors
				fTimeGraphWrapper.getTimeGraphViewer().getTimeGraphControl()
						.colorSettingsChanged(fPresentation.getStateTable());

				boolean hasEntries = false;
				if (fEntryList == null) {
					fEntryList = new CopyOnWriteArrayList<>();
				} else if (fEntryComparator != null) {
					ArrayList<TimeGraphEntry> list = new ArrayList<>(fEntryList);
					sort(list);
					fEntryList.clear();
					fEntryList.addAll(list);
				}
				hasEntries = fEntryList.size() != 0;

				if (fEntryList != fTimeGraphWrapper.getInput()) {
					fTimeGraphWrapper.setInput(fEntryList);
				} else {
					fTimeGraphWrapper.refresh();
				}

				// set time unit
				if (currentShownTrace != null) {
					TimeUnit unit = TimeUnit.getTimeUnit(currentShownTrace.getTimeUnit());
					fTimeGraphWrapper.getTimeGraphViewer().setTimeUnit(unit);
					fTimeBar.setTimeUnit(unit);
					fPresentation.setTimeUnit(unit);
				}
				// set timebar bounds (min, max)
				fTimeBar.setMaxTimestamp(fMaxTime);
				fTimeBar.setMinTimestamp(fMinTime);
				// set the viewer time bounds (including all the loaded events)
				fTimeGraphWrapper.getTimeGraphViewer().setTimeBounds(fStartTime, fEndTime);
				// set the viewer start and end times (the visible part)
				if (!fUserChangedTimeRange) {
					fTimeGraphWrapper.getTimeGraphViewer().setStartFinishTime(fStartTime, fEndTime);
				}
				if (!fUserChangedSelection) {
					// reset selection in the viewer and set the bounds in the
					// timebar
					fTimeGraphWrapper.getTimeGraphViewer().setSelectionRange(0, 0);
					fTimeBar.setSelection(fStartTime, fEndTime);
				}
				fTimeBar.setDisplayInterval(fStartTime, fEndTime);
				if (fTimeGraphWrapper instanceof TimeGraphComboWrapper && !fPackDone) {
					/*for (TreeColumn column : ((TimeGraphComboWrapper) fTimeGraphWrapper)
							.getTreeViewer().getTree().getColumns()) {
						column.pack();
					}*/
					if (hasEntries) {
						fPackDone = true;
					}
				}

				fTimeBar.setEnabled(true);

				if (!fUserChangedTimeRange) {
					startZoomThread(fStartTime, fEndTime);
				}
			}
		});
	}

	/**
	 * Redraw the canvas
	 */
	protected void redraw() {
		synchronized (fSyncObj) {
			if (fRedrawState == State.IDLE) {
				fRedrawState = State.BUSY;
			} else {
				fRedrawState = State.PENDING;
				return;
			}
		}
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				logger.debug("refreshing");
				if (fTimeGraphWrapper.isDisposed()) {
					return;
				}
				fTimeGraphWrapper.redraw();
				fTimeGraphWrapper.update();
				synchronized (fSyncObj) {
					if (fRedrawState == State.PENDING) {
						fRedrawState = State.IDLE;
						redraw();
					} else {
						fRedrawState = State.IDLE;
					}
				}
			}
		});
	}

	/**
	 * Start the zoom thread.
	 * 
	 * @param startTime
	 *            current visible start time
	 * @param endTime
	 *            current visible end time
	 */
	private void startZoomThread(long startTime, long endTime) {
		if (fZoomThread != null) {
			fZoomThread.cancel();
		}
		fZoomThread = new ZoomThread(fEntryList, startTime, endTime);
		fZoomThread.start();
	}

	/**
	 * Refresh only the passed interval after a request to show a part of an already loaded window.
	 * 
	 * @param interval
	 *            time interval to show
	 */
	protected void refresh(TimeInterval interval) {
		fUserChangedSelection = false;
		fUserChangedTimeRange = false;
		setStartTime(interval.startTimestamp);
		setEndTime(interval.endTimestamp);
		refresh();
	}

	/**
	 * Add actions to local tool bar manager
	 * 
	 * @param manager
	 *            the tool bar manager
	 */
	protected abstract void fillLocalToolBar(IToolBarManager manager);

	protected IAction getPreviousResourceAction() {
		return fPreviousResourceAction;
	}
	
	protected IAction getNextResourceAction() {
		return fNextResourceAction;
	}
	
	/**
	 * Recursively sort the entries
	 * 
	 * @param list
	 *            list of entries
	 */
	private void sort(ArrayList<TimeGraphEntry> list) {
		Collections.sort(list, fEntryComparator);
		for (TimeGraphEntry entry : list) {
			if (!entry.hasChildren())
				continue;
			ArrayList<TimeGraphEntry> sl = new ArrayList<>(entry.getChildren());
			sort(sl);
			entry.getChildren().clear();
			entry.getChildren().addAll(sl);
		}
	}

	private void makeActions() {
		fPreviousResourceAction = fTimeGraphWrapper.getTimeGraphViewer().getPreviousItemAction();
		fPreviousResourceAction.setText(getPrevText());
		fPreviousResourceAction.setToolTipText(getPrevTooltip());
		fNextResourceAction = fTimeGraphWrapper.getTimeGraphViewer().getNextItemAction();
		fNextResourceAction.setText(getNextText());
		fNextResourceAction.setToolTipText(getNextTooltip());
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}

	protected void resetBeforeLoad() {
		fUserChangedSelection = false;
		fUserChangedTimeRange = false;
	}

	protected TimeGraphFilterDialog getTypeFilterDialog() {
		return fTypeFilterDialog;
	}

}
