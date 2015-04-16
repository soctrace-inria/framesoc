/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.model;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;

import fr.inria.soctrace.framesoc.core.bus.FramesocBus;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;

/**
 * Base class for all the actions to show a trace time interval in another view.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class TraceIntervalAction extends Action {

	/**
	 * Get the describing the time interval we want to show in another analysis view.
	 * 
	 * @return the <code>TraceIntervalDescriptor</code> we want to show
	 */
	public abstract TraceIntervalDescriptor getTraceIntervalDescriptor();

	/**
	 * Get the Framesoc bus topic this action will send a message on.
	 * 
	 * @return the Framesoc topic used by this action
	 */
	protected abstract FramesocBusTopic getTopic();
	
	@Override
	public void run() {
		TraceIntervalDescriptor des = getTraceIntervalDescriptor();
		// TODO check for CTRL pressed
		System.out.println(getAccelerator());
		if (des != null) {
			FramesocBus.getInstance().send(getTopic(), des);
		}
	}
}
