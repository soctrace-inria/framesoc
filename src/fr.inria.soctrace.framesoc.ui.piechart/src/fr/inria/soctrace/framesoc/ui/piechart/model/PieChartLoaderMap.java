/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.piechart.model;

import java.util.HashMap;
import java.util.Map;

import fr.inria.soctrace.framesoc.ui.model.TimeInterval;

/**
 * Map used by a statistics loader.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class PieChartLoaderMap {

	private Map<String, Double> fMap = new HashMap<>();
	private TimeInterval fInterval  = new TimeInterval(0, 0);
	private boolean fComplete;
	private boolean fStop;

	/**
	 * Put a snapshot for the map with the corresponding time interval.
	 * 
	 * @param snapshot
	 *            map snapshot
	 * @param interval
	 *            time interval
	 */
	public synchronized void putSnapshot(Map<String, Double> snapshot, TimeInterval interval) {
		fMap = new HashMap<>(snapshot);
		fInterval.copy(interval);
	}

	/**
	 * Get a snapshot of the map at a given time. The corresponding time interval is set in the
	 * passed parameter.
	 * 
	 * @param interval
	 *            output parameter, where the snapshot interval is set
	 * @return a snapshot of the map
	 */
	public synchronized Map<String, Double> getSnapshot(TimeInterval interval) {
		interval.copy(fInterval);
		return new HashMap<>(fMap);
	}

	/**
	 * Set the complete flag.
	 * 
	 * The map is complete when all the requested interval has been loaded.
	 * 
	 * @param complete
	 *            the complete flag to set.
	 */
	public synchronized void setComplete(boolean complete) {
		fComplete = complete;
	}

	/**
	 * Set the stop flag. 
	 * 
	 * This flag means that something bad happened and the map won't be complete.
	 * 
	 * @param stop
	 *            the stop flag to set.
	 */
	public synchronized void setStop(boolean stop) {
		fStop = stop;
	}
	
	/**
	 * 
	 * @return the complete flag
	 */
	public boolean isComplete() {
		return fComplete;
	}
	
	/**
	 * 
	 * @return the stop flag
	 */
	public boolean isStop() {
		return fStop;
	}
	
	/**
	 * Return true if the map is complete or the stop flag
	 * has been raised.
	 * 
	 * @return true if we are done
	 */
	public synchronized boolean done() {
		return fStop || fComplete;
	}

	public int size() {
		return fMap.size();
	}

}
