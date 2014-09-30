/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.eventtable.view;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

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

	private final EventTableRow FROM = new EventTableRow();
	private final EventTableRow TO = new EventTableRow();

	/**
	 * Largest requested interval so far
	 */
	private TimeInterval fRequestedInterval;

	/**
	 * Currently loaded interval
	 */
	private TimeInterval fLoadededInterval;

	/**
	 * Cached event table rows
	 */
	private SortedSet<EventTableRow> fRows;

	/**
	 * View of the above rows in the active interval
	 */
	private SortedSet<EventTableRow> fActiveRows;

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
		updateInterval(row.getTimestamp(), fLoadededInterval);
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
		System.out.println("index: " + fIndex.size());
		System.out.println("rows: " + fRows.size());
		System.out.println("active: " + fActiveRows.size());
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
		FROM.setTimestamp(interval.startTimestamp);
		TO.setTimestamp(interval.endTimestamp + 1);
		fActiveRows = fRows.subSet(FROM, TO);
		index();
	}

	/**
	 * Re-index the cache, considering all the active rows.
	 */
	public synchronized void index() {
		fIndex = new HashMap<>();
		fCurrentIndex = 0;
		for (EventTableRow row : fActiveRows) {
			fIndex.put(fCurrentIndex, row);
			fCurrentIndex++;
		}
	}

	/**
	 * Get the total number of active lines
	 * 
	 * @return the number of active lines
	 */
	public synchronized int getActiveRowCount() {
		return fActiveRows.size();
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
		fRows = new TreeSet<>(new Comparator<EventTableRow>() {
			public int compare(EventTableRow r1, EventTableRow r2) {
				// never return 0 to have different events with the same timestamp
				if (r1.getTimestamp() > r2.getTimestamp())
					return 1;
				return -1;
			}
		});
		FROM.setTimestamp(Long.MIN_VALUE);
		TO.setTimestamp(Long.MAX_VALUE);
		fActiveRows = fRows.subSet(FROM, TO);
		fIndex = new HashMap<>();
		fRequestedInterval = new TimeInterval(Long.MAX_VALUE, Long.MIN_VALUE);
		fLoadededInterval = new TimeInterval(Long.MAX_VALUE, Long.MIN_VALUE);
		fCurrentIndex = 0;
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
