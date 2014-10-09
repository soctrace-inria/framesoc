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
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TraceTableCache {

	private List<TraceTableRow> fSortedRows;
	private Map<Trace, TraceTableRow> fCache;
	private Map<Integer, Trace> fIndex;
	private TraceTableRowFilter fFilter;

	public void init(List<Trace> traces) {
		fFilter = new TraceTableRowFilter();

		fSortedRows = new ArrayList<>();
		fCache = new HashMap<>();
		for (Trace trace : traces) {
			TraceTableRow row = new TraceTableRow(trace);
			fCache.put(trace, row);
			fSortedRows.add(row);
		}
		Collections.sort(fSortedRows, new Comparator<TraceTableRow>() {
			@Override
			public int compare(TraceTableRow o1, TraceTableRow o2) {
				try {
					return o1.get(TraceTableColumn.ALIAS).compareTo(o2.get(TraceTableColumn.ALIAS));
				} catch (SoCTraceException e) {
					e.printStackTrace();
				}
				return 0;
			}
		});

		int index = 0;
		fIndex = new HashMap<>();
		for (TraceTableRow trace : fSortedRows) {
			fIndex.put(index++, trace.getTrace());
		}
	}

	public TraceTableRow get(int index) {
		return fCache.get(fIndex.get(index));
	}

	public void setSearchText(TraceTableColumn col, String string) {
		fFilter.setSearchText(col, string);
	}

	public void applyFilter() {
		// TODO apply sorter first
		int index = 0;
		fIndex = new HashMap<>();
		for (TraceTableRow t : fSortedRows) {
			if (fFilter.matches(t)) {
				fIndex.put(index++, t.getTrace());
			}
		}
	}

	public int getItemCount() {
		return fIndex.size();
	}

	public void cleanFilter() {
		fFilter.clean();
	}

	public void sort(final TraceTableColumn col, final int dir) {
		Collections.sort(fSortedRows, new Comparator<TraceTableRow>() {
			@Override
			public int compare(TraceTableRow o1, TraceTableRow o2) {
				try {
					// TODO manage long integer
					if (dir == SWT.UP) {
						return o1.get(col).compareTo(o2.get(col));
					} else {
						return o2.get(col).compareTo(o1.get(col));
					}
				} catch (SoCTraceException e) {
					e.printStackTrace();
				}
				return 0;
			}
		});

		int index = 0;
		fIndex = new HashMap<>();
		boolean hasFilters = fFilter.hasFilters();
		for (TraceTableRow row : fSortedRows) {
			if (!hasFilters || fFilter.matches(row)) {
				fIndex.put(index++, row.getTrace());
			}
		}
	}

}
