/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.loaders;

import org.eclipse.core.runtime.Assert;

import fr.inria.soctrace.lib.model.Trace;

/**
 * Class providing utility methods for UI data loaders.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public final class LoaderUtils {

	/**
	 * Given a trace and an average number we want to load at each query, it computes interval
	 * duration we have to load from the DB.
	 * 
	 * @param trace
	 * @param eventPerQuery
	 * @return
	 * @throws AssertionFailedException
	 *             if the trace duration is <=0, if the event density is <=0 and if the computed
	 *             interval duration is <=0
	 */
	public static long getIntervalDuration(Trace trace, int eventPerQuery) {
		long duration = trace.getMaxTimestamp() - trace.getMinTimestamp();
		Assert.isTrue(duration > 0, "The trace duration cannot be 0");
		double density = ((double) trace.getNumberOfEvents()) / duration;
		Assert.isTrue(density > 0, "The density cannot be 0");
		long intervalDuration = (long) (eventPerQuery / density);
		Assert.isTrue(intervalDuration > 0, "The interval duration must be positive");
		return intervalDuration;
	}
}
