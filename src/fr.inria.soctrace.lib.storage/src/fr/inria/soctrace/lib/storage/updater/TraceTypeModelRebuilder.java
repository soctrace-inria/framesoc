package fr.inria.soctrace.lib.storage.updater;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.utils.SQLConstants;
import fr.inria.soctrace.lib.storage.utils.DBModelConstants.TraceTypeTableModel;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;

public class TraceTypeModelRebuilder extends DBModelRebuilder {
	
	private static final Logger logger = LoggerFactory.getLogger(TraceTypeModelRebuilder.class);

	public TraceTypeModelRebuilder() {
		table = FramesocTable.TRACE_TYPE;
		getTableQuery = SQLConstants.PREPARED_STATEMENT_TRACE_TYPE_INSERT;
	}
	
	@Override
	public void copyValues(PreparedStatement statement, ResultSet rs)
			throws SoCTraceException {
		for (TraceTypeTableModel traceTypeTableModel : TraceTypeTableModel
				.values()) {
			// If it is one of the difference
			if (oldModelMapDiff.containsKey(traceTypeTableModel.toString())) {
				// If missing parameter
				if (oldModelMapDiff.get(traceTypeTableModel.toString()) == MISSING_PARAMETER_VALUE) {
					// Put default value
					addToStatement(statement,
							traceTypeTableModel.getDefaultValue(),
							traceTypeTableModel.getPosition(),
							traceTypeTableModel.getType());
				} else {
					// Put the value at the correct place
					addToStatement(statement, rs,
							traceTypeTableModel.getPosition(),
							oldModelMapDiff.get(traceTypeTableModel.toString()),
							traceTypeTableModel.getType());
				}
				// Remove parameter from table
				oldModelMapDiff.remove(traceTypeTableModel.toString());
			} else {
				// Just copy it
				addToStatement(statement, rs, traceTypeTableModel.getPosition(),
						traceTypeTableModel.getPosition(),
						traceTypeTableModel.getType());
			}
		}
		
		if (!oldModelMapDiff.keySet().isEmpty()) {
			logger.info("Trace Type: the following parameters were not imported in the new model:");
			for (String columnName : oldModelMapDiff.keySet())
				logger.info("  *" + columnName);
		}
	}
	
	@Override
	public String getValueAt(int pos) {
		return TraceTypeTableModel.getValueAt(pos).getDbColumnName();
	}
	
	@Override
	public int getColumnNumber() {
		return TraceTypeTableModel.numberOfColumns();
	}

}
