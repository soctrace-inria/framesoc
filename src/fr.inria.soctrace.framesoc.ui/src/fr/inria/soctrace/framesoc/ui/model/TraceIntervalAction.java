/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.model;

import org.eclipse.jface.action.Action;

import fr.inria.soctrace.framesoc.core.bus.FramesocBus;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;

/**
 * TODO comment
 * 
 * Base class for all the actions to show a trace time interval in another view.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class TraceIntervalAction extends Action {

	public abstract TraceIntervalDescriptor getTraceIntervalDescriptor();

	protected abstract FramesocBusTopic getTopic();

	@Override
	public void run() {
		TraceIntervalDescriptor des = getTraceIntervalDescriptor();
		if (des != null) {
			FramesocBus.getInstance().send(getTopic(), des);
		}
	}
}
