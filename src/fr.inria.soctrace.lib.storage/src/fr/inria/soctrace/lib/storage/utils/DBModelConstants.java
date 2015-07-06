/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Youenn Corre - initial API and implementation
 ******************************************************************************/
package fr.inria.soctrace.lib.storage.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.ModelConstants.TimeUnit;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;

/**
 * Provide series of enum and constants for the description 
 * of the tables in the system database
 * 
 * For a table model, each column is represented by a value of an
 * enum where:
 * 	-the name of the enum is the name of the column in DB
 * 	-the field name is a description of the column
 * 	-the field position is the column index of the column
 * 	-the field type is the type of the data stored in the column
 *  -the field default value provide a value when the field is not initialized.
 * a null value means that the field should not be blank, and should be handled 
 * as an error.
 * 
 * @author "Youenn Corre <youenn.corre@inria.fr>"
 */
public abstract class DBModelConstants {

	/**
	 * Dictionary associating a table of the DB with its model
	 */
	public static final Map<FramesocTable, Class<?> > TableModelDictionary;
	static {
		Map<FramesocTable, Class<?>> aMap = new HashMap<FramesocTable, Class<?>>();
		aMap.put(FramesocTable.TRACE, TraceTableModel.class);
		aMap.put(FramesocTable.TRACE_TYPE, TraceTypeTableModel.class);
		aMap.put(FramesocTable.TRACE_PARAM, TraceParamTableModel.class);
		aMap.put(FramesocTable.TRACE_PARAM_TYPE, TraceParamTypeTableModel.class);
		aMap.put(FramesocTable.TOOL, ToolTableModel.class);
		TableModelDictionary = Collections.unmodifiableMap(aMap);
	}
	
	/**
	 * Provide API to get information about a table model
	 * 
	 * @author "Youenn Corre <youenn.corre@inria.fr>"
	 */
	public interface TableModel {
		/**
		 * Set the description of a column db
		 * 
		 * @param description
		 *            the new description
		 */
		public void setDescription(String description) ;

		/**
		 * @return the current column index of a column in DB
		 */
		public int getPosition() ;

		/**
		 * Set the position of a column in database
		 * 
		 * @param pos
		 *            the new position
		 */
		public void setPosition(int pos);

		/**
		 * @return the name of the column in Database
		 */
		public String getDbColumnName();

		/**
		 * Get a default value for the column. Used when the value is missing.
		 * When returning a null value, means that databse cannot be updated if
		 * missing
		 * 
		 * @return the default value for the column
		 */
		public Object getDefaultValue();

		/**
		 * 
		 * @return the type of the value stored in database
		 */
		public String getType();
	}
	
