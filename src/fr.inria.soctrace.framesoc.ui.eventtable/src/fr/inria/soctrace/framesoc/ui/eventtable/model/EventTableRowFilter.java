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

package fr.inria.soctrace.framesoc.ui.eventtable.model;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Filter for a EventTableRow objects.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class EventTableRowFilter {

	private Map<EventTableColumn, String> searchStrings;

	public EventTableRowFilter() {
		searchStrings = new HashMap<>();
		for (EventTableColumn col : EventTableColumn.values()) {
			searchStrings.put(col, "");
		}
	}

	public void setSearchText(EventTableColumn col, String s) {
		searchStrings.put(col, s);
	}

	public boolean matches(EventTableRow row) {
		System.out.println(row);
		boolean matched = true;
		for (EventTableColumn col : EventTableColumn.values()) {
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
		System.out.println("matched: " + matched);
		return matched;
	}

}