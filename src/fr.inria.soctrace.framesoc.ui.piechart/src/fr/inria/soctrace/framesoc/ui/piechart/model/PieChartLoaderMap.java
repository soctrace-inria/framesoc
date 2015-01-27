/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.piechart.model;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import fr.inria.soctrace.framesoc.ui.model.TimeInterval;

/**
 * Map used by a statistics loader.
 * 
 * TODO dispose old framesoc colors
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class PieChartLoaderMap {

	private Map<String, Double> fMap = new HashMap<>();
	private TimeInterval fInterval = new TimeInterval(0, 0);
	private CountDownLatch fDoneLatch = new CountDownLatch(1);
	private boolean fComplete;
	private boolean fStop;
	private boolean fDirty;

	/**
	 * Set a snapshot for the map with the corresponding time interval.
	 * 
	 * @param snapshot
	 *            map snapshot
	 * @param interval
	 *            time interval
	 */
	public synchronized void setSnapshot(Map<String, Double> snapshot, TimeInterval interval) {
		fMap = new HashMap<>(snapshot);
		fInterval.copy(interval);
		fDirty = true;
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
		fDirty = false;
		return new HashMap<>(fMap);
	}

	/**
	 * Set the complete flag.
	 * 
	 * The map is complete when all the requested interval has been loaded.
	 */
	public synchronized void setComplete() {
		fComplete = true;
		fDoneLatch.countDown();
	}

	/**
	 * Set the stop flag.
	 * 
	 * This flag means that something bad happened and the map won't be complete. 
	 */
	public synchronized void setStop() {
		fStop = true;
		fDoneLatch.countDown();
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
	 * Check if the map is dirty.
	 * 
	 * The map is dirty if a snapshot has been set and it has not been read yet.
	 * 
	 * @return the dirty flag
	 */
	public boolean isDirty() {
		return fDirty;
	}

	/**
	 * Wait until we are done or the timeout elapsed.
	 * 
	 * @param timeout max wait timeout in milliseconds
	 * @param unit timeout unit
	 * @return true if we are done, false if the timeout elapsed
	 */
	public boolean waitUntilDone(long timeout) {
		boolean done = false;
		try {
			done = fDoneLatch.await(timeout, TimeUnit.MILLISECONDS);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		return done;
	}
	
	/**
	 * Get the number of items in the map.
	 * 
	 * @return the number of items
	 */
	public int size() {
		return fMap.size();
	}

	@Override
	public String toString() {
		return "PieChartLoaderMap [fMap.size()=" + fMap.size() + ", fInterval=" + fInterval + ", fComplete="
				+ fComplete + ", fStop=" + fStop + ", fDirty=" + fDirty + "]";
	}

}
