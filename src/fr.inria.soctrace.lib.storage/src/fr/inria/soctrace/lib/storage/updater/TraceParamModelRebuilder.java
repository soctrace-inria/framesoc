package fr.inria.soctrace.lib.storage.updater;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.utils.DBModelConstants.TraceParamTableModel;
import fr.inria.soctrace.lib.storage.utils.SQLConstants;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;

public class TraceParamModelRebuilder extends DBModelRebuilder {
	
	private static final Logger logger = LoggerFactory.getLogger(TraceParamModelRebuilder.class);

	public TraceParamModelRebuilder() {
		table = FramesocTable.TRACE_PARAM;
		getTableQuery = SQLConstants.PREPARED_STATEMENT_TRACE_PARAM_INSERT;
	}
	
	@Override
	public void copyValues(PreparedStatement statement, ResultSet rs)
			throws SoCTraceException {
		for (TraceParamTableModel traceParamTableModel : TraceParamTableModel
				.values()) {
			// If it is one of the difference
			if (oldModelDiff.containsKey(traceParamTableModel.toString())) {
				// If missing parameter
				if (oldModelDiff.get(traceParamTableModel.toString()) == MISSING_PARAMETER_VALUE) {
					// Put default value
					addToStatement(statement,
							traceParamTableModel.getDefaultValue(),
							traceParamTableModel.getPosition(),
							traceParamTableModel.getType());
				} else {
					// Put the value at the correct place
					addToStatement(statement, rs,
							traceParamTableModel.getPosition(),
							oldModelDiff.get(traceParamTableModel.toString()),
							traceParamTableModel.getType());
				}
				// Remove parameter from table
				oldModelDiff.remove(traceParamTableModel.toString());
			} else {
				// Just copy it
				addToStatement(statement, rs, traceParamTableModel.getPosition(),
						traceParamTableModel.getPosition(),
						traceParamTableModel.getType());
			}
		}
		
		if (!oldModelDiff.keySet().isEmpty()) {
			logger.info("Trace Parameters: the following parameters were not imported in the new model:");
			for (String columnName : oldModelDiff.keySet())
				logger.info("  *" + columnName);
		}
	}
	
	@Override
	public String getValueAt(int pos) {
		return TraceParamTableModel.getValueAt(pos).getDbColumnName();
	}
	
	@Override
	public int getColumnNumber() {
		return TraceParamTableModel.numberOfColumns();
	}
}
