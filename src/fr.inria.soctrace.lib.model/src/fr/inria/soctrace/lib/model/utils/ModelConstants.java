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
/**
 * 
 */
package fr.inria.soctrace.lib.model.utils;

/**
 * Data model constants.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class ModelConstants {
	
	/**
	 * Data model entities.
	 * Only the entities <i>visitable</i> by an {@link fr.inria.soctrace.lib.model.IModelVisitor}
	 * are considered actual model entities here. 
	 */
	public enum ModelEntity {
		TRACE,
		TRACE_TYPE,
		TRACE_PARAM,
		TRACE_PARAM_TYPE,
		TOOL,
		EVENT,
		EVENT_TYPE,
		EVENT_PARAM,
		EVENT_PARAM_TYPE,
		EVENT_PRODUCER,
		FILE,
		ANALYSIS_RESULT,
		/** Special value indicating an unknown entity */
		NO_ENTITY; 
	}
	
	public static class EventCategory {
		
		public final static int PUNCTUAL_EVENT = 0;
		public final static int STATE = 1;
		public final static int LINK = 2;
		public final static int VARIABLE = 3;
		
		public static String categoryToString(int category) {
			switch(category) {
			case PUNCTUAL_EVENT:
				return "Punctual Event";
			case STATE:
				return "State";
			case LINK:
				return "Link";
			case VARIABLE:
				return "Variable";
			default:
				return "Unknown";
			}
		}
		
		public static int stringToCategory(String categoryName) {
			switch (categoryName) {
			case "Punctual Event":
				return PUNCTUAL_EVENT;
			case "State":
				return STATE;
			case "Link":
				return LINK;
			case "Variable":
				return VARIABLE;
			default:
				return -1;
			}
		}
	}
	
	public static enum TimeUnit {		

		// SI values
		SECONDS("s", 0),
		MILLISECONDS("ms", -3),
		MICROSECONDS("us", -6),
		NANOSECONDS("ns", -9),
		PICOSECONDS("ps", -12),
		FEMTOSECONDS("fs", -15),
		ATTOSECONDS("as", -18),
		ZEPTOSECONDS("zs", -21),
		YOCTOSECONDS("ys", -24),
		
		// Special values
		UNKNOWN("unknown", Integer.MAX_VALUE),
		TICK("tick", Integer.MAX_VALUE-1),
		CYCLE("cycle", Integer.MAX_VALUE-2);
		
		/**
		 * Time unit string representation
		 */
		private String name;
		
		/**
		 * Exponent to give to 10 in order to have the value in seconds. It may
		 * have special value, to identify things that are not seconds (e.g.
		 * ticks).
		 */
		private int exp;

		private TimeUnit(String name, int exp) {
			this.name = name;
			this.exp = exp;
		}

		public String getLabel() {
			return name;
		}
		
		public int getInt() {
			return exp;
		}
		
		public static String getLabel(int exp) {
			for (TimeUnit t : TimeUnit.values())
				if (t.getInt() == exp)
					return t.getLabel();
			return " x 10^" + String.valueOf(exp) + " s";
		}

		public static TimeUnit getTimeUnit(int exp) {
			for (TimeUnit t : TimeUnit.values())
				if (t.getInt() == exp)
					return t;
			return UNKNOWN;
		}

		public static int getInt(String label) {
			for (TimeUnit t : TimeUnit.values())
				if (t.getLabel().equals(label))
					return t.getInt();
			return TimeUnit.UNKNOWN.getInt();
		}

		@Override
		public String toString() {
			return getLabel();
		}

	}

}
