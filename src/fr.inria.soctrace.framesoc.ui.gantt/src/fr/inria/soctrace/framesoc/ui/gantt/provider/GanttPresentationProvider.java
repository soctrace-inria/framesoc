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
package fr.inria.soctrace.framesoc.ui.gantt.provider;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.RGB;

import fr.inria.linuxtools.tmf.ui.widgets.timegraph.StateItem;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.TimeEvent;
import fr.inria.soctrace.framesoc.ui.colors.FramesocColor;
import fr.inria.soctrace.framesoc.ui.colors.FramesocColorManager;
import fr.inria.soctrace.framesoc.ui.gantt.GanttView;
import fr.inria.soctrace.lib.model.EventType;

/**
 * Presentation provider for Gantt view.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class GanttPresentationProvider extends TimeGraphPresentationProvider {
		
	private Map<Integer, Integer> typeIndex = new HashMap<Integer, Integer>();
	private StateItem[] stateTable;
	
    /**
     * Default constructor
     */
    public GanttPresentationProvider() {
        super(GanttView.PRODUCER);
    }

    public void setTypes(Collection<EventType> types) {
    	typeIndex = new HashMap<Integer, Integer>();
    	stateTable = new StateItem[types.size()];
    	int i=0;
    	for (EventType type: types) {
        	FramesocColor color = FramesocColorManager.getInstance().getEventTypeColor(type.getName());
        	RGB rgb = new RGB(color.red, color.green, color.blue);
        	stateTable[i] = new StateItem(rgb, type.getName());    
        	typeIndex.put(type.getId(), i);
        	i++;
    	}
    }
    
    public void updateColors() {
    	for (StateItem item: stateTable) {
        	FramesocColor color = FramesocColorManager.getInstance().getEventTypeColor(item.getStateString());
        	RGB rgb = new RGB(color.red, color.green, color.blue);
        	item.setStateColor(rgb);    
    	}
    }
    
    @Override
    public StateItem[] getStateTable() {
        return stateTable;
    }

    @Override
    public int getStateTableIndex(ITimeEvent event) {
        if (event instanceof TimeEvent && ((TimeEvent) event).hasValue()) {
            int type = ((TimeEvent) event).getValue();
            if (typeIndex.containsKey(type))
            	return typeIndex.get(type);
        }
        return TRANSPARENT;
    }

    @Override
    public String getEventName(ITimeEvent event) {

    	int index = getStateTableIndex(event);
    	if (index >=0 && index < stateTable.length) {
    		return stateTable[index].getStateString();
    	}
        return "unknown";
    }	
    
}
