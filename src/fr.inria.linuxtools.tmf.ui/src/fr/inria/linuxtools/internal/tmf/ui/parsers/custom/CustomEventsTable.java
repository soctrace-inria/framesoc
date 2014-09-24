/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package fr.inria.linuxtools.internal.tmf.ui.parsers.custom;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import fr.inria.linuxtools.tmf.core.event.ITmfEvent;
import fr.inria.linuxtools.tmf.core.parsers.custom.CustomEvent;
import fr.inria.linuxtools.tmf.core.parsers.custom.CustomTraceDefinition;
import fr.inria.linuxtools.tmf.core.parsers.custom.CustomTraceDefinition.OutputColumn;
import fr.inria.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import fr.inria.linuxtools.tmf.ui.widgets.virtualtable.ColumnData;

/**
 * Events table for custom text parsers.
 *
 * @author Patrick Tassé
 */
public class CustomEventsTable extends TmfEventsTable {

    private final CustomTraceDefinition fDefinition;

    /**
     * Constructor.
     *
     * @param definition
     *            Trace definition object
     * @param parent
     *            Parent composite of the view
     * @param cacheSize
     *            How many events to keep in cache
     */
    public CustomEventsTable(CustomTraceDefinition definition, Composite parent, int cacheSize) {
        super(parent, cacheSize, new ColumnData[0]);
        fDefinition = definition;
        createColumnHeaders();
    }

    /**
     * Create the table's headers.
     */
    protected void createColumnHeaders() {
        if (fDefinition == null) {
            return;
        }
        List<ColumnData> columnData = new LinkedList<>();
        for (OutputColumn outputColumn : fDefinition.outputs) {
            ColumnData column = new ColumnData(outputColumn.name, 0, SWT.LEFT);
            columnData.add(column);
        }
        setColumnHeaders(columnData.toArray(new ColumnData[0]));
    }

    @Override
    public String[] getItemStrings(ITmfEvent event) {
        if (event instanceof CustomEvent) {
            return ((CustomEvent) event).getEventStrings();
        }
        return EMPTY_STRING_ARRAY;
    }
}
