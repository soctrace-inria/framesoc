/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.eventtable.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fr.inria.soctrace.framesoc.ui.eventtable.model.EventTableRow;
import fr.inria.soctrace.framesoc.ui.model.TimeInterval;

/**
 * Event table row cache. It is used concurrently by the virtual table listener for new item
 * retrieval and the drawer thread, which populates it.
 * 
 * The filter thread, which re-index the cache, cannot update the index concurrently to the loading.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class EventTableCache {

	/**
	 * Largest requested interval so far
	 */
	private TimeInterval fRequestedInterval;

	/**
	 * Active interval
	 */
	private TimeInterval fActiveInterval;
		
	/**
	 * Cached event table rows
	 * TODO use sorted array
	 */
	private ArrayList<EventTableRow> fRows;

	/**
	 * Map between the table index and the row
	 */
	private Map<Integer, EventTableRow> fIndex;

	/**
	 * Current value for the index, while indexing
	 */
	private int fCurrentIndex;

	public EventTableCache() {
		internalClear();
	}

	/**
	 * Get the table row for the given index
	 * 
	 * @param index
	 *            table index
	 * @return the table row corresponding to the index
	 */
	public synchronized EventTableRow get(int index) {
		return fIndex.get(index);
	}

	/**
	 * Put a new row in the cache, assigning a new index.
	 * 
	 * @param row
	 *            the row to put in the cache
	 */
	public synchronized void put(EventTableRow row) {
		updateInterval(row.getTimestamp(), fActiveInterval);
		fRows.add(row);
		fIndex.put(fCurrentIndex, row);
		fCurrentIndex++;
	}

	/**
	 * Re-map an existing row in the cache to the given index.
	 * 
	 * @param row
	 *            the row to put in the cache
	 */
	public synchronized void remap(EventTableRow row, int index) {
		fIndex.put(index, row);
	}

	/**
	 * Remove all the entry of the index starting from the passed value.
	 * 
	 * @param index
	 *            the the last index to keep
	 */
	public synchronized void cleanIndex(int index) {
		int removeIndex = index;
		while (fIndex.containsKey(removeIndex)) {
			fIndex.remove(removeIndex);
			removeIndex++;
		}
	}

	/**
	 * Check if the passed intervals is contained in the currently largest requested interval. Note
	 * that a requested interval can be larger than the loaded one, since the loaded one correspond
	 * to the actual events actually present in the requested interval.
	 * 
	 * @param interval
	 *            interval to check
	 * @return true if the passed interval has already been loaded
	 */
	public synchronized boolean contains(TimeInterval interval) {
		if (interval.startTimestamp >= fRequestedInterval.startTimestamp
				&& interval.endTimestamp <= fRequestedInterval.endTimestamp)
			return true;
		return false;
	}

	/**
	 * Re-index the cache, considering the new interval.
	 * 
	 * @param interval
	 */
	public synchronized void index(TimeInterval interval) {
		fActiveInterval = new TimeInterval(interval);
		index();
	}

	/**
	 * Re-index the cache, considering all the active rows.
	 */
	public synchronized void index() {
		fIndex = new HashMap<>();
		fCurrentIndex = 0;
		for (EventTableRow row : fRows) {
			if (row.getTimestamp()<fActiveInterval.startTimestamp)
				continue;
			if (row.getTimestamp()>fActiveInterval.endTimestamp)
				continue;
			fIndex.put(fCurrentIndex, row);
			fCurrentIndex++;
		}
	}

	/**
	 * Get the total number of indexed rows
	 * 
	 * @return the number of indexed rows
	 */
	public synchronized int getIndexedRowCount() {
		return fIndex.size();
	}

	/**
	 * @return the requestedInterval
	 */
	public synchronized TimeInterval getRequestedInterval() {
		return fRequestedInterval;
	}

	/**
	 * @param requestedInterval
	 *            the requestedInterval to set
	 */
	public synchronized void setRequestedInterval(TimeInterval requestedInterval) {
		fRequestedInterval = requestedInterval;
	}

	/**
	 * Clear the cache
	 */
	public synchronized void clear() {
		internalClear();
	}

	/*
	 * Utils
	 */

	private void internalClear() {
		fRows = new ArrayList<>();
		fIndex = new HashMap<>();
		fCurrentIndex = 0;
		fRequestedInterval = new TimeInterval(Long.MAX_VALUE, Long.MIN_VALUE);
		fActiveInterval = new TimeInterval(Long.MAX_VALUE, Long.MIN_VALUE);
	}

	private void updateInterval(long timestamp, TimeInterval interval) {
		if (timestamp < interval.startTimestamp) {
			interval.startTimestamp = timestamp;
		}
		if (timestamp > interval.endTimestamp) {
			interval.endTimestamp = timestamp;
		}
	}

}
