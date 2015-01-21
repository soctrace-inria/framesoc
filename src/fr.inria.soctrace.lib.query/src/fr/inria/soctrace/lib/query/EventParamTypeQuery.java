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
package fr.inria.soctrace.lib.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import fr.inria.soctrace.lib.model.EventParamType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.utils.ModelElementCache;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;

/**
 * Query class for EventParamType table.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class EventParamTypeQuery extends ElementQuery {

	/**
	 * The constructor.
	 * @param traceDB Trace DB object where the query is performed.
	 */
	public EventParamTypeQuery(TraceDBObject traceDB) {
		super(traceDB);
		super.clear();
	}

	@Override
	public List<EventParamType> getList() throws SoCTraceException {
		try {

			StringBuilder eventParamTypeQuery = new StringBuilder("SELECT ID FROM " + FramesocTable.EVENT_PARAM_TYPE + " ");

			if (where) {
				eventParamTypeQuery.append(" WHERE ");
			}

			if (elementWhere != null) {
				eventParamTypeQuery.append(elementWhere.getSQLString());
			}
			
			if (orderBy) {
				eventParamTypeQuery.append(" ORDER BY " + orderByColumn + " " + orderByCriterium);
			}

			if (isLimitSet()) {
				eventParamTypeQuery.append(" LIMIT " + getLimit());
			}
			
			String query = eventParamTypeQuery.toString();
			debug(query);

			Statement stm = dbObj.getConnection().createStatement();
			ResultSet rs = stm.executeQuery(query);
			
			List<EventParamType> elist = new LinkedList<EventParamType>();
			ModelElementCache cache = ((TraceDBObject)dbObj).getEventTypeCache();
			while (rs.next()) {
				elist.add(cache.get(EventParamType.class, rs.getInt(1)));
			}
			stm.close();
			return elist;		
			
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

}
