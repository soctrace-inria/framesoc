/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.eventtable.view;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
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

	// logger
	// private static final Logger logger = LoggerFactory.getLogger(EventTableCache.class);

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
	 * Active row iterator
	 */
	private Iterator<EventTableRow> fActiveRowIterator;

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
	 * Put a new row in the cache.
	 * 
	 * @param row
	 *            the row to put in the cache
	 */
	public synchronized void put(EventTableRow row) {
		updateInterval(row.getTimestamp(), fLoadededInterval);
		fRows.add(row);
		fIndex.put(fCurrentIndex, row);
		// logger.debug("index: {}, row: {}", fCurrentIndex, row);
		// logger.debug("frows: {}", fRows.size());
		// logger.debug("factiverows: {}", fActiveRows.size());
		// logger.debug("findex: {}", fIndex.size());
		fCurrentIndex++;
	}

	/**
	 * Get the active row iterator. This method is called only by the filter. It is not possible to
	 * filter during loading. Note that each call to this method resets the active row iterator.
	 * 
	 * @return the active row iterator
	 */
	public Iterator<EventTableRow> activeRowIterator() {
		fActiveRowIterator = fActiveRows.iterator();
		return fActiveRowIterator;
	}

	/**
	 * Get the next active row, using the active iterator.
	 * 
	 * @return the next active row
	 */
	public EventTableRow getNextActive() {
		return fActiveRowIterator.next();
	}

	/**
	 * Check if there is another active row.
	 * 
	 * @return true if there is another active row
	 */
	public boolean hasNextActive() {
		return fActiveRowIterator.hasNext();
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
	 * Clear the index
	 */
	public synchronized void clearIndex() {
		fIndex = new HashMap<>();
		fCurrentIndex = 0;
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
	public boolean contains(TimeInterval interval) {
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
	public TimeInterval getRequestedInterval() {
		return fRequestedInterval;
	}

	/**
	 * @param requestedInterval
	 *            the requestedInterval to set
	 */
	public void setRequestedInterval(TimeInterval requestedInterval) {
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