	/**
	 * SoC-Trace Database table models
	 * 
	 * They are built in the form of triplet. The name of the model is the name
	 * of the column in the database. The values are :
	 *  - A description of the field (e.g. to use in the GUI)
	 *  - The column index in the database 
	 *  - The type of the value stored at that position 
	 *  - A default value, that will be used if the column does not exist. 
	 *  If a default value cannot be given then it is set to null, meaning t
	 *  that the update will fail if this field is absent in the database
	 */
	public static enum TraceTableModel implements TableModel {		
		ID("ID", 1, Integer.class.getSimpleName(), null),
		TRACE_TYPE_ID("Trace type", 2, Integer.class.getSimpleName(), Trace.UNKNOWN_INT),
		TRACING_DATE("Tracing date", 3, String.class.getSimpleName(), Trace.UNKNOWN_STRING),
		TRACED_APPLICATION("Traced application", 4, String.class.getSimpleName(), Trace.UNKNOWN_STRING),
		BOARD("Board", 5, String.class.getSimpleName(), Trace.UNKNOWN_STRING),
		OPERATING_SYSTEM("Operating system", 6, String.class.getSimpleName(), Trace.UNKNOWN_STRING),
		NUMBER_OF_CPUS("Number of CPUs", 7, Integer.class.getSimpleName(), Trace.UNKNOWN_INT),
		NUMBER_OF_EVENTS("Number of events", 8, Integer.class.getSimpleName(), Trace.UNKNOWN_INT),
		OUTPUT_DEVICE("Output device", 9, String.class.getSimpleName(), Trace.UNKNOWN_STRING),
		DESCRIPTION("Description", 10, String.class.getSimpleName(), Trace.UNKNOWN_STRING),
		PROCESSED("Porcessed", 11, Boolean.class.getSimpleName(), false),
		TRACE_DB_NAME("DB name", 12, String.class.getSimpleName(), Trace.UNKNOWN_STRING),
		ALIAS("Alias", 13, String.class.getSimpleName(), Trace.UNKNOWN_STRING),
		MIN_TIMESTAMP("Min timestamp", 14, Long.class.getSimpleName(), Trace.UNKNOWN_INT),
		MAX_TIMESTAMP("Max timestamp", 15, Long.class.getSimpleName(), Trace.UNKNOWN_INT),
		TIMEUNIT("Time-unit", 16, Integer.class.getSimpleName(), TimeUnit.UNKNOWN.getInt()),
		NUMBER_OF_PRODUCERS("Number of producers", 17, Integer.class.getSimpleName(), Trace.UNKNOWN_INT);
		
		private String description;
		private int position;
		private String type;
		private Object defaultValue;
		
		private TraceTableModel(String name, int pos, String type,
				Object defaultValue) {
			this.description = name;
			this.position = pos;
			this.type = type;
			this.defaultValue = defaultValue;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public int getPosition() {
			return position;
		}

		public void setPosition(int pos) {
			this.position = pos;
		}

		public String getDbColumnName() {
			return this.toString();
		}
		
		/**
		 * Return the value of at column index pos
		 * 
		 * @param pos
		 *            the index of the column
		 * @return the name of the column at the index pos, or null if there no
		 *         column at this index
		 */
		public static TraceTableModel getValueAt(Integer pos) {
			for (TraceTableModel traceTableModel : values())
				if (traceTableModel.getPosition() == pos)
					return traceTableModel;
			
			return null;
		}

		public static int numberOfColumns() {
			return values().length;
		}

		@Override
		public String getType() {
			return type;
		}

		@Override
		public Object getDefaultValue() {
			return defaultValue;
		}
	}
	
	public static enum TraceTypeTableModel implements TableModel {		
		ID("ID", 1, Integer.class.getSimpleName(), null),
		NAME("Name", 2, String.class.getSimpleName(), null);

		private String name;
		private int pos;
		private String type;
		private Object defaultValue;
		
		private TraceTypeTableModel(String name, int pos, String type,
				Object defaultValue) {
			this.name = name;
			this.pos = pos;
			this.type = type;
			this.defaultValue = defaultValue;
		}

		public String getDescription() {
			return name;
		}

		public void setDescription(String name) {
			this.name = name;
		}

		public int getPosition() {
			return pos;
		}

		public void setPosition(int pos) {
			this.pos = pos;
		}

		public String getDbColumnName() {
			return this.toString();
		}
		
		public static TraceTypeTableModel getValueAt(Integer pos) {
			for (TraceTypeTableModel traceTableModel : values())
				if (traceTableModel.getPosition() == pos)
					return traceTableModel;
			
			return null;
		}
		
		public static int numberOfColumns() {
			return values().length;
		}

		@Override
		public String getType() {
			return type;
		}
		
		@Override
		public Object getDefaultValue() {
			return defaultValue;
		}
	}
	
	public static enum TraceParamTableModel implements TableModel {		
		ID("ID", 1, Integer.class.getSimpleName(), null),
		TRACE_ID("Trace ID", 2, Integer.class.getSimpleName(), null),
		TRACE_PARAM_TYPE_ID("Trace parameter type id", 3, Integer.class.getSimpleName(), null),
		VALUE("Value", 4, String.class.getSimpleName(), null);

