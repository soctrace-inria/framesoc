/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.eventtable.view;

import org.eclipse.core.runtime.IProgressMonitor;

import fr.inria.soctrace.framesoc.ui.eventtable.model.LoaderQueue;
import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.Trace;

/**
 * Interface for event table event loaders.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public interface IEventLoader {

	/**
	 * Set the trace the loader must work with.
	 * 
	 * @param trace
	 *            trace
	 */
	void setTrace(Trace trace);

	/**
	 * Set the queue where the lists of events must be pushed.
	 * 
	 * @param queue
	 *            loader queue
	 */
	void setQueue(LoaderQueue<Event> queue);

	/**
	 * Load a time window for the trace set using {@link #setTrace()}, filling the queue set using
	 * {@link #setQueue()}. It has to be called in a Job, whose progress monitor is passed.
	 * 
	 * The contract is that the loader must call either {@link LoaderQueue#setComplete()} or
	 * {@link LoaderQueue#setStop()} at the end of its operations. This prevents any thread waiting
	 * for data to wait indefinitely.
	 * 
	 * @param start
	 *            start timestamp
	 * @param end
	 *            end timestamp
	 * @param monitor
	 *            progress monitor
	 */
	void loadWindow(long start, long end, IProgressMonitor monitor);

}
