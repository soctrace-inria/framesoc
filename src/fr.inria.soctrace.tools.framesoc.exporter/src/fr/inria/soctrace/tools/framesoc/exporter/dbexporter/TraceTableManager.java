package fr.inria.soctrace.tools.framesoc.exporter.dbexporter;

/* Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Generoso Pagano - initial API and implementation
 ******************************************************************************/

import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.search.ITraceSearch;
import fr.inria.soctrace.lib.search.TraceSearch;

/**
 * @author "Youenn Corre <youenn.corre@inria.fr>"
 */
public class TraceTableManager {

	private Table table;

	public TraceTableManager(Table table) {
		this.table = table;
	}

	public List<Trace> getSelectedTraces() {
		List<Trace> traces = new ArrayList<Trace>();
		for (TableItem aTableItem : table.getItems()) {
			if(aTableItem.getChecked())
				traces.add((Trace) aTableItem.getData());
		}

		return traces;
	}

	public void load(List<Trace> traces) {
		table.removeAll();

		for (Trace t : traces) {
			TableItem item = new TableItem(table, SWT.NONE);
			// Associate the trace with the item
			item.setData(t);
			item.setText(t.getAlias());
		}
	}

	public void loadAll() {
		ITraceSearch search = null;
		try {
			search = new TraceSearch().initialize();
			load(search.getTraces());
			search.uninitialize();
		} catch (SoCTraceException e) {
			e.printStackTrace();
		} finally {
			TraceSearch.finalUninitialize(search);
		}
	}

	public void loadAll(ITraceSearch search) {
		try {
			load(search.getTraces());
		} catch (SoCTraceException e) {
			e.printStackTrace();
		}
	}

}
