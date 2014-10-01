/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.eventtable.view;

import fr.inria.soctrace.framesoc.ui.eventtable.model.EventTableRow;
import fr.inria.soctrace.framesoc.ui.model.TimeInterval;
import fr.inria.soctrace.lib.utils.DeltaManager;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class CacheTest {

	public static void main(String[] args) {

		EventTableCache cache = new EventTableCache();
		int N = 10;
		DeltaManager dm = new DeltaManager();
		
		System.out.println("Loading " + N + " elements");
		dm.start();
		for (int i = 0; i < N; i++) {
			EventTableRow r;
			r = new EventTableRow();
			r.setTimestamp(i);
			cache.put(r);
			r = new EventTableRow();
			r.setTimestamp(i);
			cache.put(r);
		}
		cache.setRequestedInterval(new TimeInterval(0, 2*N - 1));
		dm.end("load");
		print(cache, 2*N);

		System.out.println("Sub interval: " + 1 + ", " + ((2*N - 1) / 2));
		dm.start();
		TimeInterval interval = new TimeInterval(1, (2*N - 1) / 2);
		if (!cache.contains(interval)) {
			System.out.println("Interval not contained");
			return;
		}
		cache.index(interval);
		dm.end("reindex");
		print(cache, cache.getActiveRowCount());

		System.out.println("Filtering all even lines");
		dm.start();
		int activeRows = cache.getActiveRowCount();
		int matched = 0;
		for (int i=0; i< activeRows; i++) {
			EventTableRow r = cache.get(i);
			if (r.getTimestamp() % 2 == 0) {
				cache.remap(r, matched++);
			}
		}
		cache.cleanIndex(matched);
		dm.end("filter");
		print(cache, matched);

	}

	private static void print(EventTableCache cache, int n) {
		for (int i = 0; i < n; i++) {
			EventTableRow r = cache.get(i);
			System.out.println("r: " + r.getTimestamp());
		}
	}
}
