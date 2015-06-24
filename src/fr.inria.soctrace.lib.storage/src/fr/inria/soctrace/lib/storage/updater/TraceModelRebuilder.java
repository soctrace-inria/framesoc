package fr.inria.soctrace.lib.storage.updater;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.utils.SQLConstants;
import fr.inria.soctrace.lib.storage.utils.DBModelConstants.TraceTableModel;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;

public class TraceModelRebuilder extends DBModelRebuilder {
	
	public TraceModelRebuilder() {
		table = FramesocTable.TRACE;
		getTableQuery = SQLConstants.PREPARED_STATEMENT_TRACE_INSERT;
	}

	@Override
	public void copyValues(PreparedStatement statement, ResultSet rs)
			throws SoCTraceException {
	
		
		for (TraceTableModel traceTableModel : TraceTableModel.values()) {
			System.out.println(traceTableModel.toString());
			if(traceTableModel.toString().equals("NUMBER_OF_PRODUCERS"))
				System.out.println(traceTableModel.toString());
			
			// If it is one of the difference
			if (oldModelMapDiff.containsKey(traceTableModel.toString())) {
				
				int value = oldModelMapDiff.get(traceTableModel.toString());
				// If missing parameter
				if (value == DBModelRebuilder.MISSING_PARAMETER_VALUE) {
					// Put default value
					addToStatement(statement,
							traceTableModel.getDefaultValue(),
							traceTableModel.getPosition(),
							traceTableModel.getType());
				} else {
					// Put the value at the correct place
					addToStatement(statement, rs,
							traceTableModel.getPosition(),
							oldModelMapDiff.get(traceTableModel.toString()),
							traceTableModel.getType());
				}
				// Remove parameter from table
				oldModelMapDiff.remove(traceTableModel.toString());
			} else {
				// Just copy it
				addToStatement(statement, rs, traceTableModel.getPosition(),
						traceTableModel.getPosition(),
						traceTableModel.getType());
			}
		}
		
		
		// TODO Add custom parameters
		//FramesocManager.getInstance().saveParam(traces, .getName(),
		//		dlg.getType(), dlg.getValue());
	}

}
