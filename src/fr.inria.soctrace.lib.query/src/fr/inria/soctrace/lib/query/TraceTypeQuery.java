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

import fr.inria.soctrace.lib.model.TraceType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.utils.ModelElementCache;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;

/**
 * Query class for TraceType table.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class TraceTypeQuery extends ElementQuery {

	/**
	 * The constructor.
	 * @param sysDB System DB object where the query is performed.
	 */
	public TraceTypeQuery(SystemDBObject sysDB) {
		super(sysDB);
		super.clear();
	}

	@Override
	public List<TraceType> getList() throws SoCTraceException {
		try {

			StringBuilder eventTypeQuery = new StringBuilder("SELECT ID FROM " + FramesocTable.TRACE_TYPE + " ");

			if (where) {
				eventTypeQuery.append(" WHERE ");
			}

			if (elementWhere != null) {
				eventTypeQuery.append(elementWhere.getSQLString());
			}
			
			if (orderBy) {
				eventTypeQuery.append(" ORDER BY " + orderByColumn + " " + orderByCriterium);
			}

			if (isLimitSet()) {
				eventTypeQuery.append(" LIMIT " + getLimit());
			}
			
			String query = eventTypeQuery.toString();
			debug(query);

			Statement stm = dbObj.getConnection().createStatement();
			ResultSet rs = stm.executeQuery(query);
			
			List<TraceType> elist = new LinkedList<TraceType>();
			ModelElementCache cache = ((SystemDBObject)dbObj).getTraceTypeCache();
			while (rs.next()) {
				elist.add(cache.get(TraceType.class, rs.getLong(1)));
			}
			stm.close();
			return elist;		
			
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

}
