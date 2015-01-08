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

import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;

/**
 * Query class for Tool table.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class ToolQuery extends ElementQuery {
	
	/**
	 * The constructor.
	 * @param sysDB System DB object where the query is performed.
	 */
	public ToolQuery(SystemDBObject sysDB) {
		super(sysDB);
		super.clear();
	}
		
	@Override
	public List<Tool> getList() throws SoCTraceException {
		
		try {

			StringBuilder toolQuery = new StringBuilder("SELECT * FROM " + FramesocTable.TOOL + " ");

			if (where) {
				toolQuery.append(" WHERE ");
			}

			if (elementWhere != null) {
				toolQuery.append(elementWhere.getSQLString());
			}
			
			if (orderBy) {
				toolQuery.append(" ORDER BY " + orderByColumn + " " + orderByCriterium);
			}
			
			if (isLimitSet()) {
				toolQuery.append(" LIMIT " + getLimit());
			}
			
			String query = toolQuery.toString();
			debug(query);

			Statement stm = dbObj.getConnection().createStatement();
			ResultSet rs = stm.executeQuery(query);
			
			List<Tool> tools = new LinkedList<Tool>();
			while (rs.next()) {
				Tool t = new Tool(rs.getInt(1));
				t.setName(rs.getString(2));
				t.setType(rs.getString(3));
				t.setCommand(rs.getString(4));
				t.setPlugin(rs.getBoolean(5));
				t.setDoc(rs.getString(6));
				t.setExtensionId(rs.getString(7));
				tools.add(t);
			}
			stm.close();
			return tools;			

		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}
	
}
