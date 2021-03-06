/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Generoso Pagano - initial API and implementation
 ******************************************************************************/

package fr.inria.soctrace.framesoc.ui.tracetable;

import java.util.HashMap;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import fr.inria.soctrace.framesoc.ui.filter.TableRowFilter;
import fr.inria.soctrace.framesoc.ui.model.ITableRow;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Filter for a TraceTableRow objects.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TraceTableRowFilter extends TableRowFilter {
	
	private TraceTableCache traceTableCache;

	public TraceTableRowFilter(TraceTableCache traceTableCache) {
		this.traceTableCache = traceTableCache;
		// Reperform the clean after the cache has been initialized
		clean();
	}

	public boolean matches(ITableRow row) {
		boolean matched = true;
		for (TraceTableColumn col : traceTableCache.getTableColumns().values()) {
			String searchString = searchStrings.get(col);
			if (searchString == null || searchString.length() == 0) {
				continue;
			}
			try {
				if (!row.get(col).matches(searchString)) {
					matched = false;
					break;
				}
			} catch (PatternSyntaxException e) {
				MessageDialog.openError(Display.getDefault().getActiveShell(),
						"Wrong search string",
						"The expression used as search string is not valid: " + searchString);
				searchStrings.put(col, "");
				matched = false;
				break;
			} catch (SoCTraceException e) {
				e.printStackTrace();
				matched = false;
				break;
			}
		}
		return matched;
	}
	
	public void clean() {
		searchStrings = null;
		searchStrings = new HashMap<>();
		if (traceTableCache != null) {
			for (TraceTableColumn col : traceTableCache.getTableColumns()
					.values()) {
				searchStrings.put(col, "");
			}
		}
	}

}