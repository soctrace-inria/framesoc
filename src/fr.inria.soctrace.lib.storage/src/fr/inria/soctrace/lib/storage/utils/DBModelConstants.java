package fr.inria.soctrace.lib.storage.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;

@SuppressWarnings("rawtypes")
public abstract class DBModelConstants {

	public static final Map<FramesocTable, Class > TableModelDictionary;
	static {
		Map<FramesocTable, Class> aMap = new HashMap<FramesocTable, Class>();
		aMap.put(FramesocTable.TRACE, TraceTableModel.class);
	/*	aMap.put(FramesocTable.TRACE_TYPE, TraceTypeTableModel.class);
		aMap.put(FramesocTable.TRACE_PARAM, TraceParamTableModel.class);
		aMap.put(FramesocTable.TRACE_PARAM_TYPE, TraceParamTypeTableModel.class);
		aMap.put(FramesocTable.TOOL, ToolTableModel.class);*/
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
	}

}
