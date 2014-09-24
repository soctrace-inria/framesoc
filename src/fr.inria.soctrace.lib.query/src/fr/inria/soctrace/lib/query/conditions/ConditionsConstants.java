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

/**
 * Constants used to build conditions.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public abstract class ConditionsConstants {

	public static enum ComparisonOperation {		

		EQ(" = "),
		NE(" <> "),
		GT(" > "),
		LT(" < "),
		GE(" >= "),
		LE(" <= "),
		LIKE(" LIKE "),
		NOT_LIKE(" NOT LIKE "),
		IN(" IN "),
		BETWEEN(" BETWEEN ");
				
		private String name;
		
		private ComparisonOperation(String name){
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public static enum LogicalOperation {		

		AND("AND"),
		OR("OR");
				
		private String name;
		
		private LogicalOperation(String name){
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}
	
	/**
	 * Order By Criteria
	 */
	public static enum OrderBy {		
		ASC,
		DESC
	}


}
