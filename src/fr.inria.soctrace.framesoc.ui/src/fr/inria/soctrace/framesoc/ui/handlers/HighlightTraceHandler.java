/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import fr.inria.soctrace.framesoc.core.bus.FramesocBus;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.lib.model.Trace;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class HighlightTraceHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		List<Trace> traces = HandlerCommons.getSelectedTraces(event);
		FramesocBus.getInstance().send(FramesocBusTopic.TOPIC_UI_HIGHLIGHT_TRACES, traces);
		return null;
	}

}
