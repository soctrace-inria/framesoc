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
package fr.inria.soctrace.lib.query.conditions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.SelfDefiningElementQuery;
import fr.inria.soctrace.lib.query.SelfDefiningElementQuery.ParamType;
import fr.inria.soctrace.lib.query.ValueListString;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.utils.IdManager;

/**
 * Simple expression describing a condition involving a 
 * self-defining-elements parameter.
 * 
 * Even if the self-defining-pattern stores the name of a
 * parameter and its value in two different tables (xParamType
 * and xParam), this class provide a simpler interface allowing
 * the user to specify a condition with the format:
 * 		<param_name> <comparison_operator> <param_value> 
 * 		(e.g. ADDRESS = 0xbf068544)
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class ParamSimpleCondition implements IParamCondition {
	
	private String name;
	private ComparisonOperation op;
	private String value;
	private boolean numericComparisonSet;
	private boolean isNumericComparison;
	
	/**
	 * Create a simple parameter condition: <name><operation><value> (e.g. X >= 3)
	 * @param name name of the parameter
	 * @param op comparison operation
	 * @param value value to compare with
	 */
	public ParamSimpleCondition(String name, ComparisonOperation op, String value) {
		this.name = name;
		this.op = op;
		this.value = value;
		this.numericComparisonSet = false;
		this.isNumericComparison = false;
	}

	/**
	 * Set the numeric comparison flag.
	 * @param numeric value to set
	 */
	public void setNumericComparison(boolean numeric) {
		this.numericComparisonSet = true;
		this.isNumericComparison = numeric;
	}
	
	/**
	 * Returns a list of 'Element' ID.
	 * E.g.: "ID IN ( 1, 2, 3)"
	 * 
	 * If the list is empty, the list actually returned
	 * contains only a reserved value which cannot be a 
	 * valid ID.
	 * 
	 * Note: a numeric comparison is performed if the type
	 * of the param is INTEGER or BIGINT, or if the corresponding
	 * flag is set.
	 */
	@Override
	public String getSQLString(SelfDefiningElementQuery access, int typeId) throws SoCTraceException {

		ParamType param = access.getParamType(name, typeId);
		if (param==null)
			return "ID IN ( "+ String.valueOf(IdManager.RESERVED_NO_ID) +" )";
		
		String elementName = access.getElementTableName();
		StringBuilder sb = new StringBuilder("SELECT ");
		sb.append(elementName+"_ID");
		sb.append(" FROM ");
		sb.append(elementName+"_PARAM");
		sb.append(" WHERE ");
		
		// check numeric comparison
		String valueAttribute;
		if (numericComparisonSet) {
			if (isNumericComparison)
				valueAttribute = "CAST(VALUE AS SIGNED)";
			else 
				valueAttribute = "VALUE";
		} else {
			if (param.type.equals("INTEGER") || param.type.equals("BIGINT") )
				valueAttribute = "CAST(VALUE AS SIGNED)";
			else
				valueAttribute = "VALUE";
		}
		
		// where clause

		// fix for IN/BETWEEN operator: no ''
		String valueString;
		if (op==ComparisonOperation.IN || op==ComparisonOperation.BETWEEN)
			valueString = valueAttribute + op.toString() + value + ")";
		else 
			valueString = valueAttribute + op.toString() + "'" + value + "')";

		sb.append("("+elementName+"_PARAM_TYPE_ID="+param.id+
				" AND "+valueString);
		
		try {
			DBObject dbObj = access.getDBObject();
			Statement stm = dbObj.getConnection().createStatement();
			ResultSet rs = stm.executeQuery(sb.toString());
			ValueListString vls = new ValueListString();
			while (rs.next()) {
				vls.addValue(rs.getString(1));
			}
			if (vls.size() > 0)
				return "ID IN " + vls.getValueString();
			else 
				return "ID IN ( "+ String.valueOf(IdManager.RESERVED_NO_ID) +" )";
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}			
	}

}
