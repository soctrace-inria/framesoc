package fr.inria.soctrace.lib.storage.updater;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.utils.DBModelConstants.TraceParamTypeTableModel;
import fr.inria.soctrace.lib.storage.utils.SQLConstants;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;

public class TraceParamTypeModelRebuilder extends DBModelRebuilder {
	
	private static final Logger logger = LoggerFactory.getLogger(TraceParamTypeModelRebuilder.class);

	public TraceParamTypeModelRebuilder() {
		table = FramesocTable.TRACE_PARAM_TYPE;
		getTableQuery = SQLConstants.PREPARED_STATEMENT_TRACE_PARAM_TYPE_INSERT;
	}
	
	@Override
	public void copyValues(PreparedStatement statement, ResultSet rs)
			throws SoCTraceException {
		for (TraceParamTypeTableModel traceParamTypeTableModel : TraceParamTypeTableModel
				.values()) {
			// If it is one of the difference
			if (oldModelDiff.containsKey(traceParamTypeTableModel.toString())) {
				// If missing parameter
				if (oldModelDiff.get(traceParamTypeTableModel.toString()) == MISSING_PARAMETER_VALUE) {
					// Put default value
					addToStatement(statement,
							traceParamTypeTableModel.getDefaultValue(),
							traceParamTypeTableModel.getPosition(),
							traceParamTypeTableModel.getType());
				} else {
					// Put the value at the correct place
					addToStatement(statement, rs,
							traceParamTypeTableModel.getPosition(),
							oldModelDiff.get(traceParamTypeTableModel.toString()),
							traceParamTypeTableModel.getType());
				}
				// Remove parameter from table
				oldModelDiff.remove(traceParamTypeTableModel.toString());
			} else {
				// Just copy it
				addToStatement(statement, rs, traceParamTypeTableModel.getPosition(),
						traceParamTypeTableModel.getPosition(),
						traceParamTypeTableModel.getType());
			}
		}
		
		if (!oldModelDiff.keySet().isEmpty()) {
			logger.info("Trace Parameters Type: the following parameters were not imported in the new model:");
			for (String columnName : oldModelDiff.keySet())
				logger.info("  *" + columnName);
		}
	}
	
	@Override
	public String getValueAt(int pos) {
		return TraceParamTypeTableModel.getValueAt(pos).getDbColumnName();
	}
	
	@Override
	public int getColumnNumber() {
		return TraceParamTypeTableModel.numberOfColumns();
	}

}
