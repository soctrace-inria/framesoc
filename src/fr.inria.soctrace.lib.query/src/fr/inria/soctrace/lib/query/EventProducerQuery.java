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
package fr.inria.soctrace.lib.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;

/**
 * Query class for EventProducer table.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class EventProducerQuery extends ElementQuery {
	
	/**
	 * The constructor.
	 * @param traceDB Trace DB object where the query is performed.
	 */
	public EventProducerQuery(TraceDBObject traceDB) {
		super(traceDB);
		super.clear();
	}
		
	@Override
	public List<EventProducer> getList() throws SoCTraceException {
		
		try {

			StringBuilder eventProducerQuery = new StringBuilder("SELECT * FROM " + FramesocTable.EVENT_PRODUCER + " ");

			if (where) {
				eventProducerQuery.append(" WHERE ");
			}

			if (elementWhere != null) {
				eventProducerQuery.append(elementWhere.getSQLString());
			}
			
			if (orderBy) {
				eventProducerQuery.append(" ORDER BY " + orderByColumn + " " + orderByCriterium);
			}
			
			if (isLimitSet()) {
				eventProducerQuery.append(" LIMIT " + getLimit());
			}
			
			String query = eventProducerQuery.toString();
			debug(query);

			Statement stm = dbObj.getConnection().createStatement();
			ResultSet rs = stm.executeQuery(query);
			
			List<EventProducer> eventProducers = new LinkedList<EventProducer>();
			while (rs.next()) {
				EventProducer s = new EventProducer(rs.getInt(1));
				s.setType(rs.getString(2));
				s.setLocalId(rs.getString(3));
				s.setName(rs.getString(4));
				s.setParentId(rs.getInt(5));
				eventProducers.add(s);
			}
			stm.close();
			return eventProducers;			
			
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}

	}

}
