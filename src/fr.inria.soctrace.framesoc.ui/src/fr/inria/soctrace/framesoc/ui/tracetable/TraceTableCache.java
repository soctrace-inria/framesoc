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

import fr.inria.soctrace.framesoc.ui.model.ITableColumn;
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
	private Map<String, TraceTableColumn> tableColumns;

	/**
	 * Initialize the cache with existing traces.
	 * 
	 * @param traces
	 *            traces
	 */
	public void init(List<Trace> traces) {
		fSortedRows = new ArrayList<>();
		tableColumns = new HashMap<String, TraceTableColumn>();
		
		for (TraceTableColumnEnum traceTableColumnEnum : TraceTableColumnEnum
				.values()) {
			tableColumns.put(traceTableColumnEnum.getHeader(),
					new TraceTableColumn(traceTableColumnEnum));
		}
		
		for (Trace trace : traces) {
			fSortedRows.add(new TraceTableRow(trace, this));
		}
		
		// Initialize custom parameters in every row
		for (TraceTableRow traceTablerow : fSortedRows) {
			traceTablerow.initValues(this);
		}

		// Init filter only after filling the rows, in order to have all the
		// column data
		fFilter = new TraceTableRowFilter(this);
		sort(tableColumns.get(TraceTableColumnEnum.ALIAS.getHeader()), SWT.UP);
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
	
	public Map<String, TraceTableColumn> getTableColumns() {
		return tableColumns;
	}

	/**
	 * Set the filter text for the given column.
	 * 
	 * @param col
	 *            column to filter
	 * @param string
	 *            filter text
	 */
	public void setFilterText(ITableColumn col, String string) {
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
	public void sort(final ITableColumn col, final int dir) {

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

				if (col.getHeader().equals(
						TraceTableColumnEnum.MAX_TIMESTAMP.getHeader()))
					return Long.compare(o1.getTrace().getMaxTimestamp(), o2
							.getTrace().getMaxTimestamp());
				if (col.getHeader().equals(
						TraceTableColumnEnum.MIN_TIMESTAMP.getHeader()))
					return Long.compare(o1.getTrace().getMinTimestamp(), o2
							.getTrace().getMinTimestamp());
				if (col.getHeader().equals(
						TraceTableColumnEnum.NUMBER_OF_CPUS.getHeader()))
					return Integer.compare(o1.getTrace().getNumberOfCpus(), o2
							.getTrace().getNumberOfCpus());
				if (col.getHeader().equals(
						TraceTableColumnEnum.NUMBER_OF_EVENTS.getHeader()))
					return Long.compare(o1.getTrace().getNumberOfEvents(),
							o2.getTrace().getNumberOfEvents());
				if (col.getHeader().equals(
						TraceTableColumnEnum.TRACING_DATE.getHeader()))
					return o1.getTrace().getTracingDate()
							.compareTo(o2.getTrace().getTracingDate());

				// Default:
				try {
					return o1.get(col).compareTo(o2.get(col));
				} catch (SoCTraceException e) {
					e.printStackTrace();
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
