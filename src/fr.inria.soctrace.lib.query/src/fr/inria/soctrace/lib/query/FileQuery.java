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

import fr.inria.soctrace.lib.model.File;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;

/**
 * Query class for File table.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class FileQuery extends ElementQuery {
		
	/**
	 * The constructor.
	 * @param traceDB Trace DB object where the query is performed.
	 */
	public FileQuery(TraceDBObject traceDB) {
		super(traceDB);
		super.clear();
	}
		
	@Override
	public List<File> getList() throws SoCTraceException {
		
		try {

			StringBuilder fileQuery = new StringBuilder("SELECT * FROM " + FramesocTable.FILE + " ");

			if (where) {
				fileQuery.append(" WHERE ");
			}

			if (elementWhere != null) {
				fileQuery.append(elementWhere.getSQLString());
			}
			
			if (orderBy) {
				fileQuery.append(" ORDER BY " + orderByColumn + " " + orderByCriterium);
			}
			
			if (isLimitSet()) {
				fileQuery.append(" LIMIT " + getLimit());
			}
			
			String query = fileQuery.toString();
			debug(query);

			Statement stm = dbObj.getConnection().createStatement();
			ResultSet rs = stm.executeQuery(query);
			
			List<File> files = new LinkedList<File>();
			while (rs.next()) {
				File f = new File(rs.getInt(1));
				f.setPath(rs.getString(2));
				f.setDescription(rs.getString(3));
				files.add(f);
			}
			stm.close();
			return files;			
			
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}

	}

}
