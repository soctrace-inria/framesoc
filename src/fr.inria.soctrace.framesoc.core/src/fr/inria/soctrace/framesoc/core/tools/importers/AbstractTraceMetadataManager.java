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
package fr.inria.soctrace.framesoc.core.tools.importers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.TraceParam;
import fr.inria.soctrace.lib.model.TraceParamType;
import fr.inria.soctrace.lib.model.TraceType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;
import fr.inria.soctrace.lib.utils.IdManager;

/**
 * Abstract class to manage Trace metadata.
 * 
 * <p>
 * Normally subclasses have simply to override the methods 
 * {@link TraceMetadataManager#setTraceFields(Trace)}
 * and {@link TraceMetadataManager#getTraceTypeName()} 
 * of {@link TraceMetadataManager} interface.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class AbstractTraceMetadataManager implements TraceMetadataManager {
	
	private SystemDBObject sysDB;
	private TraceType traceType;
	private Trace trace;
	private Map<String, TraceParamType> tptMap;

	/**
	 * Constructor.
	 * 
	 * @param sysDB System DB object
	 * @throws SoCTraceException
	 */
	public AbstractTraceMetadataManager(SystemDBObject sysDB) throws SoCTraceException {
		this.sysDB = sysDB;
		this.tptMap = new HashMap<String, TraceParamType>();
	}

	@Override
	public List<ParameterDescriptor> getParameterDescriptors() {
		return new LinkedList<>();
	}
	
	@Override
	public void createMetadata() throws SoCTraceException {
		
		// Trace Type
		buildTraceType();
		
		// Trace		
		buildTrace();
		
	}
	
	@Override
	public void saveMetadata() throws SoCTraceException {
		
		if (!isTraceTypeExisting()) {
			sysDB.save(traceType);
			for (TraceParamType tpt: traceType.getTraceParamTypes()) {
				sysDB.save(tpt);
			}
		}
					
		sysDB.save(trace);
		for (TraceParam tp: trace.getParams()) {
			sysDB.save(tp);
		}		
	}
	
	private void buildTraceType() throws SoCTraceException {
		
		if (isTraceTypeExisting()) {
			traceType = sysDB.getTraceType(getTraceTypeName());
			for (TraceParamType tpt: traceType.getTraceParamTypes()) {
				tptMap.put(tpt.getName(), tpt);
			}
		} else {
			traceType = new TraceType(sysDB.getNewId(FramesocTable.TRACE_TYPE.toString(), "ID"));
			traceType.setName(getTraceTypeName());
			
			List<ParameterDescriptor> descriptors = getParameterDescriptors();
			if (descriptors.isEmpty())
				return;

			IdManager tptIdManager = new IdManager();
			tptIdManager.setNextId(sysDB.getMaxId(FramesocTable.TRACE_PARAM_TYPE.toString(), "ID") + 1);
			TraceParamType tpt;
			for (ParameterDescriptor p: descriptors) {
				tpt = new TraceParamType(tptIdManager.getNextId());
				tpt.setTraceType(traceType);
				tpt.setName(p.getName());
				tpt.setType(p.getType());
				tptMap.put(p.toString(), tpt);
			}
		}		
	}

	/**
	 * Builds the Trace object
	 * @throws SoCTraceException
	 */
	private void buildTrace() throws SoCTraceException {
				
		trace = new Trace(sysDB.getNewId(FramesocTable.TRACE.toString(), "ID"));
		trace.setType(traceType);		
		setTraceFields(trace);
		
		List<ParameterDescriptor> descriptors = getParameterDescriptors();
		if (descriptors.isEmpty())
			return;
		
		IdManager tpIdManager = new IdManager();
		tpIdManager.setNextId(sysDB.getMaxId(FramesocTable.TRACE_PARAM.toString(), "ID") + 1);
		TraceParam tp;	
		for (ParameterDescriptor des: descriptors) {
			tp = new TraceParam(tpIdManager.getNextId());
			tp.setTrace(trace);
			tp.setTraceParamType(tptMap.get(des.getName()));
			tp.setValue(des.getValue());			
		}
	}

	/**
	 * Check if the trace type is already in the DB
	 * @return true, if the trace type is already in the DB, false otherwise.
	 * @throws SoCTraceException
	 */
	private boolean isTraceTypeExisting() throws SoCTraceException {
		return sysDB.isTraceTypePresent(getTraceTypeName());
	}
}
