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

import fr.inria.soctrace.lib.model.Trace;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TraceTableCache {

	private List<Trace> fSortedTraces;
	private Map<Trace, TraceTableRow> fCache;
	private Map<Integer, Trace> fIndex;
	private TraceTableRowFilter fFilter;
	
	public void init(List<Trace> traces) {
		fFilter = new TraceTableRowFilter();
		
		fSortedTraces = new ArrayList<>();
		fCache = new HashMap<>();
		for (Trace trace : traces) {
			fCache.put(trace, new TraceTableRow(trace));
			fSortedTraces.add(trace);
		}
		Collections.sort(fSortedTraces, new Comparator<Trace>() {
			@Override
			public int compare(Trace o1, Trace o2) {
				return o1.getAlias().compareTo(o2.getAlias());
			}
		});
		
		int index = 0;
		fIndex = new HashMap<>();
		for (Trace trace : fSortedTraces) {
			fIndex.put(index++, trace);
		}
		System.out.println(fSortedTraces);
		
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
		for (Trace t : fSortedTraces) {
			if (fFilter.matches(fCache.get(t))) {
				fIndex.put(index++, t);
			}
		}
	}

	public int getItemCount() {
		return fIndex.size();
	}

	public void cleanFilter() {
		fFilter.clean();
	}
	
}
