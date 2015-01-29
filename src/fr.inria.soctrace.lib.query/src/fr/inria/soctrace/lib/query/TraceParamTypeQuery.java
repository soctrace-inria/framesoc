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

import fr.inria.soctrace.lib.model.TraceParamType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.utils.ModelElementCache;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;

/**
 * Query class for TraceParamType table.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class TraceParamTypeQuery extends ElementQuery {

	/**
	 * The constructor.
	 * @param sysDB System DB object where the query is performed.
	 */
	public TraceParamTypeQuery(SystemDBObject sysDB) {
		super(sysDB);
		super.clear();
	}

	@Override
	public List<TraceParamType> getList() throws SoCTraceException {
		try {

			StringBuilder eventParamTypeQuery = new StringBuilder("SELECT ID FROM " + FramesocTable.TRACE_PARAM_TYPE + " ");

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
			
			List<TraceParamType> elist = new LinkedList<TraceParamType>();
			ModelElementCache cache = ((SystemDBObject)dbObj).getTraceTypeCache();
			while (rs.next()) { 
				elist.add(cache.get(TraceParamType.class, rs.getLong(1)));
			}
			stm.close();
			return elist;		
			
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

}
