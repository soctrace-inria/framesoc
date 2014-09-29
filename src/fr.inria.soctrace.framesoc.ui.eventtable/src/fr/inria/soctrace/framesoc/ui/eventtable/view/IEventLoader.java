/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.eventtable.view;

import org.eclipse.core.runtime.IProgressMonitor;

import fr.inria.soctrace.framesoc.ui.eventtable.model.LoaderQueue;
import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.Trace;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public interface IEventLoader {

	void loadWindow(long startTimestamp, long endTimestamp, IProgressMonitor monitor);

	void setTrace(Trace trace);

	void setQueue(LoaderQueue<Event> queue);

}
