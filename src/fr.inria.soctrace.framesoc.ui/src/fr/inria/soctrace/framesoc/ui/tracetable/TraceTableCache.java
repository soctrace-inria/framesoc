/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.tracetable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;

import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Cache for trace table rows. This cache is not aware of the filter rows. The first row actual has
 * index 0.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TraceTableCache {

	private List<TraceTableRow> fSortedRows;
	private Map<Integer, TraceTableRow> fIndex;
	private TraceTableRowFilter fFilter;

	/**
	 * Initialize the cache with existing traces.
	 * 
	 * @param traces
	 *            traces
	 */
	public void init(List<Trace> traces) {
		fFilter = new TraceTableRowFilter();
		fSortedRows = new ArrayList<>();
		for (Trace trace : traces) {
			fSortedRows.add(new TraceTableRow(trace));
		}
		sort(TraceTableColumn.ALIAS, SWT.UP);
	}

	/**
	 * Get the trace table row at the give table index.
	 * 
	 * @param index
	 *            row index
	 * @return the trace table row for the passed index
	 */
	public TraceTableRow get(int index) {
		return fIndex.get(index);
	}

	/**
	 * Get the number of visible rows.
	 * 
	 * @return the number of visible rows.
	 */
	public int getItemCount() {
		return fIndex.size();
	}

	/**
	 * Set the filter text for the given column.
	 * 
	 * @param col
	 *            column to filter
	 * @param string
	 *            filter text
	 */
	public void setFilterText(TraceTableColumn col, String string) {
		fFilter.setFilterText(col, string);
	}

	/**
	 * Apply the filter set.
	 */
	public void applyFilter() {
		int index = 0;
		fIndex = new HashMap<>();
		for (TraceTableRow t : fSortedRows) {
			if (fFilter.matches(t)) {
				fIndex.put(index++, t);
			}
		}
	}

	/**
	 * Clean the filter.
	 */
	public void cleanFilter() {
		fFilter.clean();
	}

	/**
	 * Sort the rows according to the given column in the given direction.
	 * 
	 * @param col
	 *            sort column
	 * @param dir
	 *            direction
	 */
	public void sort(final TraceTableColumn col, final int dir) {

		// sort rows
		Collections.sort(fSortedRows, new Comparator<TraceTableRow>() {
			@Override
			public int compare(TraceTableRow o1, TraceTableRow o2) {

				if (dir != SWT.UP) {
					// swap for reverse ordering
					TraceTableRow tmp = o1;
					o1 = o2;
					o2 = tmp;
				}

				switch (col) {
				case MAX_TIMESTAMP:
					return Long.compare(o1.getTrace().getMaxTimestamp(), o2.getTrace()
							.getMaxTimestamp());
				case MIN_TIMESTAMP:
					return Long.compare(o1.getTrace().getMinTimestamp(), o2.getTrace()
							.getMinTimestamp());
				case NUMBER_OF_CPUS:
					return Integer.compare(o1.getTrace().getNumberOfCpus(), o2.getTrace()
							.getNumberOfCpus());
				case NUMBER_OF_EVENTS:
					return Integer.compare(o1.getTrace().getNumberOfEvents(), o2.getTrace()
							.getNumberOfEvents());
				case TRACING_DATE:
					return o1.getTrace().getTracingDate().compareTo(o2.getTrace().getTracingDate());
				default:
					try {
						return o1.get(col).compareTo(o2.get(col));
					} catch (SoCTraceException e) {
						e.printStackTrace();
					}
				}
				return 0;
			}
		});

		// re-index visible rows
		int index = 0;
		fIndex = new HashMap<>();
		boolean hasFilters = fFilter.hasFilters();
		for (TraceTableRow row : fSortedRows) {
			if (!hasFilters || fFilter.matches(row)) {
				fIndex.put(index++, row);
			}
		}
	}

}