		private String description;
		private int pos;
		private String type;
		private Object defaultValue;
		
		private TraceParamTableModel(String name, int pos, String type,
				Object defaultValue) {
			this.description = name;
			this.pos = pos;
			this.type = type;
			this.defaultValue = defaultValue;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String name) {
			this.description = name;
		}

		public int getPosition() {
			return pos;
		}

		public void setPosition(int pos) {
			this.pos = pos;
		}

		public String getDbColumnName() {
			return this.toString();
		}
		
		public static TraceParamTableModel getValueAt(Integer pos) {
			for (TraceParamTableModel traceTableModel : values())
				if (traceTableModel.getPosition() == pos)
					return traceTableModel;
			
			return null;
		}
		
		public static int numberOfColumns() {
			return values().length;
		}

		@Override
		public String getType() {
			return type;
		}
		
		@Override
		public Object getDefaultValue() {
			return defaultValue;
		}
	}

	public static enum TraceParamTypeTableModel implements TableModel {		
		ID("ID", 1, Integer.class.getSimpleName(), null),
		TRACE_TYPE_ID("Trace ID", 2, Integer.class.getSimpleName(), null),
		NAME("Name", 3, String.class.getSimpleName(), null),
		TYPE("Type", 4, String.class.getSimpleName(), null);

		private String description;
		private int pos;
		private String type;
		private Object defaultValue;
		
		private TraceParamTypeTableModel(String name, int pos, String type,
				Object defaultValue) {
			this.description = name;
			this.pos = pos;
			this.type = type;
			this.defaultValue = defaultValue;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String name) {
			this.description = name;
		}

		public int getPosition() {
			return pos;
		}

		public void setPosition(int pos) {
			this.pos = pos;
		}

		public String getDbColumnName() {
			return this.toString();
		}
		
		public static TraceParamTypeTableModel getValueAt(Integer pos) {
			for (TraceParamTypeTableModel traceTableModel : values())
				if (traceTableModel.getPosition() == pos)
					return traceTableModel;
			
			return null;
		}
		
		public static int numberOfColumns() {
			return values().length;
		}

		@Override
		public String getType() {
			return type;
		}
		
		@Override
		public Object getDefaultValue() {
			return defaultValue;
		}
	}
	
	public static enum ToolTableModel implements TableModel {		
		ID("ID", 1, Integer.class.getSimpleName(), null),
		NAME("Name", 2, String.class.getSimpleName(), ""),
		TYPE("Type", 3, String.class.getSimpleName(), ""),
		COMMAND("Command", 4, String.class.getSimpleName(), ""),
		IS_PLUGIN("Is plugin", 5, Boolean.class.getSimpleName(), null),
		DOC("Doc", 6, String.class.getSimpleName(), ""),
		EXTENSION_ID("Extension", 7, String.class.getSimpleName(), "");

		private String description;
		private int pos;
		private String type;
		private Object defaultValue;
		
		private ToolTableModel(String name, int pos, String type,
				Object defaultValue) {
			this.description = name;
			this.pos = pos;
			this.type = type;
			this.defaultValue = defaultValue;
		}

		public String getName() {
			return description;
		}

		public void setDescription(String name) {
			this.description = name;
		}

		public int getPosition() {
			return pos;
		}

		public void setPosition(int pos) {
			this.pos = pos;
		}

		public String getDbColumnName() {
			return this.toString();
		}
		
		public static ToolTableModel getValueAt(Integer pos) {
			for (ToolTableModel traceTableModel : values())
				if (traceTableModel.getPosition() == pos)
					return traceTableModel;
			
			return null;
		}
		
		public static int numberOfColumns() {
			return values().length;
		}

		@Override
		public String getType() {
			return type;
		}
		
		@Override
		public Object getDefaultValue() {
			return defaultValue;
		}
	}
}
