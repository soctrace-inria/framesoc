package fr.inria.soctrace.lib.storage.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;

public abstract class DBModelConstants {

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
	
	public interface TableModel {
		public void setName(String name) ;
		public int getPos() ;
		public void setPos(int pos);
		public String getDbColumnName();
	}
	
	/**
	 * SoC-Trace Database trace model
	 */
	public static enum TraceTableModel implements TableModel {		
		ID("ID", 1),
		TRACE_TYPE_ID("Trace type", 2),
		TRACING_DATE("Tracing date", 3),
		TRACED_APPLICATION("Traced application", 4),
		BOARD("Board", 5),
		OPERATING_SYSTEM("Operating system", 6),
		NUMBER_OF_CPUS("Number of CPUs", 7),
		NUMBER_OF_EVENTS("Number of events", 8),
		OUTPUT_DEVICE("Output device", 9),
		DESCRIPTION("Description", 10),
		PROCESSED("Porcessed", 11),
		TRACE_DB_NAME("DB name", 12),
		ALIAS("Alias", 13),
		MIN_TIMESTAMP("Min timestamp", 14),
		MAX_TIMESTAMP("Max timestamp", 15),
		TIMEUNIT("Time-unit", 16),
		NUMBER_OF_PRODUCERS("Number of producers", 17);
		
		private String name;
		private int pos;
		
		private TraceTableModel(String name, int pos) {
			this.name = name;
			this.pos = pos;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getPos() {
			return pos;
		}

		public void setPos(int pos) {
			this.pos = pos;
		}

		public String getDbColumnName() {
			return this.toString();
		}
		
		public static TraceTableModel getValueAt(Integer pos) {
			for (TraceTableModel traceTableModel : values())
				if (traceTableModel.getPos() == pos)
					return traceTableModel;
			
			return null;
		}

		public static int numberOfColumns() {
			return values().length;
		}
	}
	
	public static enum TraceTypeTableModel implements TableModel {		
		ID("ID", 1),
		NAME("Name", 2);

		private String name;
		private int pos;
		
		private TraceTypeTableModel(String name, int pos) {
			this.name = name;
			this.pos = pos;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getPos() {
			return pos;
		}

		public void setPos(int pos) {
			this.pos = pos;
		}

		public String getDbColumnName() {
			return this.toString();
		}
		
		public static TraceTypeTableModel getValueAt(Integer pos) {
			for (TraceTypeTableModel traceTableModel : values())
				if (traceTableModel.getPos() == pos)
					return traceTableModel;
			
			return null;
		}
		
		public static int numberOfColumns() {
			return values().length;
		}
	}
	
	public static enum TraceParamTableModel implements TableModel {		
		ID("ID", 1),
		TRACE_ID("Trace ID", 2),
		TRACE_PARAM_TYPE_ID("Trace parameter type id", 3),
		VALUE("Value", 4);

		private String name;
		private int pos;
		
		private TraceParamTableModel(String name, int pos) {
			this.name = name;
			this.pos = pos;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getPos() {
			return pos;
		}

		public void setPos(int pos) {
			this.pos = pos;
		}

		public String getDbColumnName() {
			return this.toString();
		}
		
		public static TraceParamTableModel getValueAt(Integer pos) {
			for (TraceParamTableModel traceTableModel : values())
				if (traceTableModel.getPos() == pos)
					return traceTableModel;
			
			return null;
		}
		
		public static int numberOfColumns() {
			return values().length;
		}
	}

	public static enum TraceParamTypeTableModel implements TableModel {		
		ID("ID", 1),
		TRACE_TYPE_ID("Trace ID", 2),
		NAME("Name", 3),
		TYPE("Type", 4);

		private String name;
		private int pos;
		
		private TraceParamTypeTableModel(String name, int pos) {
			this.name = name;
			this.pos = pos;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getPos() {
			return pos;
		}

		public void setPos(int pos) {
			this.pos = pos;
		}

		public String getDbColumnName() {
			return this.toString();
		}
		
		public static TraceParamTypeTableModel getValueAt(Integer pos) {
			for (TraceParamTypeTableModel traceTableModel : values())
				if (traceTableModel.getPos() == pos)
					return traceTableModel;
			
			return null;
		}
		
		public static int numberOfColumns() {
			return values().length;
		}
	}
	
	public static enum ToolTableModel implements TableModel {		
		ID("ID", 1),
		NAME("Name", 2),
		TYPE("Type", 3),
		COMMAND("Command", 4),
		IS_PLUGIN("Is plugin", 5),
		DOC("Doc", 6),
		EXTENSION_ID("Extension", 7);

		private String name;
		private int pos;
		
		private ToolTableModel(String name, int pos) {
			this.name = name;
			this.pos = pos;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getPos() {
			return pos;
		}

		public void setPos(int pos) {
			this.pos = pos;
		}

		public String getDbColumnName() {
			return this.toString();
		}
		
		public static ToolTableModel getValueAt(Integer pos) {
			for (ToolTableModel traceTableModel : values())
				if (traceTableModel.getPos() == pos)
					return traceTableModel;
			
			return null;
		}
		
		public static int numberOfColumns() {
			return values().length;
		}
	}
}
