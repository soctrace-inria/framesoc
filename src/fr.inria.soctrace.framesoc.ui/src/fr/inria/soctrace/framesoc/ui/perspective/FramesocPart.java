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
package fr.inria.soctrace.framesoc.ui.perspective;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.core.bus.FramesocBus;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopicList;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusVariable;
import fr.inria.soctrace.framesoc.core.bus.IFramesocBusListener;
import fr.inria.soctrace.framesoc.ui.loaders.TraceLoader.TraceChange;
import fr.inria.soctrace.framesoc.ui.model.GanttTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.HistogramTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.PieTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.TableTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.TraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.TraceIntervalDescriptor;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.TraceQuery;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.SystemDBObject;

/**
 * Base class for Framesoc views displaying a trace (histogram, pie-chart, table, ...).
 * 
 * <p>
 * The views extending this class are also referred as Framesoc analysis views.
 * 
 * <p>
 * This base class factorizes the following functionalities:
 * <ul>
 * <li>correct secondary id management at view creation
 * <li>notification on the Framesoc Bus of the selected trace when the view is given focus
 * <li>unregister all followed topics (if any) at disposal
 * </ul>
 * 
 * <p>
 * Furthermore the class provides a protected method for basic trace alias change management (
 * {@link #handleCommonTopics(String, Object)}), to be called at the beginning of the
 * {@link IFramesocBusListener#handle(String, Object)} implementation in subclasses.
 * 
 * <p>
 * The class defines also the generic API to show a trace in a view, through the abstract method
 * {@link #showTrace(Trace, Object)}.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class FramesocPart extends ViewPart implements IFramesocBusListener {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(FramesocPart.class);

	/**
	 * Highlight start constant
	 */
	private final static String HIGHLIGHT_START = "> ";

	/**
	 * Followed topics
	 */
	protected FramesocBusTopicList topics = null;

	/**
	 * Current shown trace
	 */
	protected Trace currentShownTrace = null;

	/**
	 * Base class constructor, initialize the trace selection and the topics objects. Subclasses
	 * must invoke the super() method at the beginning of their constructors.
	 */
	public FramesocPart() {
		topics = new FramesocBusTopicList(this);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_TRACES_SYNCHRONIZED);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_REFRESH_TRACES_NEEDED);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_SYSTEM_INITIALIZED);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_FOCUSED_TRACE);
		topics.registerAll();
	}

	@Override
	public void createPartControl(final Composite parent) {

		// get the secondary id that the view was created with
		String secondaryId = getViewSite().getSecondaryId();

		// If the secondary id was null that means that Eclipse opened the view, and not us.
		// That's bad because it creates a view without a secondary ID and causes the view
		// to be "different" from one that we opened ourselves.
		if (secondaryId == null) {
			logger.debug("View created by Eclipse.");
			// we need to do it asynchronously as we can't close the view until the create code has
			// finished
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					// destroy ourselves
					logger.debug("Destroy the view created by Eclipse.");
					getViewSite().getPage().hideView(FramesocPart.this);
					// open the same view our way
					logger.debug("Create the view with a secondary ID.");
					FramesocPartManager.getInstance().getPartInstance(getId(), null, false);
				}
			});
			// return now as we're done with the non-secondary-id view
			return;
		}

		createFramesocPartControl(parent);
	}

	/**
	 * Notify on the bus the shown trace when the view gets the focus. Subclasses should call super
	 * setFocus at the end of their setFocus methods.
	 */
	@Override
	public void setFocus() {
		if (currentShownTrace != null) {
			logger.debug("current shown: {}", currentShownTrace.getAlias());
			Trace selected = ((Trace) FramesocBus.getInstance().getVariable(
					FramesocBusVariable.TRACE_VIEW_SELECTED_TRACE));
			// If selected is null or it is different than the current trace
			// then change the selected trace
			if (selected == null || !selected.equals(currentShownTrace)) {
				FramesocBus.getInstance().send(FramesocBusTopic.TOPIC_UI_FOCUSED_TRACE,
						currentShownTrace);
			}
		}
	}

	/**
	 * Dispose method: unregister all the topics. Subclasses must call super.dispose() at the end of
	 * their dispose methods.
	 */
	@Override
	public void dispose() {

		if (topics != null)
			topics.unregisterAll();

		// tell the view manager we've been disposed
		FramesocPartManager.getInstance().disposeFramesocPart(this);

		super.dispose();
	}

	/**
	 * Return the shown trace, if any
	 * 
	 * @return the shown trace or null if no trace is shown
	 */
	public Trace getCurrentShownTrace() {
		return currentShownTrace;
	}

	/**
	 * Check if a trace is already shown
	 * 
	 * @return true if a trace is already shown
	 */
	public boolean traceAlreadyShown() {
		return (currentShownTrace != null);
	}

	/**
	 * Activate the view
	 */
	public void activateView() {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				getSite().getPage().activate(FramesocPart.this);
			}
		});
	}

	/**
	 * Hide the view
	 */
	public void hideView() {
		// we need an asyncExec, otherwise we get a ConcurrentModificationException
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				getSite().getPage().hideView(FramesocPart.this);
			}
		});
	}

	public void higlightTitle(boolean highlight) {
		if (currentShownTrace != null) {
			String name = currentShownTrace.getAlias();
			logger.trace("Name before: '" + name + "'");
			if (highlight) {
				this.setPartName(HIGHLIGHT_START + getGroupString() + name);
			} else {
				this.setPartName(getGroupString() + name);
			}
			logger.trace("Name after: '" + name + "'");
		}
	}

	private String getGroupString() {
		int group = FramesocPartManager.getInstance().getPartGroup(currentShownTrace, this);
		if (group == FramesocPartManager.NO_GROUP) {
			return "";
		}
		if (FramesocPartManager.getInstance().isUniqueGroup(currentShownTrace, group)) {
			return "";
		}
		return "(" + (group + 1) + ") ";
	}

	@Override
	public void handle(FramesocBusTopic topic, Object data) {
		// manage the common topics
		handleCommonTopics(topic, data);

		// call the concrete class method
		partHandle(topic, data);
	}

	/**
	 * Handle the topics related to trace update/delete/selection.
	 * <ul>
	 * <li>TOPIC_UI_REFRESH_TRACES_NEEDED: the view title is updated
	 * <li>TOPIC_UI_TRACES_SYNCHRONIZED: updated or deleted shown trace
	 * <li>TOPIC_UI_SYSTEM_INITIALIZED: shown trace no more existing
	 * <li>TOPIC_UI_FOCUSED_TRACE: focused trace has changed
	 * </ul>
	 * 
	 * @param topic
	 *            bus topic
	 * @param data
	 *            bus event data
	 */
	private void handleCommonTopics(FramesocBusTopic topic, Object data) {
		if (currentShownTrace == null)
			return;
		Trace t = currentShownTrace;
		if (topic.equals(FramesocBusTopic.TOPIC_UI_REFRESH_TRACES_NEEDED)) {
			this.setPartName(t.getAlias());
		} else if (topic.equals(FramesocBusTopic.TOPIC_UI_TRACES_SYNCHRONIZED)) {
			@SuppressWarnings("unchecked")
			Map<TraceChange, List<Trace>> traceChangeMap = ((Map<TraceChange, List<Trace>>) data);
			if (traceChangeMap.get(TraceChange.REMOVE).contains(t)) {
				hideView();
			} else if (traceChangeMap.get(TraceChange.UPDATE).contains(t)) {
				this.setPartName(t.getAlias());
			}
		} else if (topic.equals(FramesocBusTopic.TOPIC_UI_SYSTEM_INITIALIZED)) {
			// check if the trace still exists
			SystemDBObject sysDB = null;
			boolean hide = false;
			try {
				sysDB = SystemDBObject.openNewIstance();
				TraceQuery tq = new TraceQuery(sysDB);
				tq.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, String
						.valueOf(t.getId())));
				List<Trace> ts = tq.getList();
				if (ts.isEmpty())
					hide = true;
				Iterator<Trace> it = ts.iterator();
				if (!it.hasNext())
					hide = true;
				if (!it.next().equals(t))
					hide = true;
			} catch (Exception e) {
				logger.error(e.getMessage());
				hide = true;
			} finally {
				DBObject.finalClose(sysDB);
				if (hide)
					hideView();
			}
		} else if (topic.equals(FramesocBusTopic.TOPIC_UI_FOCUSED_TRACE) && data != null) {
			logger.debug("Updating titles after TOPIC_UI_FOCUSED_TRACE");
			if (currentShownTrace.equals((Trace) data)) {
				logger.debug("Highlight " + getPartName());
				higlightTitle(true);
			} else {
				logger.debug("Unhighlight " + getPartName());
				higlightTitle(false);
			}
		}
	}

	/**
	 * Enable/disable all the toolbar actions.
	 * 
	 * @param enabled
	 *            enable flag
	 */
	protected void enableActions(boolean enabled) {
		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager toolBar = actionBars.getToolBarManager();
		for (IContributionItem item : toolBar.getItems()) {
			if (item instanceof ActionContributionItem) {
				((ActionContributionItem) item).getAction().setEnabled(enabled);
			}
		}
	}

	protected TraceIntervalAction createTableAction() {
		return new TableTraceIntervalAction(this) {
			@Override
			public TraceIntervalDescriptor getTraceIntervalDescriptor() {
				return getIntervalDescriptor();
			}
		};
	}

	protected TraceIntervalAction createGanttAction() {
		return new GanttTraceIntervalAction(this) {
			@Override
			public TraceIntervalDescriptor getTraceIntervalDescriptor() {
				return getIntervalDescriptor();
			}
		};
	}

	protected TraceIntervalAction createPieAction() {
		return new PieTraceIntervalAction(this) {
			@Override
			public TraceIntervalDescriptor getTraceIntervalDescriptor() {
				return getIntervalDescriptor();
			}
		};
	}

	protected TraceIntervalAction createHistogramAction() {
		return new HistogramTraceIntervalAction(this) {
			@Override
			public TraceIntervalDescriptor getTraceIntervalDescriptor() {
				return getIntervalDescriptor();
			}
		};
	}

	/**
	 * Return the trace interval descriptor corresponding to the current shown interval. The base
	 * class implementation returns the current shown trace and the whole trace duration.
	 * 
	 * @return the trace interval descriptor for the current shown interval
	 */
	protected TraceIntervalDescriptor getIntervalDescriptor() {
		if (currentShownTrace == null)
			return null;
		TraceIntervalDescriptor des = new TraceIntervalDescriptor();
		des.setTrace(currentShownTrace);
		des.setStartTimestamp(currentShownTrace.getMinTimestamp());
		des.setEndTimestamp(currentShownTrace.getMaxTimestamp());
		return des;
	}

	/**
	 * Method called in the handle() method of this base class.
	 * 
	 * The base class implementation does nothing. Subclasses may redefine this method to handle
	 * other topics, beside the common ones handled by this class (
	 * {@link #handleCommonTopics(String, Object)}).
	 * 
	 * @param topic
	 *            bus topic
	 * @param data
	 *            bus event data
	 */
	public void partHandle(FramesocBusTopic topic, Object data) {
	}

	/*
	 * Abstract methods
	 */

	/**
	 * Create the part control. This method is called in FramesocPart createPartControl after
	 * performing some checks on the secondary ID.
	 * 
	 * @param parent
	 */
	protected abstract void createFramesocPartControl(final Composite parent);

	/**
	 * Return the view ID.
	 * 
	 * @return the view ID.
	 */
	public abstract String getId();

	/**
	 * Show the passed trace, considering the parameters contained in data. These parameters are
	 * specific for different view types. See concrete Framesoc views documentation. Note that data
	 * is null when this method is called by the contextual menu in trace explorer, so in this case
	 * the view should implement a default behavior (e.g. show the whole trace).
	 * 
	 * @param trace
	 *            the trace that should to be shown.
	 * @param data
	 *            view specific data
	 * 
	 * @throws SoCTraceException
	 */
	public abstract void showTrace(Trace trace, Object data);

}
