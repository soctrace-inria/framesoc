/*******************************************************************************
 * Copyright (c) 2012-2014 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Generoso Pagano - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package fr.inria.soctrace.tools.framesoc.exporter.dbexporter;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Combo;

import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.search.ITraceSearch;
import fr.inria.soctrace.lib.search.TraceSearch;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TraceComboManager {

	private Combo combo;
	private boolean autoSelect;
	private Map<Integer, Trace>  pos2trace 	= new HashMap<Integer, Trace>();

	public TraceComboManager(Combo combo, boolean autoSelect) {
		this.combo = combo;
		this.autoSelect = autoSelect;
	}

	public Trace getSelectedTrace() {
		if (pos2trace.containsKey(combo.getSelectionIndex()))
			return pos2trace.get(combo.getSelectionIndex());
		else 
			return null;
	}

	public void load(List<Trace> traces) {
		combo.removeAll();
		pos2trace.clear();

		int pos = 0;
		for (Trace t: traces) {
			pos2trace.put(pos, t);
			combo.add(t.getAlias(), pos);
			pos++;		
		}
		if (autoSelect && pos>0)
			combo.select(0);
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

	public Collection<Trace> getTraces() {
		return pos2trace.values();
	}

}
