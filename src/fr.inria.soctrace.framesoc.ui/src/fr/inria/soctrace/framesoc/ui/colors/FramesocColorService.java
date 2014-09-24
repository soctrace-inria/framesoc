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
package fr.inria.soctrace.framesoc.ui.colors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.core.FramesocManager;
import fr.inria.soctrace.framesoc.core.bus.FramesocBus;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopicList;
import fr.inria.soctrace.framesoc.core.bus.IFramesocBusListener;
import fr.inria.soctrace.framesoc.core.tools.management.PluginImporterJob;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventProducerQuery;
import fr.inria.soctrace.lib.query.EventTypeQuery;
import fr.inria.soctrace.lib.query.TraceQuery;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;

/**
 * Service created at UI startup to update trace colors
 * when a new trace is imported to the system.
 * 
 * <p>
 * The service reacts to the {@link FramesocBus} topic
 * {@link FramesocBusTopic#TOPIC_UI_COLORS_CHANGED}
 * initializing the Framesoc colors for the newly 
 * created traces at each trace import performed within a
 * {@link PluginImporterJob}.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class FramesocColorService implements IFramesocBusListener {

	private final static Logger logger = LoggerFactory.getLogger(FramesocColorService.class);

	/**
	 * List containing all SystemDB traces at a given time.
	 */
	private Set<Trace> traces;

	/**
	 * List of followed topics
	 */
	private FramesocBusTopicList topics;

	/**
	 * Constructor.
	 * Initializes the service, loading the existing trace metadata.
	 */
	public FramesocColorService() {

		// register to the topic related to the import of a new trace
		topics = new FramesocBusTopicList(this);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_SYNCH_TRACES_NEEDED);
		topics.registerAll();

		// load the traces, if a SystemDB exists
		traces = new HashSet<Trace>();
		SystemDBObject sysDB = null;
		try {
			if (!FramesocManager.getInstance().isSystemDBExisting())
				return;
			sysDB = SystemDBObject.openNewIstance();
			TraceQuery tq = new TraceQuery(sysDB);
			List<Trace> tmp = tq.getList();
			for (Trace t: tmp) {
				traces.add(t);
			}
		} catch (SoCTraceException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			DBObject.finalClose(sysDB);
		}
	}

	@Override
	public void handle(String topic, Object data) {
		if (topic.equals(FramesocBusTopic.TOPIC_UI_SYNCH_TRACES_NEEDED.toString())) {
			if ( ((Boolean) data ) == true ) {
				try {
					updateColors();
				} catch (SoCTraceException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}

	private void updateColors() throws SoCTraceException {

		if (!FramesocManager.getInstance().isSystemDBExisting())
			return;

		SystemDBObject sysDB= null;
		try {
			sysDB = SystemDBObject.openNewIstance();
			TraceQuery tq = new TraceQuery(sysDB);
			List<Trace> tmp = tq.getList();
			sysDB.close();
			for (Trace t: tmp) {
				if (!traces.contains(t)) {
					logger.debug("Update colors for trace: {}", t.getAlias());
					traces.add(t);
					updateTraceColors(t);
				}
			}	
		} finally {
			DBObject.finalClose(sysDB);
		}
	}

	private void updateTraceColors(Trace t) throws SoCTraceException {

		if (!FramesocManager.getInstance().isDBExisting(t.getDbName()))
			return;

		TraceDBObject traceDB = null;
		try {
			traceDB = TraceDBObject.openNewIstance(t.getDbName());

			EventTypeQuery etq = new EventTypeQuery(traceDB);
			List<EventType> types = etq.getList();
			for (EventType et: types) {
				FramesocColorManager.getInstance().getEventTypeColor(et.getName());
			}
			FramesocColorManager.getInstance().saveEventTypeColors();

			EventProducerQuery epq = new EventProducerQuery(traceDB);
			List<EventProducer> eps = epq.getList();
			for (EventProducer ep: eps) {
				FramesocColorManager.getInstance().getEventProducerColor(ep.getName());
			}
			FramesocColorManager.getInstance().saveEventProducerColors();

		} finally {
			DBObject.finalClose(traceDB);
		}
	}

}
